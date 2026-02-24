import com.dbms.DemoApplication;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellScanner;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
@Transactional
public class BigDataDemoHbase {

    @Test
    public void testHbase() {

        String tableName = "{\"USER\":{\"USER_NAME\":[\"UPDATE\"],\"PASSWORD\":[\"UPDATE\"]}}";
        tableName.replace("\\","");
        tableName.replace("\"","");
        System.out.println(tableName);

        Configuration config = null;
        Connection conn = null;
        Table table = null;
        System.out.println("======hello hbase======");
        // 创建配置

        config = HBaseConfiguration.create();
        System.out.println("======create===========");
//        config.set("hbase.rootdir","hdfs://101.43.142.72:9000/hbase");
//        config.set("hbase.zookeeper.quorum", "101.43.142.72");
//        config.set("hbase.zookeeper.property.clientPort", "2181");


        config.set("hbase.zookeeper.quorum", "101.43.142.72");
        config.set("hbase.zookeeper.property.clientPort", "2181");
        try {
            // 创建连接
            System.out.println("======创建连接==========");
            conn = ConnectionFactory.createConnection(config);
            // 获取表
            System.out.println("======获取表==========");
            table = conn.getTable(TableName.valueOf("fruits"));

            // 查询指定表的全部数据
//            queryAllTableData(table);

            // 查询指定rowkey的数据
            queryRowKey(table);
            // 略。。。

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

    /**
     * 查询指定rowkey的数据
     */
    public static void queryRowKey(Table table) {
        try {
            // get对象指定行键
            Get get = new Get("1".getBytes(StandardCharsets.UTF_8));

            Result result = table.get(get);

            System.out.printf("|%10s|%10s|%10s|%10s|\n", "row key", "family", "qualifier", "value");
            output(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 输出
     * @param result
     * @throws IOException
     */
    private static void output(Result result) throws IOException {
        CellScanner cellScanner = result.cellScanner();
        while (cellScanner.advance()) {
            Cell cell = cellScanner.current();
            byte[] rowArray = cell.getRowArray();  //本kv所属的行键的字节数组
            byte[] familyArray = cell.getFamilyArray();  //列族名的字节数组
            byte[] qualifierArray = cell.getQualifierArray();  //列名的字节数据
            byte[] valueArray = cell.getValueArray(); // value的字节数组

            System.out.printf("|%10s|%10s|%10s|%10s|\n",
                    new String(rowArray, cell.getRowOffset(), cell.getRowLength()),
                    new String(familyArray, cell.getFamilyOffset(), cell.getFamilyLength()),
                    new String(qualifierArray, cell.getQualifierOffset(), cell.getQualifierLength()),
                    new String(valueArray, cell.getValueOffset(), cell.getValueLength()));
        }
    }

}
