package com.da.orm;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

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
    private String username;
    private String password;
    private String url;
    private String driver;
    private Connection connection;

    public DBUtil() {
        initConnect(0);
    }

    public DBUtil(String username, String password, String url, String driver) {
        this.username = username;
        this.password = password;
        this.url = url;
        this.driver = driver;
        initConnect(1);
    }

    //    初始化连接
    private void initConnect(int type) {
        try {
            if (type == 0) {
//              读取配置文件
                final InputStream is = this.getClass().getClassLoader().getResourceAsStream("simple-orm.properties");
                final Properties properties = new Properties();
                properties.load(is);
                this.username = properties.getProperty("username");
                this.password = properties.getProperty("password");
                this.url = properties.getProperty("url");
                this.driver = properties.getProperty("driver");
            }
            Class.forName(driver);
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("数据库连接成功");
        } catch (ClassNotFoundException | SQLException | IOException e) {
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
