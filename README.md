# 简单封装一个自己的orm框架

创建一张user表

```sql
create table user
(
    id   int auto_increment primary key,
    name varchar(25) null,
    pass varchar(25) null,
    time datetime null
);
```

在resources目录下创建数据库连接配置文件 simple-orm.properties

```properties
username=root
password=123456
url=jdbc:mysql://192.168.31.126:3306/demo?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true
driver=com.mysql.cj.jdbc.Driver
```

设置实体类(实体类的属性类型必须要跟数据库中的类型一致,不然可能会报错)

```java
import com.da.orm.annotation.Col;
import com.da.orm.annotation.Table;

import java.time.LocalDateTime;

@Table(tableName = "user")
public class User {
    @Col(name = "id", primaryKey = true)
    private Integer id;

    @Col(name = "name")
    private String name;

    @Col(name = "pass")
    private String pass;

    private LocalDateTime time;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
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

直接使用BaseDao

```java
import com.da.orm.core.BaseCrud;
import com.da.orm.core.BaseDao;
import com.da.po.User;

public class App {

    public static void main(String[] args) {
        final BaseCrud<User> dao = new BaseDao<>(User.class);
        System.out.println(dao.getById(2));
        dao.closeConnection();
    }

}
```

继承BaseDao获得基础的增删改查能力

```java
import com.da.orm.core.BaseDao;
import com.da.po.User;

import java.util.List;

public class UserDao extends BaseDao<User> {
    public UserDao() {
        super(User.class);
    }

    //    (自己扩展方法)通过name获取
    public User getUserByName(String name) {
//        构建查询sql
        final String sql = this.getSqlBuild().select().where().eq("name", "'" + name + "'").build();
//        使用query方法解析查询语句
        final List<User> users = this.query(sql);
        if (users.size() > 1) {
            throw new RuntimeException("查出的用户name为" + name + "的不止一个");
        }
        return users.get(0);
    }
}
```

增删改查

```java
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
        user.setTime(LocalDateTime.now());
        final UserDao userDao = new UserDao();
//        新增
        System.out.println(userDao.add(user));
//        获取表中所有的数据
        final List<User> users = userDao.list();
//        分页查询(当前页,每页的条数)
        final List<User> pages = userDao.pages(2, 2);
        pages.forEach(System.out::println);
//        通过主键获取
        final User id = userDao.getById(11);
//        通过主键删除
        System.out.println(userDao.deleteById(16));
//        通过主键更新
        System.out.println(userDao.updateById(user));
//        执行sql语句查询信息
        final List<User> list = userDao.query("select id,name,pass from user where pass='bb' and id=19");
        System.out.println(list);
//        执行增删改操作
        System.out.println(userDao.exec("insert into user(name,pass) values('aa','bb')"));
        System.out.println(userDao.exec("delete from user where name='aa'"));
//        使用自己拓展的方法
        final User name = userDao.getUserByName("a1");
        System.out.println(name);
//        关闭连接
        userDao.closeConnection();
    }
}
```

简单的用代码拼接sql语句

```java
import com.da.orm.core.Sql;
import com.da.po.User;

public class App {

    public static void main(String[] args) {
        final Sql sql = new Sql(User.class);

        final String s = sql.select().build();
        System.out.println(s);

        final String s1 = sql.select("name", "pass").build();
        System.out.println(s1);

        final String s2 = sql.insert().build();
        System.out.println(s2);

        final String s3 = sql.insert("name", "pass").build();
        System.out.println(s3);

        final String s4 = sql.update()
                .where()
                .eq("name", "aa")
                .and()
                .ne("a1", "a")
                .and()
                .gt("a3", 1)
                .and()
                .lt("a4", 3)
                .build();
        System.out.println(s4);

        final String s5 = sql.update("name", "pass")
                .where()
                .eq("pass", "bb")
                .and()
                .eq("name", "aa")
                .and()
                .le("a", 10)
                .build();
        System.out.println(s5);

        final String s6 = sql.delete()
                .where()
                .eq("name", "aa")
                .build();
        System.out.println(s6);

//      构建分页查询语句
        String s7 = sqlBuild.select()
                .limit()
                .and(当前页数)
                .offset()
                .and(每页条数 * (当前页数 - 1))
                .build();
        System.out.println(s7);

//        允许通过方法引用获取对应的属性名字
        String s8 = new Sql(User.class).select().where().eq(User::getName, "root").build();
        System.out.println(s8);
    }

}
```
