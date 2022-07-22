package com.da;

import com.da.dao.UserMapper;
import com.da.orm.annotation.Select;
import com.da.orm.core.MapperProxyFactory;

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
//        从mapper工厂获取到UserMapper的代理类
        final UserMapper userMapper = MapperProxyFactory.getMapper(UserMapper.class);
//        允许在before中执行一些操作,参数是方法和传入方法的参数
        MapperProxyFactory.before = (method, args1) -> {
            if (method.isAnnotationPresent(Select.class)) {
                System.out.println(method.getAnnotation(Select.class).value());
            }
        };
//        执行查询语句
        userMapper.list().forEach(System.out::println);
    }
}
