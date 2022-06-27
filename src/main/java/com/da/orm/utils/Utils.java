package com.da.orm.utils;

import com.da.orm.function.ConsumerListAndIndex;
import com.da.orm.function.IGetter;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
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
 * @Date: 2022-06-24
 * @Time: 10:03
 * 工具类
 */
public class Utils {

    //    遍历List时可以获取其对应的坐标
    public static <T> void ListEach(List<T> data, ConsumerListAndIndex<T> consumer) {
        for (int i = 0; i < data.size(); i++) {
            consumer.each(data.get(i), i);
        }
    }

    //    获取类的所有属性,并且转成list
    public static <T> List<Field> getAllField(Class<T> t) {
        return Arrays.asList(t.getDeclaredFields());
    }

    //    获取类属性的名字
    public static <T> String getPoFieldName(IGetter<T> fn) {
        try {
//            获取传入lambda类的writeReplace方法
            final Method writeReplace = fn.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
//            获取传入lambda对象
            SerializedLambda lambda = (SerializedLambda) writeReplace.invoke(fn);
//            得到get方法的全名
            final String methodName = lambda.getImplMethodName();
            if (methodName.startsWith("get")) {
                final String get = methodName.substring(methodName.indexOf("get") + 3);
                return get.substring(0, 1).toLowerCase() + get.substring(1);
            } else if (methodName.startsWith("is")) {
                final String get = methodName.substring(methodName.indexOf("is") + 2);
                return get.substring(0, 1).toLowerCase() + get.substring(1);
            }
            throw new RuntimeException("无效的get方法名字 => " + methodName);
        } catch (Exception e) {
            throw new RuntimeException("获取属性名字出错 => " + e.getMessage());
        }
    }

}
