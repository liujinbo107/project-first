package com.sso.web;

import com.alibaba.fastjson.JSON;
import com.ljb.exception.LoginException;
import com.ljb.jwt.JWTUtils;
import com.ljb.pojo.ResponseResult;
import com.ljb.pojo.entity.UserInfo;
import com.ljb.randm.VerifyCodeUtils;
import com.ljb.utils.MD5;
import com.ljb.utils.UID;
import com.sso.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author 刘进波
 * @create 2019-08-05 11:59
 */
@Controller
@Api(tags = "这是sso登录的服务接口")
public class AuthController {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private UserService userService;

    /**
     * 获取滑动验证的验证码
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("getCode")
    @ResponseBody
    @ApiOperation("获取验证码")
    public ResponseResult getCode(HttpServletRequest request, HttpServletResponse response){

        Cookie[] cookies = request.getCookies();

        //生成一个长度是5的随机字符串
        String code = VerifyCodeUtils.generateVerifyCode(5);
        System.out.println(code);
        ResponseResult responseResult = ResponseResult.getResponseResult();
        responseResult.setResult(code);
        String uidCode = "CODE"+ UID.getUUID16();
        //将生成的随机字符串标识后存入redis
        redisTemplate.opsForValue().set(uidCode,code);
        //设置过期时间
        redisTemplate.expire(uidCode,1, TimeUnit.MINUTES);
        //回写cookie
        Cookie cookie = new Cookie("authcode", uidCode);
        cookie.setPath("/");
        cookie.setDomain("localhost");
        response.addCookie(cookie);

        return responseResult;
    }


    @RequestMapping("getPhoneCode")
    @ResponseBody
    @ApiOperation("手机获取验证码")
    public ResponseResult getPhoneCode(@RequestBody Map<String,String> map){

        String phone = map.get("tel");

        //生成一个长度是5的随机字符串
        String code = VerifyCodeUtils.generateVerifyCode(5);

        String phoneCode = userService.getPhoneCode(phone, code);

        ResponseResult responseResult = ResponseResult.getResponseResult();

        if(phoneCode.equals("error")){
            responseResult.setCode(500);
            return responseResult;
        }
        String md5 = phone + code;
        //加密手机号和验证码
        //String ljb = MD5.encryptPassword(md5.toString(), "ljb");
        //设置过期时间
        redisTemplate.opsForValue().set(md5,md5,180,TimeUnit.SECONDS);

        responseResult.setCode(200);
        return responseResult;
    }

    @RequestMapping("phoneLogin")
    @ResponseBody
    @ApiOperation("手机登陆")
    public ResponseResult phoneLogin(@RequestBody Map<String,String> map){
        //获取手机号  验证码
        String phone = map.get("tel");
        String code = map.get("telCode");
        String phcode = phone+code;
        //加密判断
        //String ljb = MD5.encryptPassword(phcode.toString(), "ljb");
        ResponseResult responseResult = ResponseResult.getResponseResult();
        //判断是否过期
        if(redisTemplate.hasKey(phcode)){
            //判断验证码是否正确
            String s = redisTemplate.opsForValue().get(phcode);
            if(s.equals(phcode)){

                //根据手机号查询用户
                UserInfo userInfoByTel = userService.getUserInfoByTel(phone);
                //判断是否用户已注册
                if(userInfoByTel!=null){
                    UserInfo user = userService.getUserByLogin(userInfoByTel.getLoginName());
                    //将用户信息转存为JSON串
                    String userinfo = JSON.toJSONString(user);

                    //将用户信息使用JWT进行加密，将加密信息作为票据
                    String token = JWTUtils.generateToken(userinfo);

                    //将加密信息存入statuInfo
                    responseResult.setToken(token);

                    //将生成的token存储到redis库
                    redisTemplate.opsForValue().set("USERINFO"+user.getId().toString(),token);
                    //将生成的数据访问权限信息存入缓存中
                    if( redisTemplate.hasKey("USERDATAAUTH"+user.getId().toString())){
                        redisTemplate.delete("USERDATAAUTH"+user.getId().toString());
                    }
                    redisTemplate.opsForHash().putAll("USERDATAAUTH"+user.getId().toString(),user.getAuthmap());

                    System.out.println(user.getAuthmap()+"====权限集合");

                    //设置token过期 30分钟
                    redisTemplate.expire("USERINFO"+user.getId().toString(),600, TimeUnit.SECONDS);

                    //增加用户登录次数
                    Date date = new Date();
                    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
                    String fmt = sdf1.format(date);
                    if(redisTemplate.hasKey(fmt)){
                        redisTemplate.opsForValue().increment(fmt,1);
                    }else{
                        redisTemplate.opsForValue().set(fmt,"1",7,TimeUnit.DAYS);
                    }

                    //设置返回值
                    responseResult.setResult(user);

                    System.out.println(user);

                    responseResult.setCode(200);
                    //设置成功的信息
                    responseResult.setSuccess("登陆成功");
                }else{
                    responseResult.setCode(510);
                }
            }else{
                responseResult.setCode(505);
                responseResult.setError("验证码错误");
            }
        }else {
            responseResult.setCode(500);
            responseResult.setError("验证码已过期");
        }

        return responseResult;

    }


    @ResponseBody
    @RequestMapping("login")
    @ApiOperation("用户登录")
    public ResponseResult toLogin(@RequestBody Map<String,Object> map) throws LoginException {
        ResponseResult responseResult = ResponseResult.getResponseResult();
        //获取生成的验证码
        String code = redisTemplate.opsForValue().get(map.get("codekey").toString());
        //获取传入的验证码是否是生成后存在redis中的验证码
        if(code==null||!code.equals(map.get("code").toString())){
            responseResult.setCode(500);
            responseResult.setError("验证码错误，请重新刷新页面登录");
            return responseResult;
        }
        //进行用户密码的校验
        if(map!=null&&map.get("loginname")!=null){
            //根据用户名获取用户信息
            UserInfo user = userService.getUserByLogin(map.get("loginname").toString());
            if(user!=null){
                //对比密码
                String password = MD5.encryptPassword(map.get("password").toString(), "lcg");
                if(user.getPassword().equals(password)){

                    //将用户信息转存为JSON串
                    String userinfo = JSON.toJSONString(user);

                    //将用户信息使用JWT进行加密，将加密信息作为票据
                    String token = JWTUtils.generateToken(userinfo);

                    //将加密信息存入statuInfo
                    responseResult.setToken(token);

                    //将生成的token存储到redis库
                    redisTemplate.opsForValue().set("USERINFO"+user.getId().toString(),token);
                    //将生成的数据访问权限信息存入缓存中
                    if( redisTemplate.hasKey("USERDATAAUTH"+user.getId().toString())){
                        redisTemplate.delete("USERDATAAUTH"+user.getId().toString());
                    }
                    redisTemplate.opsForHash().putAll("USERDATAAUTH"+user.getId().toString(),user.getAuthmap());

                    System.out.println(user.getAuthmap()+"====权限集合");

                    //设置token过期 30分钟
                    redisTemplate.expire("USERINFO"+user.getId().toString(),600, TimeUnit.SECONDS);
                    //添加用户访问量
                    Date date = new Date();
                    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
                    String fmt = sdf1.format(date);
                    if(redisTemplate.hasKey(fmt)){
                        redisTemplate.opsForValue().increment(fmt,1);
                    }else{
                        redisTemplate.opsForValue().set(fmt,"1",7,TimeUnit.DAYS);
                    }
                    //设置返回值
                    responseResult.setResult(user);

                    System.out.println(user);

                    responseResult.setCode(200);
                    //设置成功的信息
                    responseResult.setSuccess("登陆成功");

                    return responseResult;
                }else{
                    throw new LoginException("用户名或密码错误");
                }
            }else{
                throw new LoginException("用户名或密码错误");
            }
        }else{
            throw new LoginException("用户名或密码错误");
        }

    }

    @ResponseBody
    @RequestMapping("loginout")
    @ApiOperation("用户退出")
    public ResponseResult loginout(@RequestBody Map<String,String> map){
        //获取用户id
        String id = map.get("id");

        ResponseResult responseResult = ResponseResult.getResponseResult();
        try {
            //删除redis数据库中的用户信息token
            redisTemplate.delete("USERINFO"+id);
            //删除redis数据库中的用户访问权限
            redisTemplate.delete("USERDATAAUTH"+id);

            responseResult.setCode(200);
        }catch (Exception e){
            responseResult.setCode(500);
        }
        return responseResult;
    }

}
