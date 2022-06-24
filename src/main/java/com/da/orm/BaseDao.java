package com.da.orm;

import com.da.orm.annotation.Col;
import com.da.orm.annotation.Table;
import com.da.orm.utils.StringUtil;
import com.da.orm.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
    //    对应的实体类类型
    private final Class<T> po;
    //    保存查询出来的实体类集合
    private List<T> pos;

    public BaseDao(Class<T> po) {
        this.po = po;
//        初始化时就查询一下数据库中所有的信息
        this.pos = list();
    }

    //    关闭连接
    public void closeConnection() {
        dbUtil.closeConnection();
        System.out.println("数据库连接关闭");
    }

    //    新增数据
    public boolean add(T t) {
        PreparedStatement statement = null;
        try {
            final StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO ")
                    .append(getTableName(t))
                    .append(" (")
                    .append(getTableField(t))
                    .append(") VALUES (")
                    .append(getTableFieldValue(t))
                    .append(")");
            System.out.println("sql = " + sql);
//            获取填充好值的 PreparedStatement
            statement = getStatement(connection.prepareStatement(sql.toString()), t);
            assert statement != null;
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    //    获取表中所有的数据
    public List<T> list() {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        pos = new ArrayList<>();
        try {
//            通过传入的实例获取信息
            final T po = this.po.newInstance();
            final StringBuilder sql = new StringBuilder();
            sql.append("SELECT ")
                    .append(getTableField(po))
                    .append(" FROM ")
                    .append(getTableName(po));
            System.out.println(sql);
            statement = connection.prepareStatement(sql.toString());
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
//                实例化一个要填充内容的对象
                final T t = this.po.newInstance();
                ResultSet finalResultSet = resultSet;
//                拿到一行的数据
                final List<Object> data = getAllFieldName(po).stream().map(name -> {
                    Object o = null;
                    try {
                        o = finalResultSet.getObject(name);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return o;
                }).collect(Collectors.toList());
//                获取类的所有属性
                final Field[] fields = t.getClass().getDeclaredFields();
//                填充属性
                for (int i = 0; i < fields.length; i++) {
                    String name = fields[i].getName();
                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
//                    获取对应的set方法
                    final Method method = t.getClass().getMethod("set" + name, fields[i].getType());
//                    拿到对应的值
                    Object o = data.get(i);
//                    处理时间类型
                    if (LocalDateTime.class.isAssignableFrom(o.getClass())) {
                        o = Date.from(((LocalDateTime) o).atZone(ZoneId.systemDefault()).toInstant());
                    }
                    method.invoke(t, o);
                }
                pos.add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return pos;
    }

    //    通过id获取实体类
    public <O> T getById(O id) {
//        获取一下所有的数据
        if (pos == null || pos.size() == 0) {
            throw new RuntimeException("没有从数据库中查到数据");
        }
//        获取主键属性
        Field primaryKey = getTablePrimaryKey();
//        过滤出主键对应的内容
        final List<T> po = pos.stream().filter(t -> {
            final Object o;
            try {
                final Field field = t.getClass().getDeclaredField(primaryKey.getName());
                field.setAccessible(true);
                o = field.get(t);
                field.setAccessible(false);
                return id == o;
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
            return false;
        }).collect(Collectors.toList());
        if (po.size() > 1) {
            throw new RuntimeException("获取到多个值,请仔细检查原因");
        }
        return po.get(0);
    }

    //    填充?的值
    private PreparedStatement getStatement(PreparedStatement statement, T t) {
//        获取所有的属性
        final List<Field> fields = getAllField(t);
//        遍历属性
        Utils.ListEach(fields, (field, index) -> {
            field.setAccessible(true);
            try {
                statement.setObject(index + 1, field.get(t));
            } catch (SQLException | IllegalAccessException e) {
                e.printStackTrace();
            } finally {
                field.setAccessible(false);
            }
        });
        return statement;
    }

    //    填充属性对应的?
    private String getTableFieldValue(T t) {
        final List<Field> fields = getAllField(t);
        final String[] data = new String[fields.size()];
        Arrays.fill(data, "?");
        return StringUtil.join(data, ",");
    }

    //    获取表的字段
    private String getTableField(T t) {
//        获取类上的所有属性名字并且用,隔开
        return StringUtil.join(getAllFieldName(t), ",");
    }

    //    获取类上的所有属性,并且转成List
    private List<Field> getAllField(T t) {
        return Arrays.asList(t.getClass().getDeclaredFields());
    }

    //    获取类上所有属性的名字,并且转成List
    private List<String> getAllFieldName(T t) {
//        优先使用注解上的值
        return getAllField(t).stream().map(field -> {
            if (field.isAnnotationPresent(Col.class)) {
                return field.getAnnotation(Col.class).name();
            }
            return field.getName();
        }).collect(Collectors.toList());
    }

    //    获取表的主键
    private Field getTablePrimaryKey() {
        try {
            final List<Field> primaryKeyList = getAllField(po.newInstance()).stream().filter(field -> {
                if (field.isAnnotationPresent(Col.class)) {
                    return field.getAnnotation(Col.class).primaryKey();
                }
                return false;
            }).collect(Collectors.toList());
            if (primaryKeyList.size() > 1) {
                throw new RuntimeException("主键数量不能大于1");
            }
            return primaryKeyList.get(0);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("没有找到主键,需要指定主键");
    }

    //    获取表名
    private String getTableName(T t) {
//        有注解优先用注解中的值
        if (t.getClass().isAnnotationPresent(Table.class)) {
            final Table table = t.getClass().getAnnotation(Table.class);
            final String prefix = table.prefix();
            final String tableName = table.tableName();
            final String suffix = table.suffix();
            return prefix + tableName + suffix;
        }
//        没有就是类名,驼峰转下划线格式
        return StringUtil.convertToUnderline(t.getClass().getSimpleName());
    }
}
