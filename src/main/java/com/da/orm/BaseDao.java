package com.da.orm;

import com.da.orm.annotation.Col;
import com.da.orm.annotation.Table;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
 * @Date: 2022-06-23
 * @Time: 11:54
 * 基础的增删改查
 */
public class BaseDao<T> {
    //    获取数据库连接
    private final DBUtil dbUtil = new DBUtil();
    public final Connection connection = dbUtil.getConnection();
    //    增删改查标志
    private final int ADD = 0;

    //    关闭连接
    public void closeConnection() {
        dbUtil.closeConnection();
        System.out.println("数据库连接关闭");
    }

    //    添加操作
    public boolean add(T t) {
        final String sql = buildSql(t, ADD);
        System.out.println(sql);
        try {
            final PreparedStatement statement = getStatement(t, connection.prepareStatement(sql), ADD);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    //    获取填充好对应的属性值的PreparedStatement
    private PreparedStatement getStatement(T t, PreparedStatement statement, int type) {
        final List<Field> fieldList = getAllField(t);
        try {
            switch (type) {
                case ADD: {
                    for (int i = 0; i < fieldList.size(); i++) {
                        final Field field = fieldList.get(i);
                        field.setAccessible(true);
//                        填充更新时间
                        if (field.getName().equals("updateTime")) {
                            statement.setObject(i + 1, new Timestamp(System.currentTimeMillis()));
                        }
//                        填充其他字段
                        else {
                            statement.setObject(i + 1, field.get(t));
                        }
                        field.setAccessible(false);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statement;
    }

    //    生成sql语句
    private String buildSql(T t, int type) {
        String sql = "";
        switch (type) {
            case ADD: {
                sql = "insert into `" + getTableName(t) + "` (" + StringUtil.join(getTableFields(t), ",") + ") values (" + getTableFieldValue(t) + ")";
                break;
            }
        }
        return sql;
    }

    //    对应的占位符
    private String getTableFieldValue(T t) {
        final List<String> tableFields = getTableFields(t);
        final String[] values = new String[tableFields.size()];
        Arrays.fill(values, "?");
        return StringUtil.join(values, ",");
    }

    //    获取实体类的属性,对应表中的字段
    private List<String> getTableFields(T t) {
        List<Field> fieldList = getAllField(t);
        return fieldList.stream().map(field -> {
            if (field.isAnnotationPresent(Col.class)) {
                final Col col = field.getAnnotation(Col.class);
//                优先用注解中的属性名字
                return col.name();
            }
            return StringUtil.convertToUnderline(field.getName());
        }).collect(Collectors.toList());
    }

    //    获取实体类上的所有属性
    private List<Field> getAllField(T t) {
        //        获取当前类自身上的属性
        final Field[] fields = t.getClass().getDeclaredFields();
        final List<Field> fieldList = new ArrayList<>(Arrays.asList(fields));
//        判断是不是继承了基类,添加基类上的属性
        if (BaseEntity.class.isAssignableFrom(t.getClass())) {
            fieldList.addAll(Arrays.asList(t.getClass().getSuperclass().getDeclaredFields()));
        }
        return fieldList;
    }

    //    获取实体类对应的表名
    private String getTableName(T t) {
//        注解中的表名优先
        if (t.getClass().isAnnotationPresent(Table.class)) {
            final Table table = t.getClass().getAnnotation(Table.class);
            final String prefix = table.prefix();
            final String tableName = table.tableName();
            final String suffix = table.suffix();
            return prefix + tableName + suffix;
        }
        return StringUtil.convertToUnderline(t.getClass().getSimpleName());
    }

}
