package com.ljb.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    //删除用户时删除中间表
    public void deluserrole(@Param("userId") Long userId);

    //添加用户时添加中间表
    public void adduserrole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    //删除角色时删除角色和用户的中间表
    public void delroleuser(@Param("roleId") Long roleId);

    //删除角色时删除角色和权限的中间表
    public void delrolemenu(@Param("roleId") Long roleId);

    //给角色绑定权限
    public void addrolemenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

    public List<Map> selectRoleDao();
}
