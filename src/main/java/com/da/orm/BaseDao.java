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
    //    获取数据库连接
    private final DBUtil dbUtil = new DBUtil();
    public final Connection connection = dbUtil.getConnection();
    //    对应的实体类类型
    private final Class<T> po;

    public BaseDao(Class<T> po) {
        this.po = po;
        try {
//            关闭自动提交
            connection.setAutoCommit(false);
        } catch (SQLException e) {
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
            String sql = "INSERT INTO " + getTableName(t) + " (" + getTableField(t) + ") VALUES (" + getTableFieldValue(t) + ")";
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
            String sql = "SELECT " + getTableField(po) + " FROM " + getTableName(po);
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
                    .append(getTableField(t))
                    .append(" FROM ")
                    .append(getTableName(t))
                    .append(" LIMIT ")
                    .append(pageSize)
                    .append(" OFFSET ")
                    .append(pageSize * (current - 1));
            statement = connection.prepareStatement(sql.toString());
            resultSet = statement.executeQuery();
            return parseResultSet(resultSet, t);
        } catch (InstantiationException | IllegalAccessException | SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection(statement, resultSet);
        }
        throw new RuntimeException("没有找到当前分页的信息");
    }

    //    解析查询出来的结果
    private List<T> parseResultSet(ResultSet resultSet, T po) {
        final List<T> list = new ArrayList<>();
        try {
            while (resultSet.next()) {
//                 实例化一个要填充内容的对象
                final T t = this.po.newInstance();
//                 拿到一行的数据
                final List<Object> data = getAllFieldName(po).stream().map(name -> {
                    Object o = null;
                    try {
                        o = resultSet.getObject(name);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return o;
                }).collect(Collectors.toList());
//                 获取类的所有属性
                final Field[] fields = t.getClass().getDeclaredFields();
//                 填充属性
                for (int i = 0; i < fields.length; i++) {
                    String name = fields[i].getName();
                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
//                     获取对应的set方法
                    final Method method = t.getClass().getMethod("set" + name, fields[i].getType());
//                     拿到对应的值
                    Object o = data.get(i);
//                     处理时间类型
                    if (LocalDateTime.class.isAssignableFrom(o.getClass())) {
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

    //    通过主键获取实体类
    @Override
    public <O> T getById(O id) {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        final StringBuilder sql = new StringBuilder();
        try {
            sql.append("SELECT ")
                    .append(getTableField(po.newInstance()))
                    .append(" FROM ")
                    .append(getTableName(po.newInstance()))
                    .append(" WHERE ")
                    .append(getTablePrimaryKeyName())
                    .append("=")
                    .append(id);
            statement = connection.prepareStatement(sql.toString());
            resultSet = statement.executeQuery();
            final List<T> list = parseResultSet(resultSet, po.newInstance());
            if (list.size() > 1) throw new RuntimeException("当前主键对应的值不止1个");
            return list.get(0);
        } catch (InstantiationException | IllegalAccessException | SQLException e) {
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
                    .append(getTableName(po.newInstance()))
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
        } catch (InstantiationException | IllegalAccessException | SQLException e) {
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
            final Field primaryKey = getTablePrimaryKey();
            primaryKey.setAccessible(true);
            sql.append("UPDATE ")
                    .append(getTableName(t))
                    .append(" SET ")
                    .append(getUpdateParams(t))
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
        } catch (IllegalAccessException | SQLException e) {
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
        System.out.println(sql);
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            return parseResultSet(resultSet, po.newInstance());
        } catch (SQLException | InstantiationException | IllegalAccessException e) {
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
        } catch (SQLException e) {
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
    private String getUpdateParams(T t) {
        final List<String> fieldNames = getAllFieldName(t);
        final Optional<String> reduce = fieldNames.stream().reduce((o, n) -> o + "=?, " + n);
        final String params = reduce.orElse("");
        return "".equals(params) ? "" : params + "=?";
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

    //    获取表的主键名字
    private String getTablePrimaryKeyName() {
        final Field primaryKey = getTablePrimaryKey();
        if (primaryKey.isAnnotationPresent(Col.class)) {
            return primaryKey.getAnnotation(Col.class).name();
        } else {
            return StringUtil.convertToUnderline(primaryKey.getName());
        }
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
