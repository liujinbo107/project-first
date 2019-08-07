package com.ljb.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.persistence.EntityManager;

/**
 * @author 刘进波
 * @create 2019-08-07 8:56
 */
@Configuration
public class Myconfig {

    @Autowired
    private LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean;

    @Bean("myEntityManager")
    public EntityManager getEntityManager(LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean){
        return localContainerEntityManagerFactoryBean.getNativeEntityManagerFactory().createEntityManager();
    }
}
