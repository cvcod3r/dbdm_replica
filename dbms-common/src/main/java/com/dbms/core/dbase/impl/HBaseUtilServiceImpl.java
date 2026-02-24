package com.dbms.core.dbase.impl;

import com.alibaba.fastjson.JSON;
import com.dbms.core.dbase.HBaseShellMeta;
import com.dbms.core.dbase.IHBaseShellParser;
import com.dbms.core.dbase.IHBaseUtilService;
import com.dbms.entity.DbaseEntity;
import com.github.CCweixiao.hbase.sdk.template.IHBaseAdminTemplate;
import com.github.CCweixiao.hbase.sdk.template.IHBaseSqlTemplate;
import com.github.CCweixiao.hbase.sdk.template.IHBaseTableTemplate;
import com.github.CCweixiao.hbase.sdk.template.impl.HBaseAdminTemplateImpl;
import com.github.CCweixiao.hbase.sdk.template.impl.HBaseSqlTemplateImpl;
import com.github.CCweixiao.hbase.sdk.template.impl.HBaseTableTemplateImpl;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.security.access.AccessControlClient;
import org.apache.hadoop.hbase.security.access.Permission;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class HBaseUtilServiceImpl implements IHBaseUtilService {
    private static final Logger logger = LoggerFactory.getLogger(DataBaseUtilServiceImpl.class);

    public static final Map<String, Connection> connMap = new ConcurrentHashMap<>();

    @Autowired
    private IHBaseShellParser hbaseShellParser;

    @Override
    public IHBaseAdminTemplate getHBaseAdminTemplate(DbaseEntity dbaseEntity) {
        Properties properties = new Properties();
        properties.setProperty("hbase.zookeeper.quorum", dbaseEntity.getHost());
        properties.setProperty("hbase.zookeeper.property.clientPort", dbaseEntity.getPort());
        IHBaseAdminTemplate adminTemplate = new HBaseAdminTemplateImpl.Builder().properties(properties).build();
        return adminTemplate;
    }

    @Override
    public IHBaseTableTemplate getHBaseTableTemplate(DbaseEntity dbaseEntity) {
        Properties properties = new Properties();
        properties.setProperty("hbase.zookeeper.quorum", dbaseEntity.getHost());
        properties.setProperty("hbase.zookeeper.property.clientPort", dbaseEntity.getPort());
        IHBaseTableTemplate tableTemplate = new HBaseTableTemplateImpl.Builder().properties(properties).build();
        return tableTemplate;
    }

    @Override
    public IHBaseSqlTemplate getHBaseSqlTemplate(DbaseEntity dbaseEntity) {
        Properties properties = new Properties();
        properties.setProperty("hbase.zookeeper.quorum", dbaseEntity.getHost());
        properties.setProperty("hbase.zookeeper.property.clientPort", dbaseEntity.getPort());
        IHBaseSqlTemplate sqlTemplate = new HBaseSqlTemplateImpl.Builder().properties(properties).build();
        return sqlTemplate;
    }

    @Override
    public Connection getConnection(DbaseEntity dbaseEntity) throws Exception{
        Connection connection = null;
        try{
            Configuration configuration = HBaseConfiguration.create();
            configuration.set("hbase.zookeeper.quorum", dbaseEntity.getHost());
            configuration.set("hbase.zookeeper.property.clientPort", dbaseEntity.getPort());
            connection = ConnectionFactory.createConnection(configuration);
        } catch (Exception e){
            throw new Exception("获取HBase Client连接失败!");
        }
        return connection;
    }

    @Override
    public Connection getConnection(DbaseEntity dbaseEntity, String connKey) throws Exception{
        Connection connection = null;
        try{
            connection = connMap.getOrDefault(connKey, null);
            if (connection == null){
                Configuration configuration = HBaseConfiguration.create();
                configuration.set("hbase.zookeeper.quorum", dbaseEntity.getHost());
                configuration.set("hbase.zookeeper.property.clientPort", dbaseEntity.getPort());
                connection = ConnectionFactory.createConnection(configuration);
                System.out.println("获取新的连接" + connKey);
                connMap.put(connKey, connection);
            }
        } catch (Exception e){
            throw new Exception("获取HBase Client连接失败!");
        }
        return connection;
    }

    @Override
    public Connection getConnectionByKey(String connKey) throws Exception {
        Connection connection = connMap.getOrDefault(connKey, null);
        if (connection == null){
            throw new Exception("HBase连接会话已关闭，请尝试重新连接！");
        }
        return connection;
    }

    @Override
    public void createTable(String sql, String connKey) throws Exception {
        Connection connection = getConnectionByKey(connKey);
        try{
            System.out.println("====创建表====");
            HBaseShellMeta shellMeta = hbaseShellParser.getHBaseShellMeta(sql);
            Admin admin = connection.getAdmin();
            admin.createTable(shellMeta.getTableDescriptor());
        }catch (Exception e){
            throw new Exception("create 执行失败！");
        }
    }

    @Override
    public void closeConn(String connKey) throws Exception{
        try {
            Connection conn = getConnectionByKey(connKey);
            if (conn != null){
                conn.close();
                connMap.remove(connKey);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Map<String, List<String>> getTables(String connKey) throws Exception {
        Map<String, List<String>> tableMap = new HashMap<>();
        Connection connection = getConnectionByKey(connKey);
        Admin admin = connection.getAdmin();
        NamespaceDescriptor[] namespaceDescriptors = admin.listNamespaceDescriptors();
        for (NamespaceDescriptor namespace : namespaceDescriptors){
            TableName[] tableNames = admin.listTableNamesByNamespace(namespace.getName());
            List<String> tables = new ArrayList<>();
            for (TableName tableName:tableNames){
                tables.add(tableName.getNameAsString());
            }
            tableMap.put(namespace.getName(), tables);
        }
        return tableMap;
    }

    @Override
    public List<Map<String, Object>> listNameSpaces(String connKey) throws Exception{
        List<Map<String, Object>> resultList = new ArrayList<>();
        Connection connection = getConnectionByKey(connKey);
        Admin admin = connection.getAdmin();
        NamespaceDescriptor[] namespaceDescriptors = admin.listNamespaceDescriptors();
        for (NamespaceDescriptor namespace : namespaceDescriptors){
            Map<String, Object> map = new HashMap<>();
            map.put("NAMESPACE", namespace.getName());
            resultList.add(map);
        }
        return resultList;
    }

    @Override
    public List<Map<String, Object>> listTable(String sql, String connKey) throws Exception {
        System.out.println("查询表");
        List<Map<String, Object>> resultList = new ArrayList<>();
        Connection connection = getConnectionByKey(connKey);
        Admin admin = connection.getAdmin();
        TableName[] tableNames = admin.listTableNames();
        for (TableName tableName:tableNames){
            Map<String, Object> map = new HashMap<>();
            map.put("TABLE", tableName.getNameAsString());
            resultList.add(map);
        }
        return resultList;
    }

    @Override
    public List<Map<String, Object>> listNameSpaceTables(String sql, String connKey) throws Exception {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Connection connection = getConnectionByKey(connKey);
        HBaseShellMeta shellMeta = hbaseShellParser.getHBaseShellMeta(sql);
        Admin admin = connection.getAdmin();
        TableName[] tableNames = admin.listTableNamesByNamespace(shellMeta.getNameSpace());
        for (TableName tableName:tableNames){
            Map<String, Object> map = new HashMap<>();
            map.put("TABLE", tableName.getNameAsString());
            resultList.add(map);
        }
        return resultList;
    }

    @Override
    public void putData(String sql, String connKey) throws Exception{
        Connection connection = getConnectionByKey(connKey);
        HBaseShellMeta shellMeta = hbaseShellParser.getHBaseShellMeta(sql);
        HashMap<String, String> tableName = shellMeta.getTableName();
        String tableNameStr = "";
        for(Map.Entry<String , String> entry : tableName.entrySet()) tableNameStr = entry.getKey();
        Table table = connection.getTable(TableName.valueOf(tableNameStr));
        Put put = shellMeta.getPut();
        table.put(put);
    }

    @Override
    public Map<String, Object> putDataList(List<String> sqlList, String connKey) throws Exception {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        List<String> failureList = new ArrayList<>(); // 保存执行失败的sql语句
        int failedCount = 0;

        Connection connection = getConnectionByKey(connKey);
        for(String sql: sqlList) {
            try {
                HBaseShellMeta shellMeta = hbaseShellParser.getHBaseShellMeta(sql);
                HashMap<String, String> tableName = shellMeta.getTableName();
                String tableNameStr = "";
                for(Map.Entry<String , String> entry : tableName.entrySet()) {
                    tableNameStr = entry.getKey();
                }
                Table table = connection.getTable(TableName.valueOf(tableNameStr));
                Put put = shellMeta.getPut();
                table.put(put); // 执行sql语句
                successCount++;
            } catch(Exception e) {
                failureList.add(sql); // 保存失败的sql语句
                failedCount++;
            }
        }
        System.out.println("成功执行了" + successCount + "条SQL语句。");
        System.out.println("失败执行了" + failedCount + "条SQL语句。");
        System.out.println("执行失败的SQL语句为：" + failureList);
        result.put("success", successCount);
        result.put("failure", failedCount);
        result.put("failureList", failureList);
        return result;
    }


    @Override
    public List<Map<String, Object>> scanData(String sql, String connKey) throws Exception{
        List<Map<String, Object>> resultList = new ArrayList<>();
        Connection connection = getConnectionByKey(connKey);
        HBaseShellMeta shellMeta = hbaseShellParser.getHBaseShellMeta(sql);
        HashMap<String, String> tableName = shellMeta.getTableName();
        String tableNameStr = "";
        for(Map.Entry<String , String> entry : tableName.entrySet()){
            tableNameStr = entry.getKey();
        }
        Scan scan = shellMeta.getScan();
        TableName tn = TableName.valueOf(tableNameStr);
        Table table = connection.getTable(tn);
        ResultScanner scanner = table.getScanner(scan);
        System.out.println(scan);
        for(Result result : scanner){
            Cell[] cells = result.rawCells();
            for (Cell cell : cells) {
                Map<String, Object> map = new HashMap<>();
                map.put("ROWKEY", Bytes.toString(result.getRow()));
                map.put("COLUMN", Bytes.toString(CellUtil.cloneFamily(cell)) + ":" + Bytes.toString(CellUtil.cloneQualifier(cell)));
                map.put("CELL", "timestamp=" + cell.getTimestamp() + ", value=" + Bytes.toString(CellUtil.cloneValue(cell)));
                resultList.add(map);
            }
            System.out.println(result);
        }
        System.out.println(resultList);
        return resultList;
    }
    // 向表中添加一个列族
    public void addColumnFamily(String connKey, String myTableName, ColumnFamilyDescriptor columnFamilyDescriptor) throws Exception{
        Connection connection = getConnectionByKey(connKey);
        Admin admin = connection.getAdmin();
        TableName tableName=TableName.valueOf(myTableName);
        if(admin.tableExists(tableName)) {
//            TableDescriptor tableDes=TableDescriptorBuilder.newBuilder(tableName).build();
            admin.disableTable(tableName);
            admin.addColumnFamily(tableName, columnFamilyDescriptor);
            admin.enableTable(tableName);
            System.out.println("add "+columnFamilyDescriptor.getNameAsString()+" successful!");
        }
        admin.close();
    }
    // 从表中移除一个列族
    public void removeColumnFamily(String connKey, String myTableName, ColumnFamilyDescriptor columnFamilyDescriptor) throws Exception {
        Connection connection = getConnectionByKey(connKey);
        Admin admin = connection.getAdmin();
        TableName tableName=TableName.valueOf(myTableName);
        if(admin.tableExists(tableName)) {
            System.out.println("It's ready to remove "+columnFamilyDescriptor.getNameAsString());
//            TableDescriptor tableDes=TableDescriptorBuilder.newBuilder(tableName).build();
            admin.disableTable(tableName);
            admin.deleteColumnFamily(tableName, columnFamilyDescriptor.getName());
            admin.enableTable(tableName);
            System.out.println("remove "+columnFamilyDescriptor.getNameAsString()+" successful!");
        }
        admin.close();
    }
    @Override
    public void alterData(String sql, String connKey) throws Exception {
        HBaseShellMeta shellMeta = hbaseShellParser.getHBaseShellMeta(sql);
        HashMap<String, String> tableName = shellMeta.getTableName();
        String tableNameStr = "";
        for(Map.Entry<String , String> entry : tableName.entrySet()){
            tableNameStr = entry.getKey();
        }
        List<ColumnFamilyDescriptor> columnFamilyDescriptors = shellMeta.getColumnFamilyDescriptors();
        ColumnFamilyDescriptor columnFamilyDescriptor = columnFamilyDescriptors.get(0);
        boolean isDelete = shellMeta.isAlterDelete();
        if (isDelete){
            removeColumnFamily(connKey, tableNameStr, columnFamilyDescriptor);
        }else{
            addColumnFamily(connKey, tableNameStr, columnFamilyDescriptor);
        }
    }
    @Override
    public void disableTable(String sql, String connKey) throws Exception {
        HBaseShellMeta shellMeta = hbaseShellParser.getHBaseShellMeta(sql);
        HashMap<String, String> tableName = shellMeta.getTableName();
        String tableNameStr = "";
        for(Map.Entry<String , String> entry : tableName.entrySet()){
            tableNameStr = entry.getKey();
        }
        Connection connection = getConnectionByKey(connKey);
        Admin admin = connection.getAdmin();
        TableName tbName=TableName.valueOf(tableNameStr);
        if(admin.tableExists(tbName)) {
            admin.disableTable(tbName);
        }
        System.out.println("disable the table!");
        admin.close();
    }
    @Override
    public void enableTable(String sql, String connKey) throws Exception {
        HBaseShellMeta shellMeta = hbaseShellParser.getHBaseShellMeta(sql);
        HashMap<String, String> tableName = shellMeta.getTableName();
        String tableNameStr = "";
        for(Map.Entry<String , String> entry : tableName.entrySet()){
            tableNameStr = entry.getKey();
        }
        Connection connection = getConnectionByKey(connKey);
        Admin admin = connection.getAdmin();
        TableName tbName=TableName.valueOf(tableNameStr);
        if(admin.tableExists(tbName)) {
            admin.enableTable(tbName);
        }
        System.out.println("enable the table!");
        admin.close();
    }
    @Override
    public Map<String, Object> descTable(String sql, String connKey) throws Exception {
//    public String descTable(String sql, String connKey) throws Exception {
        Map<String, Object> result = new HashMap<>();
        HBaseShellMeta shellMeta = hbaseShellParser.getHBaseShellMeta(sql);
        HashMap<String, String> tableName = shellMeta.getTableName();
        String tableNameStr = "";
        for(Map.Entry<String , String> entry : tableName.entrySet()){
            tableNameStr = entry.getKey();
        }
        Connection connection = getConnectionByKey(connKey);
        Admin admin = connection.getAdmin();
        TableName tbName = TableName.valueOf(tableNameStr);
        List<String> columnFamilies = new ArrayList<>();
        String jsonString = "";
        if(admin.tableExists(tbName)) {
            ColumnFamilyDescriptor[]  colFamilies=admin.getDescriptor(tbName).getColumnFamilies();
            TableDescriptor  tableDescriptor = admin.getDescriptor(tbName);
            jsonString = tableDescriptor.toString();
            System.out.println("==============describe  "+tableNameStr+" ================");
            Map<String, Object> discMap = new HashMap<>();
            discMap.put("DESCRIPTION", jsonString);
            for(ColumnFamilyDescriptor colFamily:colFamilies) {
                columnFamilies.add(colFamily.getNameAsString());
//                colMap.put(colFamily.getNameAsString(), JSON.toJSONString(colFamily));
            }
//            List<Map<String, Object>> resultList = Collections.singletonList(colMap);
            List<Map<String, Object>> resultList = new ArrayList<>();
            resultList.add(discMap);
            List<Map<String, Object>> columns = listColumns("DESCRIPTION");
            result.put("resultList", resultList);
            result.put("columns", columns);
            System.out.println(resultList);
            System.out.println(columns);
        }
        admin.close();
        return result;
//        return jsonString;
    }
    @Override
    public List<Map<String, Object>> listSomeColumns(List<String> columnFamilies) {
        List<Map<String, Object>> columns = new ArrayList<>();
        for (String columnName:columnFamilies){
            Map<String, Object> column = new HashMap<>();
            column.put("column_name", columnName);
            columns.add(column);
        }
        return columns;
    }
    @Override
    public boolean existTable(String sql, String connKey) throws Exception {
        HBaseShellMeta shellMeta = hbaseShellParser.getHBaseShellMeta(sql);
        HashMap<String, String> tableName = shellMeta.getTableName();
        String tableNameStr = "";
        for(Map.Entry<String , String> entry : tableName.entrySet()){
            tableNameStr = entry.getKey();
        }
        Connection connection = getConnectionByKey(connKey);
        Admin admin = connection.getAdmin();
        TableName tbName = TableName.valueOf(tableNameStr);
        if(admin.tableExists(tbName)){
            System.out.println("Table " + tableNameStr + " exists.");
        }else{
            System.out.println("Table " + tableNameStr + " doesn't exist.");
        }
        return admin.tableExists(tbName);
    }
    @Override
    public List<Map<String, Object>> getData(String sql, String connKey) throws Exception {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Connection connection = getConnectionByKey(connKey);
        HBaseShellMeta shellMeta = hbaseShellParser.getHBaseShellMeta(sql);
        HashMap<String, String> tableName = shellMeta.getTableName();
        String tableNameStr = "";
        for(Map.Entry<String , String> entry : tableName.entrySet()){
            tableNameStr = entry.getKey();
        }
        Get get = shellMeta.getGet();
        TableName tn = TableName.valueOf(tableNameStr);
        Table table = connection.getTable(tn);
        Result result=table.get(get);
        Cell[] cells = result.rawCells();
        for (Cell cell : cells) {
            Map<String, Object> map = new HashMap<>();
            map.put("ROWKEY", Bytes.toString(result.getRow()));
            map.put("COLUMN", Bytes.toString(CellUtil.cloneFamily(cell)) + ":" + Bytes.toString(CellUtil.cloneQualifier(cell)));
            map.put("CELL", "timestamp=" + cell.getTimestamp() + ", value=" + Bytes.toString(CellUtil.cloneValue(cell)));
            resultList.add(map);
        }
        System.out.println(result);
        System.out.println(resultList);
        return resultList;
    }
    @Override
    public void dropTable(String sql, String connKey) throws Exception {
        Connection connection = getConnectionByKey(connKey);
        HBaseShellMeta shellMeta = hbaseShellParser.getHBaseShellMeta(sql);
        HashMap<String, String> tableName = shellMeta.getTableName();
        String tableNameStr = "";
        for(Map.Entry<String , String> entry : tableName.entrySet()){
            tableNameStr = entry.getKey();
        }
        TableName tn=TableName.valueOf(tableNameStr);
        //TableName tn = TableName.valueOf(sqlShell[1]);
        Admin admin = connection.getAdmin();
        if(admin.isTableEnabled(tn))
            admin.disableTable(tn);
        admin.deleteTable(tn);
        System.out.println("成功删除表："+tn);
        admin.close();
    }
    @Override
    public List<Map<String, Object>> listColumns(String name) {
        Map<String, Object> columns = new HashMap<>();
        columns.put("column_name", name);
        return Collections.singletonList(columns);
    }
    /**
     *高危和脱敏配置使用
     *@paramdbaseEntity
     *@return
     *@throwsException
     */
    @Override
    public List<String> listNameSpacesByDbaseEntity(DbaseEntity dbaseEntity) throws Exception {
        List<String> resultList = new ArrayList<>();
        Connection connection = getConnection(dbaseEntity);
        Admin admin = connection.getAdmin();
        NamespaceDescriptor[] namespaceDescriptors = admin.listNamespaceDescriptors();
        for (NamespaceDescriptor namespace : namespaceDescriptors){
            resultList.add(namespace.getName());
        }
        admin.close();
        connection.close();
        return resultList;
    }

    /**
     *高危和脱敏配置
     *@paramdbaseEntity
     *@paramschemaName
     *@return
     */
    @Override
    public List<Map<String, Object>> getHBaseTableMetas(DbaseEntity dbaseEntity, String schemaName) throws Exception {
        List<Map<String, Object>> tableMetas = new ArrayList<>();
        Connection connection = getConnection(dbaseEntity);
        Admin admin = connection.getAdmin();
        List<TableDescriptor> tableDescriptors = admin.listTableDescriptorsByNamespace(schemaName.getBytes(StandardCharsets.UTF_8));
        for (TableDescriptor tableDescriptor : tableDescriptors){
            String tableName = tableDescriptor.getTableName().getNameAsString();
            ColumnFamilyDescriptor[] columnFamilies = tableDescriptor.getColumnFamilies();
//            tableDescriptor.getColumnFamilyNames();
            // 输出列族和列信息
            // 存放列族名和对应列名的Map
            Map<String, String[]> columnMap = new HashMap<>();
            for (ColumnFamilyDescriptor columnDesc : tableDescriptor.getColumnFamilies()) {
                String cfName = columnDesc.getNameAsString();
                System.out.printf("列族：%s%n", columnDesc.getNameAsString());
                byte[] cfBytes = columnDesc.getName();
                TableName tn = TableName.valueOf(tableName);
                // 获取列族下的所有列名
                try (Table table = connection.getTable(tn)) {
                    Scan scan = new Scan().addFamily(cfBytes);
                    ResultScanner resultScanner = table.getScanner(scan);
                    for (Result result : resultScanner) {
                        List<Cell> cells = result.listCells();
                        String[] columns = new String[cells.size()];
                        for (int i = 0; i < cells.size(); i++) {
                            Cell cell = cells.get(i);
                            columns[i] = Bytes.toString(cell.getQualifierArray(),
                                    cell.getQualifierOffset(),
                                    cell.getQualifierLength());
                        }
                        columnMap.put(cfName, columns);
                    }
                }
            }
            if (columnMap.isEmpty()) {
//                System.out.println("\t该列族下没有列");
                for (ColumnFamilyDescriptor columnFamilyDescriptor:columnFamilies){
                    Map<String, Object> map = new HashMap<>();
                    System.out.println(columnFamilyDescriptor.getNameAsString());
                    map.put("TABLE_NAME", tableName);
                    map.put("COLUMN_NAME", columnFamilyDescriptor.getNameAsString());
                    tableMetas.add(map);
                }
            } else {
                // 输出列族名和对应列名的Map
                for (String cfName : columnMap.keySet()) {
//                System.out.printf("列族：%s%n", cfName);
                    String[] columns = columnMap.get(cfName);
                    if (columns != null) {
//                    System.out.println("\t列：");
                        for (String columnName : columns) {
//                        System.out.printf("\t\t%s%n", columnName);
                            Map<String, Object> map = new HashMap<>();
                            map.put("TABLE_NAME", tableName);
                            map.put("COLUMN_NAME", cfName + ":" + columnName);
                            tableMetas.add(map);
                        }
                    }
                }
            }
        }
        admin.close();
        connection.close();
        return tableMetas;
    }

    @Override
    public boolean truncateTable(String sql, String connKey) throws Exception{
        HBaseShellMeta shellMeta = hbaseShellParser.getHBaseShellMeta(sql);
        HashMap<String, String> tableName = shellMeta.getTableName();
        String tableNameStr = "";
        for(Map.Entry<String , String> entry : tableName.entrySet()){
            tableNameStr = entry.getKey();
        }
        Connection connection = getConnectionByKey(connKey);
        Admin admin = connection.getAdmin();
        TableName tbName = TableName.valueOf(tableNameStr);
        if(!admin.tableExists(tbName)){
            System.out.println("Table " + tableNameStr + " exists.");
            return false;
        }
        admin.truncateTable(tbName, true);
        return true;
    }

    @Override
    public boolean grant(String sql, String connKey) throws Exception {
        //初始化
        HBaseShellMeta shellMeta = hbaseShellParser.getHBaseShellMeta(sql);
        Connection connection = getConnectionByKey(connKey);
        Admin admin = connection.getAdmin();
        String userName = shellMeta.getUserName();
        char[] actions = shellMeta.getActions();
        Set<Character> actionSet = new HashSet<Character>();
        for(char c : actions){
            actionSet.add(c);
        }
        String nameSpace;
        String tableNameStr = "";
        HashMap<String, String> tableNameMap;
        TableName tableName;
        //对全局授权（没有第三个字段）
        if(shellMeta.isChangeForALl()){
            if(actionSet.contains('R')){
                try {
                    AccessControlClient.grant(connection, userName, Permission.Action.READ);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
            if(actionSet.contains('W')){
                try {
                    AccessControlClient.grant(connection, userName, Permission.Action.WRITE);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
            if(actionSet.contains('X')){
                try {
                    AccessControlClient.grant(connection, userName, Permission.Action.EXEC);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
            if(actionSet.contains('C')){
                try {
                    AccessControlClient.grant(connection, userName, Permission.Action.CREATE);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
            if(actionSet.contains('A')){
                try {
                    AccessControlClient.grant(connection, userName, Permission.Action.ADMIN);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        }
        //对namespace或table授权
        else{
            //namespace
            if(shellMeta.isNsOrTb()){
                nameSpace = shellMeta.getNameSpace();
                if(actionSet.contains('R')){
                    try {
                        AccessControlClient.grant(connection, nameSpace, userName, Permission.Action.READ);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
                if(actionSet.contains('W')){
                    try {
                        AccessControlClient.grant(connection, nameSpace, userName, Permission.Action.WRITE);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
                if(actionSet.contains('X')){
                    try {
                        AccessControlClient.grant(connection, nameSpace, userName, Permission.Action.EXEC);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
                if(actionSet.contains('C')){
                    try {
                        AccessControlClient.grant(connection, nameSpace, userName, Permission.Action.CREATE);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
                if(actionSet.contains('A')){
                    try {
                        AccessControlClient.grant(connection, nameSpace, userName, Permission.Action.ADMIN);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            //table
            else{
                tableNameMap = shellMeta.getTableName();
                for(Map.Entry<String, String> entry : tableNameMap.entrySet()){
                    tableNameStr = entry.getKey();
                }
                tableName = TableName.valueOf(tableNameStr);
                if(!admin.tableExists(tableName)) return false;
                if(actionSet.contains('R')){
                    try {
                        AccessControlClient.grant(connection, tableName, userName, null, null, Permission.Action.READ);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
                if(actionSet.contains('W')){
                    try {
                        AccessControlClient.grant(connection, tableName, userName, null, null, Permission.Action.WRITE);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
                if(actionSet.contains('X')){
                    try {
                        AccessControlClient.grant(connection, tableName, userName, null, null, Permission.Action.EXEC);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
                if(actionSet.contains('C')){
                    try {
                        AccessControlClient.grant(connection, tableName, userName, null, null, Permission.Action.CREATE);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
                if(actionSet.contains('A')){
                    try {
                        AccessControlClient.grant(connection, tableName, userName, null, null, Permission.Action.ADMIN);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean revoke(String sql, String connKey) throws Exception {
        //初始化
        HBaseShellMeta shellMeta = hbaseShellParser.getHBaseShellMeta(sql);
        Connection connection = getConnectionByKey(connKey);
        Admin admin = connection.getAdmin();
        String userName = shellMeta.getUserName();
        String nameSpace;
        String tableNameStr = "";
        HashMap<String, String> tableNameMap;
        TableName tableName;
        //对全局撤回
        if(shellMeta.isChangeForALl()){
            try {
                AccessControlClient.revoke(connection, userName);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        //对namespace或table撤回
        else{
            //namespace
            if(shellMeta.isNsOrTb()){
                nameSpace = shellMeta.getNameSpace();
                try {
                    AccessControlClient.revoke(connection, nameSpace, userName);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
            //table
            else{
                tableNameMap = shellMeta.getTableName();
                for(Map.Entry<String, String> entry : tableNameMap.entrySet()){
                    tableNameStr = entry.getKey();
                }
                tableName = TableName.valueOf(tableNameStr);
                if(!admin.tableExists(tableName)) return false;
                try {
                    AccessControlClient.revoke(connection, tableName, userName, null, null);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return true;
    }

    @Override
    public boolean createNameSpace(String sql, String connKey) throws Exception {
        HBaseShellMeta shellMeta = hbaseShellParser.getHBaseShellMeta(sql);
        HashMap<String, String> tableName = shellMeta.getTableName();
        String nameSpaceStr = "";
        for(Map.Entry<String , String> entry : tableName.entrySet()){
            nameSpaceStr = entry.getKey();
        }
        Connection connection = getConnectionByKey(connKey);
        Admin admin = connection.getAdmin();
        NamespaceDescriptor.Builder builder = NamespaceDescriptor.create(nameSpaceStr);
        NamespaceDescriptor nsd = builder.build();
        admin.createNamespace(nsd);
        return true;
    }

    @Override
    public boolean dropNameSpace(String sql, String connKey) throws Exception {
        HBaseShellMeta shellMeta = hbaseShellParser.getHBaseShellMeta(sql);
        HashMap<String, String> tableName = shellMeta.getTableName();
        String nameSpaceStr = "";
        for(Map.Entry<String , String> entry : tableName.entrySet()){
            nameSpaceStr = entry.getKey();
        }
        Connection connection = getConnectionByKey(connKey);
        Admin admin = connection.getAdmin();
        admin.deleteNamespace(nameSpaceStr);
        return true;
    }

    @Override
    public void setHost(String host, String dbName) {
        try {
            // 打开hosts文件进行读取操作
            FileReader fileReader = new FileReader("/etc/hosts");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            // 逐行读取hosts文件内容，并查找是否已经存在指定的映射记录
            String line;
            boolean exists = false;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(host) && line.contains(dbName)) {
                    exists = true;
                    break;
                }
            }
            // 关闭文件读取流
            bufferedReader.close();
            fileReader.close();

            // 如果不存在，则将HBase主机名映射到IP地址并写入hosts文件
            if (!exists) {
                FileWriter fileWriter = new FileWriter("/etc/hosts", true);
                PrintWriter printWriter = new PrintWriter(fileWriter);

                printWriter.println(host + "\t" + dbName);

                printWriter.close();
                fileWriter.close();
                System.out.println("成功将HBase主机名映射添加到hosts文件中！");
            } else {
                System.out.println("hosts文件中已经存在指定的HBase主机名和IP地址映射记录，不需要再次添加。");
            }
        } catch (Exception e) {
            System.out.println("读取或写入hosts文件时出错：" + e.getMessage());
        }
    }
    //获取表列簇
    @Override
    public TableDescriptor getTableStructure(String tableName, String connKey) throws Exception {
//        Connection connection = getConnectionByKey(connKey);
//        // 获取 admin
//        Admin admin = connection.getAdmin();
//        TableDescriptor tableDescriptor = admin.getDescriptor(TableName.valueOf(tableName));
//        tableDescriptor.getColumnFamilyNames();
//        ColumnFamilyDescriptor[] columnFamilyDescriptors = tableDescriptor.getColumnFamilies();
//        for (ColumnFamilyDescriptor columnFamilyDescriptor :columnFamilyDescriptors) {
//            System.out.println(String.valueOf(columnFamilyDescriptor.getNameAsString()));
//        }
        // 创建配置对象
        Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", "newHbaseQuorumIP");  // 指定新的HBase的ZooKeeper地址

        // 创建HBase连接
        Connection connection = getConnectionByKey(connKey);
        Admin admin = connection.getAdmin();

        // 获取源表
        TableName sourceTable = TableName.valueOf(tableName);

        // 获取源表的表描述符
        TableDescriptor sourceTableDescriptor = admin.getDescriptor(sourceTable);

        // 将表描述符转换为 JSON 字符串
//        String jsonString = new String(sourceTableDescriptor.toString().getBytes(), StandardCharsets.UTF_8);
//
//        // 输出 JSON 字符串
//        System.out.println(jsonString);

//        // 新建表的名称
//        TableName newTable = TableName.valueOf("new_" + tableName);
//
//        // 定义表描述符，使用源表描述符的信息（包括列族、属性等）
//        TableDescriptorBuilder tableBuilder = TableDescriptorBuilder.newBuilder(newTable);
//        for (ColumnFamilyDescriptor cf : sourceTableDescriptor.getColumnFamilies()) {
//            tableBuilder.setColumnFamily(cf);
//        }
//        TableDescriptor newTableDescriptor = tableBuilder.build();
//
//        // 创建新表
//        admin.createTable(newTableDescriptor);

        // 关闭连接
        admin.close();
        return sourceTableDescriptor;
    }

    //创建表
    @Override
    public void createNewTable(List<TableDescriptor> Td, String connKey) throws Exception {
        Connection connection = getConnectionByKey(connKey);
        // 获取 admin
        Admin admin = connection.getAdmin();
        //        // 新建表的名称
        try{
            for (TableDescriptor tableDescriptor : Td) {
                TableName tableName = tableDescriptor.getTableName();
//                TableName tableName = TableName.valueOf("new_table");
                if (!admin.tableExists(tableName)) {
                    admin.createTable(tableDescriptor);
                    System.out.println("表: " + tableName + " 创建成功！");
                } else {
                    System.out.println("表: " + tableName + " 已经存在，无需创建！");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        admin.close();
    }

}
