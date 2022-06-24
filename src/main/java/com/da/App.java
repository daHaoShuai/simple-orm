package com.da;

import com.da.dao.UserDao;
import com.da.po.User;

import java.util.List;

/**
 * @Author Da
 * @Description: <br/>
 * 三十年生死两茫茫，写程序，到天亮。
 * 千行代码，Bug何处藏。
 * 纵使上线又怎样，朝令改，夕断肠。
 * 领导每天新想法，天天改，日日忙。
 * 相顾无言，惟有泪千行。
 * 每晚灯火阑珊处，夜难寐，又加班。
 * @Date: 2022-06-22
 * @Time: 8:21
 */
public class App {

    public static void main(String[] args) {
        final UserDao userDao = new UserDao();
//        执行sql语句查询信息
        final List<User> list = userDao.query("select id,name,pass from user where pass='bb' and id=19");
        System.out.println(list);
//        执行增删改操作
//        System.out.println(userDao.exec("insert into user(name,pass) values('aa','bb')"));
//        userDao.closeConnection();
    }

}
