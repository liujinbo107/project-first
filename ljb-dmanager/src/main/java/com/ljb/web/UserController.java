package com.ljb.web;

import com.ljb.config.ResponseResult;
import com.ljb.dao.RoleDao;
import com.ljb.dao.UserDao;
import com.ljb.dao.UserMapper;
import com.ljb.pojo.entity.RoleInfo;
import com.ljb.pojo.entity.UserInfo;
import com.ljb.utils.MD5;
import com.ljb.utils.UID;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author 刘进波
 * @create 2019-08-07 8:53
 */
@RestController
public class UserController {

    @Autowired
    @Qualifier("myEntityManager")
    private EntityManager myEntityManager;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private UserDao userDao;

    @Autowired(required = false)
    private UserMapper userMapper;

    /**
     * 获取用户列表
     * @param userInfoMap
     * @return
     */
    @RequestMapping("toUserList")
    public ResponseResult touserlist(@RequestBody Map<String,String> userInfoMap){

        StringBuffer stringBuffer = new StringBuffer("select * from base_user where 1 = 1");

        StringBuffer stringBufferCount = new StringBuffer("select count(1) from base_user where 1=1");

        if(userInfoMap.get("userName")!=""){
            //查询
            stringBuffer.append(" and userName like concat('%','"+userInfoMap.get("userName").toString()+"','%')");
            //总条数
            stringBufferCount.append(" and userName like concat('%','"+userInfoMap.get("userName").toString()+"','%')");
        }
        if(userInfoMap.get("sex")!=""){
            //查询性别
            stringBuffer.append(" and sex ="+userInfoMap.get("sex"));
            //总条数
            stringBufferCount.append(" and sex ="+userInfoMap.get("sex"));
        }
        if(userInfoMap.get("sta")!=""&&userInfoMap.get("sto")!=""&&userInfoMap.get("sta")!=null&&userInfoMap.get("sto")!=null){
            //区间查询
            stringBuffer.append(" and createTime between '"+userInfoMap.get("sta").toString()+"' and '"+userInfoMap.get("sto").toString()+"'");
            //总条数
            stringBufferCount.append(" and createTime between '"+userInfoMap.get("sta").toString()+"' and '"+userInfoMap.get("sto").toString()+"'");
        }
        if(userInfoMap.get("sta")!=""&&userInfoMap.get("sta")!=null&& userInfoMap.get("sto")==""){
            //区间查询
            stringBuffer.append(" and createTime >= '"+userInfoMap.get("sta").toString()+"'");
            //总条数
            stringBufferCount.append(" and createTime >= '"+userInfoMap.get("sta").toString()+"'");
        }
        if(userInfoMap.get("sto")!=""&&userInfoMap.get("sto")!=null&& userInfoMap.get("sta")==""){
            //区间查询
            stringBuffer.append(" and createTime <= '"+userInfoMap.get("sto").toString()+"'");
            //总条数
            stringBufferCount.append(" and createTime <= '"+userInfoMap.get("sto").toString()+"'");
        }

        //获取当前页
        int pageNo = Integer.parseInt(userInfoMap.get("pageNo").toString());

        //获取每页条数
        int pageSize = Integer.parseInt(userInfoMap.get("pageSize").toString());

        //分页
        stringBuffer.append(" limit "+(pageNo-1)*pageSize+","+pageSize);

        System.out.println(userInfoMap.toString());

        //查列表
        Query nativeQuery = myEntityManager.createNativeQuery(stringBuffer.toString(), UserInfo.class);

        //总条数
        Query nativeQuery1 = myEntityManager.createNativeQuery(stringBufferCount.toString());

        List<UserInfo> resultList = nativeQuery.getResultList();

        //查询用户的角色信息
        resultList.forEach(user->{
            RoleInfo roleInfo = roleDao.forRoleInfoByUserId(user.getId());
            user.setRoleInfo(roleInfo);
        });

        ResponseResult responseResult = ResponseResult.getResponseResult();

        Map<String, Object> map = new HashMap<>();

        //放入map集合
        map.put("list",resultList);

        map.put("total",nativeQuery1.getResultList().get(0));

        responseResult.setResult(map);

        System.out.println(map);

        return responseResult;

    }

    /**
     * 删除用户
     * @param map
     * @return
     */
    @RequestMapping("todeluser")
    public ResponseResult todeluser(@RequestBody Map<String,Object> map){

        //获取userid
        long id = Long.parseLong(map.get("id").toString());

        ResponseResult responseResult = ResponseResult.getResponseResult();
        try {
            userDao.deleteById(id);

            responseResult.setSuccess("删除成功");

            responseResult.setCode(200);

        }catch (RuntimeException e){

            responseResult.setSuccess("删除失败");

            responseResult.setCode(500);
        }
        //
        return responseResult;

    }

    /**
     * 上传图片
     * @param file
     * @throws IOException
     */
    @RequestMapping("touploaduser")
    public void touploaduser(@Param("file") MultipartFile file) throws IOException {

        file.transferTo(new File("E:\\img\\"+file.getOriginalFilename()));
    }

    /**
     * 添加用户
     * @param userInfo
     * @return
     */
    @RequestMapping("toadduser")
    public ResponseResult toadduser(@RequestBody UserInfo userInfo){

        ResponseResult responseResult = ResponseResult.getResponseResult();

        System.out.println(userInfo+"用户信息******");
        try {

            //判断登录名称是否唯一
            if(userDao.findAllByLoginName(userInfo.getLoginName())!=null){

                responseResult.setCode(203);

                responseResult.setSuccess("登陆名称已存在");

                return responseResult;
            }
            //算法的id
            userInfo.setId(UID.next());
            //密码加密
            String lcg = MD5.encryptPassword(userInfo.getPassword(), "lcg");
            //重新设置密码
            userInfo.setPassword(lcg);

            userDao.save(userInfo);

            responseResult.setSuccess("添加成功");

            responseResult.setCode(200);
        }catch (Exception e){
            responseResult.setSuccess("添加失败");

            responseResult.setCode(500);
        }

        return responseResult;
    }

    /**
     * 修改用户
     * @param userInfo
     * @return
     */
    @RequestMapping("toupdateuser")
    public ResponseResult toupdateuser(@RequestBody UserInfo userInfo){

        ResponseResult responseResult = ResponseResult.getResponseResult();

        System.out.println(userInfo+"用户信息******");
        try {

            //判断登录名称是否唯一
            UserInfo allByLoginName = userDao.findAllByLoginName(userInfo.getLoginName());

            if(allByLoginName!=null){
                //判断是否是修改对象
                if(!userInfo.getId().toString().equals(allByLoginName.getId().toString())){
                    responseResult.setCode(203);

                    responseResult.setSuccess("登陆名称已存在");

                    return responseResult;
                }
            }
            //密码加密
            String lcg = MD5.encryptPassword(userInfo.getPassword(), "lcg");
            //重新设置密码
            userInfo.setPassword(lcg);

            userDao.save(userInfo);

            responseResult.setSuccess("修改成功ss");

            responseResult.setCode(200);
        }catch (Exception e){
            responseResult.setSuccess("修改失败");

            responseResult.setCode(500);
        }

        return responseResult;
    }

    /**
     * 角色列表
     * @return
     */
    @RequestMapping("tofindallrole")
    public ResponseResult findallrole(){

        //获取所有角色列表
        List<RoleInfo> all = roleDao.findAll();

        ResponseResult responseResult = ResponseResult.getResponseResult();

        responseResult.setResult(all);

        return responseResult;

    }

    /**
     * 用户绑定角色
     * @param map
     * @return
     */
    @RequestMapping("tobdrole")
    public ResponseResult tobdrole(@RequestBody Map<String,String> map){

        //获取用户id
        long userId = Long.parseLong(map.get("userId"));

        //删除用户之前绑定的角色
        userMapper.deluserrole(userId);

        //获取绑定的角色id
        long roleId = Long.parseLong(map.get("roleId"));

        ResponseResult responseResult = ResponseResult.getResponseResult();

        try {
            //角色绑定
            userMapper.adduserrole(userId,roleId);

            responseResult.setSuccess("绑定成功");

            responseResult.setCode(200);
        }catch (Exception e){

            responseResult.setSuccess("绑定失败");

            responseResult.setCode(500);
        }
        return responseResult;
    }



}
