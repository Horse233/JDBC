package com.atguigu4.connection;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;

public class DruidTest {

    @Test
    public void testGetConnection() throws Exception{
        Properties props = new Properties();
        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("druid.properties");
        props.load(is);
        DataSource dataSource = DruidDataSourceFactory.createDataSource(props);
        Connection conn = dataSource.getConnection();
        System.out.println(conn);

    }
}
