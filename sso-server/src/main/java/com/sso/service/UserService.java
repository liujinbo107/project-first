package com.sso.service;

import com.ljb.pojo.entity.UserInfo;
import com.sso.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Component;

/**
 * @author 刘进波
 * @create 2019-08-05 19:48
 */
@Component
public class UserService {

    @Autowired
    private UserDao userDao;

    public UserInfo getUserByLogin(String loginName){
        //获取用户信息
        UserInfo byLoginName = userDao.findByLoginName(loginName);
        return byLoginName;
    }
}
