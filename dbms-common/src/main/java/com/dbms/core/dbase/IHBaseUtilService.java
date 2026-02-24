package com.dbms.core.dbase;

import com.dbms.entity.DbaseEntity;
import com.github.CCweixiao.hbase.sdk.template.IHBaseAdminTemplate;
import com.github.CCweixiao.hbase.sdk.template.IHBaseSqlTemplate;
import com.github.CCweixiao.hbase.sdk.template.IHBaseTableTemplate;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.TableDescriptor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IHBaseUtilService {
    IHBaseAdminTemplate getHBaseAdminTemplate(DbaseEntity dbaseEntity);

    IHBaseTableTemplate getHBaseTableTemplate(DbaseEntity dbaseEntity);

    IHBaseSqlTemplate getHBaseSqlTemplate(DbaseEntity dbaseEntity);

    Connection getConnection(DbaseEntity dbaseEntity) throws Exception;

    Connection getConnection(DbaseEntity dbaseEntity, String connKey) throws Exception;

    Connection getConnectionByKey(String connKey) throws Exception;

    void createTable(String sql, String connKey) throws Exception;

    void closeConn(String connKey) throws Exception;

    Map<String, List<String>> getTables(String connKey) throws Exception;

    List<Map<String, Object>> listNameSpaces(String connKey) throws Exception;

    List<Map<String, Object>> listTable(String sql, String connKey) throws Exception;

    List<Map<String, Object>> listColumns(String name);

    List<Map<String, Object>> listNameSpaceTables(String sql, String connKey) throws Exception;

    void putData(String sql, String connKey) throws Exception;

    Map<String, Object> putDataList(List<String> sqlList, String connKey) throws Exception;

    void alterData(String sql, String connKey) throws Exception;

    void disableTable(String sql, String connKey) throws Exception;

    void enableTable(String sql, String connKey) throws Exception;

    Map<String, Object> descTable(String sql, String connkey) throws Exception;
//    String descTable(String sql, String connkey) throws Exception;

    List<Map<String, Object>> listSomeColumns(List<String> columnFamilies);

    boolean existTable(String sql, String connKey) throws Exception;

    List<Map<String, Object>> getData(String sql, String connKey) throws Exception;

    void dropTable(String sql, String connKey) throws Exception;

    List<Map<String, Object>> scanData(String sql, String connKey) throws Exception;

    List<String> listNameSpacesByDbaseEntity(DbaseEntity dbaseEntity) throws Exception;

    List<Map<String, Object>> getHBaseTableMetas(DbaseEntity dbaseEntity, String schemaName) throws Exception;

    boolean truncateTable(String sql, String connKey) throws Exception;

    boolean grant(String sql, String connKey) throws Exception;

    boolean revoke(String sql, String connKey) throws Exception;

    boolean createNameSpace(String sql, String connKey) throws Exception;

    boolean dropNameSpace(String sql, String connKey) throws Exception;

    void setHost(String host, String dbName);

    //获取表列簇
    TableDescriptor getTableStructure(String tableName, String connKey) throws Exception;

    //创建表
    void createNewTable(List<TableDescriptor> Td, String connKey) throws Exception;
}
