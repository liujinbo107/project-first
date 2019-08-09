package com.ljb.pojo.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Transient;
import java.util.List;

/**
 * @author 刘进波
 * @create 2019-08-09 9:39
 */
@Data
public class Role {

    private Long id;

    private String roleName;


    private String miaoShu;


    private List<MenuInfo> listMenuInfo;


    private List<UserInfo> listUserInfo;


    private String userNames;
}
