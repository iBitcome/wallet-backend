package com.rst.cgi.service;

import com.rst.cgi.data.dto.CommonResult;
import com.rst.cgi.data.dto.PageRep;
import com.rst.cgi.data.dto.PageReq;
import com.rst.cgi.data.dto.SecondIdentify;
import com.rst.cgi.data.dto.request.*;
import com.rst.cgi.data.dto.response.*;
import com.rst.cgi.data.entity.CountData;
import com.rst.cgi.data.entity.UserContacts;
import com.rst.cgi.data.entity.UserEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * @author huangxiaolin
 * @date 2018-05-14 下午3:31
 */
public interface UserService {
    String HAVE_WALLET = "已有钱包";
    String NOT_HAVEN_WALLET = "没有钱包";
    String DELETED = "已删除";

    UserEntity saveOrUpdate(UserEntity user);

    /**
     * 添加冻结用户
     * @author hxl
     * 2018/5/31 下午1:48
     */
    void addFrozenUser(String email, Date frozenTime);

    /**
     * 忘记密码接口，如果用户处于冻结状态则设置为非冻结状态
     * @author huangxiaolin
     * @date 2018-05-15 11:23
     */
    void forgetPwd(String email, String password);

    /**
     * 通过邮箱查询用户
     * @author hxl
     * 2018/5/28 下午4:58
     */
    UserEntity findByEmail(String email);

    /**
     * 修改密码
     * @author huangxiaolin
     * @date 2018-05-14 15:35
     * @param oldPwd 原密码
     * @param newPwd 新密码
     */
    void updatePassword(int userId, String oldPwd, String newPwd);

    /**
     * 更新用户的冻结状态
     * @author hxl
     * 2018/5/28 下午4:57
     */
    void updateFrozen(String email, int frozen, Date frozenTime);

    /**
     * 联系人保存
     * @author hxl
     * 2018/5/29 下午3:17
     */
    void saveOrUpdateContacts(int userId, UserContactsDTO uc);

    /**
     * 删除联系人
     * @author hxl
     * 2018/5/29 下午3:53
     */
    void deleteContacts(int userId, int contactsId);

    /**
     * 查询联系人信息
     * @author hxl
     * 2018/5/29 下午6:14
     */
    List<UserContacts> findContactsList(int userId);

    /**
     * 根据联系人查询地址列表
     * @author hxl
     * 2018/5/29 下午6:27
     */
    UserContacts findContacts(int contactsId);


    List<UserContacts> findAllContacts(int userId);

    /**
     * 注册
     * @param body
     */
    void regist(UserRegisterDTO body);

    /**
     * 发送短信验证码
     * @param body
     */
    void sendSmsCode(SmsCodeReqDTO body);

    /**
     * 激活用户账号
     * @param userId
     */
    void activationAccount(Integer userId);

    /**
     * 绑定手机号
     * @param userId
     * @param phone
     * @param code
     */
    void bindPhone(Integer userId, String phone, String code);
    /**
     * 登陆
     * @param email
     * @param token
     * @param type
     * @return
     */
    LoginResDTO login(String email, String token, int type, HttpServletRequest request);

    /**
     * 获取邮箱验证码
     * @param body
     */
    void sendEmail(MailCodeReqDTO body);

    /**
     * 获取用户信息（登录状态下）
     * @param userId
     * @return
     */
    UserInfo getUser(Integer userId);

    /**
     * 修改二次验证参数信息
     * @param userId
     * @param se
     * @return
     */
    UserInfo updateSecond(Integer userId, SecondIdentify se);

    void countData(CountData data);

    List<String> sendCountData(int id);

    int IOSsaveContacts(int userId,UserContactsDTO uc);

    /**
     * 创建一个用户
     * @return 用户标识
     */
    String createUser();

    /**
     * h
     * @param identify
     * @return
     */
    UserEntity getUser(String identify);

    /**
     * 绑定邮箱
     * @param userId
     * @param email
     * @param code
     */
    void bindEmail(Integer userId, String email, String code);

    /**
     * 提交活动地址
     * @param userId
     * @param address
     */
    void submitActiveAddress(Integer userId, String address);

    /**
     * 查询邀请信息
     * @param userId
     * @return
     */
    InvitationInfoRepDTO getInvitationInfo(Integer userId);

    /**
     * 发送邮箱超链接
     * @param email
     * @return
     */
    CommonResult sendEmailAcLink(String email,UserEntity user);

    /**
     * 分享页面发送短信验证码
     * @param body
     * @return
     */
    CommonResult sengShareCode(String body,int type,Integer ptype,Integer ctype);

    /**
     * 验证分享页面手机验证码
     * @param code
     * @param inviCode
     * @return
     */
    CommonResult validCode(String code,String inviCode,String phone,int type,UserEntity user,Integer ptype);

    /**
     *
     * @param userId
     * @return
     */
    PageRep<GetInvitedListRepDTO> getInvitedList(Integer userId, PageReq body);

    /**
     * 获取人气前十的用户
     * @return
     */
    List<InvitationTopRepDTO> getInvitationTop();


    String getMailContentTemplate();
}
