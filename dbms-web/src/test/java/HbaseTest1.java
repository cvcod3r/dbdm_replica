import com.alibaba.fastjson.JSON;
import com.dbms.DemoApplication;
import com.dbms.core.dbase.HBaseShellMeta;
import com.dbms.core.dbase.IHBaseShellParser;
import com.dbms.core.dbase.impl.HBaseSQLParserImpl;
import com.dbms.core.dbase.impl.HBaseShellParserImpl;
import com.github.CCweixiao.hbase.sdk.AbstractHBaseSqlTemplate;
import com.github.CCweixiao.hbase.sdk.HBaseSqlTemplate;
import com.github.CCweixiao.hbase.sdk.common.model.ColumnFamilyDesc;
import com.github.CCweixiao.hbase.sdk.common.model.HTableDesc;
import com.github.CCweixiao.hbase.sdk.common.type.ColumnType;
import com.github.CCweixiao.hbase.sdk.template.IHBaseAdminTemplate;
import com.github.CCweixiao.hbase.sdk.template.impl.HBaseAdminTemplateImpl;
import com.github.CCwexiao.hbase.sdk.dsl.antlr.HBaseSQLParser;
import com.github.CCwexiao.hbase.sdk.dsl.context.HBaseSqlContext;
import com.github.CCwexiao.hbase.sdk.dsl.manual.HBaseSqlAnalysisUtil;
import com.github.CCwexiao.hbase.sdk.dsl.model.HBaseTableSchema;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.checkerframework.checker.units.qual.A;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.sql.Connection;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
public class HbaseTest1 {

    @Autowired
    IHBaseShellParser hBaseShellParser;

    @Test
    public void testHbase() throws Exception{

        // 普通认证
        Properties properties = new Properties();
        properties.setProperty("hbase.zookeeper.quorum", "101.43.142.72:2181");
//        properties.setProperty("hbase.zookeeper.property.clientPort", "2181");

        IHBaseAdminTemplate adminTemplate = new HBaseAdminTemplateImpl.Builder().properties(properties).build();

        System.out.println(adminTemplate.listTableNames());

//        String hql = "";
//        Properties properties1 = new Properties();
//        HBaseSQLParserImpl hBaseSQLParser = new HBaseSQLParserImpl(properties1);
//        hBaseSQLParser.test(hql);

        // 2. 创建HBaseTableSchema
        HBaseTableSchema tableSchema1 = new HBaseTableSchema.Builder("test:test_sql")
                .addColumn("f1", "id")
                .addColumn("f1", "name")
                .addColumn("f1", "age", ColumnType.IntegerType)
                .addColumn("f2", "address")
                .addRow("row_key")
                .scanBatch(100)
                .scanCaching(1000)
                .deleteBatch(100)
                .scanCacheBlocks(false)
                .build();



        // 3. 注册HBaseTableSchema至HBaseSqlContext中
        HBaseSqlContext.registerTableSchema(tableSchema1);

//         tableSchema.printSchema();
//        String hql = "select * from test:test_sql where rowKey = 'a10001'";
//        String hql = "insert into test:test_sql ( f1:id , f1:name , f1:age , f2:address ) values ( '10001' , 'a_leo' , 15 , 'bj' ) where rowKey = 'a10001'";
        String hql = "delete f1:age from test:test_sql where rowKey = 'b20004'";
        Properties p = new Properties();
        HBaseSQLParserImpl test = new HBaseSQLParserImpl(p);
        Map tableMap = test.getTableMap(hql , test.getSqlType(hql));
        HashMap<String, ArrayList<Map<String, String>>> tableColumnMap = test.getTableColumnMap(hql, test.getSqlType(hql));
        System.out.println(tableMap);
        System.out.println('\n');
        System.out.println(tableColumnMap);
    }

    @Test
    public void testPhoenix(){
        List<String> tableList = new ArrayList<>();
        Connection conn;
        try {
            Class.forName("org.apache.phoenix.jdbc.PhoenixDriver"); System.out.println("数据库驱动加载成功");
        } catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        try {
            conn = DriverManager.getConnection("jdbc:phoenix:101.43.142.72:2181", "hbase", "hbase");
            System.out.println(conn);
            System.out.println("===========获取所有表=============");
            DatabaseMetaData databaseMetaData = null;
            try {
                assert conn != null;
                databaseMetaData = conn.getMetaData();
                ResultSet rs = null;
                rs = databaseMetaData.getTables(null, null, null, new String[] {"TABLE"});

                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    tableList.add(tableName);
                }
                rs.close();
                conn.close();
            } catch (SQLException e) {

            }
            System.out.println(JSON.toJSONString(tableList));
            System.out.println("数据库连接成功");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHbaseShell() throws Exception{

        // 普通认证
        Properties properties = new Properties();
        properties.setProperty("hbase.zookeeper.quorum", "101.43.142.72:2181");
//        properties.setProperty("hbase.zookeeper.property.clientPort", "2181");

        IHBaseAdminTemplate adminTemplate = new HBaseAdminTemplateImpl.Builder().properties(properties).build();

        String shell = "create 'student', 'info', 'data'";
        HBaseShellMeta shellMeta = hBaseShellParser.getHBaseShellMeta(shell);
        System.out.println(shellMeta.toString());
//        adminTemplate.createTable(shellMeta.getHTableDesc());
        String createShell2 = "create 'stu', {NAME => 'f1', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', VERSIONS => '3', COMPRESSION => 'NONE', MIN_VERSIONS => '0', TTL => '2147483647', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65536', IN_MEMORY => 'false', BLOCKCACHE => 'true'}, " +
                "{NAME => 'f2', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', VERSIONS => '1', COMPRESSION => 'NONE', MIN_VERSIONS => '0', TTL => '2147483647', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65536', IN_MEMORY => 'false', BLOCKCACHE => 'true'}";

        HBaseShellMeta shellMeta1 = hBaseShellParser.getHBaseShellMeta(createShell2);
        System.out.println(shellMeta1.getColumnFamily());
        TableDescriptor tableDescriptor = shellMeta1.getTableDescriptor();
//        List<ColumnFamilyDesc> columnFamilyDescs = shellMeta1.getHTableDesc().getColumnFamilyDescList();
        System.out.println(JSON.toJSONString(tableDescriptor));
        System.out.println(adminTemplate.listTableNames());

    }


    @Test
    public void testHbaseShell_PUT() throws Exception{
        String shell = "put 'customer','2','addr:city','nanjing'";
        HBaseShellMeta shellMeta = hBaseShellParser.getHBaseShellMeta(shell);
        System.out.println("********************");
        System.out.println(shellMeta.getPut());
        System.out.println(shellMeta.getPutValue());
        System.out.println(shellMeta.getColumnFamily());
        System.out.println("********************");
    }

    @Test
    public void testHbaseShell_CREATE() throws Exception{
        String shell = "create 'student', 'info', 'data'";
        HBaseShellMeta shellMeta = hBaseShellParser.getHBaseShellMeta(shell);
        System.out.println("********************");
        System.out.println(shellMeta.getTableName());
        System.out.println(shellMeta.getColumnFamily());
        System.out.println("********************");
    }

    @Test
    public void testHbaseShell_SCAN() throws Exception{
//        String shell = "scan 'my_test'";
//        String shell = "scan 'my_test',{COLUMNS => ['f1:a1','f1:b1','f2:c1']}";
//        String shell = "scan 'my_test',{COLUMNS => 'f1:b1'}";
//        String shell = "scan 'my_test',{TIMERANGE=>[1548128540,1548128614]}";
//        String shell = "scan 'my_test',{STARTROW=>'u1_td2',STOPROW=>'u2_td5'}";
//        String shell = "scan 'my_test',{ROWPREFIXFILTER => 'u2'}";
//        String shell = "scan 'my_test',{FILTER => \"PrefixFilter('u2')\"}";
        String shell = "scan 'my_test',{FILTER=>\"ColumnPrefixFilter('a') AND (ValueFilter(=,'substring:9') OR ValueFilter(=,'substring:2'))\"}";
//        String shell = "scan 'my_test',{FILTER => \"(QualifierFilter (<=,'binary:b1')) AND (QualifierFilter (=,'substring:1'))\"}";
//        String shell = "scan 'my_test',{FILTER => \"(QualifierFilter (<=,'binary:b1'))\"}";
//        String shell = "scan 'my_test',{FILTER=>\"ValueFilter(=,'binary:abc1')\"}";
//        String shell = "scan 'stu',{FILTER => \"TimestampsFilter(1588153968,1588157990)\"}";
//        String shell = "scan 'my_test',{TIMERANGE=>[1548128540,1548128614],REVERSED => true}";
//        String shell = "scan 'my_test',{RAW => true,VERSIONS => 2}";
//        String shell = "scan 'test001', {LIMIT => 123}";
        HBaseShellMeta shellMeta = hBaseShellParser.getHBaseShellMeta(shell);
        Scan scan = shellMeta.getScan();
        System.out.println("*****************************");
        System.out.println(shellMeta.getTableName());
        System.out.println(scan);
        System.out.println("*****************************");
    }

    @Test
    public void testHbaseShell_TRUNCATE() throws Exception{
        String shell = "truncate 'ns1:t1'";
        HBaseShellMeta shellMeta = hBaseShellParser.getHBaseShellMeta(shell);
        System.out.println("*****************************");
        System.out.println(shellMeta.getTableName());
        System.out.println("*****************************");
    }

    @Test
    public void testHbaseShell_DISABLE() throws Exception{
        String shell = "disable 'student'";
        HBaseShellMeta shellMeta = hBaseShellParser.getHBaseShellMeta(shell);
        System.out.println("********************");
        System.out.println(shellMeta.getTableName());
        System.out.println("********************");
    }

    @Test
    public void testHbaseShell_DESC() throws Exception{
//        String shell = "describe 'student'";
        String shell = "desc 'student'";
        HBaseShellMeta shellMeta = hBaseShellParser.getHBaseShellMeta(shell);
        System.out.println("********************");
        System.out.println(shellMeta.getTableName());
        System.out.println("********************");
    }
    @Test
    public void testHbaseShell_EXIST() throws Exception{
        String shell = "exist 'student'";
        HBaseShellMeta shellMeta = hBaseShellParser.getHBaseShellMeta(shell);
        System.out.println("********************");
        System.out.println(shellMeta.getTableName());
        System.out.println();
        System.out.println("********************");
    }
    @Test
    public void testHbaseShell_ALTER() throws Exception{
//        增加列簇
//        String shell = "alter 'customer','addr'";
//        String shell = "alter 'customer',NAME=>'sample'";
//        删除列簇
        String shell = "alter 'customer',{NAME=>'addr',METHOD=>'delete'}";
//        更改版本限制
//        String shell = "alter 'customer',{NAME=>'addr',VERSIONS=>3}";
//        保留已删除单元格
//        String shell = "alter 'customer',{NAME=>'addr',KEEP_DELETED_CELLS=>true}";
        HBaseShellMeta shellMeta = hBaseShellParser.getHBaseShellMeta(shell);
        System.out.println("********************");
        System.out.println(shellMeta.getTableName());
        System.out.println(shellMeta.getColumnFamily());
        System.out.println(shellMeta.isAlterDelete());
        System.out.println(shellMeta.getColumnFamilyDescriptors());
        System.out.println("********************");
//        HBaseUtilServiceImpl hBaseUtilService = new HBaseUtilServiceImpl();
//        Connection conn;
//        conn = DriverManager.getConnection("jdbc:phoenix:101.43.142.72:2181", "hbase", "hbase");
//        hBaseUtilService.alterData(shell, String.valueOf(conn));
    }
    @Test
    public void testHbaseShell_DROP() throws Exception{
        String shell = "drop '123'";
        HBaseShellMeta shellMeta = hBaseShellParser.getHBaseShellMeta(shell);
        System.out.println("********************");
        System.out.println(shellMeta.getTableName());
        System.out.println(shellMeta.getSqlType());
        //System.out.println(shellMeta.getPutValue());
        System.out.println("********************");
    }

    @Test
    public void testHbaseShell_GET() throws Exception{

//        String shell = "get 't1','r1',{TIMESTAMP=>1}";
        String shell = "get 'customer','1',{COLUMN=>'addr:city',VERSIONS=>1}";
//        String shell = "get 'user', 'rk0001', {FILTER => \"ValueFilter(=, 'binary:zhangsan')\"}";
//        String shell = "get 'my_test','rk0001',{FILTER => \"(QualifierFilter (<=,'binary:b1')) AND (QualifierFilter (=,'substring:1'))\"}";
//        String shell = "scan 'my_test','rk0001',{FILTER => \"(QualifierFilter (<=,'binary:b1'))\"}";
        HBaseShellMeta shellMeta = hBaseShellParser.getHBaseShellMeta(shell);
        Get get = shellMeta.getGet();
        System.out.println(get);
        System.out.println("*****************************");
        System.out.println(shellMeta.getColumnFamily());
        System.out.println("*****************************");
    }

    @Test
    public void testHbaseCreateNameSpace() throws Exception{
        String shell = "create_namespace 'ns1'";
        HBaseShellMeta shellMeta = hBaseShellParser.getHBaseShellMeta(shell);
        System.out.println(shellMeta.getNameSpace());
    }

    @Test
    public void testHbaseGrant() throws Exception{
        String shell = "grant 'root','RWXCA','@TRAF_1500000'";
        String shell1 = "grant 'root','RWXCA','agatha'";
        String shell2 = "grant 'root','RWXCA'";
        HBaseShellMeta shellMeta = hBaseShellParser.getHBaseShellMeta(shell);
        if(shellMeta.isNsOrTb()){
            System.out.println(shellMeta.getNameSpace());
        }
        else{
            System.out.println(shellMeta.getTableName());
        }
        System.out.println(shellMeta.getUserName());
        System.out.println(shellMeta.getActions());
    }

    @Test
    public void testHbaseRevoke() throws Exception{
        String shell = "revoke 'root' '@TRAF_1500000'";
        String shell1 = "revoke 'root' 'agatha'";
        String shell2 = "revoke 'root'";
        HBaseShellMeta shellMeta = hBaseShellParser.getHBaseShellMeta(shell);
        if(shellMeta.isNsOrTb()){
            System.out.println(shellMeta.getNameSpace());
        }
        else{
            System.out.println(shellMeta.getTableName());
        }
        System.out.println(shellMeta.getUserName());
    }

    @Test
    public void testHbaseConnShell() throws Exception{

        String shell = "create 'student', 'info', 'data'";
        HBaseShellMeta shellMeta = hBaseShellParser.getHBaseShellMeta(shell);
        System.out.println(shellMeta.toString());
        String createShell2 = "create 'stu', {NAME => 'f1', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', VERSIONS => '1', COMPRESSION => 'NONE', MIN_VERSIONS => '0', TTL => '2147483647', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65536', IN_MEMORY => 'false', BLOCKCACHE => 'true'}, " +
                "{NAME => 'f2', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', VERSIONS => '1', COMPRESSION => 'NONE', MIN_VERSIONS => '0', TTL => '2147483647', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65536', IN_MEMORY => 'false', BLOCKCACHE => 'true'}";

        HBaseShellMeta shellMeta1 = hBaseShellParser.getHBaseShellMeta(createShell2);
        System.out.println(shellMeta1.getColumnFamily());
        List<ColumnFamilyDesc> columnFamilyDescs = shellMeta1.getHTableDesc().getColumnFamilyDescList();
        System.out.println(columnFamilyDescs.toString());
        // 普通认证
        Configuration config = null;
        org.apache.hadoop.hbase.client.Connection conn = null;
        Table table = null;
        System.out.println("======hello hbase======");
        // 创建配置
        config = HBaseConfiguration.create();
        System.out.println("======create===========");
        config.set("hbase.zookeeper.quorum", "101.43.142.72");
        config.set("hbase.zookeeper.property.clientPort", "2181");
        try {
            // 创建连接
            System.out.println("======创建连接==========");
            conn = ConnectionFactory.createConnection(config);
            // 获取表
            System.out.println("======获取表==========");
//            HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf("stu"));
//            TableDescriptor tableDescriptor = TableDescriptorBuilder.newBuilder(TableName.valueOf("student"))
//                            .setColumnFamilies()
////            tableDescriptor.addFamily(new HColumnDescriptor("per"));
////            tableDescriptor.addFamily(new HColumnDescriptor(("pro")));
//            Admin admin = conn.getAdmin();
//            admin.createTable(tableDescriptor);
//            table = conn.getTable(TableName.valueOf("fruits"));

            // 查询指定表的全部数据
//            queryAllTableData(table);


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (table != null) {
                    table.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }



}
