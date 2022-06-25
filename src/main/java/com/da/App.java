package com.da;

import com.da.dao.UserDao;
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
 * @Date: 2022-06-22
 * @Time: 8:21
 */
public class App {

    public static void main(String[] args) {
        final UserDao userDao = new UserDao();
        final User user = new User();
//        user.setId(30);
        user.setName("aaaaa");
        user.setPass("aaaa1");
//        System.out.println(userDao.add(user));
//        userDao.list().forEach(System.out::println);
//        userDao.pages(1, 2).forEach(System.out::println);
//        System.out.println(userDao.getById(1));
        System.out.println(userDao.deleteById(33));
//        System.out.println(userDao.updateById(user));
        userDao.closeConnection();
    }

}
