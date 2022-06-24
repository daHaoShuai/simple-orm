package com.da.orm;

import com.da.orm.utils.StringUtil;

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
//            连接信息只要一个为空就尝试从配置文件中获取连接信息
            if (StringUtil.isEmpty(username) || StringUtil.isEmpty(password) || StringUtil.isEmpty(url) || StringUtil.isEmpty(driver)) {
                readConfigInfo();
                if (StringUtil.isEmpty(username)) throw new RuntimeException("username 信息有误");
                if (StringUtil.isEmpty(password)) throw new RuntimeException("password 信息有误");
                if (StringUtil.isEmpty(url)) throw new RuntimeException("url 信息有误");
                if (StringUtil.isEmpty(driver)) throw new RuntimeException("driver 信息有误");
            }
            Class.forName(driver);
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("数据库连接成功");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("数据库连接失败");
            e.printStackTrace();
        }
    }

    //    读取配置文件
    private void readConfigInfo() {
        try {
            final InputStream is = this.getClass().getClassLoader().getResourceAsStream("simple-orm.properties");
            if (null == is) {
                throw new RuntimeException("没有找到配置文件,请检查配置文件 simple-orm.properties 是否在 resources 目录下");
            } else {
                final Properties properties = new Properties();
                properties.load(is);
                this.username = properties.getProperty("username");
                this.password = properties.getProperty("password");
                this.url = properties.getProperty("url");
                this.driver = properties.getProperty("driver");
            }
        } catch (Exception e) {
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
