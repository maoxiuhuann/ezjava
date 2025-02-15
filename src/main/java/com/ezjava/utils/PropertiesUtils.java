package com.ezjava.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PropertiesUtils 工具类，用于读取 application.properties 文件中的配置信息。
 */
public class PropertiesUtils {
    private static Properties props = new Properties();
    private static Map<String, String> PROPER_MAP = new ConcurrentHashMap();

    static{
        InputStream is = null;
        try{
            is = PropertiesUtils.class.getClassLoader().getResourceAsStream("application.properties");//加载 application.properties 文件。
            props.load(new InputStreamReader(is,"utf-8"));// 以 UTF-8 编码读取文件内容。

            Iterator<Object> iterator = props.keySet().iterator();//遍历 Properties 对象中的所有键，并将键值对存入 PROPER_MAP 中。
            while(iterator.hasNext()){
                //props.keySet() 返回 Properties 对象中所有键的集合 (Set)。
                //iterator() 返回该集合的 Iterator 对象，用于遍历键集合。
                //hasNext() 方法检查是否还有下一个元素可供遍历。如果有，返回 true。
                //props.getProperty(key) 根据键获取对应的值
                String key = (String) iterator.next();
                PROPER_MAP.put(key,props.getProperty(key));
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(is!= null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getString(String key){
        return PROPER_MAP.get(key);
    }
}
