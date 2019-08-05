package com.ljb;

import com.ljb.config.MyReslover;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 刘进波
 * @create 2019-08-04 20:04
 */
@SpringBootApplication
@RestController
public class GateWayApllication {

    public static void main(String[] args) {
        SpringApplication.run(GateWayApllication.class,args);
    }

    @RequestMapping("serverhealth")
    public String serverhealth(){
        System.out.println("===========gateway ok========");
        return "ok";
    }

    @Bean(name="myAddrReslover")
    public MyReslover getMyReslover(){
        return new MyReslover();
    }

}
