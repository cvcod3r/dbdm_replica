import com.dbms.DemoApplication;
import com.github.CCweixiao.hbase.sdk.AbstractHBaseSqlTemplate;
import com.github.CCweixiao.hbase.sdk.HBaseSqlTemplate;
import com.github.CCweixiao.hbase.sdk.template.IHBaseAdminTemplate;
import com.github.CCweixiao.hbase.sdk.template.impl.HBaseAdminTemplateImpl;
import com.github.CCwexiao.hbase.sdk.dsl.antlr.HBaseSQLParser;
import com.github.CCwexiao.hbase.sdk.dsl.manual.HBaseSqlAnalysisUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellScanner;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
@Transactional
public class HbaseSQLTest {

    @Test
    public void testHbaseSQL() throws Exception{

        // 普通认证
        Properties properties = new Properties();
        properties.setProperty("hbase.zookeeper.quorum", "101.43.142.72");
        properties.setProperty("hbase.zookeeper.property.clientPort", "2181");

        IHBaseAdminTemplate adminTemplate = new HBaseAdminTemplateImpl.Builder().properties(properties).build();
        System.out.println(adminTemplate.listTableNames());


    }
}
