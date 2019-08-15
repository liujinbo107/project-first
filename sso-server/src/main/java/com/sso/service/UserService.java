package com.sso.service;

import com.ljb.pojo.entity.MenuInfo;
import com.ljb.pojo.entity.RoleInfo;
import com.ljb.pojo.entity.UserInfo;
import com.ljb.utils.HttpUtils;
import com.sso.dao.MenuDao;
import com.sso.dao.RoleDao;
import com.sso.dao.UserDao;
import org.apache.http.HttpResponse;
import org.aspectj.apache.bcel.classfile.Code;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
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

    /**
     * 根据登陆名
     * @param loginName
     * @return
     */
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
     * 根据手机号
     * @param phone
     * @return
     */
    public UserInfo getUserByphone(String phone){
        //获取用户信息
        UserInfo byLoginName = userDao.findByTel(phone);
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

    /**
     * 查询是否有此电话的用户
     * @param phone
     * @return
     */
    public UserInfo getUserInfoByTel(String phone){

        UserInfo userInfo = userDao.findByTel(phone);

        return userInfo;
    }

    /**
     * 发送手机验证码
     * @param phone
     */
    public String getPhoneCode(String phone,String code){
        String host = "http://dingxin.market.alicloudapi.com";
        String path = "/dx/sendSms";
        String method = "POST";
        String appcode = "59f2cc29ccd9454a87bf327bc1438a37";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", phone);
        querys.put("param", "code:"+code);
        querys.put("tpl_id", "TP1711063");
        Map<String, String> bodys = new HashMap<String, String>();
        String msg = "";
        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            msg = "sccess";
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
            msg = "error";
        }
        return msg;
    }
}
