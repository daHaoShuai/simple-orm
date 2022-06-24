package com.da.orm.utils;

import java.util.List;
import java.util.Optional;

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
 * @Time: 16:13
 * 字符串工具类
 */
public class StringUtil {
    //    字符串不为空
    public static boolean isEmpty(String str) {
        return null == str || "".equals(str);
    }

    //    驼峰式命名转下划线命名
    public static String convertToUnderline(String str) {
        if (isEmpty(str)) throw new RuntimeException("输入字符为空");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            final char c = str.charAt(i);
//            处理有大写的地方
            if (Character.isUpperCase(c)) {
                if (i == 0) {
                    result.append(String.valueOf(c).toLowerCase());
                } else {
                    result.append("_").append(String.valueOf(c).toLowerCase());
                }
                continue;
            }
            result.append(c);
        }
        return result.toString();
    }

    //    拼接List并且用指定的字符隔开
    public static String join(List<String> data, String str) {
        final Optional<String> reduce = data.stream().reduce((o, n) -> o + str + n);
        return reduce.orElse("");
    }

    //    拼接String数组并且用指定的字符隔开
    public static String join(String[] data, String str) {
        final StringBuilder result = new StringBuilder();
        for (String d : data) {
            result.append(d).append(str);
        }
        return result.substring(0, result.lastIndexOf(str));
    }

}
