package com.ljb.filter;


import com.alibaba.fastjson.JSONObject;
import com.ljb.jwt.JWTUtils;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * @author 刘进波
 * @create 2019-08-05 15:27
 */
@Component
public class MyGlobalFilter implements GlobalFilter {

    //设置不过滤的路径
    @Value("${my.auth.urls}")
    private String[] urls;

    @Value("${my.auth.loginPath}")
    private String loginpage;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求
        ServerHttpRequest request = exchange.getRequest();
        //获取响应
        ServerHttpResponse response = exchange.getResponse();
        //获取当前的请求路径
        String currentpath = request.getURI().toString();

        //截取请求接口
        String substring = currentpath.substring(currentpath.lastIndexOf("/")+1);

        System.out.println(substring+"===请求接口");

        //验证当前路径是否是公共资源路径  是否不需要通过登录校验
        List<String> strings = Arrays.asList(urls);
        if(strings.contains(currentpath)){
            return chain.filter(exchange);
        }else{
            //获取请求头中的token
            List<String> listToken = request.getHeaders().get("token");
            //解密Token校验是否超时，如果超时的话需要重新登录
            JSONObject jsonObject=null;
            try {
                //解密判断Token是否已经失效
                jsonObject = JWTUtils.decodeJwtTocken(listToken.get(0));
                //如果不报错说明没有失效，重新加密登录信息
                String token = JWTUtils.generateToken(jsonObject.toJSONString());
                //存储到响应头中
                response.getHeaders().set("token",token);
            }catch (JwtException e){
                e.printStackTrace();
                //表示超时需要重新登录
                //或者是错误的Token信息
                //跳转到登录页面
                response.getHeaders().set("Location",loginpage);
                response.setStatusCode(HttpStatus.SEE_OTHER);
                return exchange.getResponse().setComplete();
            }
            //获取用户ID
            String userId = jsonObject.get("id").toString();
            //校验用户有没有范文该资源的权限
            Boolean isok = redisTemplate.opsForHash().hasKey("USERDATAAUTH" + userId, substring);
            //isok=true说明有访问资源的权限
            if(isok){
                //验证当前路径不是需要进行登录校验的路径
                return chain.filter(exchange);
            }else{
                throw  new RuntimeException("不能访问该资源");
            }
        }

    }


}
