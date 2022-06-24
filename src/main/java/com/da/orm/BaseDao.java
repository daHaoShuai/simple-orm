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
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
public class BaseDao<T> implements BaseCrud<T> {
    //    实例化数据库连接工具类
    private final DBUtil dbUtil;
    //    获取数据库连接
    public final Connection connection;
    //    对应的实体类类型
    private final Class<T> po;
    //    当前实体类上所有的属性
    private final List<Field> allField;
    //    对应表的字段名字
    private final List<String> allFieldName;
    //    实体类对应的表名
    private final String tableName;
    //    主键属性
    private final Field primaryKey;

    //    初始化信息
    public BaseDao(Class<T> po) {
        this.dbUtil = new DBUtil();
        connection = dbUtil.getConnection();
        this.po = po;
        allField = Arrays.asList(po.getDeclaredFields());
        allFieldName = getAllFieldName();
        tableName = getTableName(po);
        primaryKey = getTablePrimaryKey();
        try {
//            关闭自动提交
            connection.setAutoCommit(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //    关闭连接
    @Override
    public void closeConnection() {
        dbUtil.closeConnection();
        System.out.println("数据库连接关闭");
    }

    //    新增数据
    @Override
    public boolean add(T t) {
        PreparedStatement statement = null;
        try {
//            获取填充好值的 PreparedStatement
            String sql = "INSERT INTO " + tableName + " (" + getTableField() + ") VALUES (" + getTableFieldValue() + ")";
            statement = getStatement(connection.prepareStatement(sql), t);
            assert statement != null;
            final int i = statement.executeUpdate();
            if (i > 0) {
//                提交事务
                connection.commit();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
//                错误就回滚事务
                connection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            closeConnection(statement, null);
        }
        return false;
    }

    //    获取表中所有的数据
    @Override
    public List<T> list() {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
//            通过传入的实例获取信息
            final T po = this.po.newInstance();
            String sql = "SELECT " + getTableField() + " FROM " + tableName;
            System.out.println(sql);
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
//            返回解析好的类型对象
            return parseResultSet(resultSet, po);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            关闭连接
            closeConnection(statement, resultSet);
        }
        throw new RuntimeException("没有查询到对应的信息");
    }

    //    分页查询
    @Override
    public List<T> pages(int current, int pageSize) {
        final StringBuilder sql = new StringBuilder();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            final T t = po.newInstance();
            sql.append("SELECT ")
                    .append(getTableField())
                    .append(" FROM ")
                    .append(tableName)
                    .append(" LIMIT ")
                    .append(pageSize)
                    .append(" OFFSET ")
                    .append(pageSize * (current - 1));
            statement = connection.prepareStatement(sql.toString());
            resultSet = statement.executeQuery();
            return parseResultSet(resultSet, t);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection(statement, resultSet);
        }
        throw new RuntimeException("没有找到当前分页的信息");
    }

    //    解析查询出来的结果(全部的字段)
    private List<T> parseResultSet(ResultSet resultSet, T po) {
        final List<T> list = new ArrayList<>();
        try {
            while (resultSet.next()) {
//                 实例化一个要填充内容的对象
                final T t = this.po.newInstance();
//                 拿到一行的数据
                final List<Object> data = allFieldName.stream().map(name -> {
                    Object o = null;
                    try {
                        o = resultSet.getObject(name);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return o;
                }).collect(Collectors.toList());
//                 填充属性
                for (int i = 0; i < allField.size(); i++) {
                    String name = allField.get(i).getName();
                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
//                     获取对应的set方法
                    final Method method = t.getClass().getMethod("set" + name, allField.get(i).getType());
//                     拿到对应的值
                    Object o = data.get(i);
//                     处理时间类型(目前是有问题的)
                    if (null != o && LocalDateTime.class.isAssignableFrom(o.getClass())) {
                        o = Date.from(((LocalDateTime) o).atZone(ZoneId.systemDefault()).toInstant());
                    }
//                    使用set方法注入值
                    method.invoke(t, o);
                }
                list.add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    //    解析查询出来的结果(指定的字段)
    private List<T> parseResultSet(ResultSet resultSet, Class<T> po, String[] data) {
        final List<T> list = new ArrayList<>();
        try {
            while (resultSet.next()) {
                final T t = po.newInstance();
                for (String s : data) {
//                    下划线转驼峰
                    final String name = StringUtil.convertToLineHump(s);
                    final Field field = po.getDeclaredField(name.substring(0, 1).toLowerCase() + name.substring(1));
                    final Method method = t.getClass().getDeclaredMethod("set" + name, field.getType());
                    method.invoke(t, resultSet.getObject(s));
                }
                list.add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    //    通过主键获取实体类
    @Override
    public <O> T getById(O id) {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        final StringBuilder sql = new StringBuilder();
        try {
            sql.append("SELECT ")
                    .append(getTableField())
                    .append(" FROM ")
                    .append(tableName)
                    .append(" WHERE ")
                    .append(getTablePrimaryKeyName())
                    .append("=")
                    .append(id);
            statement = connection.prepareStatement(sql.toString());
            resultSet = statement.executeQuery();
            final List<T> list = parseResultSet(resultSet, po.newInstance());
            if (list.size() > 1) throw new RuntimeException("当前主键对应的值不止1个");
            return list.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            关闭连接
            closeConnection(statement, resultSet);
        }
        throw new RuntimeException("没有查到对应的信息");
    }

    //    通过主键删除
    @Override
    public <O> boolean deleteById(O id) {
        final StringBuilder sql = new StringBuilder();
        PreparedStatement statement = null;
        try {
            sql.append("DELETE FROM ")
                    .append(tableName)
                    .append(" WHERE ")
                    .append(getTablePrimaryKeyName())
                    .append("=")
                    .append(id);
            statement = connection.prepareStatement(sql.toString());
            final int i = statement.executeUpdate();
            if (i > 0) {
                connection.commit();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
//                回滚事务
                connection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            closeConnection(statement, null);
        }
        return false;
    }

    //    通过实体类的主键更新
    @Override
    public boolean updateById(T t) {
        final StringBuilder sql = new StringBuilder();
        PreparedStatement statement = null;
        try {
            primaryKey.setAccessible(true);
            sql.append("UPDATE ")
                    .append(tableName)
                    .append(" SET ")
                    .append(getUpdateParams())
                    .append(" WHERE ")
                    .append(getTablePrimaryKeyName())
                    .append("=")
                    .append(primaryKey.get(t));
            primaryKey.setAccessible(false);
            statement = getStatement(connection.prepareStatement(sql.toString()), t);
            final int i = statement.executeUpdate();
            if (i > 0) {
                connection.commit();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            closeConnection(statement, null);
        }
        return false;
    }

    //    通过自定义的sql语句查询
    @Override
    public List<T> query(String sql) {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            if (sql.contains("select")) {
                sql = sql.replace("select", "SELECT");
            }
            if (sql.contains("from")) {
                sql = sql.replace("from", "FROM");
            }
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
//            获取参数
            final String params = sql.substring(sql.indexOf("SELECT") + 6, sql.indexOf("FROM")).trim();
//            *是查全部,所有直接解析就行
            if (params.equals("*")) {
                return parseResultSet(resultSet, po.newInstance());
            } else {
//                根据传入的参数解析
                return parseResultSet(resultSet, po, params.split(","));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection(statement, resultSet);
        }
        throw new RuntimeException("执行sql语句出错");
    }

    //    执行增删改操作
    @Override
    public boolean exec(String sql) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            if (statement.executeUpdate() > 0) {
                connection.commit();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            closeConnection(statement, null);
        }
        return false;
    }

    //    拼接更新的参数
    private String getUpdateParams() {
        final Optional<String> reduce = allFieldName.stream().reduce((o, n) -> o + "=?, " + n);
        final String params = reduce.orElse("");
        return "".equals(params) ? "" : params + "=?";
    }

    //    填充?的值
    private PreparedStatement getStatement(PreparedStatement statement, T t) {
//        遍历当前类的属性
        Utils.ListEach(allField, (field, index) -> {
            field.setAccessible(true);
            try {
//                从坐标为1的地方开始填充数据
                statement.setObject(index + 1, field.get(t));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                field.setAccessible(false);
            }
        });
        return statement;
    }

    //    填充属性对应的?
    private String getTableFieldValue() {
        final String[] data = new String[allField.size()];
        Arrays.fill(data, "?");
        return StringUtil.join(data, ",");
    }

    //    获取表的字段
    private String getTableField() {
//        获取类上的所有属性名字并且用,隔开
        return StringUtil.join(allFieldName, ",");
    }

    //    获取类上所有属性的名字,并且转成List
    private List<String> getAllFieldName() {
//        优先使用注解上的值
        return allField.stream().map(field -> {
            if (field.isAnnotationPresent(Col.class)) {
                return field.getAnnotation(Col.class).name();
            }
            return field.getName();
        }).collect(Collectors.toList());
    }

    //    获取表的主键
    private Field getTablePrimaryKey() {
        try {
            final List<Field> primaryKeyList = allField.stream().filter(field -> {
                if (field.isAnnotationPresent(Col.class)) {
                    return field.getAnnotation(Col.class).primaryKey();
                }
                return false;
            }).collect(Collectors.toList());
            if (primaryKeyList.size() > 1) {
                throw new RuntimeException("主键数量不能大于1");
            }
            return primaryKeyList.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("没有找到主键,需要指定主键");
    }

    //    获取表的主键名字
    private String getTablePrimaryKeyName() {
//        优先使用注解里面的名字
        if (primaryKey.isAnnotationPresent(Col.class)) {
            return primaryKey.getAnnotation(Col.class).name();
        } else {
            return StringUtil.convertToUnderline(primaryKey.getName());
        }
    }

    //    获取表名
    private String getTableName(Class<T> t) {
//        有注解优先用注解中的值
        if (t.isAnnotationPresent(Table.class)) {
            final Table table = t.getAnnotation(Table.class);
            final String prefix = table.prefix();
            final String tableName = table.tableName();
            final String suffix = table.suffix();
            return prefix + tableName + suffix;
        }
//        没有就是类名,驼峰转下划线格式
        return StringUtil.convertToUnderline(t.getSimpleName());
    }

    //    关闭连接
    private void closeConnection(Statement statement, ResultSet resultSet) {
        try {
            if (statement != null) {
                statement.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
