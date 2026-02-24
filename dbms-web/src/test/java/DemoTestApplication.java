import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.DemoApplication;
import com.dbms.core.BusiDataBaseUtil;
import com.dbms.domain.dbase.MetaInfo;
import com.dbms.entity.DbaseEntity;
import com.dbms.service.DbaseService;
import com.dbms.vo.DbaseEntityVo;
import com.dbms.entity.RoleMenuEntity;
import com.dbms.entity.UserEntity;
import com.dbms.service.DbaseAccessService;
import com.dbms.service.RoleMenuService;
import com.dbms.service.UserService;
import com.dbms.core.SQLParserUtil;
import com.dbms.enums.SqlTypeEnum;
import net.sf.jsqlparser.JSQLParserException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
//import net.sf.jsqlparser.statement.Statement;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
@Transactional
public class DemoTestApplication {

//    @Autowired
//    private MySQLConnection mySQLConnection;
    @Autowired
    private UserService userService;

    @Autowired
    private RoleMenuService roleMenuService;

    @Autowired
    private DbaseAccessService dbaseAccessService;

    @Autowired
    private DbaseService dbaseService;

    public static final Map<String,Connection> connMap = new ConcurrentHashMap<>();

    @Test
    public void testVo(){
        DbaseEntityVo dbaseEntityVo = dbaseAccessService.getDbaseVo(2,4,1, "SQL");
        System.out.println(JSON.toJSONString(dbaseEntityVo));

        List<DbaseEntityVo> dbaseEntityVos = dbaseAccessService.getAccessibleDbaseVo(2, 4, "SQL");
        System.out.println(JSON.toJSONString(dbaseEntityVos));
    }

    @Test
    public void testJsqlParser() throws JSQLParserException {
//        String selectSql = "select username,password from user";

//        String selectSql = "show databases;";
//        String selectSql = "describe table_name;";
        String selectSql = "comment on column \"people\".\"people_sex\" is '性';";
//        String selectSql = "select * from(select \"user\".\"user_name\" from \"user\");";
//        String selectSql = "select user.username as a,user.password as b,food.id as c,food.name as d from dbo.userInfo as user,dbo.foodInfo as food;";
//        String selectSql = "SELECT E1.NAME AS Employee FROM Employee E1 INNER JOIN Employee E2 ON E1.ManagerId = E2.Id AND E1.Salary > E2.Salary;";
        String updateSql = "UPDATE user set username='小张' WHERE id='2';";
        String insertSql = "insert into user (id,username,password,create_time,icon) VALUES ('1','admin','123456','2022-04-21 10:26:16','add');";
        SqlTypeEnum sqlTypeEnum = SQLParserUtil.getSqlType(selectSql);
//        SqlTypeEnum sqlTypeEnum = SQLParserUtil.getSqlType(updateSql);
//        SqlTypeEnum sqlTypeEnum = SQLParserUtil.getSqlType(insertSql);
        System.out.println(sqlTypeEnum);
        List<String> tableList = null;
        List<String> columnList = null;
        HashMap<String, ArrayList<String>> mplist = null;
        HashMap<String, String> tableMap = null;
        switch (sqlTypeEnum){
            case SELECT:
                mplist = SQLParserUtil.getTableColumnList(selectSql, sqlTypeEnum);
                tableMap = SQLParserUtil.getTableMap(selectSql, sqlTypeEnum);
                if(mplist == null) break;
                System.out.println(JSON.toJSONString(mplist));
                System.out.println(JSON.toJSONString(tableMap));
                for(String key : mplist.keySet()){
                    ArrayList<String> list = mplist.get(key);
                    System.out.println("key = " + key);
                    for(int i = 0; i < list.size(); i++){
                        System.out.println("i = " + i + " " + list.get(i));
                    }
                }
                break;
            case UPDATE:
                mplist = SQLParserUtil.getTableColumnList(updateSql, sqlTypeEnum);
                if(mplist == null) break;
                for(String key : mplist.keySet()){
                    ArrayList<String> list = mplist.get(key);
                    System.out.println("key = " + key);
                    for(int i = 0; i < list.size(); i++){
                        System.out.println("i = " + i + " " + list.get(i));
                    }
                }
                tableList = SQLParserUtil.getTableList(updateSql, sqlTypeEnum);
                columnList = SQLParserUtil.getColumnList(updateSql,sqlTypeEnum);
                break;
            case INSERT:
                mplist = SQLParserUtil.getTableColumnList(insertSql, sqlTypeEnum);
                if(mplist == null) break;
                for(String key : mplist.keySet()){
                    ArrayList<String> list = mplist.get(key);
                    System.out.println("key = " + key);
                    for(int i = 0; i < list.size(); i++){
                        System.out.println("i = " + i + " " + list.get(i));
                    }
                }
                tableList = SQLParserUtil.getTableList(insertSql,sqlTypeEnum);
                columnList = SQLParserUtil.getColumnList(insertSql,sqlTypeEnum);
                break;
            case COMMENT:
                tableMap = SQLParserUtil.getTableMap(selectSql, sqlTypeEnum);
                break;
            default:
                break;
        }
        System.out.println(tableMap);
        System.out.println(columnList);
    }

    @Test
    public void test(){
        String mysql =  "jdbc:mysql://82.157.239.171:3306/mysql";
        String name = "root";
        String password = "root";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(mysql,name,password);
//            JdbcRowSet jrs = new JdbcRowSetImpl(con);
//            jrs.setCommand("show DATABASES");
//            jrs.execute();
//            while (jrs.next()) {
//                System.out.println(jrs.getString(1));
//            }
//
//            DatabaseMetaData databaseMetaData = con.getMetaData();
//            ResultSet schemas = databaseMetaData.getSchemas();
//            System.out.println(schemas);
//            while (schemas.next()){
//                String table_schem = schemas.getString("TABLE_SCHEMA");
//                String table_catalog = schemas.getString("TABLE_CATALOG");
//                System.out.println(table_schem);
//            }
            DatabaseMetaData databaseMetaData = conn.getMetaData();

            System.out.println("获取数据库产品名称：" + databaseMetaData.getDatabaseProductName());
            System.out.println("获取数据库的版本号：" + databaseMetaData.getDatabaseProductVersion());
            System.out.println("获取数据库驱动名称：" + databaseMetaData.getDriverName());
            System.out.println("获取数据库驱动版本：" + databaseMetaData.getDriverVersion());
            System.out.println("获取数据库连接URL：" + databaseMetaData.getURL());
            System.out.println("获取数据库的用户名：" + databaseMetaData.getUserName());
            System.out.println("数据库是否允许只读：" + databaseMetaData.supportsTransactions());
            // 获取所有数据库
            System.out.println("===========获取所有数据库===========");
            ResultSet rs1 = databaseMetaData.getCatalogs();
            while (rs1.next()) {
//                System.out.println("TABLE_SCHEM: " + rs1.getString("TABLE_SCHEM"));
                System.out.println("TABLE_CAT: " + rs1.getString("TABLE_CAT"));
            }

            // 获取指定库所有数据表
            /*
             * catalog: 指定数据库名称
             * schemaPattern: 对于mysql来说直接传null，对于oracle：用户名（大写）
             * tableNamePattern: 查询指定表，为空时查询所有表
             * types: 查询数据库表类型：TABLE（表）、VIEW（视图）
             */
            //metaData.getTables(catalog, schemaPattern, tableNamePattern, types);
            System.out.println("===========获取所有表=============");
            ResultSet rs2 = databaseMetaData.getTables(null, "dbms", null, new String[] {"TABLE"});
            while (rs2.next()) {
                System.out.println("TABLE_CAT: " + rs2.getString("TABLE_CAT"));
                System.out.println("TABLE_SCHEM: " + rs2.getString("TABLE_SCHEM"));
                System.out.println("TABLE_NAME: " + rs2.getString("TABLE_NAME"));
                System.out.println("TABLE_TYPE: " + rs2.getString("TABLE_TYPE"));
                System.out.println("REMARKS: " + rs2.getString("REMARKS"));
            }
            System.out.println("===========获取视图==============");
            // 获取所有数据表字段
            /*
             * catalog: 指定数据库名称
             * schemaPattern: 对于mysql来说直接传null，对于oracle：用户名（大写）
             * tableNamePattern: 查询指定表，为空时查询所有表
             * columnNamePattern: 列名称，为空查询所有列
             */
            // getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
            ResultSet rs3 = databaseMetaData.getFunctions(null, "mysql" ,null);
            ResultSet rs4 = databaseMetaData.getProcedures(null, "mysql", null);
            ResultSet rs = databaseMetaData.getColumns("mysql",null,  "user", null);
            while (rs.next()) {
                System.out.println("TABLE_CAT：" + rs.getString("TABLE_CAT"));
                System.out.println("TABLE_SCHEM：" + rs.getString("TABLE_SCHEM"));
                System.out.println("TABLE_NAME：" + rs.getString("TABLE_NAME"));
                System.out.println("COLUMN_NAME：" + rs.getString("COLUMN_NAME"));
//                System.out.println("DATA_TYPE：" + rs.getString("DATA_TYPE"));
//                System.out.println("TYPE_NAME：" + rs.getString("TYPE_NAME"));
//                System.out.println("COLUMN_SIZE：" + rs.getString("COLUMN_SIZE"));
//                System.out.println("BUFFER_LENGTH：" + rs.getString("BUFFER_LENGTH"));
//                System.out.println("DECIMAL_DIGITS：" + rs.getString("DECIMAL_DIGITS"));
//                System.out.println("NUM_PREC_RADIX：" + rs.getString("NUM_PREC_RADIX"));
//                System.out.println("NULLABLE：" + rs.getString("NULLABLE"));
//                System.out.println("REMARKS：" + rs.getString("REMARKS"));
//                System.out.println("COLUMN_DEF：" + rs.getString("COLUMN_DEF"));
//                System.out.println("SQL_DATA_TYPE：" + rs.getString("SQL_DATA_TYPE"));
//                System.out.println("SQL_DATETIME_SUB：" + rs.getString("SQL_DATETIME_SUB"));
//                System.out.println("CHAR_OCTET_LENGTH：" + rs.getString("CHAR_OCTET_LENGTH"));
//                System.out.println("ORDINAL_POSITION：" + rs.getString("ORDINAL_POSITION"));
//                System.out.println("IS_NULLABLE：" + rs.getString("IS_NULLABLE"));
////                System.out.println("SCOPE_CATALOG：" + rs.getString("SCOPE_CATALOG"));
//                System.out.println("SCOPE_SCHEMA：" + rs.getString("SCOPE_SCHEMA"));
//                System.out.println("SCOPE_TABLE：" + rs.getString("SCOPE_TABLE"));
//                System.out.println("SOURCE_DATA_TYPE：" + rs.getString("SOURCE_DATA_TYPE"));
//                System.out.println("IS_AUTOINCREMENT：" + rs.getString("IS_AUTOINCREMENT"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("hello");
    }

    @Test
    public void testOracle(){
        String mysql =  "jdbc:oracle:thin:@82.157.239.171:1521:helowin";
        String name = "USER11";
        String password = "USER11";
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            System.out.println("连接oracle...");
            Connection conn = DriverManager.getConnection(mysql,name,password);
            System.out.println("连接oracle成功");
//            JdbcRowSet jrs = new JdbcRowSetImpl(con);
////            jrs.setCommand("show DATABASES");
////            jrs.execute();
//            while (jrs.next()) {
//                System.out.println(jrs.getString(1));
//            }

//            DatabaseMetaData databaseMetaData = con.getMetaData();
//            ResultSet schemas = databaseMetaData.getSchemas();
//            System.out.println(schemas);
//            while (schemas.next()){
//                String table_schem = schemas.getString("TABLE_SCHEM");
//                String table_catalog = schemas.getString("TABLE_CATALOG");
//                System.out.println(table_schem);
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("hello");
    }

    @Test
    public void testSqlserver(){

        String mysql =  "jdbc:sqlserver://82.157.239.171:1433;database=master;encrypt=true;trustServerCertificate=true";
        String name = "sa";
        String password = "uchiha@123";
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection con = DriverManager.getConnection(mysql,name,password);
            System.out.println("连接sqlserver成功");
//            JdbcRowSet jrs = new JdbcRowSetImpl(con);
//            jrs.setCommand("show DATABASES");
//            jrs.execute();
//            while (jrs.next()) {
//                System.out.println(jrs.getString(1));
//            }
//
//            String connectionUrl =
//                    "jdbc:sqlserver://localhost:1433;" +
//                            "databaseName=AdventureWorks;integratedSecurity=true;" +
//                            "encrypt=true; trustServerCertificate=false;" +
//                            "trustStore=storeName;trustStorePassword=storePassword;" +
//                            "hostNameInCertificate=hostName";
//            DatabaseMetaData databaseMetaData = con.getMetaData();
//            ResultSet schemas = databaseMetaData.getSchemas();
//            System.out.println(schemas);
//            while (schemas.next()){
//                String table_schem = schemas.getString("TABLE_SCHEM");
//                String table_catalog = schemas.getString("TABLE_CATALOG");
//                System.out.println(table_schem);
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("hello");
    }

    @Test
    public void testMetaInfo(){
        DbaseEntity dbaseEntity = dbaseService.getById(1);
        BusiDataBaseUtil dataBaseUtil = new BusiDataBaseUtil(dbaseEntity);
        MetaInfo metaInfo = dataBaseUtil.getDataBaseMeta();
        System.out.println(JSON.toJSONString(metaInfo));

    }

    @Test
    public void testDMServer(){

        String dm =  "jdbc:dm://81.70.167.102:5236:";
        String name = "SYSDBA";
        String password = "SYSDBA001";
        try {
            Class.forName("dm.jdbc.driver.DmDriver");
            Connection conn = DriverManager.getConnection(dm,name,password);
            System.out.println("连接dmserver成功");
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            System.out.println("获取数据库产品名称：" + databaseMetaData.getDatabaseProductName());
            System.out.println("获取数据库的版本号：" + databaseMetaData.getDatabaseProductVersion());
            System.out.println("获取数据库驱动名称：" + databaseMetaData.getDriverName());
            System.out.println("获取数据库驱动版本：" + databaseMetaData.getDriverVersion());
            System.out.println("获取数据库连接URL：" + databaseMetaData.getURL());
            System.out.println("获取数据库的用户名：" + databaseMetaData.getUserName());
            System.out.println("数据库是否允许事务：" + databaseMetaData.supportsTransactions());
            // 获取所有数据库
            System.out.println("===========获取所有数据库===========");
            ResultSet rs1 = databaseMetaData.getCatalogs();
            while (rs1.next()) {
//                System.out.println("TABLE_SCHEM: " + rs1.getString("TABLE_SCHEM"));
                System.out.println("TABLE_CATALOG: " + rs1.getString("TABLE_CAT"));
            }

            // 获取指定库所有数据表
            /*
             * catalog: 指定数据库名称
             * schemaPattern: 对于mysql来说直接传null，对于oracle：用户名（大写）
             * tableNamePattern: 查询指定表，为空时查询所有表
             * types: 查询数据库表类型：TABLE（表）、VIEW（视图）
             */
            //metaData.getTables(catalog, schemaPattern, tableNamePattern, types);
            System.out.println("===========获取所有表=============");
            ResultSet rs2 = databaseMetaData.getTables(null, "test", null, new String[] {"TABLE"});
            while (rs2.next()) {
                System.out.println("TABLE_CAT: " + rs2.getString("TABLE_CAT"));
                System.out.println("TABLE_SCHEM: " + rs2.getString("TABLE_SCHEM"));
                System.out.println("TABLE_NAME: " + rs2.getString("TABLE_NAME"));
                System.out.println("TABLE_TYPE: " + rs2.getString("TABLE_TYPE"));
                System.out.println("REMARKS: " + rs2.getString("REMARKS"));
            }
            System.out.println("获取视图");
            // 获取所有数据表字段
            /*
             * catalog: 指定数据库名称
             * schemaPattern: 对于mysql来说直接传null，对于oracle：用户名（大写）
             * tableNamePattern: 查询指定表，为空时查询所有表
             * columnNamePattern: 列名称，为空查询所有列
             */
            // getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
            ResultSet rs3 = databaseMetaData.getFunctions(null, "test" ,null);
            ResultSet rs4 = databaseMetaData.getProcedures(null, "test", null);
            ResultSet rs = databaseMetaData.getColumns(null, "test", "user", null);
            while (rs.next()) {
                System.out.println("TABLE_CAT：" + rs.getString("TABLE_CAT"));
                System.out.println("TABLE_SCHEM：" + rs.getString("TABLE_SCHEM"));
                System.out.println("TABLE_NAME：" + rs.getString("TABLE_NAME"));
                System.out.println("COLUMN_NAME：" + rs.getString("COLUMN_NAME"));
                System.out.println("DATA_TYPE：" + rs.getString("DATA_TYPE"));
                System.out.println("TYPE_NAME：" + rs.getString("TYPE_NAME"));
                System.out.println("COLUMN_SIZE：" + rs.getString("COLUMN_SIZE"));
                System.out.println("BUFFER_LENGTH：" + rs.getString("BUFFER_LENGTH"));
                System.out.println("DECIMAL_DIGITS：" + rs.getString("DECIMAL_DIGITS"));
                System.out.println("NUM_PREC_RADIX：" + rs.getString("NUM_PREC_RADIX"));
                System.out.println("NULLABLE：" + rs.getString("NULLABLE"));
                System.out.println("REMARKS：" + rs.getString("REMARKS"));
                System.out.println("COLUMN_DEF：" + rs.getString("COLUMN_DEF"));
                System.out.println("SQL_DATA_TYPE：" + rs.getString("SQL_DATA_TYPE"));
                System.out.println("SQL_DATETIME_SUB：" + rs.getString("SQL_DATETIME_SUB"));
                System.out.println("CHAR_OCTET_LENGTH：" + rs.getString("CHAR_OCTET_LENGTH"));
                System.out.println("ORDINAL_POSITION：" + rs.getString("ORDINAL_POSITION"));
                System.out.println("IS_NULLABLE：" + rs.getString("IS_NULLABLE"));
//                System.out.println("SCOPE_CATALOG：" + rs.getString("SCOPE_CATALOG"));
                System.out.println("SCOPE_SCHEMA：" + rs.getString("SCOPE_SCHEMA"));
                System.out.println("SCOPE_TABLE：" + rs.getString("SCOPE_TABLE"));
                System.out.println("SOURCE_DATA_TYPE：" + rs.getString("SOURCE_DATA_TYPE"));
                System.out.println("IS_AUTOINCREMENT：" + rs.getString("IS_AUTOINCREMENT"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("helloDM");
    }

    @Test
    public void testKingBase(){

        String url =  "jdbc:kingbase8://localhost:54321/public";
        String username = "system";
        String password = "root";
        try {
            Class.forName("com.kingbase8.Driver");
            Connection conn = DriverManager.getConnection(url,username,password);
            System.out.println("连接kingbase成功");

            System.out.println("连接kingbase成功");
            DatabaseMetaData databaseMetaData = conn.getMetaData();

            System.out.println("获取数据库产品名称：" + databaseMetaData.getDatabaseProductName());
            System.out.println("获取数据库的版本号：" + databaseMetaData.getDatabaseProductVersion());
            System.out.println("获取数据库驱动名称：" + databaseMetaData.getDriverName());
            System.out.println("获取数据库驱动版本：" + databaseMetaData.getDriverVersion());
            System.out.println("获取数据库连接URL：" + databaseMetaData.getURL());
            System.out.println("获取数据库的用户名：" + databaseMetaData.getUserName());
            System.out.println("数据库是否允许只读：" + databaseMetaData.supportsTransactions());
            // 获取所有数据库
            System.out.println("===========获取所有数据库===========");
            ResultSet rs1 = databaseMetaData.getSchemas();
            while (rs1.next()) {
                System.out.println("TABLE_SCHEM: " + rs1.getString("TABLE_SCHEM"));
                System.out.println("TABLE_CATALOG: " + rs1.getString("TABLE_CATALOG"));
//                System.out.println("TABLE_CAT: " + rs1.getString(1));
            }

            // 获取指定库所有数据表
            /*
             * catalog: 指定数据库名称
             * schemaPattern: 对于mysql来说直接传null，对于oracle：用户名（大写）
             * tableNamePattern: 查询指定表，为空时查询所有表
             * types: 查询数据库表类型：TABLE（表）、VIEW（视图）
             */
            //metaData.getTables(catalog, schemaPattern, tableNamePattern, types);
            System.out.println("===========获取所有表=============");
            ResultSet rs2 = databaseMetaData.getTables(null, "public", null, new String[] {"TABLE"});
            while (rs2.next()) {
                System.out.println("TABLE_CAT: " + rs2.getString("TABLE_CAT"));
                System.out.println("TABLE_SCHEM: " + rs2.getString("TABLE_SCHEM"));
                System.out.println("TABLE_NAME: " + rs2.getString("TABLE_NAME"));
                System.out.println("TABLE_TYPE: " + rs2.getString("TABLE_TYPE"));
                System.out.println("REMARKS: " + rs2.getString("REMARKS"));
            }
            System.out.println("获取视图");
            // 获取所有数据表字段
            /*
             * catalog: 指定数据库名称
             * schemaPattern: 对于mysql来说直接传null，对于oracle：用户名（大写）
             * tableNamePattern: 查询指定表，为空时查询所有表
             * columnNamePattern: 列名称，为空查询所有列
             */
            // getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
            //ResultSet rs3 = databaseMetaData.getFunctions(null, "test" ,null);
            System.out.println("===========获取所有存储过程===========");
            ResultSet rs4 = databaseMetaData.getProcedures(null, "public", null);
            System.out.println("===========获取所有列===========");
            ResultSet rs = databaseMetaData.getColumns(null, "public", null, null);
            while (rs.next()) {
                System.out.println("TABLE_CAT：" + rs.getString("TABLE_CAT"));
                System.out.println("TABLE_SCHEM：" + rs.getString("TABLE_SCHEM"));
                System.out.println("TABLE_NAME：" + rs.getString("TABLE_NAME"));
                System.out.println("COLUMN_NAME：" + rs.getString("COLUMN_NAME"));
                System.out.println("DATA_TYPE：" + rs.getString("DATA_TYPE"));
                System.out.println("TYPE_NAME：" + rs.getString("TYPE_NAME"));
                System.out.println("COLUMN_SIZE：" + rs.getString("COLUMN_SIZE"));
                System.out.println("BUFFER_LENGTH：" + rs.getString("BUFFER_LENGTH"));
                System.out.println("DECIMAL_DIGITS：" + rs.getString("DECIMAL_DIGITS"));
                System.out.println("NUM_PREC_RADIX：" + rs.getString("NUM_PREC_RADIX"));
                System.out.println("NULLABLE：" + rs.getString("NULLABLE"));
                System.out.println("REMARKS：" + rs.getString("REMARKS"));
                System.out.println("COLUMN_DEF：" + rs.getString("COLUMN_DEF"));
                System.out.println("SQL_DATA_TYPE：" + rs.getString("SQL_DATA_TYPE"));
                System.out.println("SQL_DATETIME_SUB：" + rs.getString("SQL_DATETIME_SUB"));
                System.out.println("CHAR_OCTET_LENGTH：" + rs.getString("CHAR_OCTET_LENGTH"));
                System.out.println("ORDINAL_POSITION：" + rs.getString("ORDINAL_POSITION"));
                System.out.println("IS_NULLABLE：" + rs.getString("IS_NULLABLE"));
//                System.out.println("SCOPE_CATALOG：" + rs.getString("SCOPE_CATALOG"));
                System.out.println("SCOPE_SCHEMA：" + rs.getString("SCOPE_SCHEMA"));
                System.out.println("SCOPE_TABLE：" + rs.getString("SCOPE_TABLE"));
                System.out.println("SOURCE_DATA_TYPE：" + rs.getString("SOURCE_DATA_TYPE"));
                System.out.println("IS_AUTOINCREMENT：" + rs.getString("IS_AUTOINCREMENT"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGBase(){

        String url =  "jdbc:gbasedbt-sqli://192.168.108.129:9088/gbase8s";
        String username = "gbasedbt";
        String password = "gbase123";
        try {
            Class.forName("com.gbasedbt.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url,username,password);
            System.out.println("连接kingbase成功");
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            System.out.println("获取数据库产品名称：" + databaseMetaData.getDatabaseProductName());
            System.out.println("获取数据库的版本号：" + databaseMetaData.getDatabaseProductVersion());
            System.out.println("获取数据库驱动名称：" + databaseMetaData.getDriverName());
            System.out.println("获取数据库驱动版本：" + databaseMetaData.getDriverVersion());
            System.out.println("获取数据库连接URL：" + databaseMetaData.getURL());
            System.out.println("获取数据库的用户名：" + databaseMetaData.getUserName());
            System.out.println("数据库是否支持事务：" + databaseMetaData.supportsTransactions());

            // 获取所有数据库
            System.out.println("===========获取所有数据库===========");
            ResultSet rs1 = databaseMetaData.getCatalogs();
            while (rs1.next()) {
//                System.out.println("TABLE_SCHEM: " + rs1.getString("TABLE_SCHEM"));
                System.out.println("TABLE_CATALOG: " + rs1.getString("TABLE_CAT"));
//                System.out.println("TABLE_CAT: " + rs1.getString(1));
            }

            // 获取指定库所有数据表
            /*
             * catalog: 指定数据库名称
             * schemaPattern: 对于mysql来说直接传null，对于oracle：用户名（大写）
             * tableNamePattern: 查询指定表，为空时查询所有表
             * types: 查询数据库表类型：TABLE（表）、VIEW（视图）
             */
            //metaData.getTables(catalog, schemaPattern, tableNamePattern, types);
            System.out.println("===========获取所有表=============");
            ResultSet rs2 = databaseMetaData.getTables("gbase8s", "gbasedbt", null, new String[] {"TABLE"});
            while (rs2.next()) {
                System.out.println("TABLE_CAT: " + rs2.getString("TABLE_CAT"));
                System.out.println("TABLE_SCHEM: " + rs2.getString("TABLE_SCHEM"));
                System.out.println("TABLE_NAME: " + rs2.getString("TABLE_NAME"));
                System.out.println("TABLE_TYPE: " + rs2.getString("TABLE_TYPE"));
                System.out.println("REMARKS: " + rs2.getString("REMARKS"));
            }
            System.out.println("获取视图");
            // 获取所有数据表字段
            /*
             * catalog: 指定数据库名称
             * schemaPattern: 对于mysql来说直接传null，对于oracle：用户名（大写）
             * tableNamePattern: 查询指定表，为空时查询所有表
             * columnNamePattern: 列名称，为空查询所有列
             */
            // getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
            //ResultSet rs3 = databaseMetaData.getFunctions(null, "test" ,null);
            System.out.println("===========获取所有存储过程===========");
            ResultSet rs4 = databaseMetaData.getProcedures("gbase8s", "gbasedbt", null);

            System.out.println("===========获取所有列===========");
            ResultSet rs = databaseMetaData.getColumns("gbase8s", "gbasedbt", "t_user", null);
            while (rs.next()) {
                System.out.println("TABLE_CAT：" + rs.getString("TABLE_CAT"));
                System.out.println("TABLE_SCHEM：" + rs.getString("TABLE_SCHEM"));
                System.out.println("TABLE_NAME：" + rs.getString("TABLE_NAME"));
                System.out.println("COLUMN_NAME：" + rs.getString("COLUMN_NAME"));
                System.out.println("DATA_TYPE：" + rs.getString("DATA_TYPE"));
                System.out.println("TYPE_NAME：" + rs.getString("TYPE_NAME"));
                System.out.println("COLUMN_SIZE：" + rs.getString("COLUMN_SIZE"));
                System.out.println("BUFFER_LENGTH：" + rs.getString("BUFFER_LENGTH"));
                System.out.println("DECIMAL_DIGITS：" + rs.getString("DECIMAL_DIGITS"));
                System.out.println("NUM_PREC_RADIX：" + rs.getString("NUM_PREC_RADIX"));
                System.out.println("NULLABLE：" + rs.getString("NULLABLE"));
                System.out.println("REMARKS：" + rs.getString("REMARKS"));
                System.out.println("COLUMN_DEF：" + rs.getString("COLUMN_DEF"));
                System.out.println("SQL_DATA_TYPE：" + rs.getString("SQL_DATA_TYPE"));
                System.out.println("SQL_DATETIME_SUB：" + rs.getString("SQL_DATETIME_SUB"));
                System.out.println("CHAR_OCTET_LENGTH：" + rs.getString("CHAR_OCTET_LENGTH"));
                System.out.println("ORDINAL_POSITION：" + rs.getString("ORDINAL_POSITION"));
                System.out.println("IS_NULLABLE：" + rs.getString("IS_NULLABLE"));
//                System.out.println("SCOPE_CATALOG：" + rs.getString("SCOPE_CATALOG"));
                System.out.println("SCOPE_SCHEMA：" + rs.getString("SCOPE_SCHEMA"));
                System.out.println("SCOPE_TABLE：" + rs.getString("SCOPE_TABLE"));
                System.out.println("SOURCE_DATA_TYPE：" + rs.getString("SOURCE_DATA_TYPE"));
                System.out.println("IS_AUTOINCREMENT：" + rs.getString("IS_AUTOINCREMENT"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGBase8a(){

        String url =  "jdbc:gbase://101.43.142.72:5258/gbase";
        String username = "root";
        String password = "root";
        try {
            Class.forName("com.gbase.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url,username,password);
            System.out.println("连接gbase8a成功");
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            System.out.println("获取数据库产品名称：" + databaseMetaData.getDatabaseProductName());
            System.out.println("获取数据库的版本号：" + databaseMetaData.getDatabaseProductVersion());
            System.out.println("获取数据库驱动名称：" + databaseMetaData.getDriverName());
            System.out.println("获取数据库驱动版本：" + databaseMetaData.getDriverVersion());
            System.out.println("获取数据库连接URL：" + databaseMetaData.getURL());
            System.out.println("获取数据库的用户名：" + databaseMetaData.getUserName());
            System.out.println("数据库是否支持事务：" + databaseMetaData.supportsTransactions());

            // 获取所有数据库
            System.out.println("===========获取所有数据库===========");
            ResultSet rs1 = databaseMetaData.getCatalogs();
            while (rs1.next()) {
//                System.out.println("TABLE_SCHEM: " + rs1.getString("TABLE_SCHEM"));
                System.out.println("TABLE_CATALOG: " + rs1.getString("TABLE_CAT"));
//                System.out.println("TABLE_CAT: " + rs1.getString(1));
            }

            // 获取指定库所有数据表
            /*
             * catalog: 指定数据库名称
             * schemaPattern: 对于mysql来说直接传null，对于oracle：用户名（大写）
             * tableNamePattern: 查询指定表，为空时查询所有表
             * types: 查询数据库表类型：TABLE（表）、VIEW（视图）
             */
            //metaData.getTables(catalog, schemaPattern, tableNamePattern, types);
            System.out.println("===========获取所有表=============");
            ResultSet rs2 = databaseMetaData.getTables("gbase", "gbase", null, new String[] {"TABLE"});
            while (rs2.next()) {
                System.out.println("TABLE_CAT: " + rs2.getString("TABLE_CAT"));
                System.out.println("TABLE_SCHEM: " + rs2.getString("TABLE_SCHEM"));
                System.out.println("TABLE_NAME: " + rs2.getString("TABLE_NAME"));
                System.out.println("TABLE_TYPE: " + rs2.getString("TABLE_TYPE"));
                System.out.println("REMARKS: " + rs2.getString("REMARKS"));
            }
            System.out.println("获取视图");
            // 获取所有数据表字段
            /*
             * catalog: 指定数据库名称
             * schemaPattern: 对于mysql来说直接传null，对于oracle：用户名（大写）
             * tableNamePattern: 查询指定表，为空时查询所有表
             * columnNamePattern: 列名称，为空查询所有列
             */
            // getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
            //ResultSet rs3 = databaseMetaData.getFunctions(null, "test" ,null);
            System.out.println("===========获取所有存储过程===========");
            ResultSet rs4 = databaseMetaData.getProcedures("gbase", "gbase", null);

            System.out.println("===========获取所有列===========");
            ResultSet rs = databaseMetaData.getColumns("gbase", "gbase", "t_user", null);
            while (rs.next()) {
                System.out.println("TABLE_CAT：" + rs.getString("TABLE_CAT"));
                System.out.println("TABLE_SCHEM：" + rs.getString("TABLE_SCHEM"));
                System.out.println("TABLE_NAME：" + rs.getString("TABLE_NAME"));
                System.out.println("COLUMN_NAME：" + rs.getString("COLUMN_NAME"));
                System.out.println("DATA_TYPE：" + rs.getString("DATA_TYPE"));
                System.out.println("TYPE_NAME：" + rs.getString("TYPE_NAME"));
                System.out.println("COLUMN_SIZE：" + rs.getString("COLUMN_SIZE"));
                System.out.println("BUFFER_LENGTH：" + rs.getString("BUFFER_LENGTH"));
                System.out.println("DECIMAL_DIGITS：" + rs.getString("DECIMAL_DIGITS"));
                System.out.println("NUM_PREC_RADIX：" + rs.getString("NUM_PREC_RADIX"));
                System.out.println("NULLABLE：" + rs.getString("NULLABLE"));
                System.out.println("REMARKS：" + rs.getString("REMARKS"));
                System.out.println("COLUMN_DEF：" + rs.getString("COLUMN_DEF"));
                System.out.println("SQL_DATA_TYPE：" + rs.getString("SQL_DATA_TYPE"));
                System.out.println("SQL_DATETIME_SUB：" + rs.getString("SQL_DATETIME_SUB"));
                System.out.println("CHAR_OCTET_LENGTH：" + rs.getString("CHAR_OCTET_LENGTH"));
                System.out.println("ORDINAL_POSITION：" + rs.getString("ORDINAL_POSITION"));
                System.out.println("IS_NULLABLE：" + rs.getString("IS_NULLABLE"));
//                System.out.println("SCOPE_CATALOG：" + rs.getString("SCOPE_CATALOG"));
                System.out.println("SCOPE_SCHEMA：" + rs.getString("SCOPE_SCHEMA"));
                System.out.println("SCOPE_TABLE：" + rs.getString("SCOPE_TABLE"));
                System.out.println("SOURCE_DATA_TYPE：" + rs.getString("SOURCE_DATA_TYPE"));
                System.out.println("IS_AUTOINCREMENT：" + rs.getString("IS_AUTOINCREMENT"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUser(){
        UserEntity user = userService.getById(1);
        System.out.println(user);
    }

    @Test
    public void commit() throws SQLException {
        DbaseEntity dbaseEntity = new DbaseEntity();
        String mysql =  "jdbc:mysql://itachisan.mysql.rds.aliyuncs.com:3306/demo";
        String port = "3306";
        String dbName = "demo";
        String name = "naruto";
        String password = "itachi@123";
        List<String> sqls = new ArrayList<>();
        String sql1 = "";
//        String password = "aXRhY2hpQDEyMw==";
//        String dbType = "MySql";
//        dbaseEntity.setDbName(dbName);
//        dbaseEntity.setHost(mysql);
//        dbaseEntity.setPort(port);
//        dbaseEntity.setUsername(name);
//        dbaseEntity.setPassword(password);
//        dbaseEntity.setDbType(dbType);
//        BusiDataBaseUtil db = new BusiDataBaseUtil(dbaseEntity);
        for (int i = 1;i<5;i++){
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection conn = DriverManager.getConnection(mysql,name,password);
//                conn.setNetworkTimeout();
                connMap.put("conn" + i,conn);
                System.out.println("conn"+i+":"+conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (int i = 1;i<5;i++){
            try {
                Connection conn = connMap.get("conn" + i);
                System.out.println("conn"+i+":"+conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Connection conn1 = connMap.get("conn1");
        Connection conn2 = connMap.get("conn2");

        Statement stmt1 = null;
        ResultSet rs1 = null;
        Statement stmt2 = null;
        ResultSet rs2 = null;
        try {
            conn1.setAutoCommit(false);
            stmt1 = conn1.createStatement();
            String insertSql = "INSERT INTO `demo`.`usertwo` (`id`,`name`, `password`, `createtime`) VALUES (20,'qe222', '123', '2022-04-07 22:52:12');";
            String selectSql = "select * from `usertwo`;";
            stmt1.executeUpdate(insertSql);
            rs1 = stmt1.executeQuery(selectSql);
            Map<String, Object> row = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            while (rs1.next())
            {
                System.out.println(rs1.getString(1) + "," + rs1.getString(2));
            }
        } catch (SQLException e) {
            conn1.rollback();
            e.printStackTrace();
        }

        try {
            stmt2 = conn2.createStatement();
//            conn2.setAutoCommit(false);
//            String insertSql = "INSERT INTO `demo`.`usertwo` (`id`,`name`, `password`, `createtime`) VALUES (20,'qe222', '123', '2022-04-07 22:52:12');";
            String selectSql = "select * from `usertwo`;";
//            stmt2.executeUpdate(insertSql);
            rs2 = stmt2.executeQuery(selectSql);
            Map<String, Object> row = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            while (rs2.next())
            {
                System.out.println(rs2.getString(1) + "," + rs2.getString(2));
            }
        } catch (SQLException e) {
            conn2.rollback();
            e.printStackTrace();
        }

        Connection conn3 = connMap.get("conn1");
        System.out.println(conn3.getAutoCommit());
    }
//    @Test
//    public void testMenu(){
////        getRouters();
//    }
    @Test
    public void testGetRoleMenu(){
        QueryWrapper<RoleMenuEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id",1);
        List<RoleMenuEntity> roleMenuEntities = roleMenuService.list(queryWrapper);
        List<Integer> menuCheckedIds = new ArrayList<>();
        for (RoleMenuEntity roleMenuEntity:roleMenuEntities){
            System.out.println(JSON.toJSONString(roleMenuEntity));
            menuCheckedIds.add(roleMenuEntity.getMenuId());
        }
        System.out.println(JSON.toJSONString(menuCheckedIds));
    }

//    @Test
//    public void testRemoveMenu(){
//        QueryWrapper<RoleMenuEntity> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("role_id", 9);
//        RoleMenuEntity roleMenuEntity = new RoleMenuEntity();
//        roleMenuEntity.setRoleId(9);
//        roleMenuService.remove(queryWrapper);
//    }

    @Test
    public void testString(){
        String str = "ksdskja";
        str = str.replace("k","a");
        System.out.println(str);
    }


//    @Test
//    @Transactional
//    public void testUpdate(){
//        QueryWrapper<ScriptsUserEntity> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("scripts_id",1);
//
//        ScriptsUserEntity scriptsUserEntity = new ScriptsUserEntity();
//        scriptsUserEntity.setIsDelete(1);
//        boolean flag = scriptsUserService.update(scriptsUserEntity,queryWrapper);
//        System.out.println(flag);
//        System.out.println(JSON.toJSONString(scriptsUserService.list()));
//    }
//
//    @Test
//    public void testQueryScript(){
//        QueryWrapper<ScriptsUserEntity> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("user_id",1);
//        queryWrapper.eq("is_delete",0);
////        ScriptsUserEntity scriptsUserEntity = new ScriptsUserEntity();
////        scriptsUserEntity.setIsDelete(1);
////        boolean flag = scriptsUserService.update(scriptsUserEntity,queryWrapper);
////        System.out.println(flag);
////        List<ScriptsUserEntity> scriptsUserEntities = scriptsUserDao.selectList(queryWrapper);
//        List<ScriptsUserEntity> scriptsUserEntities = scriptsUserDao.selectList(
//                new QueryWrapper<ScriptsUserEntity>()
//                        .eq("user_id", 1)
//                        .eq("is_delete",0)
//        );
//        System.out.println(JSON.toJSONString(scriptsUserEntities));
//        System.out.println(scriptsUserEntities.size());
//
//    }
}
