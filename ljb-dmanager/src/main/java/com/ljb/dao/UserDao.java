package com.ljb.dao;

import com.ljb.pojo.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserDao extends JpaRepository<UserInfo,Long> {

    public UserInfo findAllByLoginName(String loginName);

    @Query(value = "select GROUP_CONCAT(bu.userName) userNames from base_user bu INNER JOIN base_user_role bur on bu.id=bur.userId where bur.roleId=?1",nativeQuery = true)
    public String findUserForRoleId(Long roleId);

}
