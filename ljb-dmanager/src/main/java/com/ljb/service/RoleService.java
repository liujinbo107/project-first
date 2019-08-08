package com.ljb.service;

import com.ljb.dao.MenuDao;
import com.ljb.dao.RoleDao;
import com.ljb.dao.UserDao;
import com.ljb.pojo.entity.MenuInfo;
import com.ljb.pojo.entity.RoleInfo;
import com.ljb.pojo.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * @author 刘进波
 * @create 2019-08-08 15:14
 */
@Component
public class RoleService {

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private MenuDao menuDao;

    @Autowired
    private UserDao userDao;

    /**
     * 角色获取权限列表
     * @param roleInfo
     * @return
     */
    public List<MenuInfo> getRoleMenu(RoleInfo roleInfo){

            //获取用户的权限信息
            List<MenuInfo> firstMenuInfo = menuDao.getFirstMenuInfo(roleInfo.getId(), 1,0L);
            //递归的查询子菜单权限
            Map<String,String> authMap=new Hashtable<>();
            this.getForMenuInfo(firstMenuInfo,roleInfo.getId());

            return firstMenuInfo;
    }

    /**
     * 获取子权限的递归方法
     * @param firstMenuInfo
     * @param roleId
     */
    public void getForMenuInfo(List<MenuInfo> firstMenuInfo,Long roleId){

        for(MenuInfo menuInfo:firstMenuInfo){
            int leval=menuInfo.getLeval() + 1;
            //获取下级的菜单信息
            List<MenuInfo> firstMenuInfo1 = menuDao.getFirstMenuInfo(roleId, leval,menuInfo.getId());
            if(firstMenuInfo1!=null){
                //设置查出来的菜单到父级对象中
                menuInfo.setMenuInfoList(firstMenuInfo1);
                //根据查出来的下级菜单继续查询该菜单包含的子菜单
                getForMenuInfo(firstMenuInfo1,roleId);
            }else{
                break;
            }
        }

    }

    /**
     * 获取绑定该角色的用户
     * @param roleInfo
     * @return
     */
    public String getUserForRole(RoleInfo roleInfo){

        String userForRoleId = userDao.findUserForRoleId(roleInfo.getId());

        return userForRoleId;
    }

    /**
     * 权限列表
     * @return
     */
    public List<MenuInfo> menuInfoList(List<MenuInfo> allMenuList){

        for(MenuInfo menuInfo:allMenuList) {
            int leval = menuInfo.getLeval() + 1;
            List<MenuInfo> allMenuList1 = menuDao.getAllMenuList(leval, menuInfo.getId());
            if (allMenuList1 != null) {

                menuInfo.setMenuInfoList(allMenuList1);
                menuInfoList(allMenuList1);
            } else {
                break;
            }
        }
        return allMenuList;
    }
}
