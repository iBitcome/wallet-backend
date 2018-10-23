package com.rst.cgi.controller;

import com.rst.cgi.common.constant.Constant;
import com.rst.cgi.common.hbc.DecryptRequest;
import com.rst.cgi.common.hbc.EncryptResponse;
import com.rst.cgi.conf.security.CurrentThreadData;
import com.rst.cgi.data.dao.mysql.CommonDao;
import com.rst.cgi.data.entity.UserEntity;
import com.rst.cgi.service.UserService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Api(tags = "邮箱验证页面跳转")
@RequestMapping("/valid")
@Controller
public class PageController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserService userService;
    @Autowired
    private CommonDao commonDao;
    @Value("${push.server.message.type}")
    private int type;
    @GetMapping("/validEmail")
    public String validEmail(@RequestParam String token, @RequestParam String email,@RequestParam String ibit){
        String prefix = Constant.USER_BIND_EMAIL_KEY_PREFIX;
        String s = stringRedisTemplate.opsForValue().get(prefix + email);
        if(s==null){
            if(type == 0){
                return "/fail";
            }else{
                return "/fail2";
            }
        }else {
            if(ibit != null){
                UserEntity user = userService.getUser(ibit);
                user.setEmail(email);
                commonDao.update(user);
            }
            if(type == 0){
                return "/success";
            }else{
                return "/success2";
            }

        }
    }
}
