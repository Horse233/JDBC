package com.atguigu1.transaction;

import com.atguigu1.util.JDBCUtils;
import org.junit.Test;

import java.lang.reflect.Field;
import java.sql.*;

/**
 * 1. 什么是事务？
 * 事务：一组逻辑操作单元，使数据从一种状态变换到另外一种状态。
 *      > 一组逻辑操作单元：一个或多个DML操作。
 *
 * 2. 事务的处理原则：保证所有事务都作为一个工作单元来执行，即使出了故障，都不能改变这种执行方式。
 * 当在一个事务中执行多个操作时，要么所有的事务都被提交（commit），那么这些修改就永久的保存下来；
 * 要么数据库管理系统将放弃所做的所有的修改，整个事务回滚（rollback）到最初状态。
 *
 * 3. 数据一旦提交，就不可以回滚
 *
 * 4. 哪些操作会导致数据的自动提交？
 *      >DDL操作一旦执行，都会自动提交
 *          >set autocommit = false的方式对DDL操作无效
 *      >DML操作默认情况下，一旦执行，都会自动提交。
 *          >我们可以通过set autocommit = false的方式取消DML操作的自动提交。
*       >默认在关闭连接时，会自动提交数据
 */

public class TransactionTest {

    /**************************未考虑数据库事务的转账操作***********************************/
    /**
     * 针对数据表user_table来说：
     * AA给BB用户转账100
     *
     * update user_table set balance = balance - 100 where user = 'AA'
     * update user_table set balance = balance + 100 where user = 'BB'
     */
    @Test
    public void testUpdate(){

        String sql1 = "update user_table set balance = balance - 100 where user = ?";
        update(sql1, "AA");

        //模拟网络异常
        System.out.println(10 / 0);

        String sql2 = "update user_table set balance = balance + 100 where user = ?";
        update(sql2, "BB");

        System.out.println("转账成功");
    }

    //通用的增删改操作---version 1.0
    public int update(String sql, Object ...args){ //sql中占位符的个数与可变形参的长度一致！
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            //1. 获取数据库连接
            conn = JDBCUtils.getConnection();
            //2. 预编译sql语句，返回PreparedStatement的实例
            ps = conn.prepareStatement(sql);
            //3. 填充占位符
            for(int i = 0; i < args.length; i++){
                ps.setObject(i+1, args[i]); //小心参数声明错误
            }
            //4. 执行
            return ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            //5. 关闭资源
            JDBCUtils.closeResource(conn, ps);
        }
        return 0;
    }

    /**************************考虑数据库事务的转账操作***********************************/

    @Test
    public void testUpdateWithTx() throws Exception{
        Connection conn = null;
        try {
            conn = JDBCUtils.getConnection();
            System.out.println(conn.getAutoCommit());//true
            //1、取消数据的自动提交功能
            conn.setAutoCommit(false);

            String sql1 = "update user_table set balance = balance - 100 where user = ?";
            update(conn, sql1, "AA");

            //模拟网络异常
            System.out.println(10 / 0);

            String sql2 = "update user_table set balance = balance + 100 where user = ?";
            update(conn, sql2, "BB");

            System.out.println("转账成功");

            //2.提交数据
            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
            //3.回滚数据
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            JDBCUtils.closeResource(conn, null);
        }

    }

    //通用的增删改操作---version 2.0（考虑上事务）
    public int update(Connection conn, String sql, Object ...args){ //sql中占位符的个数与可变形参的长度一致！
        PreparedStatement ps = null;
        try {
            //1. 预编译sql语句，返回PreparedStatement的实例
            ps = conn.prepareStatement(sql);
            //2. 填充占位符
            for(int i = 0; i < args.length; i++){
                ps.setObject(i+1, args[i]); //小心参数声明错误
            }
            //3. 执行
            return ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{

            //修改其为自动提交数据
            //主要针对数据库连接池的使用
//            try {
//                conn.setAutoCommit(true);
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
            //4. 关闭资源
            JDBCUtils.closeResource(null, ps);
        }
        return 0;
    }

    /**************************不同隔离级别实验*******************************/
    @Test
    public void testTransactionSelect() throws Exception{
        Connection conn = JDBCUtils.getConnection();

        //获取当前连接的隔离级别
        System.out.println(conn.getTransactionIsolation());
        //设置数据库的隔离级别
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        //取消自动提交数据
        conn.setAutoCommit(false);

        String sql = "select user, password, balance from user_table where user = ?";
        User user = getInstance(conn, User.class, sql, "CC");
        System.out.println(user);
    }

    @Test
    public void testTransactionUpdate() throws Exception{
        Connection conn = JDBCUtils.getConnection();

        //取消自动提交数据
        conn.setAutoCommit(false);

        String sql = "update user_table set balance = ? where user = ?";
        update(conn, sql, 5000, "cc");

        Thread.sleep(15000);
        System.out.println("修改结束");
    }

    /**
     * 针对不同表的通用查询操作，返回表的一条操作
     * 通用的查询操作，用于返回数据表中的一条记录（version 2.0，考虑上事务）
     * @param clazz
     * @param sql
     * @param args
     * @return
     * @param <T>
     */
    public <T> T getInstance(Connection conn, Class<T> clazz, String sql, Object... args){
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            for(int i = 0; i < args.length; i++){
                ps.setObject(i+1, args[i]);
            }

            rs = ps.executeQuery();
            //获取结果集的元数据 :ResultSetMetaData
            ResultSetMetaData rsmd = rs.getMetaData();
            //通过ResultSetMetaData获取结果集中的列数
            int columnCount = rsmd.getColumnCount();
            if(rs.next()){
                T t = clazz.newInstance();
                //处理结果集一行中的每一个列
                for(int i = 0; i < columnCount; i++){
                    //获取列值
                    Object columnValue = rs.getObject(i + 1);
                    //获取每个列的列名
//                    String columnName = rsmd.getColumnName(i + 1);
                    String columnLabel = rsmd.getColumnLabel(i + 1);

                    //给cust这个对象指定的某个属性，赋值为value: 通过反射
//                    Field field = Customer.class.getDeclaredField(columnName);
                    Field field = clazz.getDeclaredField(columnLabel);
                    field.setAccessible(true);
                    field.set(t, columnValue);
                }
                return t;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            JDBCUtils.closeResource(null, ps, rs);
        }
        return null;
    }
}
