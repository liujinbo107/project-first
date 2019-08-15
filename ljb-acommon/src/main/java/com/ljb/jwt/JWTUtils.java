package com.ljb.jwt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class JWTUtils {

    //生成JWT 加密信息
    public static String generateToken(String userinfo) {
        Map<String, Object> map = new HashMap<String,Object> ();
        map.put("userinfo", userinfo);
        map.put("created", new Date ());

        return Jwts.builder().setClaims(map)//payload 设置信息
                .setExpiration(new Date(System.currentTimeMillis() + 2*24*7*30*60*1000L))  //过期时间
                           //加密算法名称              //签名的键
                .signWith(SignatureAlgorithm.HS512,"secretkey").compact();  //加密方式
    }

    /**
     * 解密JWT加密信息
     * 超过设置的JWT的时间长度的话解密会报异常
     * ExpiredJwtException
     * @param token
     * @return 返回的结果是一个JSONObject对象,这里的都是用户信息
     */
    public static JSONObject decodeJwtTocken(String token){
        Claims claims = Jwts.parser().setSigningKey("secretkey").parseClaimsJws(token).getBody();
        JSONObject userinfo = JSON.parseObject ( claims.get ( "userinfo" ).toString () );
        return userinfo;
    }


}
