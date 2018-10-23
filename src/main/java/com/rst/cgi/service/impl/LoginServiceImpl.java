package com.rst.cgi.service.impl;

import com.rst.cgi.common.constant.Constant;
import com.rst.cgi.conf.security.BitUserDetails;
import com.rst.cgi.conf.security.LoginDetails;
import com.rst.cgi.service.LoginService;
import com.rst.cgi.service.RememberMeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author hujia
 * @date 2017/3/14
 */
@Service
public class LoginServiceImpl implements LoginService {
    private final Logger logger = LoggerFactory.getLogger(LoginService.class);
//    private static final int DEFAULT_SESSION_LIVE_TIME = 60 * 60;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RememberMeService rememberMeService;

    @Autowired
    private SessionRegistry sessionRegistry;

    private SessionAuthenticationStrategy sessionAuthenticationStrategy;

    @PostConstruct
    void init() {
        //配置session的处理策略
        if (sessionAuthenticationStrategy == null) {
            ConcurrentSessionControlAuthenticationStrategy sessionStrategy =
                    new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry);
            sessionStrategy.setMaximumSessions(1);
            sessionAuthenticationStrategy = sessionStrategy;
        }
    }

    @Override
    public void login(Long userId, boolean ignoreSessionStrategy,
                      HttpServletRequest request) throws AuthenticationException {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(userId, "auto");
        token.setDetails(new LoginDetails(LoginDetails.AUTH_TYPE_USER_ID));
        authentication(request, token, ignoreSessionStrategy);
    }

    @Override
    public void login(String account, String password, int passwordType,
                      HttpServletRequest request) throws AuthenticationException {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(account, password);
        token.setDetails(new LoginDetails(passwordType));
        authentication(request, token, false);
    }

    private void authentication(HttpServletRequest request,
                                UsernamePasswordAuthenticationToken token,
                                boolean ignoreSessionStrategy) {
        Authentication authenticatedUser = authenticationManager
                .authenticate(token);

        if (!ignoreSessionStrategy) {
            sessionAuthenticationStrategy.onAuthentication(authenticatedUser, request, null);
        }

        SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
        HttpSession session  = request.getSession();
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());
        session.setMaxInactiveInterval(Constant.DEFAULT_SESSION_LIVE_TIME);

        if (!ignoreSessionStrategy) {
            sessionRegistry.registerNewSession(session.getId(), authenticatedUser.getPrincipal());
        }

        BitUserDetails userDetails = (BitUserDetails) authenticatedUser.getPrincipal();
        session.setAttribute(RememberMeService.KEY_NAME,
                rememberMeService.set((long)userDetails.getUser().getId()));
    }
}
