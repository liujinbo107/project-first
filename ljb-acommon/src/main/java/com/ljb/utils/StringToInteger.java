package com.ljb.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 刘进波
 * @create 2019-08-08 21:14
 */
public class StringToInteger {

    public static List<Long> toInteger(String[] split){
        List<Long> list = new ArrayList<>();

        for (String s : split) {
            list.add(Long.parseLong(s));
        }

        return list;
    }
}
