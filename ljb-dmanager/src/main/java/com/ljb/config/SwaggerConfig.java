package com.ljb.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

/**
 * @author 刘进波
 * @create 2019-07-20 8:53
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Autowired
    private Environment environment;

    @Bean
    public Docket getDocket(){
        Docket docket = new Docket(DocumentationType.SWAGGER_2);

        //配置接口的过滤start
        docket.select().apis(RequestHandlerSelectors.basePackage("com.ljb.web"))
            .paths(PathSelectors.ant("/**"))
            .build();
        //配置忽略的参数
        //docket.ignoredParameterTypes(String.class);

        //配置动态的显示接口文档 要求springboot版本在2.1.0以上
       // Profiles of = Profiles.of("dev", "test");
        //boolean b = environment.acceptsProfiles(of);
        //docket.enable(b);

        //配置API的分组  多个分组需要多个Docket实例
        docket.groupName("sso");

        //配置全局的参数
        ArrayList<Parameter> parameterList = new ArrayList<>();
        Parameter parameter=new ParameterBuilder()
                .name("token")
                .parameterType("cookie")
                .description("请求令牌")
                .modelRef(new ModelRef("string"))
                .build();
        parameterList.add(parameter);
        docket.globalOperationParameters(parameterList);
        docket.apiInfo(getApiInfo());
        return docket;

    }

    /*@Bean
    public Docket getDocket(){
        Docket docket = new Docket(DocumentationType.SWAGGER_2);
        docket.apiInfo(getApiInfo());

        //配置接口的过滤start
        docket.select().apis(RequestHandlerSelectors.basePackage("com.liujinbo.testswaggerdemo01.web"))
                .paths(PathSelectors.ant("/swagger/**"))
                .build();
        //配置忽略的参数
        docket.ignoredParameterTypes(String.class);

        //配置动态的显示接口文档 要求springboot版本在2.1.0以上
        Profiles of = Profiles.of("dev", "test");
        boolean b = environment.acceptsProfiles(of);
        docket.enable(b);
        return docket;

        //配置API的分组  多个分组需要多个Docket实例
        docket.groupName("swagger2");

    }*/

    private ApiInfo getApiInfo(){

        Contact contact = new Contact("刘进波", "http://www.baidu.com", "5555@.qq.com");
        ApiInfo apiInfo = new ApiInfo(
                "用户的管理服务",
                "APP用户管理的API接口文档",
                "v-1.0",
                "http://www.jd.com",
                contact,"监听信息",
                "http://www.taobao.com",
                new ArrayList<VendorExtension>()
        );
        return apiInfo;
    }


}
