package com.da.orm.utils;

import com.da.orm.annotation.Col;
import com.da.orm.annotation.Table;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

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
    //    表名
    private final String tableName;
    //    当前类对应的所有属性
    private final List<Field> allField;
    //    表中对应的所有字段的名字
    private final List<String> allTableFieldName;
    //    拼接好的sql语句
    private String sql = "";
    //    条件判断操作
    private final Map<String, BiConsumer<Object, Object>> condition = new HashMap<>();

    //    拼接条件语句
    {
        condition.put("eq", (k, v) -> condition(k, v, "="));
        condition.put("ne", (k, v) -> condition(k, v, "!="));
        condition.put("gt", (k, v) -> condition(k, v, ">"));
        condition.put("lt", (k, v) -> condition(k, v, "<"));
        condition.put("ge", (k, v) -> condition(k, v, ">="));
        condition.put("le", (k, v) -> condition(k, v, "<="));
    }

    public <T> Sql(Class<T> t) {
        this.tableName = getTableName(t);
        this.allField = Utils.getAllField(t);
        this.allTableFieldName = getAllTableFieldName(this.allField);
    }

    //    拼接查询语句
    public Sql select() {
        clearOldSql();
        this.sql = "SELECT " + StringUtil.join(allTableFieldName, ",") + " FROM " + tableName;
        return this;
    }

    public Sql select(String... args) {
        clearOldSql();
        this.sql = "SELECT " + StringUtil.join(args, ",") + " FROM " + tableName;
        return this;
    }

    //    拼接插入语句
    public Sql insert() {
        clearOldSql();
        this.sql = "INSERT INTO " + tableName + " (" + StringUtil.join(allTableFieldName, ",") + ") VALUES (" + getFillFieldsValues(allTableFieldName) + ")";
        return this;
    }

    public Sql insert(String... args) {
        clearOldSql();
        this.sql = "INSERT INTO " + tableName + " (" + StringUtil.join(args, ",") + ") VALUES (" + getFillFieldsValues(Arrays.asList(args)) + ")";
        return this;
    }

    //    拼接更新语句
    public Sql update() {
        clearOldSql();
        this.sql = "UPDATE " + tableName + " SET " + getUpdateParams();
        return this;
    }

    public Sql update(String... args) {
        clearOldSql();
        this.sql = "UPDATE " + tableName + " SET " + getUpdateParams(args);
        return this;
    }

    //    拼接删除语句
    public Sql delete() {
        clearOldSql();
        this.sql = "DELETE FROM " + tableName;
        return this;
    }

    //    拼接 where
    public Sql where() {
        this.sql += " WHERE ";
        return this;
    }

    //    拼接 and
    public Sql and() {
        this.sql += " AND ";
        return this;
    }

    //    相等
    public Sql eq(String key, Object value) {
        condition.get("eq").accept(key, value);
        return this;
    }

    //    不相等
    public Sql ne(String key, Object value) {
        condition.get("ne").accept(key, value);
        return this;
    }

    //    大于
    public Sql gt(String key, Object value) {
        condition.get("gt").accept(key, value);
        return this;
    }

    //    大于等于
    public Sql ge(String key, Object value) {
        condition.get("ge").accept(key, value);
        return this;
    }

    //    小于
    public Sql lt(String key, Object value) {
        condition.get("lt").accept(key, value);
        return this;
    }

    //    小于等于
    public Sql le(String key, Object value) {
        condition.get("le").accept(key, value);
        return this;
    }

    //    拼接判断条件
    public Sql condition(Object key, Object value, String condition) {
        this.sql += key + condition + value;
        return this;
    }

    //    拼接更新的参数
    private String getUpdateParams() {
        final Optional<String> reduce = this.allTableFieldName.stream().reduce((o, n) -> o + "=?, " + n);
        final String params = reduce.orElse("");
        return "".equals(params) ? "" : params + "=?";
    }

    private String getUpdateParams(String[] args) {
        StringBuilder res = new StringBuilder();
        for (String s : args) {
            res.append(s).append("=").append("?,").append(" ");
        }
        return res.substring(0, res.lastIndexOf(","));
    }

    //    填充对应的?
    private String getFillFieldsValues(List<String> fieldNames) {
        final String[] values = new String[fieldNames.size()];
        Arrays.fill(values, "?");
        return StringUtil.join(values, ",");
    }

    //    通过属性list获取得到对应的表的字段list
    private List<String> getAllTableFieldName(List<Field> allField) {
        return allField.stream().map(field -> {
//            优先使用注解的值
            if (field.isAnnotationPresent(Col.class)) {
                return field.getAnnotation(Col.class).name();
            }
            return StringUtil.convertToUnderline(field.getName());
        }).collect(Collectors.toList());
    }

    //    获取表名
    private <T> String getTableName(Class<T> t) {
        if (t.isAnnotationPresent(Table.class)) {
            final Table table = t.getAnnotation(Table.class);
            final String prefix = table.prefix();
            final String name = table.tableName();
            final String suffix = table.suffix();
            return prefix + name + suffix;
        }
        return StringUtil.convertToUnderline(t.getSimpleName());
    }

    //    清除原来拼接的sql
    private void clearOldSql() {
        this.sql = "";
    }

    //    获取拼接好的sql语句
    public String build() {
        return this.sql;
    }

}
