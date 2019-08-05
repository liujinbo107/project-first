package com.sso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sso-Server 启动类
 * @author 刘进波
 * @create 2019-08-05 11:52
 */
@SpringBootApplication
@EnableJpaAuditing
@RestController
@EntityScan(basePackages = {"com.ljb.pojo.**"})
public class SsoServer {

    public static void main(String[] args) {
        SpringApplication.run(SsoServer.class,args);
    }

    @RequestMapping("health")
    public String health(){
        System.out.println("=========SSO-SERVER");
        return "ok";
    }
}
