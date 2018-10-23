package com.rst.cgi.conf.security;

import com.rst.cgi.common.constant.Constant;
import com.rst.cgi.common.constant.Error;
import com.rst.cgi.common.utils.EncodeUtil;
import com.rst.cgi.controller.interceptor.CustomException;
import com.rst.cgi.data.dao.mysql.UserDao;
import com.rst.cgi.data.entity.RememberMe;
import com.rst.cgi.data.entity.UserEntity;
import com.rst.cgi.service.RememberMeService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * 用户认证过程
 * @author huangxiaolin
 * @date 2018-05-15 14:48
 */
@Component
public class BitUserAuthProvider extends AbstractUserDetailsAuthenticationProvider {

    @Autowired
    private UserDao userDao;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RememberMeService rememberMeService;

     /**
      * 该方法只是查询根据用户名查询用户信息，还没有进行验证用户名和密码是否正确
      * @author huangxiaolin
      * @date 2018-05-15 14:47
      */
    @Override
    protected UserDetails retrieveUser(String username,
                                       UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        LoginDetails loginDetails = (LoginDetails)authentication.getDetails();

        UserEntity user;
        if (loginDetails.getType() == LoginDetails.AUTH_TYPE_AUTO) {
            RememberMe rm = rememberMeService.get((String)authentication.getCredentials());
            if (rm == null || rm.getUserId() == null) {
                throw new BadCredentialsException("自动登录token过期或不正确");
            }
            user = userDao.findById(rm.getUserId().intValue());
        } else if (loginDetails.getType() == LoginDetails.AUTH_TYPE_USER_ID) {
            user = userDao.findById(Integer.parseInt(username));
        } else {
            user = userDao.findByEmail(username);
        }

        if (user == null) {
            CustomException.response(Error.ACCOUNT_NOT_EXIST);
        }

        if (org.springframework.util.StringUtils.isEmpty(user.getLoginName())) {
            user.setLoginName(EncodeUtil.generateIdentify());
            userDao.update(user);
        }

        return new BitUserDetails(user);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        //type为1代表邮箱登录，type为2代表验证码登录
        LoginDetails loginDetails = (LoginDetails)authentication.getDetails();

        if (loginDetails == null) {
            throw new BadCredentialsException(Error.LOGIN_TYPE_INVALID.getMsg());
        }

        Object credential = authentication.getCredentials();

        //用户输入的密码字段
        String password = (credential == null) ? null : credential.toString();
        if (StringUtils.isEmpty(password)) {
            throw new BadCredentialsException(Error.PASSWORD_NOT_EMPTY.getMsg());
        }

        if (loginDetails.getType() == LoginDetails.AUTH_TYPE_AUTO ||
                loginDetails.getType() == LoginDetails.AUTH_TYPE_USER_ID) {
            //no check
        }
        //密码登录
        else if (loginDetails.getType() == LoginDetails.AUTH_TYPE_PWD) {
            //比较密码
            if (!userDetails.getPassword().equals(DigestUtils.md5Hex(password))) {
                throw new BadCredentialsException(Error.USERNAME_OR_PASSWORD_INVALID.getMsg());
            }
        } else if (loginDetails.getType() == LoginDetails.AUTH_TYPE_EMAIL_CODE) {
            String validateCode = redisTemplate.opsForValue().get(
                    Constant.USER_LOGIN_KEY_PREFIX + userDetails.getUsername());
            if (StringUtils.isEmpty(validateCode) || !validateCode.equals(password)) {
                CustomException.response(Error.VALIDATE_CODE_INVALID);
            }
             //保存成功后删除验证码
            redisTemplate.delete(Constant.USER_LOGIN_KEY_PREFIX + userDetails.getUsername());
        } else {
            throw new BadCredentialsException(Error.LOGIN_TYPE_INVALID.getMsg());
        }

        BitUserDetails bitUserDetails = (BitUserDetails) userDetails;
        CurrentThreadData.setIBitID(bitUserDetails.getUser().getLoginName());
    }
}
