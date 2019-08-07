package com.sso.service;

import com.ljb.pojo.entity.MenuInfo;
import com.ljb.pojo.entity.RoleInfo;
import com.ljb.pojo.entity.UserInfo;
import com.sso.dao.MenuDao;
import com.sso.dao.RoleDao;
import com.sso.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * @author 刘进波
 * @create 2019-08-05 19:48
 */
@Component
public class UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private MenuDao menuDao;

    public UserInfo getUserByLogin(String loginName){
        //获取用户信息
        UserInfo byLoginName = userDao.findByLoginName(loginName);
        if(byLoginName!=null){
            //获取用户的角色信息
            RoleInfo roleInfoByUserId = roleDao.forRoleInfoByUserId(byLoginName.getId());
            //设置用户的角色信息
            byLoginName.setRoleInfo(roleInfoByUserId);

            if(roleInfoByUserId!=null){
                //获取用户的权限信息
                List<MenuInfo> firstMenuInfo = menuDao.getFirstMenuInfo(roleInfoByUserId.getId(), 1,0L);
                //递归的查询子菜单权限
                Map<String,String> authMap=new Hashtable<>();
                this.getForMenuInfo(firstMenuInfo,roleInfoByUserId.getId(),authMap);
                //设置菜单的子权限
                byLoginName.setAuthmap(authMap);
                byLoginName.setListMenuInfo(firstMenuInfo);
            }
        }
        return byLoginName;
    }

    /**
     * 获取子权限的递归方法
     * @param firstMenuInfo
     * @param roleId
     */
    public void getForMenuInfo(List<MenuInfo> firstMenuInfo,Long roleId,Map<String,String> authMap){

        for(MenuInfo menuInfo:firstMenuInfo){
            int leval=menuInfo.getLeval() + 1;
            //获取下级的菜单信息
            List<MenuInfo> firstMenuInfo1 = menuDao.getFirstMenuInfo(roleId, leval,menuInfo.getId());
            if(firstMenuInfo1!=null){

                //整理后台的数据访问链接
                if(leval==4){
                    for(MenuInfo menu:firstMenuInfo1){
                        authMap.put(menu.getUrl(),"");
                    }
                }
                //设置查出来的菜单到父级对象中
                menuInfo.setMenuInfoList(firstMenuInfo1);
                //根据查出来的下级菜单继续查询该菜单包含的子菜单
                getForMenuInfo(firstMenuInfo1,roleId,authMap);
            }else{
                break;
            }
        }

    }
}
