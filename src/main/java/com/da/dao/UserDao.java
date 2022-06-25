package com.da.dao;

import com.da.orm.BaseDao;
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
 * @Date: 2022-06-23
 * @Time: 15:54
 */
public class UserDao extends BaseDao<User> {
    public UserDao() {
        super(User.class);
    }

    //    (自己扩展方法)通过name获取
    public User getUserByName(String name) {
//        构建查询sql
        final String sql = this.getSqlBuild().select().where().eq("name", "'" + name + "'").build();
//        使用query方法解析查询语句
        final List<User> users = this.query(sql);
        if (users.size() > 1) {
            throw new RuntimeException("查出的用户name为" + name + "的不止一个");
        }
        return users.get(0);
    }
}
