package com.atguigu4.connection;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DBCPTest {

    /**
     * 测试DBCP的数据库连接池技术
     */
    //方式一：不推荐
    @Test
    public void testGetConnection() throws SQLException {
        //创建DBCP数据库连接池
        BasicDataSource dataSource = new BasicDataSource();



        //设置基本信息
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/test");
        dataSource.setUsername("root");
        dataSource.setPassword("root");

        //还可以设置其他数据库连接池管理的相关属性
        dataSource.setInitialSize(10);
        dataSource.setMaxActive(10);
        //......

        Connection conn = dataSource.getConnection();
        System.out.println(conn);
    }

    //方式二：推荐：使用配置文件
    @Test
    public void testGetConnection1() throws Exception {
        Properties props = new Properties();
        //方式1：
//        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("dbcp.properties");
        //方式2：
        FileInputStream is = new FileInputStream(new File("src/dbcp.properties"));
        props.load(is);
        DataSource dataSource = BasicDataSourceFactory.createDataSource(props);
        Connection conn = dataSource.getConnection();
        System.out.println(conn);
    }
}
