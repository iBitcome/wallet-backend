package com.rst.cgi.controller;

import com.google.common.collect.Maps;
import com.rst.cgi.common.constant.Constant;
import com.rst.cgi.common.constant.Error;
import com.rst.cgi.common.hbc.DecryptRequest;
import com.rst.cgi.common.hbc.EncryptResponse;
import com.rst.cgi.conf.security.CurrentThreadData;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.data.dto.CommonResult;
import com.rst.cgi.data.dto.PageRep;
import com.rst.cgi.data.dto.PageReq;
import com.rst.cgi.data.dto.SecondIdentify;
import com.rst.cgi.data.dto.request.*;
import com.rst.cgi.data.dto.response.*;
import com.rst.cgi.data.entity.CountData;
import com.rst.cgi.data.entity.InvitationCode;
import com.rst.cgi.data.entity.UserContacts;
import com.rst.cgi.data.entity.UserEntity;
import com.rst.cgi.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户相关接口
 * @author huangxiaolin
 * @date 2018-05-14 下午3:11
 */
@Api(tags = "用户相关接口")
@RestController
@RequestMapping("/user")
public class UserController {

    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private InvitationCodeService invitationCodeService;
    @Autowired
    private LoginService loginService;
    @Value("${ac.now.type}")
    private String type;
    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "用户注册接口")
    @PostMapping("/regist")
    public CommonResult<LoginResDTO> regist(@RequestBody UserRegisterDTO body) {
        userService.regist(body);

        return new CommonResult();
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation("用户忘记密码接口")
    @PostMapping("/forgetPwd")
    public CommonResult forgetPwd(@RequestBody ForgetPwdReq userReq) {
        String email = userReq.getEmail();
        String validateCode = redisTemplate.opsForValue().get(Constant.USER_RESET_PWD_KEY_PREFIX + email);
        if (StringUtils.isEmpty(validateCode) || !validateCode.equals(userReq.getCode())) {
            CustomException.response(Error.VALIDATE_CODE_INVALID);
        }
        userService.forgetPwd(email, userReq.getPassword());
        //保存成功后删除验证码
        redisTemplate.delete(Constant.USER_RESET_PWD_KEY_PREFIX + email);
        String passwordCountKey = Constant.USER_PASSWORD_COUNT_KEY_PREFIX + email;
        //忘记密码操作删除密码输入错误次数
        if (redisTemplate.hasKey(passwordCountKey)) {
            redisTemplate.delete(passwordCountKey);
        }
        return new CommonResult();
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation("手势二次验证基础信息修改")
    @PostMapping("/upSecond")
    public CommonResult<UserInfo> secondIdentify(@RequestBody SecondIdentify se){
        CommonResult<UserInfo> rst = new CommonResult<>();
        UserEntity user = userService.getUser(CurrentThreadData.iBitID());
        Integer userId = user.getId();
        UserInfo userInfo=userService.updateSecond(userId, se);
        rst.setData(userInfo);
        return rst;
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation("获取用户数据（登录状态下）")
    @PostMapping("/getLoginUser")
    public CommonResult<UserInfo> getLoginUser() {
        CommonResult<UserInfo> rst = new CommonResult<>();
        Integer userId = userService.getUser(CurrentThreadData.iBitID()).getId();
        rst.setData(userService.getUser(userId));
        return rst;
    }

    @Autowired
    private WalletDataService dataSyncService;

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "用户登录接口")
    @PostMapping("/login")
    public CommonResult<LoginResDTO> login(@RequestBody UserReqDTO userReq, HttpServletRequest request) {
        String username = (userReq.getEmail() == null) ? null : userReq.getEmail().trim();//用户名去除前后空格

        try {
            userService.login(username, userReq.getToken(), userReq.getType(), request);
        } catch (LockedException e) {
            CustomException.response(Error.ACCOUNT_FROZEN);
        } catch (BadCredentialsException e) {
            if (userReq.getType() != 0) {
                dealPasswordError(username);
            } else {
                CustomException.response(Error.USERNAME_OR_PASSWORD_INVALID);
            }
        } catch (AuthenticationException e) {
            CustomException.response(Error.USERNAME_OR_PASSWORD_INVALID);
        }

        String passwordCountKey = Constant.USER_PASSWORD_COUNT_KEY_PREFIX + username;
        //登录成功删除密码输入错误次数
        if (redisTemplate.hasKey(passwordCountKey)) {
            redisTemplate.delete(passwordCountKey);
        }

        LoginResDTO res = new LoginResDTO();
        UserEntity user = userService.getUser(CurrentThreadData.iBitID());
        res.setEmail(user.getEmail());
        res.setId(user.getId());
        res.setSession(CurrentThreadData.iBitID());
        res.setAutoToken((String) request.getSession().getAttribute(RememberMeService.KEY_NAME));
        res.setPhone(user.getPhone());
        SecondIdentify se= new SecondIdentify();
        se.setHand(user.getHand());
        se.setFingerPrint(user.getFingerPrint());
        se.setHandWord(user.getHandWord());
        se.setFingerStatus(user.getFingerStatus());
        se.setHandStatus(user.getHandStatus());
        se.setHandPath(user.getHandPath());
        res.setSecondIdentify(se);
        return CommonResult.make(res);
    }

    /**
     * 登陆密码输入错误处理逻辑
     * @author hxl
     * 2018/5/28 上午11:06
     */
    private void dealPasswordError(String username) {
        String passwordCountKey = Constant.USER_PASSWORD_COUNT_KEY_PREFIX + username;
        String countStr = redisTemplate.opsForValue().get(passwordCountKey);
        int count = StringUtils.isEmpty(countStr) ? 0 : Integer.parseInt(countStr);
        if (++count >= Constant.PASSWORD_ERROR_MAX_COUNT) {
            Date frozenTime = new Date();
            //密码输入错误超出上限则设置账户为冻结状态
            userService.updateFrozen(username, 1, frozenTime);
            userService.addFrozenUser(username, frozenTime);
            //冻结用户删除key
            redisTemplate.delete(passwordCountKey);
            CustomException.response(Error.ACCOUNT_FROZEN);
        } else {
            LocalDateTime now = LocalDateTime.now();


            LocalDateTime dayBeginTime = now.plusDays(1)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0);
            //计算当日内剩下的秒数
            long timeout = ChronoUnit.SECONDS.between(now, dayBeginTime);
            //当日内错误次数加1
            redisTemplate.opsForValue().set(passwordCountKey, String.valueOf(count), timeout, TimeUnit.SECONDS);
            if (count >= 3) {
                int restCount = Constant.PASSWORD_ERROR_MAX_COUNT - count;
                CustomException.response(Error.ERR_MSG_ACCOUNT_LOCKING.format(String.valueOf(restCount)));
            } else {
                CustomException.response(Error.USERNAME_OR_PASSWORD_INVALID);
            }
        }
    }



    @ApiOperation("用户注销接口")
    @PostMapping("/logout")
    public CommonResult logout() {
        return new CommonResult();
    }

    //需要登录
    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "用户修改密码接口", notes = "json参数说明：oldPwd：原密码；newPwd：新密码")
    @PostMapping("/updatePassword")
    public CommonResult updatePassword(@RequestBody Map<String, String> paramMap) {
        int userId = userService.getUser(CurrentThreadData.iBitID()).getId();
        userService.updatePassword(userId, paramMap.get("oldPwd"), paramMap.get("newPwd"));
        return new CommonResult();
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "用户联系人保存/更新接口")
    @PostMapping("/contacts/save")
    public CommonResult saveContacts(@RequestBody UserContactsDTO uc) {
        UserEntity user = userService.getUser(CurrentThreadData.iBitID());
        if (user == null) {
            CustomException.response(Error.USER_NOT_EXIST);
        }

        userService.saveOrUpdateContacts(user.getId(), uc);
        return new CommonResult();
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "用户联系人保存/更新接口")
    @PostMapping("/contacts/IOSsave")
    public CommonResult IossaveContacts(@RequestBody UserContactsDTO uc) {
        UserEntity user = userService.getUser(CurrentThreadData.iBitID());
        if (user == null) {
            CustomException.response(Error.USER_NOT_EXIST);
        }

        int i=userService.IOSsaveContacts(user.getId(), uc);
        CommonResult result=new CommonResult();
        result.setData(i);
        return result;
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "用户删除联系人接口", notes = "参数说明：contactsId:联系人ID")
    @PostMapping("/contacts/delete")
    public CommonResult deleteContacts(@RequestBody Map<String, String> paramMap) {
        Integer contactsId = Integer.parseInt(paramMap.get("contactsId"));
        int conId = (contactsId == null) ? 0 : contactsId.intValue();
        if (conId <= 0) {
            CustomException.response(Error.REQUEST_PARAM_INVALID);
        }

        UserEntity user = userService.getUser(CurrentThreadData.iBitID());
        if (user == null) {
            CustomException.response(Error.USER_NOT_EXIST);
        }
        userService.deleteContacts(user.getId(), conId);
        return new CommonResult();
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "查询用户的联系人接口")
    @PostMapping("/contacts/showContactsList")
    public CommonResult showContactsList() {
        UserEntity user = userService.getUser(CurrentThreadData.iBitID());
        if (user == null) {
            CustomException.response(Error.USER_NOT_EXIST);
        }
        List<UserContacts> contactsList = userService.findContactsList(user.getId());
        CommonResult result = new CommonResult<>();
        result.setData(contactsList);
        return result;
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "查询某个联系人的地址接口", notes = "参数说明：contactsId:联系人ID")
    @PostMapping("/contacts/showContacts")
    public CommonResult showContacts(@RequestBody Map<String, Integer> paramMap) {
        Integer contactsId = paramMap.get("contactsId");
        int conId = (contactsId == null) ? 0 : contactsId.intValue();
        if (conId <= 0) {
            CustomException.response(Error.REQUEST_PARAM_INVALID);
        }
        UserContacts uc = userService.findContacts(contactsId);
        CommonResult result = new CommonResult();
        result.setData(uc);
        return result;
    }


    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "查询某个用户的所有联系人接口")
    @PostMapping("/contacts/showAllContacts")
    public CommonResult<List<UserContacts>> showAllContacts() {
        UserEntity user = userService.getUser(CurrentThreadData.iBitID());
        if (user == null) {
            CustomException.response(Error.USER_NOT_EXIST);
        }
        List<UserContacts> allContacts = userService.findAllContacts(user.getId());
        return CommonResult.make(allContacts);

    }

    //不需要登录
    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "发送邮件激活链接",notes = "{'email':邮箱账号}")
    @PostMapping("/sendAcLink")
    public CommonResult sendEmailLink(@RequestBody Map map){
        UserEntity user = userService.getUser(CurrentThreadData.iBitID());
        if (user == null) {
            CustomException.response(Error.USER_NOT_EXIST);
        }
        CommonResult commonResult = userService.sendEmailAcLink((String)map.get("email"),user);
        return commonResult;
    }

    //不需要登录
    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "发送邮件验证码")
    @PostMapping("/sendEmail")
    public CommonResult sendEmail(@RequestBody MailCodeReqDTO body) {
        if (StringUtils.isBlank(body.getEmail())) {
            CustomException.response(Error.EMAIL_NOT_EMPTY);
        }
        userService.sendEmail(body);
        return new CommonResult();
    }


    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "发送短信验证码")
    @PostMapping("/sendSmsCode")
    public CommonResult sendSmsCode(@RequestBody SmsCodeReqDTO body) {

        if (StringUtils.isBlank(body.getPhone())) {
            CustomException.response(Error.PHONE_NOT_EMPTY);
        }
        userService.sendSmsCode(body);
        return new CommonResult();
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "分享页面发送短信验证码",notes = "{'phone':手机号,type:(0.分享页面发送  1.会员页面发送  2.预热页面发送) ptype:(当ptype为9,为ibitcome自己的活动),ctype:(0 国内短信，1 国外短信)}")
    @PostMapping("/sendShareCode")
    public CommonResult sendSharePageSmsCode(@RequestBody Map map) {
        String phone = (String)map.get("phone");
        Integer type = (Integer)map.get("type");
        Integer ptype = 0;
        if(type == 2){
            ptype = (Integer)map.get("ptype");
        }
        if (StringUtils.isBlank(phone)) {
            CustomException.response(Error.PHONE_NOT_EMPTY);
        }
        Integer ctype = (Integer)map.get("ctype");
        if(ctype == null){
            ctype = 0;
        }
        CommonResult commonResult = userService.sengShareCode(phone,type,ptype,ctype);
        return commonResult;
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "分享页面短信验证码验证",notes = "{code:验证码，invicode:邀请码,phone:手机号,type:0.分享页面发送验证码  1.会员身份页面发送验证码 2.预热页面发送 ptype:1,2,3,4，5 }")
    @PostMapping("/validShareCode")
    public CommonResult validSharePageSmsCode(@RequestBody Map map) {
        String code = (String)map.get("code");
        String invicode = (String)map.get("invicode");
        String phone = (String)map.get("phone");
        Integer type = (Integer) map.get("type");
        Integer ptype = 0;
        if(type == 2){
            ptype = (Integer)map.get("ptype");
        }
        if((type != 2) && StringUtils.isBlank(invicode)){
            CustomException.response(Error.REQUEST_PARAM_INVALID);
        }
        if (StringUtils.isBlank(code) || StringUtils.isBlank(phone)) {
            CustomException.response(Error.REQUEST_PARAM_INVALID);
        }
        UserEntity user = userService.getUser(CurrentThreadData.iBitID());
        if(type == 1){
            if (user == null) {
                CustomException.response(Error.USER_NOT_EXIST);
            }
        }
        CommonResult commonResult = userService.validCode(code,invicode,phone,type,user,ptype);
        return commonResult;
    }



    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "获取邀请码")
    @RequestMapping(value = "/productCode", method = RequestMethod.POST)
    public CommonResult<InvitationCodeRepDTO> productCode() {
        UserEntity user = userService.getUser(CurrentThreadData.iBitID());
        if (user == null) {
            CustomException.response(Error.USER_NOT_EXIST);
        }
        CommonResult<InvitationCodeRepDTO> rst = new CommonResult<>();
        InvitationCodeRepDTO rsp = new InvitationCodeRepDTO();
        rsp.setInvitationCode(invitationCodeService.productCode(user.getId()));
        rsp.setUserNo(user.getLoginName());
        rst.setData(rsp);
        rst.setOK();
        return rst;
    }



    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "查询用户邀请的的新用户已激活数量")
    @RequestMapping(value = "/invitationMembers", method = RequestMethod.POST)
    public CommonResult<InvitationMembersRepDTO> invitationMembers() {
        CommonResult<InvitationMembersRepDTO> rst = new CommonResult<>();
        UserEntity user = userService.getUser(CurrentThreadData.iBitID());
        if (user == null) {
            CustomException.response(Error.USER_NOT_EXIST);
        }
        List<CodeConsumersDTO> codeConsumersList = invitationCodeService.getCodeConsumersList(user.getId());
        List<CodeConsumersDTO> activationList = codeConsumersList.stream().filter(codeConsumersDTO ->
                UserService.HAVE_WALLET.equalsIgnoreCase(codeConsumersDTO.getStatus())).
                collect(Collectors.toList());
        InvitationMembersRepDTO data = new InvitationMembersRepDTO();
        data.setActivatedNum(activationList.size());
        rst.setData(data);
        rst.setOK();
        return rst;
    }


    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "绑定手机号",notes = "参数说明：{phone:手机号码，code:验证码}")
    @RequestMapping(value = "/bindPhone", method = RequestMethod.POST)
    public CommonResult bindPhone(@RequestBody Map<String, String> body) {
        UserEntity user = userService.getUser(CurrentThreadData.iBitID());
        if (user == null) {
            CustomException.response(Error.USER_NOT_EXIST);
        }
        Integer userId = user.getId();
        String phone = body.get("phone");
        String code = body.get("code");
        userService.bindPhone(userId, phone, code);
        return new CommonResult();
    }


    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "数据统计")
    @RequestMapping(value = "/countData", method = RequestMethod.POST)
    public CommonResult count(@RequestBody CountData data) {
        data.setCreateTime(new Date());
        userService.countData(data);
        return new CommonResult();
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "绑定邮箱",notes = "参数说明：{email:邮箱，code:验证码}")
    @RequestMapping(value = "/bindEmail", method = RequestMethod.POST)
    public CommonResult bindEmail(@RequestBody Map<String, String> body) {
        UserEntity user = userService.getUser(CurrentThreadData.iBitID());
        if (user == null) {
            CustomException.response(Error.USER_NOT_EXIST);
        }
        Integer userId = user.getId();
        String email = body.get("email");
        String code = body.get("code");
        userService.bindEmail(userId, email, code);
        return new CommonResult();
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "提交活动钱包地址",notes = "参数说明：{address:钱包地址}")
    @RequestMapping(value = "/submitActiveAddress", method = RequestMethod.POST)
    public CommonResult submitActiveAddress(@RequestBody Map<String, String> body) {
        UserEntity user = userService.getUser(CurrentThreadData.iBitID());
        if (user == null) {
            CustomException.response(Error.USER_NOT_EXIST);
        }
        Integer userId = user.getId();
        String address = body.get("address");
        userService.submitActiveAddress(userId, address);
        return new CommonResult();
    }


    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "使用邀请码",notes = "参数说明：{invitationCode:邀请码}")
    @RequestMapping(value = "/useInvitationCode", method = RequestMethod.POST)
    public CommonResult useInvitationCode(@RequestBody Map<String, String> body) {
        UserEntity user = userService.getUser(CurrentThreadData.iBitID());
        if (user == null) {
            CustomException.response(Error.USER_NOT_EXIST);
        }
        Integer userId = user.getId();
        String invitationCode = body.get("invitationCode");
        //使用邀请码
        if (!invitationCodeService.consumeCode(invitationCode, userId)) {
            CustomException.response(Error.INVITATION_CODE_NOT_EXIST);
        }

        return new CommonResult();
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "邀请相关信息查询")
    @RequestMapping(value = "/getInvitationInfo", method = RequestMethod.POST)
    public CommonResult<InvitationInfoRepDTO> getInvitationInfo() {
        CommonResult<InvitationInfoRepDTO> result = new CommonResult<>();
        UserEntity user = userService.getUser(CurrentThreadData.iBitID());
        if (user == null) {
            CustomException.response(Error.USER_NOT_EXIST);
        }
        Integer userId = user.getId();
        result.setData(userService.getInvitationInfo(userId));
        return result;
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "获取当前用户已邀请的用户列表")
    @RequestMapping(value = "/getInvitedList", method = RequestMethod.POST)
    public CommonResult<PageRep<GetInvitedListRepDTO>> getInvitedList(@RequestBody PageReq body) {
        CommonResult<PageRep<GetInvitedListRepDTO>> result = new CommonResult<>();
        UserEntity user = userService.getUser(CurrentThreadData.iBitID());
        if (user == null) {
            CustomException.response(Error.USER_NOT_EXIST);
        }
        result.setData(userService.getInvitedList(user.getId(), body));
        return result;
    }

    @DecryptRequest
    @EncryptResponse
    @ApiOperation(value = "获取邀请前10的人气用户")
    @RequestMapping(value = "/getInvitationTop", method = RequestMethod.POST)
    public CommonResult<List<InvitationTopRepDTO>> getInvitationTop() {
        CommonResult<List<InvitationTopRepDTO>> result = new CommonResult<>();
        result.setData(userService.getInvitationTop());
        return result;
    }

    @ApiOperation(value = "活动是否开启  0 关闭 1开启")
    @RequestMapping(value = "/acControl", method = RequestMethod.GET)
    public CommonResult getACNumber(){
        String[] array = type.split(",");
        Map map = Maps.newLinkedHashMap();
        for(int i=0;i<array.length;i++){
            String s = redisTemplate.opsForValue().get(Constant.ACTIVITY_RESERVE_NUMBER+array[i]);
            Integer number = Integer.parseInt(s);
            map.put(Constant.ACTIVITY_RESERVE_NUMBER+array[i],number);
        }
        CommonResult result = new CommonResult();
        result.setData(map);
        return result;
    }


    @ApiOperation(value = "活动开关")
    @RequestMapping(value = "/ocControl", method = RequestMethod.GET)
    public CommonResult ControlACNumber(@ApiParam(value = "项目方id",required = true)@RequestParam int id,@ApiParam(value = "0 关闭  1开启")@RequestParam int open) {
        if (open !=0 && open != 1) {
            CustomException.response(Error.REQUEST_PARAM_INVALID);
        }
        redisTemplate.opsForValue().set(Constant.ACTIVITY_RESERVE_NUMBER+id,open+"");
        return new CommonResult();
    }

}



