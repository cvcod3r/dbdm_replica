import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLValuableExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.hive.visitor.HiveSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlExportParameterVisitor;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.parser.SQLParser;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;
import com.dbms.core.SQLParserUtil;
import com.dbms.core.dbase.impl.DruidSQLParserImpl;
import com.sun.jna.platform.win32.DBT;
import org.aspectj.weaver.ast.ASTNode;
import org.junit.jupiter.api.Test;
import com.alibaba.druid.util.Utils;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.*;

public class DruidTest {
    /**
     * hive-sql-parser versus druid-sql-parser
     */
    @Test
    public void testDruidSqlParser() throws Exception {
//        String sql = "select userInfo.username as a,userInfo.password as b,foodInfo.id as c,foodInfo.name as d from dbo.userInfo user,dbo.foodInfo food;";//别名查询测试
//        String sql = "INSERT INTO Websites (name, url, alexa, country) VALUES ('百度','https://www.baidu.com/','4','CN'), ('新浪','https://www.xinlang.com/','5','CN');";//插入测试
//        String sql = "select a.id, a.name FROM users a, money b where a.id=b.id and a.id in (select id from innertables)";//子查询
//        String sql = "select id, name from user, class where user.id = 100 and user.name='jluo' and class.id = 1;";//条件查询
//        String sql = "insert into table student_ptn select id,name,sex,age,department from student where department='MA'"; //单重插入测试
//        String sql = "SELECT * FROM A WHERE A.a IN (SELECT foo FROM B);";//子查询

        String sql = "select userInfo.username as a from dbo.userInfo;";
        SQLStatement statement = SQLUtils.parseSingleStatement(sql, DbType.hive);
        HiveSchemaStatVisitor visitor = new HiveSchemaStatVisitor();
        MySqlExportParameterVisitor pVisitor= new MySqlExportParameterVisitor();
        statement.accept(visitor);
        statement.accept(pVisitor);
//        System.out.println(visitor.getTables());//表名
//        System.out.println(visitor.getColumns());//列名
//        System.out.println(visitor.getConditions());//where
//        System.out.println(pVisitor.getParameters());//values


        DruidSQLParserImpl test = new DruidSQLParserImpl();
//        HashMap<String, String> talbeMap = test.getTableMap(sql, test.getSqlType(sql));//原方法：<表别名， 表名>
//        System.out.println(talbeMap);
//        HashMap<String, String> tableMapAndSQLType = test.getTableMapAndSQLType(sql, test.getSqlType(sql));//<表全名， 操作类型>
//        System.out.println(tableMapAndSQLType);
        HashMap<String, ArrayList<Map<String, String>>> tableColumnMap = test.getTableColumnMap(sql, test.getSqlType(sql));//原方法：<列名，<列别名><列全名><列名>>
        System.out.println(tableColumnMap);
//        HashMap<Integer, Map<String, String>> insertValuesMap = test.getInsertValuesMap(sql);//<要插入条目编号，<所插入条目的列属性， 插入值>>
//        System.out.println(insertValuesMap);
//        HashMap<String, String> tableConditionsMap = test.getConditionsMap(sql);<列属性， 判断值>
//        System.out.println(tableConditionsMap);//判断条件map
//        Map<String, ArrayList<String>> tableColumnList = test.getTableColumnList(sql, test.getSqlType(sql));
//        System.out.println(tableColumnList);

        String JParsersql = "select username from userInfo";
        HashMap<String, ArrayList<Map<String, String>>> JParserTableColumnMap = SQLParserUtil.getTableColumnMap(JParsersql, SQLParserUtil.getSqlType(JParsersql));
//        System.out.println(JParserTableColumnMap);
    }
}
