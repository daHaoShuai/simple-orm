package com.da;

import com.da.dao.UserMapper;
import com.da.orm.annotation.Select;
import com.da.orm.core.MapperProxyFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @Author Da
 * @Description: <br/>
 * 三十年生死两茫茫，写程序，到天亮。
 * 千行代码，Bug何处藏。
 * 纵使上线又怎样，朝令改，夕断肠。
 * 领导每天新想法，天天改，日日忙。
 * 相顾无言，惟有泪千行。
 * 每晚灯火阑珊处，夜难寐，又加班。
 * @Date: 2022-06-27
 * @Time: 11:09
 */
public class App {
    public static void main(String[] args) {
        for (Method method : UserMapper.class.getDeclaredMethods()) {
            final Parameter[] parameters = method.getParameters();
            for (Parameter parameter : parameters) {
                System.out.println(parameter.getName());
            }
        }
    }
}
