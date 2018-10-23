package com.rst.cgi.conf;

import com.rst.cgi.common.constant.Constant;
import com.rst.cgi.common.hbc.SpecialAuthInterceptor;
import com.rst.cgi.controller.interceptor.SessionRenewalInterceptor;
import com.rst.cgi.controller.interceptor.TestControllerInterceptor;
import com.rst.cgi.service.EncryptDecryptService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by hujia on 2017/3/30.
 */
@Configuration
public class WebMvcConfigurer extends WebMvcConfigurerAdapter {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(make()).addPathPatterns("/test/**");
        registry.addInterceptor(new SpecialAuthInterceptor()).addPathPatterns("/inner/**");
        registry.addInterceptor(new SessionRenewalInterceptor())
                .addPathPatterns("/**").excludePathPatterns("/inner/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .exposedHeaders(Constant.CLIENT_VERSION,
                        "x-inner-token","x-auth-token",EncryptDecryptService.KEY_INDEX_HEADER);
    }

    @Bean
    public TestControllerInterceptor make(){
        return new TestControllerInterceptor();
    }

}
