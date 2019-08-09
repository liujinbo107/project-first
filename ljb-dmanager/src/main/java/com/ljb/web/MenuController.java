package com.ljb.web;

import com.ljb.config.ResponseResult;
import com.ljb.dao.MenuDao;
import com.ljb.pojo.entity.MenuInfo;
import com.ljb.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author 刘进波
 * @create 2019-08-09 15:10
 */
@RestController
public class MenuController {

    @Autowired
    private MenuDao menuDao;

    @Autowired
    private MenuService menuService;

    /**
     * 三级权限列表
     * @return
     */
    @RequestMapping("toMenuList")
    public ResponseResult toMenuList(){
        //查询权限列表
        List<MenuInfo> menuList = menuService.getMenuList();

        ResponseResult responseResult = ResponseResult.getResponseResult();

        responseResult.setResult(menuList);

        return responseResult;
    }

    /**
     * 根据3级id获取4级菜单
     * @param map
     * @return
     */
    @RequestMapping("toMenuForMenu")
    public ResponseResult toMenuForMenu(@RequestBody Map<String,String> map){

        //获取3级权限的id
        long id = Long.parseLong(map.get("id"));

        //根据parentid查询4级权限
        List<MenuInfo> allMenuList = menuDao.getAllMenuList(4, id);

        ResponseResult responseResult = ResponseResult.getResponseResult();

        responseResult.setResult(allMenuList);

        return responseResult;

    }

}
