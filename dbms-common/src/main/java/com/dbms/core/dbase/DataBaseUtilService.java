package com.dbms.core.dbase;

import com.dbms.domain.dbase.MetaInfo;
import com.dbms.entity.DbaseEntity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DataBaseUtilService {

    Connection getConnection(DbaseEntity dbaseEntity);

    Connection getConnectionByKey(String key);

    Connection getConnection(DbaseEntity dbaseEntity, String key);

    void checkConnectionSession();

    void closeConn(String key) throws Exception;

    void startTransaction(DbaseEntity dbaseEntity, String key) throws Exception;

    void commit(String key) throws Exception;

    void rollback(String key) throws Exception;

    List<Map<String, Object>> executeSQL(DbaseEntity dbaseEntity, String sql) throws SQLException;

    boolean testConnection(DbaseEntity dbaseEntity);

    int execUpdate(DbaseEntity dbaseEntity, String sql) throws Exception;

    Map<String, Object> execUpdateList(DbaseEntity dbaseEntity, List<String> sqlList) throws Exception;

    int execUpdate(DbaseEntity dbaseEntity, String sql, String key)throws Exception;

    int executeUpdate(DbaseEntity dbaseEntity, String sql, String key) throws Exception;

    int updateExecuteBatch(DbaseEntity dbaseEntity, List<String> sqlList)throws Exception;

    List<Map<String, Object>> queryForList(DbaseEntity dbaseEntity, String sql, String key)throws Exception;

    List<Map<String, Object>> queryForList(DbaseEntity dbaseEntity, String sql)throws Exception;

    List<Map<String, Object>> executeSqlForColumns(DbaseEntity dbaseEntity, String sql,String key) throws Exception;

    List<Map<String, Object>> executeSqlForColumns(DbaseEntity dbaseEntity, String sql) throws Exception;

    int executeQueryForCount(DbaseEntity dbaseEntity, String sql);

    int executeQueryForCount2(DbaseEntity dbaseEntity, String sql);

    boolean executeQuery(DbaseEntity dbaseEntity, String sql);

    String getPrimaryKeys(DbaseEntity dbaseEntity, String databaseName, String tableName);

    List<String> getPrimaryKeyss(DbaseEntity dbaseEntity, String databaseName, String tableName);

    int executeQueryForCountForOracle(DbaseEntity dbaseEntity, String sql);

    List<String> getSchemas(DbaseEntity dbaseEntity);

    List<String> getTables(DbaseEntity dbaseEntity, String connKey);

    List<String> getViews(DbaseEntity dbaseEntity, String connKey);

    List<Map<String, Object>> getFuncs(DbaseEntity dbaseEntity, String connKey);

    List<Map<String, Object>> getTableMetas(DbaseEntity dbaseEntity, String connKey);

    List<String> getColumns(DbaseEntity dbaseEntity, String tableName, String connKey);

    MetaInfo getDataBaseMeta(DbaseEntity dbaseEntity);

    Integer countConnectionSession(Integer dbId);

    List<Map<String, Object>> getColumnsFromResultList(List<Map<String, Object>> list);
}
