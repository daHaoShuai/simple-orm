package com.da.dao;

import com.da.orm.annotation.Insert;
import com.da.orm.annotation.Select;
import com.da.po.User;

import java.util.List;

/**
 * @Author Da
 * @Description:
 * @Date: 2022-07-22
 * @Time: 10:22
 */
public interface UserMapper {

    @Select("select * from user")
    List<User> list();

    @Select("select * from user where name = #{name} and id = #{id}")
    User getById(Integer id, String name);

    @Insert("insert into user (name,pass) values(?,?)")
    boolean add(User user);

}
