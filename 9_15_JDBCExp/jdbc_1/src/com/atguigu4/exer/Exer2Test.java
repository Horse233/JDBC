package com.atguigu4.exer;

import com.atguigu3.util.JDBCUtils;
import org.junit.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Scanner;

//课后练习2
public class Exer2Test {
    public static void main(String[] args) {
        new Exer2Test().testInsert();
    }

    //问题一：向examstudent表中添加一条数据
    @Test
    public void testInsert(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("四级/六级？");
        int type = scanner.nextInt();
        System.out.println("身份证号：");
        String IDCard = scanner.next();
        System.out.println("准考证号：");
        String examCard = scanner.next();
        System.out.println("学生姓名：");
        String studentName = scanner.next();
        System.out.println("所在城市：");
        String location = scanner.next();
        System.out.println("考试成绩：");
        int grade = scanner.nextInt();

        String sql = "insert into examstudent(type, IDCard, examCard, studentName, location, grade) values(?,?,?,?,?,?)";
        int insertCount = update(sql, type, IDCard, examCard, studentName, location, grade);
        if (insertCount > 0) {
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

    // 问题2：根据身份证号或者学生证号查询学生信息
    @Test
    public void queryWithIDCardOrExamCard(){
        System.out.println("请选择您要输入的类型：\n" +
                "a:准考证号\n" +
                "b:身份证号");
        Scanner scanner = new Scanner(System.in);
        String selection = scanner.next();
        if("a".equalsIgnoreCase(selection)){//if(selection.equalsIgnoreCase("a")){
            System.out.println("请输入准考证号：");
            String examCard = scanner.next();
            String sql = "select flowID flowID, Type type, IDCard, ExamCard examCard, StudentName name, location, grade from examstudent where ExamCard = ?";
            Student student = getInstance(Student.class, sql, examCard);
            if(student != null){
                System.out.println(student);
            }else{
                System.out.println("输入的准考证号有误！");
            }

        }else if("b".equalsIgnoreCase(selection)){
            System.out.println("请输入身份证号：");
            String IDCard = scanner.next();
            String sql = "select flowID flowID, Type type, IDCard, ExamCard examCard, StudentName name, location, grade from examstudent where IDCard = ?";
            Student student = getInstance(Student.class, sql, IDCard);
            if(student != null){
                System.out.println(student);
            }else{
                System.out.println("输入的身份证号有误！");
            }
        }else{
            System.out.println("您的输入有误，请重新进入程序");
        }
    }


    /**
     * 针对不同表的通用查询操作，返回表的一条操作
     * @param clazz
     * @param sql
     * @param args
     * @return
     * @param <T>
     */
    public <T> T getInstance(Class<T> clazz, String sql, Object... args){
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtils.getConnection();

            ps = conn.prepareStatement(sql);
            for(int i = 0; i < args.length; i++){
                ps.setObject(i+1, args[i]);
            }

            rs = ps.executeQuery();
            //获取结果集的元数据 :ResultSetMetaData
            ResultSetMetaData rsmd = rs.getMetaData();
            //通过ResultSetMetaData获取结果集中的列数
            int columnCount = rsmd.getColumnCount();
            while(rs.next()){
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
            JDBCUtils.closeResource(conn, ps, rs);
        }
        return null;
    }

    //问题3：删除指定的学生信息
    @Test
    public void testDeleteByExamCard(){
        System.out.println("请输入学生的考号：");
        Scanner scanner = new Scanner(System.in);
        String examCard = scanner.next();
        //查询指定准考证号的学生
        String sql = "select flowID flowID, Type type, IDCard, ExamCard examCard, StudentName name, location, grade from examstudent where ExamCard = ?";
        Student student = getInstance(Student.class, sql, examCard);
        if(student == null){
            System.out.println("查无此人，请重新输入");
        }else{
            String sql1 = "delete from examstudent where examCard = ?";
            int deleteCount = update(sql1, examCard);
            if(deleteCount > 0){
                System.out.println("删除成功");
            }
        }
    }

    @Test
    public void testDeleteByExamCard1(){
        System.out.println("请输入学生的考号：");
        Scanner scanner = new Scanner(System.in);
        String examCard = scanner.next();
        String sql1 = "delete from examstudent where examCard = ?";
        int deleteCount = update(sql1, examCard);
        if(deleteCount > 0){
            System.out.println("删除成功");
        }else{
            System.out.println("查无此人，请重新输入");
        }
    }
}
