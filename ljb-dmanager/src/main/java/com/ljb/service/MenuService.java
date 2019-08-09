package com.ljb.service;

import com.ljb.dao.MenuDao;
import com.ljb.pojo.entity.MenuInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author 刘进波
 * @create 2019-08-09 15:19
 */
@Component
public class MenuService {

    @Autowired
    private MenuDao menuDao;

    /**
     * 获取前三级菜单
     * @return
     */
    public List<MenuInfo> getMenuList(){

        //获取一级
        List<MenuInfo> allMenuList = menuDao.getAllMenuList(1, 0L);

        getForMenuInfo(allMenuList);
        return allMenuList;
    }

    /**
     * 递归查询权限
     * @param allMenuList
     */
    public void getForMenuInfo(List<MenuInfo> allMenuList){

        for(MenuInfo menuInfo:allMenuList){
            int leval=menuInfo.getLeval() + 1;
            if(leval==4){
                break;
            }
            //获取下级的菜单信息
            List<MenuInfo> firstMenuInfo1 = menuDao.getAllMenuList(leval,menuInfo.getId());
            if(firstMenuInfo1!=null){
                //设置查出来的菜单到父级对象中
                menuInfo.setMenuInfoList(firstMenuInfo1);
                //根据查出来的下级菜单继续查询该菜单包含的子菜单
                getForMenuInfo(firstMenuInfo1);
            }else{
                break;
            }
        }

    }
}
