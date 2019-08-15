package com.ljb.web;

import com.ljb.config.ResponseResult;
import com.ljb.dao.MenuDao;
import com.ljb.dao.RoleDao;
import com.ljb.dao.UserMapper;
import com.ljb.pojo.entity.MenuInfo;

import com.ljb.pojo.entity.RoleInfo;

import com.ljb.service.MenuService;
import com.ljb.service.RoleService;
import com.ljb.utils.StringToInteger;
import com.ljb.utils.UID;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 刘进波
 * @create 2019-08-08 14:09
 */
@RestController
@Api(tags = "角色管理接口")
public class RoleController {

    @Autowired
    @Qualifier("myEntityManager")
    private EntityManager myEntityManager;

    @Autowired
    private RoleDao roleDao;

    @Autowired(required = false)
    private UserMapper userMapper;

    @Autowired
    private RoleService roleService;

    @Autowired
    private MenuDao menuDao;


    /**
     * 获取角色列表 模糊查询
     * @param roleInfoMap
     * @return
     */
    @RequestMapping("toRoleList")
    @ApiOperation("获取角色列表")
    public ResponseResult toRoleList(@RequestBody Map<String,String> roleInfoMap){

        StringBuffer stringBuffer = new StringBuffer("select * from base_role where 1 = 1");

        StringBuffer stringBufferCount = new StringBuffer("select count(1) from base_role where 1=1");

        if(roleInfoMap.get("roleName")!=""){
            //查询
            stringBuffer.append(" and roleName like concat('%','"+roleInfoMap.get("roleName").toString()+"','%')");
            //总条数
            stringBufferCount.append(" and roleName like concat('%','"+roleInfoMap.get("roleName").toString()+"','%')");
        }

        //获取当前页
        int pageNo = Integer.parseInt(roleInfoMap.get("pageNo").toString());

        //获取每页条数
        int pageSize = Integer.parseInt(roleInfoMap.get("pageSize").toString());

        //分页
        stringBuffer.append(" ORDER BY createTime desc  limit "+(pageNo-1)*pageSize+","+pageSize);

        System.out.println(roleInfoMap.toString());

        myEntityManager.clear();
        //查列表
        Query nativeQuery = myEntityManager.createNativeQuery(stringBuffer.toString(), RoleInfo.class);

        nativeQuery.setFlushMode(FlushModeType.AUTO);

        //总条数
        Query nativeQuery1 = myEntityManager.createNativeQuery(stringBufferCount.toString());



        List<RoleInfo> resultList = nativeQuery.getResultList();

        Map<String, Object> map = new HashMap<>();

        ResponseResult responseResult = ResponseResult.getResponseResult();

        //查询角色的权限信息以及绑定的用户
        resultList.forEach(role->{
            //List<MenuInfo> roleMenu = roleService.getRoleMenu(role);
            List<MenuInfo> getrolemenu = menuDao.getrolemenu(role.getId());
            String userForRole = roleService.getUserForRole(role);
            role.setUserNames(userForRole);
            role.setListMenuInfo(getrolemenu);

        });
        //放入map集合
        map.put("list",resultList);

        map.put("total",nativeQuery1.getResultList().get(0));

        responseResult.setResult(map);

        System.out.println(map);

        return responseResult;
    }

    /**
     * 添加角色信息
     * @param roleInfo
     * @return
     */
    @RequestMapping("toaddrole")
    @ApiOperation("添加角色")
    public ResponseResult toaddrole(@RequestBody RoleInfo roleInfo){
        ResponseResult responseResult = ResponseResult.getResponseResult();

        //判断roleName是否唯一
        RoleInfo allByRoleName = roleDao.findAllByRoleName(roleInfo.getRoleName());

       try {
           if(allByRoleName!=null){
               responseResult.setCode(203);

               responseResult.setSuccess("角色名称已存在");

               return responseResult;
           }
           //随机设置角色id
           roleInfo.setId(UID.next());
           //添加角色
           roleDao.saveAndFlush(roleInfo);



           responseResult.setSuccess("添加成功");

           responseResult.setCode(200);
       }catch (Exception e){
           responseResult.setSuccess("添加失败");

           responseResult.setCode(500);
       }
       return responseResult;
    }

    /**
     * 删除角色
     * @param map
     * @return
     */
    @RequestMapping("todelrole")
    @ApiOperation("删除角色")
    public ResponseResult todelrole(@RequestBody Map<String,Object> map){

        //获取角色id
        long id = Long.parseLong(map.get("id").toString());

        ResponseResult responseResult = ResponseResult.getResponseResult();
        try {
            //删除角色和用户的中间表数据
            userMapper.delroleuser(id);

            //删除角色和权限的中间表数据
            userMapper.delrolemenu(id);

            //删除角色
            roleDao.deleteById(id);

            responseResult.setSuccess("删除成功");

            responseResult.setCode(200);

        }catch (Exception e){

            responseResult.setSuccess("删除失败");

            responseResult.setCode(500);
        }
        //
        return responseResult;
    }

    /**
     * 获取用户角色的权限列表
     * @return
     */
    @RequestMapping("tofindallmenuByRoleId")
    @ApiOperation("获取用户角色的权限列表")
    public ResponseResult tofindallmenuByRoleId(@RequestBody Map<String,String> map){

        //获取用户的角色id
        long id = Long.parseLong(map.get("id"));

        RoleInfo roleInfo = roleDao.findById(id).get();

        ResponseResult responseResult = ResponseResult.getResponseResult();

        List<MenuInfo> menuInfos = roleService.getRoleMenu(roleInfo);

        responseResult.setResult(menuInfos);

        return responseResult;
    }

    /**
     * 获取所有权限列表
     * @param
     * @return
     */
    @RequestMapping("tofindallmenu")
    @ApiOperation("获取所有的权限")
    public ResponseResult tofindallmenu(){

        ResponseResult responseResult = ResponseResult.getResponseResult();

        List<MenuInfo> allMenuList = menuDao.getAllMenuList(1, 0L);

        List<MenuInfo> menuInfos = roleService.menuInfoList(allMenuList);

        responseResult.setResult(menuInfos);

        return responseResult;
    }

    /**
     *角色绑定权限  修改角色
     * @param map
     * @return
     */
    @RequestMapping("tobdmenu")
    @ApiOperation("角色绑定权限")
    public ResponseResult tobdmenu(@RequestBody Map<String,String> map){

        //获取id
        Long id = Long.parseLong(map.get("id"));

        String roleName = map.get("roleName");

        String miaoShu = map.get("miaoShu");

        String mid = map.get("mid");

        //将字符串转换为数组
        String[] split = mid.split(",");

        List<Long> integers = StringToInteger.toInteger(split);

        //根据查询对象
        RoleInfo roleInfo = roleDao.findById(id).get();

        roleInfo.setRoleName(roleName);

        roleInfo.setMiaoShu(miaoShu);

        roleInfo.setId(id);

        ResponseResult responseResult = ResponseResult.getResponseResult();
        try {
            //修改角色
            roleDao.saveAndFlush(roleInfo);



            userMapper.delrolemenu(id);

            //添加中间表
           for(Long ii: integers){
                userMapper.addrolemenu(id,ii);
            }

            responseResult.setSuccess("编辑成功");

            responseResult.setCode(200);

        }catch (Exception e){
            responseResult.setSuccess("编辑失败");

            responseResult.setCode(500);
        }


        return responseResult;

    }
}
