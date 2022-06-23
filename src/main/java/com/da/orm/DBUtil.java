package com.da.orm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
 * @Time: 9:23
 */
public class DBUtil {
    private String username = "root";
    private String password = "123456";
    private String url = "jdbc:mysql://192.168.31.126:3306/demo?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true";
    private String driver = "com.mysql.cj.jdbc.Driver";
    private Connection connection;

    public DBUtil() {
        initConnect();
    }

    public DBUtil(String username, String password, String url, String driver) {
        this.username = username;
        this.password = password;
        this.url = url;
        this.driver = driver;
        initConnect();
    }

    //    初始化连接
    private void initConnect() {
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("数据库连接成功");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("数据库连接失败");
            e.printStackTrace();
        }
    }

    //    获取连接
    public Connection getConnection() {
        return connection;
    }

    //    关闭连接
    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
