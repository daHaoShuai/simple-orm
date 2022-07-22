package com.da;

import com.da.dao.UserMapper;
import com.da.orm.core.MapperProxyFactory;
import com.da.po.User;

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
//        执行查询语句
        userMapper.list().forEach(System.out::println);
        final User user = new User();
        user.setName("222");
        user.setPass("222");
//        执行插入语句
        System.out.println(userMapper.add(user));
//        更新id为2的数据
        user.setId(2);
//        执行更新语句
        System.out.println(userMapper.update(user));
//        执行删除语句(通过id来删除)
        System.out.println(userMapper.delete(12));
    }
}
