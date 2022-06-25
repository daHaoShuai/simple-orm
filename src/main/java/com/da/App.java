package com.da;

import com.da.orm.utils.Sql;
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
        final Sql sql = new Sql(User.class);

        final String s = sql.select().build();
        System.out.println(s);

        final String s1 = sql.select("name", "pass").build();
        System.out.println(s1);

        final String s2 = sql.insert().build();
        System.out.println(s2);

        final String s3 = sql.insert("name", "pass").build();
        System.out.println(s3);

        final String s4 = sql.update()
                .where()
                .eq("name", "aa")
                .and()
                .ne("a1", "a")
                .and()
                .gt("a3", 1)
                .and()
                .lt("a4", 3)
                .build();
        System.out.println(s4);

        final String s5 = sql.update("name", "pass")
                .where()
                .eq("pass", "bb")
                .and()
                .eq("name", "aa")
                .and()
                .le("a", 10)
                .build();
        System.out.println(s5);

        final String s6 = sql.delete()
                .where()
                .eq("name", "aa")
                .build();
        System.out.println(s6);

    }

}
