package com.da;


import com.da.dao.UserDao;
import com.da.orm.BaseEntity;
import com.da.orm.DBUtil;
import com.da.orm.StringUtil;
import com.da.po.User;

import java.lang.reflect.Field;
import java.util.Date;

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
        user.setName("admin2");
        user.setPass("admin2");
        user.setTime(new Date(System.currentTimeMillis()));
        final UserDao userDao = new UserDao();
        final boolean add = userDao.add(user);
        System.out.println(add);
        userDao.closeConnection();
    }
}
