package com.da;


import com.da.dao.UserDao;
import com.da.po.User;

import java.util.Date;
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
        final User user = new User();
        user.setId(11);
        user.setName("admin15");
        user.setPass("admin15");
        user.setTime(new Date(System.currentTimeMillis()));
        final UserDao userDao = new UserDao();
//        新增
//        System.out.println(userDao.add(user));
//        获取表中所有的数据
        final List<User> users = userDao.list();
        users.forEach(System.out::println);
//        分页查询(当前页,每页的条数)
//        final List<User> pages = userDao.pages(1, 2);
//        pages.forEach(System.out::println);
//        通过主键获取
//        final User id = userDao.getById(11);
//        通过主键删除
//        System.out.println(userDao.deleteById(15));
//        通过主键更新
//        System.out.println(userDao.updateById(user));
//        关闭连接
        userDao.closeConnection();
    }
}
