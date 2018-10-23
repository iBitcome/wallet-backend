package com.rst.cgi.conf.security;

import com.rst.cgi.common.constant.Error;
import com.rst.cgi.common.utils.WebUtil;
import com.rst.cgi.data.dto.CommonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Spring Security配置
 * @author huangxiaolin
 * @date 2018-05-15 14:22
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private BitUserAuthProvider bitUserAuthProvider;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(bitUserAuthProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .headers()
                    .frameOptions().disable().contentTypeOptions().disable().and()//禁用头，使前端可以使用iframe标签
                .authorizeRequests()
                .mvcMatchers(
                        "/**",
                        "/swagger**", //swagger
                        "/swagger-resources/**", //swagger
                        "/webjars/**", //swagger
                        "/v2/*", //swagger
                        "/test/**",       //测试接口
                        "/user/login",//登录接口地址
                        "/user/sendEmail",
                        "/user/countData",
                        "/user/sendSmsCode",
                        "/user/regist",
                        "/user/countData",
                        "/user/accountIsExist",
                        "/user/forgetPwd",
                        "/mobile/config/**",
                        "/mobile/news/**",
                        "/market/**",
                        "/valid/**",
//                        "/mobile/wallet/getKey",
                        "/mobile/wallet/**",
                        "**/anon/**",
                        "/mobile/transaction/getAllRate"
                ).permitAll()
                .mvcMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated().and()
            .formLogin()
                .disable()//禁用security的表单登录，自定义实现
            .logout()
                .logoutUrl("/user/logout")
                .logoutSuccessHandler(new BaseAuthHandler()).and()
            .exceptionHandling()
                .accessDeniedHandler(new BaseAuthHandler())
                .authenticationEntryPoint(new BaseAuthHandler()).and()
            .sessionManagement();
//                .maximumSessions(1)//配置同一用户同时登录的数量
//                .expiredSessionStrategy(new BaseAuthHandler());

    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    //用户认证基础类
    private static class BaseAuthHandler implements AccessDeniedHandler, AuthenticationEntryPoint,
                      SessionInformationExpiredStrategy, LogoutSuccessHandler {
        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response,
                           AccessDeniedException accessDeniedException) throws IOException, ServletException {
            WebUtil.handleHeaderData(request);
            WebUtil.writeJSON(request, response, Error.SESSION_EXPIRE);
        }

        //未登录时访问
        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response,
                             AuthenticationException authException) throws IOException, ServletException {
            WebUtil.handleHeaderData(request);
            WebUtil.writeJSON(request, response, Error.SESSION_EXPIRE);
        }

        //session过期策略
        @Override
        public void onExpiredSessionDetected(SessionInformationExpiredEvent see)
                throws IOException, ServletException {
            WebUtil.handleHeaderData(see.getRequest());
            WebUtil.writeJSON(see.getRequest(), see.getResponse(), Error.SESSION_EXPIRE);
        }

        //退出成功
        @Override
        public void onLogoutSuccess(HttpServletRequest request,
                                    HttpServletResponse response, Authentication authentication)
                throws IOException, ServletException {
            WebUtil.handleHeaderData(request);
            WebUtil.writeJSON(request, response, new CommonResult<>());
        }
    }
}
