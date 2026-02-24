import org.junit.Test;

import java.sql.*;
public class EsDemo {
    Connection con;

    @Test
    public void getConnection() {
        try {
            Class.forName("org.elasticsearch.xpack.sql.jdbc.EsDriver"); System.out.println("数据库驱动加载成功");
        } catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        try {
            con = DriverManager.getConnection("jdbc:es://101.43.142.72:9200" ,"elastic","changeme");
            Statement statement = con.createStatement();
            int affectedRows = statement.executeUpdate(
                    "UPDATE test_user SET username = ? WHERE id = ?"
            );
            System.out.println("Affected Rows: " + affectedRows);
//            PreparedStatement preparedStatement = con.prepareStatement(
//                    "UPDATE test_user SET username = ? WHERE id = ?"
//            );
//            preparedStatement.setString(1, "New Title");
//            preparedStatement.setInt(2, 1);
//            int affectedRows = preparedStatement.executeUpdate();
//            System.out.println("Affected Rows: " + affectedRows);
            DatabaseMetaData databaseMetaData = con.getMetaData();
            System.out.println("获取数据库产品名称：" + databaseMetaData.getDatabaseProductName());
            System.out.println("获取数据库的版本号：" + databaseMetaData.getDatabaseProductVersion());
            System.out.println("获取数据库驱动名称：" + databaseMetaData.getDriverName());
            System.out.println("获取数据库驱动版本：" + databaseMetaData.getDriverVersion());
            System.out.println("获取数据库连接URL：" + databaseMetaData.getURL());
            System.out.println("获取数据库的用户名：" + databaseMetaData.getUserName());
            System.out.println("数据库是否允许事务：" + databaseMetaData.supportsTransactions());
            System.out.println("===========获取所有catalog=============");
            ResultSet rs1 = databaseMetaData.getCatalogs();
            while (rs1.next()) {
//                System.out.println("TABLE_SCHEM: " + rs1.getString("TABLE_SCHEM"));
                System.out.println("TABLE_CAT: " + rs1.getString("TABLE_CAT"));
            }


//            SHOW CATALOGS
            System.out.println("===========获取所有表=============");
            ResultSet rs2 = databaseMetaData.getTables(null, null, null, null);
            while (rs2.next()) {
                System.out.println("TABLE_CAT: " + rs2.getString("TABLE_CAT"));
                System.out.println("TABLE_SCHEM: " + rs2.getString("TABLE_SCHEM"));
                System.out.println("TABLE_NAME: " + rs2.getString("TABLE_NAME"));
                System.out.println("TABLE_TYPE: " + rs2.getString("TABLE_TYPE"));
                System.out.println("REMARKS: " + rs2.getString("REMARKS"));
            }
            // 获取所有数据库
            System.out.println("===========获取所有数据库===========");
            ResultSet rs3 = databaseMetaData.getSchemas();
            while (rs3.next()) {
//                System.out.println("TABLE_SCHEM: " + rs1.getString("TABLE_SCHEM"));
                System.out.println("TABLE_SCHEM: " + rs3.getString("TABLE_SCHEM"));
            }
//            ResultSet rs1 = databaseMetaData.getCatalogs();
//            while (rs1.next()) {
////                System.out.println("TABLE_SCHEM: " + rs1.getString("TABLE_SCHEM"));
//                System.out.println("TABLE_CAT: " + rs1.getString("TABLE_CAT"));
//            }

            // 获取指定库所有数据表
            /*
             * catalog: 指定数据库名称
             * schemaPattern: 对于mysql来说直接传null，对于oracle：用户名（大写）
             * tableNamePattern: 查询指定表，为空时查询所有表
             * types: 查询数据库表类型：TABLE（表）、VIEW（视图）
             */
            //metaData.getTables(catalog, schemaPattern, tableNamePattern, types);

            System.out.println("获取视图");
            System.out.println("数据库连接成功");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

