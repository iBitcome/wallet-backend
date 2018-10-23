package com.rst.cgi.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/** 
 * Created by jiaoweiwei on 2017/03/02. 
 */  
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    /** 
     * 可以定义多个组，比如本类中定义把test和demo区分开了 （访问页面就可以看到效果了） 
     * 
     */  
    @Bean
    public Docket Api() {
        List<Parameter> operationParameters = new ArrayList<Parameter>();
        ParameterBuilder aParameterBuilder = new ParameterBuilder();
        aParameterBuilder.name("x-auth-token").description("Token 除了登录接口,注册接口外都需要").modelRef(new ModelRef("string")).parameterType("header").required(false).build();
        operationParameters.add(aParameterBuilder.build());
        return new Docket(DocumentationType.SWAGGER_2)
                .globalOperationParameters(operationParameters)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.rst.cgi.controller"))
                .paths(PathSelectors.any()).build();  
    }


    private ApiInfo apiInfo() {  
        return new ApiInfoBuilder()  
                .title("BitMain电子钱包cgi APIs")
                .description(" API")
                .termsOfServiceUrl("") 
                .version("1.0")  
                .build();  
    }
}  