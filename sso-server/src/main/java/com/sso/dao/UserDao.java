package com.sso.dao;

import com.ljb.pojo.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * @author 刘进波
 * @create 2019-08-05 19:49
 */
public interface UserDao extends JpaRepository<UserInfo,Long> {

    @Query(value = "select * from base_user where loginName=?1",nativeQuery = true)
    public UserInfo findByLoginName(String loginName);

    @Query(value = "select * from base_user where tel=?1",nativeQuery = true)
    public UserInfo findByTel(String tel);
}
