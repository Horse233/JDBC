package com.atguigu3.dao;

import com.atguigu1.util.JDBCUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO: data(base) access object
 * 封装了对于数据表的通用的操作
 */
public abstract class BaseDAO<T> {

    private Class<T> clazz = null;

//    public BaseDAO(){
//
//    }

    {
        //获取当前BaseDAO的子类继承的父类中的泛型
        //子类对象的带泛型的父类
        Type genericSuperclass = this.getClass().getGenericSuperclass();
        ParameterizedType paramType = (ParameterizedType) genericSuperclass;

        Type[] typeArguments = paramType.getActualTypeArguments();//获取父类的泛型参数
        clazz = (Class<T>) typeArguments[0]; //泛型的第一个参数
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

    /**
     * 针对不同表的通用查询操作，返回表的一条操作
     * 通用的查询操作，用于返回数据表中的一条记录（version 2.0，考虑上事务）
     * @param clazz
     * @param sql
     * @param args
     * @return
     * @param <T>
     */
    public  T getInstance(Connection conn, String sql, Object... args){
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

    /**
     * 通用的查询操作，用于返回数据表中的多条记录构成的集合（version 2.0，考虑上事务）
     * @param conn
     * @param clazz
     * @param sql
     * @param args
     * @return
     * @param <T>
     */
    public List<T> getForList(Connection conn, String sql, Object... args){
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
            //创建集合对象
            ArrayList<T> list = new ArrayList<T>();
            while(rs.next()){
                T t = clazz.newInstance();
                //给t对象的属性赋值的过程
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
                list.add(t);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            JDBCUtils.closeResource(null, ps, rs);
        }
        return null;
    }

    //用于查询特殊值的通用方法
    public <E> E getValue(Connection conn, String sql, Object...orgs){
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            for(int i = 0; i < orgs.length; i++){
                ps.setObject(i+1, orgs[i]);
            }
            rs = ps.executeQuery();
            if(rs.next()){
                return (E) rs.getObject(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.closeResource(null, ps, rs);
        }
        return null;
    }
}
