package com.atguigu5.blob;

import com.atguigu3.util.JDBCUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * 使用PrepareStatement实现批量数据操作
 *
 * update、delete本身就具有批量操作的效果。
 * 此时的批量操作主要值的是批量插入。使用PreparedStatement如何实现更高效的批量插入？
 *
 * 题目：向goods表中插入20000条数据
 * create table goods(
 * id int primary key auto_increment,
 * name varchar(25)
 * );
 *
 * 方式一：Statement
 * Connection conn = JDBCUtils.getConnection();
 * Statement st = conn.createStatement();
 * for(int i = 0; i < 20000; i++){
 *     String sql = "insert into goods(name) value('name_"+i+"')";
 *     st.execute(sql);
 * }
 *
 * 方式二：
 *
 */
public class InsertTest {

    /**
     * 批量插入方式二：使用PrepareStatement
     */
    @Test
    public void testInsert1(){
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            long start = System.currentTimeMillis();
            conn = JDBCUtils.getConnection();
            String sql = "insert into goods(name)value(?)";
            ps = conn.prepareStatement(sql);
            for(int i = 1; i <= 20000; i++){
                ps.setObject(1, "name_"+i);

                ps.execute();
            }
            long end = System.currentTimeMillis();
            System.out.println("花费的时间为：" + (end-start)); //20000花费的时间为：77342
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.closeResource(conn, ps);
        }
    }

    /**
     * 批量插入的方式三：
     * 1. addBatch()、executeBatch()、clearBatch()
     * 2. mysql服务器默认是关闭批处理的，我们需要通过一个参数，让mysql开启批处理的支持，
     *    ?rewriteBatchedStatements=true 写在配置文件的url后面
     * 3.
     */
    @Test
    public void testInsert2(){
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            long start = System.currentTimeMillis();
            conn = JDBCUtils.getConnection();
            String sql = "insert into goods(name)value(?)";
            ps = conn.prepareStatement(sql);
            for(int i = 1; i <= 20000; i++){
                ps.setObject(1, "name_"+i);

                //1."攒"sql
                ps.addBatch();
                if(i % 500 == 0){
                    //2.执行
                    ps.executeBatch();

                    //3.清空batch
                    ps.clearBatch();
                }

            }
            long end = System.currentTimeMillis();
            System.out.println("花费的时间为：" + (end-start)); //20000花费的时间为：4266/69699
                                                                //1000000:26789
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.closeResource(conn, ps);
        }
    }

    /**
     * 批量插入的方式四：设置连接不允许数据自动提交
     *
     */
    @Test
    public void testInsert3(){
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            long start = System.currentTimeMillis();
            conn = JDBCUtils.getConnection();

            //设置不允许自动提交数据
            conn.setAutoCommit(false);

            String sql = "insert into goods(name)value(?)";
            ps = conn.prepareStatement(sql);
            for(int i = 1; i <= 1000000; i++){
                ps.setObject(1, "name_"+i);

                //1."攒"sql
                ps.addBatch();
                if(i % 500 == 0){
                    //2.执行
                    ps.executeBatch();

                    //3.清空batch
                    ps.clearBatch();
                }

            }

            //提交数据
            conn.commit();

            long end = System.currentTimeMillis();
            System.out.println("花费的时间为：" + (end-start)); //20000花费的时间为：77342 --> 4266
                                                                //1000000:26789 --> 16319
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.closeResource(conn, ps);
        }
    }
}
