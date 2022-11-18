package com.atguigu4.exer;

import com.atguigu3.util.JDBCUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Scanner;

//课后练习1
public class Exer1Test {
    public static void main(String[] args) {
        new Exer1Test().testInsert();
    }
    @Test
    public void testInsert(){
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入用户名:");
        String name = scanner.next();
        System.out.print("请输入邮箱:");
        String email = scanner.next();
        System.out.print("请输入生日:");
        String birth = scanner.next();

        String sql = "insert into customers(name, email, birth) values(?, ?, ?)";
        int insertCount = update(sql, name, email, birth);
        if(insertCount > 0){
            System.out.println("添加成功");
        }else{
            System.out.println("添加失败");
        }
    }

    //通用的增删改操作
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
            /**
             * ps.execute():
             * 如果执行的是查询操作，有返回结果，则此方法返回true；
             * 如果执行的增、删、改操作，没有返回结果，则此方法返回false。
             */
            //方式一：
//            ps.execute();
            //方式二：
            return ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            //5. 关闭资源
            JDBCUtils.closeResource(conn, ps);
        }
        return 0;
    }
}
