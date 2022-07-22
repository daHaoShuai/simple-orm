package com.da.dao;

import com.da.orm.annotation.Delete;
import com.da.orm.annotation.Insert;
import com.da.orm.annotation.Select;
import com.da.orm.annotation.Update;
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

    @Select("select * from user where id = #{id}")
    User getById(Integer id);

    @Insert("insert into user (name,pass) values(#{name},#{pass})")
    boolean add(User user);

    @Update("update user set name = #{name}, pass = #{pass} where id = #{id}")
    boolean update(User user);

    @Delete("delete from user where id = #{id}")
    boolean delete(Integer id);

}
