package com.da.orm.core;

import com.da.orm.annotation.Delete;
import com.da.orm.annotation.Insert;
import com.da.orm.annotation.Select;
import com.da.orm.annotation.Update;
import com.da.orm.utils.DBUtil;
import com.da.orm.utils.StringUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        final Object proxyInstance = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{mapper}, (proxy, method, args) -> {
//             处理 @Select 或者 @Insert 或者 @Update 或者 @Delete 注解的方法 获取要执行的sql语句
            final String sql = parseSql(method);
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            try {
                statement = connection.prepareStatement(sql);
                if (args != null && args.length > 0) {
                    for (int i = 0; i < args.length; i++) {
                        statement.setObject(i + 1, args[i]);
                    }
                }
                final boolean execute = statement.execute();
                if (execute) {
//                    获取返回的类型
                    final Class<T> resultType = getResultType(method);
//                    获取返回类型的所有set方法
                    final Map<String, Method> setterMethodMap = getResultTypeSetMap(resultType);
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
        });
        return (T) proxyInstance;
    }

    //    获取返回类型的所有set方法
    private static <T> Map<String, Method> getResultTypeSetMap(Class<T> type) {
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
