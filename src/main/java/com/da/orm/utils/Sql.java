package com.da.orm.utils;

/**
 * @Author Da
 * @Description: <br/>
 * 三十年生死两茫茫，写程序，到天亮。
 * 千行代码，Bug何处藏。
 * 纵使上线又怎样，朝令改，夕断肠。
 * 领导每天新想法，天天改，日日忙。
 * 相顾无言，惟有泪千行。
 * 每晚灯火阑珊处，夜难寐，又加班。
 * @Date: 2022-06-25
 * @Time: 0:45
 * 尝试用代码构建sql语句
 */
public class Sql {

    private String sql = "";

    public <T> Sql select(Class<T> t) {
        this.sql += "SELECT * FROM " + StringUtil.convertToUnderline(t.getSimpleName());
        return this;
    }

    public String build() {
        return this.sql;
    }

}
