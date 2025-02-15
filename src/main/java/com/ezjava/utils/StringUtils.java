package com.ezjava.utils;

public class StringUtils {
    public static String upperCaseFirstLetter(String field){
        if(org.apache.commons.lang3.StringUtils.isEmpty(field)){
            //判断field是否为空，如果为空，则直接返回field
            return field;
        }

        return field.substring(0,1).toUpperCase() + field.substring(1);
    }

    public static String lowerCaseFirstLetter(String field){
        if(org.apache.commons.lang3.StringUtils.isEmpty(field)){
            return field;
        }

        return field.substring(0,1).toLowerCase() + field.substring(1);
    }
}
