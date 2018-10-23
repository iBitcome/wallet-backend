package com.rst.cgi.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rst.cgi.common.constant.Constant;
import com.rst.cgi.common.constant.Error;
import com.rst.cgi.common.constant.Hint;
import com.rst.cgi.common.utils.*;
import com.rst.cgi.conf.security.CurrentThreadData;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.data.dao.mysql.CommonDao;
import com.rst.cgi.data.dao.mysql.UserContactsDao;
import com.rst.cgi.data.dao.mysql.UserDao;
import com.rst.cgi.data.dto.*;
import com.rst.cgi.data.dto.request.*;
import com.rst.cgi.data.dto.response.*;
import com.rst.cgi.data.entity.*;
import com.rst.cgi.data.vo.QueneUserInfo;
import com.rst.cgi.service.*;
import com.rst.cgi.service.thrift.gen.simserver.SimService;
import com.rst.thrift.export.ThriftClient;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * 用户相关服务
 * @author huangxiaolin
 * @date 2018-05-14 下午3:31
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {

    //存放冻结用户的队列
    private static final BlockingQueue<QueneUserInfo> linkedQueue = new LinkedBlockingQueue<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private LoginService loginService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private CommonDao commonDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private UserContactsDao userContactsDao;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private InvitationCodeService invitationCodeService;
    @Autowired
    private ThriftClient<SimService.Iface> simServiceClient;
    @Autowired
    private UserService userService;
    @Autowired
    private PushDeviceService pushDeviceService;
    @Value("${spring.mail.username}")
    private String mailFrom;
    @Value("${email.ac.link}")
    private String superLink;
    @Value("${bitcome.sims.wireless.username}")
    private String smsusername;
    @Value("${bitcome.sims.wireless.password}")
    private String smspassword;
    @Autowired
    private HttpService httpService;
    /**
     * 激活账号
     * @param userId
     */
    @Override
    public void activationAccount(Integer userId) {
        UserEntity user = commonDao.queryById(userId, UserEntity.class);
        if (Objects.isNull(user)) {
            CustomException.response(Error.USER_NOT_EXIST);
        } else {
            if (user.getStatus() == 0) {
                return;
            }
            user.setStatus(0);
        }
        commonDao.update(user);
    }

    @Override
    public UserInfo updateSecond(Integer userId, SecondIdentify se) {
        userDao.updateSecond(se.getHand(),se.getFingerPrint(),se.getHandWord(),se.getHandStatus(),se.getFingerStatus(),se.getHandPath(),userId);
        UserEntity loginUser = userDao.findById(userId);
        UserInfo userInfo = new UserInfo();
        BeanCopier.getInstance().copyBean(loginUser, userInfo);
        return userInfo;

    }

    @Transactional
    @Override
    public void bindPhone(Integer userId, String phone, String code) {
        if (StringUtils.isEmpty(phone)) {
            CustomException.response(Error.PHONE_NOT_EMPTY);
        }

        String bindCode = redisTemplate.opsForValue().get(Constant.USER_BIND_PHONE + phone);
        if (StringUtils.isEmpty(bindCode) || !bindCode.equalsIgnoreCase(code)) {
            CustomException.response(Error.VALIDATE_CODE_INVALID);
        }

        UserEntity userHas = commonDao.queryById(userId, UserEntity.class);
        UserEntity userPhone = userDao.findByPhone(phone);
        if (Objects.isNull(userHas) || userHas.getStatus() == 1){
            CustomException.response(Error.USER_NOT_EXIST);
        }

        if (Objects.isNull(userPhone)) {
            userHas.setPhone(phone);
            commonDao.update(userHas);
        } else {
            if (!UserEntity.SYSTEM_CHANNEL.equalsIgnoreCase(userPhone.getChannel())) {
                userHas.setPhone(phone);
                commonDao.update(userHas);

                userDao.deletePhone(userPhone.getId());
            } else {
                CustomException.response(Error.PHONE_EXIST);
            }
        }


        if (redisTemplate.hasKey(Constant.USER_BIND_PHONE + phone)) {
            redisTemplate.delete(Constant.USER_BIND_PHONE + phone);
        }
    }


    @Override
    public LoginResDTO login(String email, String token, int type, HttpServletRequest request) {
        LoginResDTO rst = new LoginResDTO();

        loginService.login(email, token, type, request);

        String passwordCountKey = Constant.USER_PASSWORD_COUNT_KEY_PREFIX + email;
        //登录成功删除密码输入错误次数
        if (redisTemplate.hasKey(passwordCountKey)) {
            redisTemplate.delete(passwordCountKey);
        }

        LoginResDTO res = new LoginResDTO();
        UserEntity user = userService.getUser(CurrentThreadData.iBitID());
        res.setEmail(user.getEmail());
        rst.setSession(request.getSession().getId());//用户的token
        rst.setAutoToken((String) request.getSession().getAttribute(RememberMeService.KEY_NAME));
        res.setPhone(user.getPhone());
        return rst;
    }



    @Value("${mail.use.smtp:true}")
    private Boolean useSmtp;
    @Override
    public void sendEmail(MailCodeReqDTO body) {
        int code = RandomUtil.genRandomNum(6);//6位随机数

        String helloWord = "";
        int activeTime = Constant.VALIDATECODE_VALID_TIME;
        UserEntity user = userDao.findByEmail(body.getEmail());
        String prefix = null;
        if (body.getType() == MailCodeReqDTO.REGISTER_TYPE) {
            if (Objects.nonNull(user)) {
                CustomException.response(Error.EMAIL_EXIST);
            }
            prefix = Constant.USER_REGIST_KEY_PREFIX;
            helloWord = Hint.REGISTER_HELLOWORD_WORD.getMsg();
        } else if (body.getType() == MailCodeReqDTO.LOGIN_TYPE) {
            if (Objects.isNull(user)) {
                CustomException.response(Error.ERR_MSG_EMAIL_NOT_REGISTER);
            }
            prefix = Constant.USER_LOGIN_KEY_PREFIX;
            helloWord = Hint.LOGIN_HELLOWORD_WORD.getMsg();
        } else if (body.getType() == MailCodeReqDTO.RESET_PWD_TYPE) {
            if (Objects.isNull(user)) {
                CustomException.response(Error.ERR_MSG_EMAIL_NOT_REGISTER);
            }
            prefix = Constant.USER_RESET_PWD_KEY_PREFIX;
            helloWord = Hint.CHANGE_PWD_HELLOWORD_WORD.getMsg();
        } else if (body.getType() == MailCodeReqDTO.BIND_EMAIL_TYPE) {
            if (!Objects.isNull(user) &&
                    UserEntity.SYSTEM_CHANNEL.equalsIgnoreCase(user.getChannel())) {
                CustomException.response(Error.EMAIL_EXIST);
            }
            prefix = Constant.USER_BIND_EMAIL_KEY_PREFIX;
            helloWord = Hint.BINGD_EMAIL_HELLOWORD_WORD.getMsg();
        } else {
            CustomException.response(Error.VALIDATE_CODE_TYPE_NOT_SUPPORT);
        }

        String content = getMailContentTemplate();
        String fromName = Hint.NICK_NAME.getMsg() + "<" + mailFrom + ">";
        String toEmail = body.getEmail();
        String subject = Hint.MAIL_SUBJECT.getMsg();
        content = content.replace("HELLO_WORD",helloWord)
                .replace("VERIFY_CODE", String.valueOf(code))
                .replace("CODE_VERIFY_NAME", Hint.CODE_VERIFY_NAME.getMsg())
                .replace("ACTIVE_WORD",
                        Hint.ACTIVE_WORD.getMsg().replace("#ACTIVE_TIME", String.valueOf(activeTime)))
                .replace("TEAM_NAME", Hint.TEAM_NAME.getMsg());
        EmailUtl.send(fromName, toEmail, subject, content, useSmtp, mailSender);

        //验证码10分钟输入有效
        redisTemplate.opsForValue().set(prefix + body.getEmail(),
                String.valueOf(code), Constant.VALIDATECODE_VALID_TIME, TimeUnit.MINUTES);
    }


    @Override
    public String getMailContentTemplate() {
        String MailcontentTemplate = null;
        InputStream in = null;
        try {
            in = Thread.currentThread()
                    .getContextClassLoader().getResourceAsStream("static/MailCodeContentTemplate.html");

            MailcontentTemplate = StreamUtils.copyToString(in, Charset.forName("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return MailcontentTemplate;
    }


    /**
     * 自动解除冻结用户
     * @author hxl
     * 2018/6/4 上午10:39
     */
    @PostConstruct
    void init() {
        //查询所有的冻结用户
        List<QueneUserInfo> frozenUsers = userDao.findFrozenUsers();
        if (!CollectionUtils.isEmpty(frozenUsers)) {
            linkedQueue.addAll(frozenUsers);
        }
        executorService.submit(() -> {
            long time = 0L;
            QueneUserInfo user = null;
            try {
                while (true) {
                    user = linkedQueue.peek();
                    if (user == null) {
                        logger.info("---quene is null, sleep time is : {}----", Constant.USER_FROZEN_TIME);
                        Thread.sleep(Constant.USER_FROZEN_TIME);
                    } else {
                        time = System.currentTimeMillis() - user.getFrozenTime().getTime();
                        if (time >= Constant.USER_FROZEN_TIME) {
                            //解除冻结状态
                            userDao.updateFrozenByEmail(user.getEmail(), 0, new Date());
                            logger.info("-----解除用户：{}冻结状态----", user.getEmail());
                            linkedQueue.poll();
                        } else {
                            logger.info("-----sleep, time is : {}" + (Constant.USER_FROZEN_TIME - time));
                            Thread.sleep(Constant.USER_FROZEN_TIME - time);
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void sendSmsCode(SmsCodeReqDTO body) {
        int code = RandomUtil.genRandomNum(6);//6位随机数
        try {
            String message  = Hint.PHONE_VERIFY_CODE.getMsg().replace("#CODE", String.valueOf(code))
                    .replace("#ACTIVE_TIME", String.valueOf(Constant.VALIDATECODE_VALID_TIME));


            UserEntity user = userDao.findByPhone(body.getPhone());
            String key = null;
            if (body.getType() == SmsCodeReqDTO.REGISTER_TYPE) {
                if (Objects.nonNull(user)) {
                    CustomException.response(Error.PHONE_EXIST);
                }
                key = Constant.USER_REGIST_KEY_PREFIX + body.getPhone();
            } else if (body.getType() == SmsCodeReqDTO.BIND_TYPE) {
                if (Objects.nonNull(user) &&
                        UserEntity.SYSTEM_CHANNEL.equalsIgnoreCase(user.getChannel())) {
                    CustomException.response(Error.PHONE_EXIST);
                }
                key = Constant.USER_BIND_PHONE + body.getPhone();
            } else {
                CustomException.response(Error.ERR_MSG_PHONE_CODE_TYPE_NOT_SUPPORT);
            }

            simServiceClient.get(SimService.Iface.class).send(body.getPhone(), message, "bitcome");
            //验证码10分钟输入有效
            redisTemplate.opsForValue().set(key,
                    String.valueOf(code), Constant.VALIDATECODE_VALID_TIME, TimeUnit.MINUTES);
        } catch (TException e) {
            e.printStackTrace();
        }

    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void regist(UserRegisterDTO body) {
        //验证验证码
        String codeAccount = "";
        if (body.getCodeType() == 1) {
            codeAccount = body.getEmail();
        } else if (body.getCodeType() == 2) {
            codeAccount = body.getPhone();
        }
        if(Arrays.asList(1, 2).contains(body.getCodeType())) {
            String validateCode = redisTemplate.opsForValue().get(Constant.USER_REGIST_KEY_PREFIX + codeAccount);
            if (StringUtils.isEmpty(validateCode) || !validateCode.equals(body.getCode())) {
                CustomException.response(Error.VALIDATE_CODE_INVALID);
            }
        } else if (body.getCodeType() != 3) {
            CustomException.response(Error.VALIDATE_CODE_TYPE_NOT_SUPPORT);
        }

        UserEntity user = new UserEntity();
        user.setEmail(body.getEmail());
        user.setPhone(body.getPhone());
        user.setChannel(body.getChannel());
        user.setPassword(body.getPassword());
        user.setStatus(2);
        Integer userId = this.saveUser(user, body.getRegisterType());

        //使用邀请码
        if (!StringUtils.isEmpty(body.getInvitationCode())) {
            if (!invitationCodeService.consumeCode(body.getInvitationCode(), userId)) {
                CustomException.response(Error.INVITATION_CODE_NOT_EXIST);
            }
        }

        //保存成功后删除验证码
        redisTemplate.delete(Constant.USER_REGIST_KEY_PREFIX + codeAccount);
    }



    private Integer saveUser(UserEntity saveUser, String registerType) {
        //验证注册信息
        if("email".equalsIgnoreCase(registerType)){
            if (StringUtils.isEmpty(saveUser.getEmail())) {
                CustomException.response(Error.EMAIL_NOT_EMPTY);
            } else {
                UserEntity emailUser = userDao.findByEmail(saveUser.getEmail());
                if (emailUser != null) {
                    CustomException.response(Error.EMAIL_EXIST);
                }
                if (!StringUtils.isEmpty(saveUser.getPhone())) {
                    UserEntity phoneUser = userDao.findByPhone(saveUser.getPhone());
                    if (phoneUser != null) {
                        CustomException.response(Error.PHONE_EXIST);
                    }
                }
            }
        } else if ("mobile".equalsIgnoreCase(registerType)) {
            if (StringUtils.isEmpty(saveUser.getPhone())) {
                CustomException.response(Error.PHONE_NOT_EMPTY);
            } else {
                UserEntity phoneUser = userDao.findByPhone(saveUser.getPhone());
                if (phoneUser != null) {
                    CustomException.response(Error.PHONE_EXIST);
                }
                if (!StringUtils.isEmpty(saveUser.getEmail())) {
                    UserEntity emailUser = userDao.findByEmail(saveUser.getEmail());
                    if (emailUser != null) {
                        CustomException.response(Error.EMAIL_EXIST);
                    }
                }
            }
        }else {
            CustomException.response(Error.REGISTER_TYPE_NOT_SUPPORT);
        }

        if (StringUtils.isEmpty(saveUser.getPassword())) {
            CustomException.response(Error.PASSWORD_NOT_EMPTY);
        }

        saveUser.setPassword(DigestUtils.md5Hex(saveUser.getPassword()));
        Date now = new Date();
        saveUser.setCreateTime(now);
        saveUser.setUpdateTime(now);
        commonDao.insert(saveUser);
        return saveUser.getId();
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public void addFrozenUser(String email, Date frozenTime) {
        linkedQueue.offer(new QueneUserInfo(email, frozenTime));
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void removeFrozeUser(String email) {
        Iterator<QueneUserInfo> iterator = linkedQueue.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getEmail().equals(email)) {
                iterator.remove();
                break;
            }
        }
    }

    @Override
    public UserEntity saveOrUpdate(UserEntity user) {
        if (StringUtils.isEmpty(user.getEmail())) {
            CustomException.response(Error.EMAIL_NOT_EMPTY);
        }
        if (StringUtils.isEmpty(user.getPassword())) {
            CustomException.response(Error.PASSWORD_NOT_EMPTY);
        }
        if (user.getId() == null) {
            UserEntity emailUser = userDao.findByEmail(user.getEmail());
            if (emailUser != null) {
                CustomException.response(Error.EMAIL_EXIST);
            }
            user.setPassword(DigestUtils.md5Hex(user.getPassword()));
            Date now = new Date();
            user.setCreateTime(now);
            user.setUpdateTime(now);
            commonDao.insert(user);
        } else {
            //更新

        }
        return null;
    }

    @Override
    public void forgetPwd(String email, String password) {
        if (StringUtils.isEmpty(email)) {
            CustomException.response(Error.EMAIL_NOT_EMPTY);
        }
        if (StringUtils.isEmpty(password)) {
            CustomException.response(Error.PASSWORD_NOT_EMPTY);
        }

        UserEntity user = userDao.findByEmail(email);
        if (user == null) {
            CustomException.response(Error.USERNAME_OR_PASSWORD_INVALID);
        }
        user.setPassword(DigestUtils.md5Hex(password));
        user.setIsFrozen(0);//如果处于冻结状态则解除冻结
        user.setUpdateTime(new Date());
        commonDao.update(user);
        //移除队列的用户
        removeFrozeUser(email);
    }

    @Override
    public UserEntity findByEmail(String email) {
        return userDao.findByEmail(email);
    }


    @Override
    public void updatePassword(int userId, String oldPwd, String newPwd) {
        if (StringUtils.isEmpty(oldPwd)) {
            CustomException.response(Error.PASSWORD_NOT_EMPTY);
        }
        if (StringUtils.isEmpty(newPwd)) {
            CustomException.response(Error.NEW_PASSWORD_NOT_EMPTY);
        }
        UserEntity user = userDao.findById(userId);
        if ((user == null) || (user.getStatus() == 1)) {
            CustomException.response(Error.USER_NOT_EXIST);
        }
        if (!user.getPassword().equals(DigestUtils.md5Hex(oldPwd))) {
            CustomException.response(Error.OLD_PASSWORD_INVALID);
        }
        userDao.updatePassword(userId, DigestUtils.md5Hex(newPwd), new Date());
    }

    @Override
    public void updateFrozen(String email, int frozen, Date frozenTime) {
        if (frozenTime == null) {
            frozenTime = new Date();
        }
        userDao.updateFrozenByEmail(email, frozen, frozenTime);
    }

    @Override
    public void saveOrUpdateContacts(int userId, UserContactsDTO uc) {
        if (StringUtils.isEmpty(uc.getContactsName())) {
            CustomException.response(Error.CONTACTS_NAME_NOT_EMPTY);
        }
        List<UserContactsAddress> ucaList = uc.getAddressList();
        if (CollectionUtils.isEmpty(ucaList)) {
            CustomException.response(Error.CONTACTS_TOKEN_OR_ADDRESS_NOT_EMPTY);
        }
        Date now = new Date();
        int contactsId = (uc.getContactsId() == null) ? 0 : uc.getContactsId();
        if (contactsId < 1) {
            //新增
            UserContacts contacts = new UserContacts();
            contacts.setContactsName(uc.getContactsName());
            contacts.setUserId(userId);
            contacts.setCreateTime(now);
            commonDao.insert(contacts);
            contactsId = contacts.getId();
        } else {
            //修改联系人
            UserContacts userContacts = userContactsDao.findById(contactsId);
            //联系人没找到或者该联系人不属于当前用户
            if ((userContacts == null) || (userContacts.getUserId() != userId)) {
                CustomException.response(Error.CONTACTS_NOT_EMPTY);
            }
            //更新联系人
            userContacts.setContactsName(uc.getContactsName());
            commonDao.update(userContacts);
            //修改先删除地址
            userContactsDao.deleteContactsAddress(contactsId);
        }
        for (UserContactsAddress uca : ucaList) {
            uca.setContactsId(contactsId);
            uca.setCreateTime(now);
            uca.setId(null);//确保为null
        }
        commonDao.batchInsert(ucaList, UserContactsAddress.class);
    }

    @Override
    public int IOSsaveContacts(int userId, UserContactsDTO uc) {
        if (StringUtils.isEmpty(uc.getContactsName())) {
            CustomException.response(Error.CONTACTS_NAME_NOT_EMPTY);
        }
        List<UserContactsAddress> ucaList = uc.getAddressList();
        if (CollectionUtils.isEmpty(ucaList)) {
            CustomException.response(Error.CONTACTS_TOKEN_OR_ADDRESS_NOT_EMPTY);
        }
        Date now = new Date();
        int contactsId = (uc.getContactsId() == null) ? 0 : uc.getContactsId();
        if (contactsId < 1) {
            //新增
            UserContacts contacts = new UserContacts();
            contacts.setContactsName(uc.getContactsName());
            contacts.setUserId(userId);
            contacts.setCreateTime(now);
            commonDao.insert(contacts);
            contactsId = contacts.getId();
        } else {
            //修改联系人
            UserContacts userContacts = userContactsDao.findById(contactsId);
            //联系人没找到或者该联系人不属于当前用户
            if ((userContacts == null) || (userContacts.getUserId() != userId)) {
                CustomException.response(Error.CONTACTS_NOT_EMPTY);
            }
            //更新联系人
            userContacts.setContactsName(uc.getContactsName());
            commonDao.update(userContacts);
            //修改先删除地址
            userContactsDao.deleteContactsAddress(contactsId);
        }
        for (UserContactsAddress uca : ucaList) {
            uca.setContactsId(contactsId);
            uca.setCreateTime(now);
            uca.setId(null);//确保为null
        }
        commonDao.batchInsert(ucaList, UserContactsAddress.class);

        return contactsId;
    }

    @Override
    public String createUser() {
        UserEntity userEntity = new UserEntity();
        String identify = EncodeUtil.generateIdentify();
        userEntity.setPassword(DigestUtils.md5Hex(identify));
        userEntity.setLoginName(identify);
        userEntity.setChannel("System");
        userEntity.setEmail("");
        userEntity.setCreateTime(new Date());
        userEntity.setUpdateTime(new Date());
        commonDao.insert(userEntity);
        UserEntity user = getUser(identify);
        invitationCodeService.productCode(user.getId());
        return identify;
    }

    @Override
    public UserInfo getUser(Integer userId) {
        if (userId == null) {
            return null;
        }

        UserEntity loginUser = userDao.findById(userId);
        return getUser(loginUser);
    }

    @Override
    public UserEntity getUser(String identify) {
        return userDao.findByLoginName(identify);
    }

    @Override
    public void bindEmail(Integer userId, String email, String code) {
        if (StringUtils.isEmpty(email)) {
            CustomException.response(Error.EMAIL_NOT_EMPTY);
        }

        String userBindEmailKey = Constant.USER_BIND_EMAIL_KEY_PREFIX + email;

        String bindCode = redisTemplate.opsForValue().get(userBindEmailKey);
        if (StringUtils.isEmpty(bindCode) || !bindCode.equalsIgnoreCase(code)) {
            CustomException.response(Error.VALIDATE_CODE_INVALID);
        }

        UserEntity emailUser = userDao.findByEmail(email);
        UserEntity userHas = commonDao.queryById(userId, UserEntity.class);
        if (Objects.isNull(userHas) || userHas.getStatus() == 1) {
            CustomException.response(Error.USER_NOT_EXIST);
        }

        if (emailUser == null) {
            userHas.setEmail(email);
            commonDao.update(userHas);
        } else {
            if (!UserEntity.SYSTEM_CHANNEL.equalsIgnoreCase(emailUser.getChannel())) {
                userHas.setEmail(email);
                commonDao.update(userHas);

                userDao.deleteEmail(emailUser.getId());
            } else {
                CustomException.response(Error.EMAIL_EXIST);
            }
        }

        if (redisTemplate.hasKey(userBindEmailKey)) {
            redisTemplate.delete(userBindEmailKey);
        }

    }

    @Override
    public void submitActiveAddress(Integer userId, String address) {
        if (StringUtils.isEmpty(address)) {
            CustomException.response(Error.ACTIVE_ADDRESS_EMPTIY);
        }

        UserEntity userHas = commonDao.queryById(userId, UserEntity.class);
        if (Objects.isNull(userHas) || userHas.getStatus() == 1) {
            CustomException.response(Error.USER_NOT_EXIST);
        }

        UserEntity activeAddrUser = userDao.findbyActiveAddrss(address);
        if (activeAddrUser != null && !activeAddrUser.getId().equals(userId)) {
            CustomException.response(Error.ADDRESS_IS_USED_BY_OTHER);
        }

        userHas.setActiveAddress(address);
        commonDao.update(userHas);
    }

    @Override
    public InvitationInfoRepDTO getInvitationInfo(Integer userId) {
        InvitationInfoRepDTO res = new InvitationInfoRepDTO();
        UserEntity user = commonDao.queryById(userId, UserEntity.class);
        InvitationCode invitationCode = invitationCodeService.findInviterByUserId(userId);
        if (invitationCode != null) {
            res.setInviterCode(invitationCode.getCode());
        }
        res.setPhone(user.getPhone());
        res.setEmail(user.getEmail());
        res.setActiveAddress(user.getActiveAddress());
        res.setInviPerson(userDao.queryInviPeopleByUserId(userId));
        return res;
    }

    @Override
    public CommonResult sendEmailAcLink(String email,UserEntity user) {
        if(email == null){
            CustomException.response(Error.EMAIL_NOT_EMPTY);
        }
        List<UserEntity> list = userDao.queryByEmail(email);
        if(list.size() != 0){
            CustomException.response(Error.EMAIL_EXIST);
        }
        boolean flag=true;
        flag = Pattern.matches(Constant.EMAIL_REGEX,email);
        if(!flag){
           CustomException.response(Error.EMAIL_FORMAT_WRONG);
        }
//        MimeMessage message = mailSender.createMimeMessage();
        try {
//            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            String content = this.getMailAcContentTemplate();
//            helper.setFrom(Hint.NICK_NAME.getMsg() + "<" + mailFrom + ">");
//            helper.setTo(email);
//            helper.setSubject(Hint.MAIL_SUBJECT.getMsg());
            String helloWord = Hint.VALID_EMAIL.getMsg();
            //生成随机数作为salt
            Random random = new Random();
            int i = random.nextInt();
            String salt=Integer.toString(i);
            //将用户id加密得到token并存入数据库
            String token = TokenUtil.encrypt(email+salt);
            StringBuffer sb = new StringBuffer();
            sb.append("\"");
            sb.append(superLink);
            sb.append(token);
            sb.append("&email=");
            sb.append(email);
            sb.append("&ibit=");
            sb.append(user.getLoginName());
            sb.append("\"");
            StringBuffer sb2 = new StringBuffer();
            sb2.append(superLink);
            sb2.append(token);
            sb2.append("&email=");
            sb2.append(email);
            sb.append("&ibit=");
            sb.append(user.getLoginName());
            content = content.replace("HELLO_WORD",helloWord)
                    .replace("SUPER_LINK", sb.toString())
                    .replace("TRUE_LINK",sb2.toString())
                    .replace("ACTIVE_WORD",
                            Hint.ACTIVE_EMAIL.getMsg())
                    .replace("TEAM_NAME", Hint.TEAM_NAME.getMsg());
//            helper.setText(content, true);
//            mailSender.send(message);
            EmailUtl.send(Hint.NICK_NAME.getMsg() + "<" + mailFrom + ">",email,Hint.MAIL_SUBJECT.getMsg(),content,useSmtp,mailSender);
            String prefix = Constant.USER_BIND_EMAIL_KEY_PREFIX;
            //验证码30分钟输入有效
            redisTemplate.opsForValue().set(prefix + email,
                    token, Constant.SUPER_LINK, TimeUnit.MINUTES);
        }catch (Exception e) {
            logger.error("发送邮件失败：{}", e);
            CustomException.response(Error.MAIL_SEND_ERROR);
        }
        return new CommonResult();

    }

    @Transactional(noRollbackFor = CustomException.class)
    @Override
    public CommonResult sengShareCode(String phone,int type,Integer ptype,Integer ctype) {
        int code = RandomUtil.genRandomNum(6);//6位随机数
        String key = Constant.USER_BIND_PHONE + phone;
        String key2 = Constant.USER_VALID_CODE_TIME + phone;
        try {
            List<UserEntity> list = userDao.queryByPhone(phone);
            List<InvitationCodePhone> list2 = userDao.queryInviId(phone);
            if(type == 0){
                if(list.size()!=0 || list2.size() != 0){
                    CustomException.response(Error.PHONE_EXIST);
                }
            }else if(type == 1){
                if(list.size()!=0){
                    CustomException.response(Error.PHONE_EXIST);
                }
            }else if(type == 2){
                String s = redisTemplate.opsForValue().get(key2);
                if(s !=null){
                    CustomException.response(Error.ERR_MSG_VALID_TIME);
                }
                List<AcUserPhone> list3 = userDao.queryAcByPhone(phone,ptype);
                if(list.size() != 0 && ptype == 13 && list3.size() != 0){
                    CustomException.response(Error.ERR_MSG_ALLREADY_VIP);
                }
                if(list3.size() != 0){
                    CustomException.response(Error.ERR_MSG_RESERVED);
                }
            }
            String message  = Hint.PHONE_VERIFY_CODE.getMsg().replace("#CODE", String.valueOf(code))
                    .replace("#ACTIVE_TIME", String.valueOf(Constant.VALIDATECODE_VALID_TIME));
            if (ctype == 0){
                simServiceClient.get(SimService.Iface.class).send(phone, message, "bitcome");
            } else {
                JSONObject param = new JSONObject();
                param.put("cust_code", smsusername);//账号
                param.put("content", message);//短信内容
                param.put("destMobiles", phone);
                param.put("sign", DigestUtils.md5Hex(message + smspassword));
                logger.info("wireless 发送短信：{}", param.toString());
                JSONObject jsonObject = httpService.postJSON(Constant.SIM_URL, null, param);
                JSONArray result = jsonObject.getJSONArray("result");
                JSONObject o = result.getJSONObject(0);
                if (o.getString("code").equals("0")){
                    logger.info("海外短信发送成功:{}",jsonObject);
                } else {
                    logger.info("海外短信发送失败:{}",jsonObject);
                    CustomException.response(Error.ERR_MSG_SEND_PHONE);
                }
            }
            //验证码10分钟输入有效
            redisTemplate.opsForValue().set(key,
                    String.valueOf(code), Constant.VALIDATECODE_VALID_TIME, TimeUnit.MINUTES);
            if (type == 3){
                redisTemplate.opsForValue().set(key2,
                        phone, 1, TimeUnit.MINUTES);
            }
        }catch (TException e) {
            logger.error("短信发送失败，手机号:{},异常:{}",phone,e.toString());
            CustomException.response(Error.ERR_MSG_SEND_PHONE);
        }
        return new CommonResult();

    }

    @Override
    public CommonResult validCode(String code, String inviCode,String phone,int type,UserEntity user,Integer ptype) {
        List<InvitationCode> in = userDao.queryByCode(inviCode);
        if(type == 0){
            if(in == null){
                CustomException.response(Error.REQUEST_PARAM_INVALID);
            }
        }
        String key = Constant.USER_BIND_PHONE + phone;
        String vi = redisTemplate.opsForValue().get(key);
        if(vi == null || !vi.equals(code)){
            CustomException.response(Error.VALIDATE_CODE_INVALID);
        }
        if(type == 0){
            InvitationCodePhone con = new InvitationCodePhone();
            con.setPhone(phone);
            con.setCodeId(in.get(0).getId());
            commonDao.insert(con);
        }else if(type == 1){
            user.setPhone(phone);
            user.setInvitedTime(new Date());
            commonDao.update(user);
            Integer inviId = userDao.queryInviIdByphone(phone);
            InvitationCodeConsumer invitationCodeConsumer = userDao.queryConsumerByUserId(user.getId());
            if(inviId != null && invitationCodeConsumer == null){
                InvitationCodeConsumer consumer = new InvitationCodeConsumer();
                consumer.setConsumerId(user.getId());
                consumer.setInvitationCodeId(inviId);
                commonDao.insert(consumer);
                Integer userId = userDao.queryUserIdByInvicodeId(inviId);
                redisTemplate.opsForZSet().incrementScore(Constant.INVITATION_TOP,userId.toString(),1);
            }

        } else if (type == 2){
            AcUserPhone ac = new AcUserPhone();
            ac.setPhone(phone);
            ac.setCreateTime(new Date());
            ac.setPtype(ptype);
            if(inviCode != null){
                ac.setSharePhone(inviCode);
            }
            commonDao.insert(ac);
        }
        return new CommonResult();
    }

    @Override
    public PageRep<GetInvitedListRepDTO> getInvitedList(Integer userId, PageReq body) {
        PageRep<GetInvitedListRepDTO> result = new PageRep<>();

        PageHelper.startPage(body.getPageNo()-1, body.getPageSize());
        List<UserEntity> users = userDao.queryInvitedListByUserId(userId);
        PageInfo<UserEntity> pageInfo = new PageInfo<>(users);

        SimpleDateFormat spf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        List<GetInvitedListRepDTO> repDTOS = new ArrayList<>();
        pageInfo.getList().forEach(userEntity -> {
            GetInvitedListRepDTO repDTO = new GetInvitedListRepDTO();
            Date invitedTime = userEntity.getInvitedTime() == null ?
                    userEntity.getCreateTime() : userEntity.getInvitedTime();
            repDTO.setInvitedTime(spf.format(invitedTime));
            repDTO.setLoginName(userEntity.getLoginName());
            repDTOS.add(repDTO);
        });

        result.setPageTotal(pageInfo.getPages());
        result.setPageNo(pageInfo.getPageNum() + 1);
        result.setTotal(pageInfo.getTotal());
        result.setContent(repDTOS);

        return result;
    }

    @Override
    public List<InvitationTopRepDTO> getInvitationTop() {
        List<InvitationTopRepDTO> result = new ArrayList<>();
        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisTemplate.opsForZSet().reverseRangeWithScores(Constant.INVITATION_TOP, 0, 9);
        Iterator<ZSetOperations.TypedTuple<String>> iterator = typedTuples.iterator();
        while (iterator.hasNext()) {
            InvitationTopRepDTO repDTO = new InvitationTopRepDTO();
            ZSetOperations.TypedTuple<String> typedTuple = iterator.next();
            UserEntity user = userDao.findById(Integer.valueOf(typedTuple.getValue()));
            repDTO.setInvitationNum(typedTuple.getScore().intValue());
            repDTO.setId(user.getId());
            repDTO.setLoginName(user.getLoginName());
            result.add(repDTO);
        }
        return result;
    }

    private String getMailAcContentTemplate() {
        String MailcontentTemplate = null;
        InputStream in = null;
        try {
            in = Thread.currentThread()
                    .getContextClassLoader().getResourceAsStream("static/invitationEmail.html");

            MailcontentTemplate = StreamUtils.copyToString(in, Charset.forName("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return MailcontentTemplate;
    }


    public UserInfo getUser(UserEntity user) {
        if (StringUtils.isEmpty(user.getLoginName())) {
            user.setLoginName(EncodeUtil.generateIdentify());
            commonDao.update(user);
        }
        UserInfo userInfo = new UserInfo();
        BeanCopier.getInstance().copyBean(user, userInfo);
        userInfo.setInvitationCode(invitationCodeService.getCode(user.getId()));
        return userInfo;
    }


    @Override
    public void deleteContacts(int userId, int contactsId) {
        int count = userContactsDao.delete(userId, contactsId);
        if (count > 0) {
            userContactsDao.deleteContactsAddress(contactsId);
        }
    }

    @Override
    public List<UserContacts> findContactsList(int userId) {

    	List<UserContacts> contactsList = userContactsDao.findContactsByUserId(userId, true);
    	//针对姓名进行排序
    	Collections.sort(contactsList, (UserContacts o1, UserContacts o2) ->{
    		String contactsName1 = o1.getContactsName();
			String contactsName2 = o2.getContactsName();
			Collator instance = Collator.getInstance(Locale.CHINA);  
			return instance.compare(contactsName1, contactsName2); 
    	});

        return contactsList;

    }
    public void countData(CountData data){
       commonDao.insert(data);
    }

    public List<String> sendCountData(int id){
        List<Integer> walletIdList = userDao.getwallletIds(id);
        int[] idArray = new int[walletIdList.size()];
        for(int i=0;i<walletIdList.size();i++){
            idArray[i]=walletIdList.get(i);
        }
        List<String> availableDevices = pushDeviceService.getAvailableDevices(idArray);
        return availableDevices;

    }

    @Override
    public UserContacts findContacts(int contactsId) {
        return userContactsDao.findContacts(contactsId);
    }

    @Override
    public List<UserContacts> findAllContacts(int userId) {
        return userContactsDao.findContactsByUserId(userId, true);
    }



}
