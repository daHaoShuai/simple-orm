package com.da.orm.core;

import com.da.orm.annotation.Delete;
import com.da.orm.annotation.Insert;
import com.da.orm.annotation.Select;
import com.da.orm.annotation.Update;
import com.da.orm.utils.DBUtil;
import com.da.orm.utils.StringUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author Da
 * @Description:
 * @Date: 2022-07-22
 * @Time: 10:27
 * mapper工厂
 */
public class MapperProxyFactory {

    private static final DBUtil dbUtil = DBUtil.getInstance();
    private static final Connection connection = dbUtil.getConnection();

    //    返回代理后的mapper
    @SuppressWarnings("unchecked")//忽略强转类型的警告
    public static <T> T getMapper(Class<T> mapper) {
//        创建代理对象
        final Object proxyInstance = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                new Class[]{mapper}, (proxy, method, args) -> {
//             处理 @Select 或者 @Insert 或者 @Update 或者 @Delete 注解的方法 获取要执行的sql语句
                    final String sql = parseSql(method);
                    if (method.isAnnotationPresent(Select.class)) {
                        return handlerSelect(sql, method, args);
                    } else if (method.isAnnotationPresent(Insert.class)) {
                        return handlerInsert(sql, method, args);
                    } else if (method.isAnnotationPresent(Update.class)) {
                        return handlerUpdate(sql, method, args);
                    } else if (method.isAnnotationPresent(Delete.class)) {
                        return handlerDelete(sql, method, args);
                    } else {
                        return null;
                    }
                });
        return (T) proxyInstance;
    }

    //    处理@Delete注解的方法
    private static Object handlerDelete(String sql, Method method, Object[] args) {
        return null;
    }

    //    处理@Update注解的方法
    private static Object handlerUpdate(String sql, Method method, Object[] args) {
        return null;
    }

    //    处理@Insert注解的方法
    @SuppressWarnings("unchecked")//忽略强转类型的警告
    private static <T> Object handlerInsert(String sql, Method method, Object[] args) throws SQLException {
        PreparedStatement statement = null;
        try {
//        获取方法的输入类型
            final Type[] types = method.getGenericParameterTypes();
//        如果输入的参数只有一个并且类型是实体类类型
            if (types.length == 1 && types[0] instanceof Class) {
//                获取当前实体类的类型
                Class<T> clz = (Class<T>) types[0];
//                获取到当前实体类的所以get方法
                final Map<String, Method> getMap = getTypeGetMap(clz);
//               获取传入实体类的所有属性对应的值
                final List<Object> objs = Arrays.stream(clz.getDeclaredFields())
                        .map(Field::getName)
                        .map(name -> name.substring(0, 1).toUpperCase() + name.substring(1))
                        .map(name -> {
                            try {
                                return getMap.get(name).invoke(args[0]);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e.getMessage());
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                statement = parseStatement(sql, objs);
            }
//            多个参数就是直接向对应的?填充
            else {
                statement = parseStatement(sql, args);
            }
//            执行插入操作,返回操作结果
//            如果返回的第一个结果是resultSet对象时,返回true,如果其为更新计数或者不存在任何结果,则返回 false
            return !statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
        return null;
    }

    //    处理@Select注解的方法
    private static <T> Object handlerSelect(String sql, Method method, Object[] args) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = parseStatement(sql, args);
            final boolean execute = statement.execute();
            if (execute) {
//                    获取返回的类型
                final Class<T> resultType = getResultType(method);
//                    获取返回类型的所有set方法
                final Map<String, Method> setterMethodMap = getTypeSetMap(resultType);
//                   获取读取到的数据库中的内容
                resultSet = statement.getResultSet();
//                    获取表中字段的名字
                final List<String> columnList = new ArrayList<>();
                final ResultSetMetaData metaData = resultSet.getMetaData();
                for (int i = 0; i < metaData.getColumnCount(); i++) {
                    columnList.add(metaData.getColumnName(i + 1));
                }
                final List<T> resultList = new ArrayList<>();
//                    一行一行读取结果集的数据
                while (resultSet.next()) {
                    final T t = resultType.getConstructor().newInstance();
                    for (String colName : columnList) {
//                        如果有表的列名有_需要处理一下
                        final String key = StringUtil.convertToLineHump(colName);
                        final Method setMethod = setterMethodMap.get(key);
                        setMethod.invoke(t, resultSet.getObject(colName));
                    }
                    resultList.add(t);
                }
                if (resultList.size() > 1) {
                    return resultList;
                } else {
                    return resultList.get(0);
                }
            } else {
                throw new SQLException("sql语句执行失败,请检查sql语句是否正确");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
            dbUtil.closeConnection(connection);
        }
        return null;
    }

    //    给sql语句的?填充对应的值
    private static PreparedStatement parseStatement(String sql, Object[] args) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }
        }
        return statement;
    }

    //    给sql语句的?填充对应的值
    private static PreparedStatement parseStatement(String sql, List<Object> args) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        if (args != null && args.size() > 0) {
            for (int i = 0; i < args.size(); i++) {
                statement.setObject(i + 1, args.get(i));
            }
        }
        return statement;
    }

    //    获取返回类型的所有set方法
    private static <T> Map<String, Method> getTypeSetMap(Class<T> type) {
        final Map<String, Method> map = new HashMap<>();
        for (Method method : type.getDeclaredMethods()) {
//            获取所以set开头的方法
            if (method.getName().startsWith("set")) {
//                去掉set作为key
                final String key = method.getName().substring(3);
                map.put(key, method);
            }
        }
        return map;
    }

    //    获取返回类型的所有get方法
    private static <T> Map<String, Method> getTypeGetMap(Class<T> type) {
        final Map<String, Method> map = new HashMap<>();
        for (Method method : type.getDeclaredMethods()) {
//            获取所以get开头的方法
            if (method.getName().startsWith("get")) {
//                去掉get作为key
                final String key = method.getName().substring(3);
                map.put(key, method);
            }
        }
        return map;
    }

    //    获取方法返回的值的类型
    @SuppressWarnings("unchecked")//忽略强转类型的警告
    private static <T> Class<T> getResultType(Method method) {
        Class<T> clz = null;
        final Type type = method.getGenericReturnType();
//        不是范型
        if (type instanceof Class) {
            clz = (Class<T>) type;
        }
//        是范型
        else if (type instanceof ParameterizedType) {
            final Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            clz = (Class<T>) types[0];
        }
        return clz;
    }

    //    解析sql语句
    private static String parseSql(Method method) {
        //        正则匹配 #{..} 的内容
        final String reg = "#\\{[a-z]+}";
        String sql;
        if (method.isAnnotationPresent(Select.class)) {
            sql = method.getAnnotation(Select.class).value();
        } else if (method.isAnnotationPresent(Insert.class)) {
            sql = method.getAnnotation(Insert.class).value();
        } else if (method.isAnnotationPresent(Update.class)) {
            sql = method.getAnnotation(Update.class).value();
        } else if (method.isAnnotationPresent(Delete.class)) {
            sql = method.getAnnotation(Delete.class).value();
        } else {
//            没有上面的4个注解就不用继续用正则匹配了
            return "";
        }
        final Pattern pattern = Pattern.compile(reg);
        final Matcher matcher = pattern.matcher(sql);
//        把匹配到的地方替换成 ?
        while (matcher.find()) {
            sql = sql.replace(matcher.group(0), "?");
        }
//        返回替换完成的sql语句
        return sql;
    }

}
