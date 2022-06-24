package com.da.orm.utils;

import com.da.orm.function.ConsumerListAndIndex;

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
 */
public class Utils {

    //    遍历List时可以获取其对应的坐标
    public static <T> void ListEach(List<T> data, ConsumerListAndIndex<T> consumer) {
        for (int i = 0; i < data.size(); i++) {
            consumer.each(data.get(i), i);
        }
    }

}
