package com.ljb.exception;

/**
 * 作者: LCG
 * 日期: 2019/8/3 16:31
 * 描述: 自定义的登录异常类
 */
public class LoginException extends Exception {

    public LoginException(String message){
        super(message);
    }

}
