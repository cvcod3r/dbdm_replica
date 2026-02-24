import org.junit.Test;

import java.sql.*;
public class HiveDemo {
    @Test
    public void getConnection() {
        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver"); System.out.println("数据库驱动加载成功");
        } catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        try {
            Connection con = DriverManager.getConnection("jdbc:hive2://101.43.142.72:10000","hive","Hive@123");
            String sql = "INSERT INTO s.stu (id,name) VALUES ('aaa','02')";
            Statement stmt = null;
            stmt = con.createStatement();
            int i = stmt.executeUpdate(sql);
            System.out.println(i);
            System.out.println("数据库连接成功");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
