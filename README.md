# 简单封装一个自己的orm框架

创建一张user表
```sql
create table user
(
    id   int auto_increment primary key,
    name varchar(25) null,
    pass varchar(25) null,
    time datetime    null
);
```

在resources目录下创建数据库连接配置文件 simple-orm.properties

```properties
username=root
password=123456
url=jdbc:mysql://192.168.31.126:3306/demo?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true
driver=com.mysql.cj.jdbc.Driver
```

设置实体类

```java
package com.da.po;

import com.da.orm.annotation.Col;
import com.da.orm.annotation.Table;

import java.util.Date;

@Table(tableName = "user")
public class User {
    @Col(name = "id", primaryKey = true)
    private Integer id;

    @Col(name = "name")
    private String name;

    @Col(name = "pass")
    private String pass;

    private Date time;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", pass='" + pass + '\'' +
                ", time=" + time +
                '}';
    }
}
```

继承BaseDao获得基础的增删改查能力

```java
package com.da.dao;

import com.da.orm.BaseDao;
import com.da.po.User;

public class UserDao extends BaseDao<User> {
    public UserDao() {
        super(User.class);
    }
}
```

增删改查
```java
package com.da;

import com.da.dao.UserDao;
import com.da.po.User;

import java.util.Date;
import java.util.List;

public class App {

    public static void main(String[] args) {
        final User user = new User();
        user.setId(15);
        user.setName("admin15");
        user.setPass("admin15");
        user.setTime(new Date(System.currentTimeMillis()));
        final UserDao userDao = new UserDao();
//        新增
        System.out.println(userDao.add(user));
//        获取表中所有的数据
        final List<User> users = userDao.list();
//        通过主键获取
        final User id = userDao.getById(11);
//        通过主键删除
        System.out.println(userDao.deleteById(16));
//        通过主键更新
        System.out.println(userDao.updateById(user));
//        关闭连接
        userDao.closeConnection();
    }
}
```
