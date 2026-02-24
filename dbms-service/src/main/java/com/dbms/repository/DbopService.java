package com.dbms.repository;


import com.dbms.entity.DbaseEntity;
import com.dbms.domain.dbase.DataBaseMetric;
import com.dbms.domain.dbase.MetaInfo;

import java.util.List;
import java.util.Map;

public interface DbopService {

    boolean testConn(DbaseEntity dbaseEntity);

    List<String> getSchemas(DbaseEntity dbaseEntity);

    List<String> getTables(DbaseEntity dbaseEntity, String schemaName);

    List<String> getViews(DbaseEntity dbaseEntity, String schemaName);

    List<Map<String, Object>> getTableMetas(DbaseEntity dbaseEntity, String schemaName);

    List<Map<String, Object>> getFuncs(DbaseEntity dbaseEntity, String schemaName);

    List<String> getTablesByType(DbaseEntity dbaseEntity, String schemaName);

    List<String> getViewsByType(DbaseEntity dbaseEntity, String schemaName);

    List<Map<String, Object>> getFuncsByType(DbaseEntity dbaseEntity, String schemaName);

    List<Map<String, Object>> getTableColumns(DbaseEntity dbaseEntity, String schemaName, String tableName);

    List<Map<String, Object>> getTableMeta(DbaseEntity dbaseEntity, String schemaName);

    List<Map<String, Object>> executeSQLQueueForDM(DbaseEntity dbaseEntity, String schemaName, List<String> sqlList, Integer limitSize);

    List<Map<String, Object>> executeSQLQueueForDMRisk(DbaseEntity dbaseEntity, String schemaName, List<String> sqlList, Integer limitSize);

    List<Map<String, Object>> executeSQLQueueForGBase(DbaseEntity dbaseEntity, String schemaName, List<String> sqlList, Integer limitSize);

    List<Map<String, Object>> executeSQLQueueForKingBase(DbaseEntity dbaseEntity, String schemaName, List<String> sqlList, Integer limitSize);

    Map<String,Object> commit(DbaseEntity dbaseEntity, String schemaName);

    Map<String,Object> rollback(DbaseEntity dbaseEntity, String schemaName);

    List<Map<String, Object>> executeUpdate(DbaseEntity dbaseEntity, String schemaName, List<String> sqlList);

    void getConnectionSession(DbaseEntity dbaseEntity);

    void closeConn(DbaseEntity dbaseEntity);

    List<Map<String, Object>> executeSQLFunction(DbaseEntity dbaseEntity, String schemaName, String sql);

    List<Map<String, Object>> executeSelectSQL(DbaseEntity dbaseEntity, String schemaName, List<String> sqlList);

    List<Map<String, Object>> getAccount(DbaseEntity dbaseEntity);

    MetaInfo getDataBaseMetaInfo(DbaseEntity dbaseEntity);

    Map<String, Object> executeSQLStatement(DbaseEntity dbaseEntity, String schemaName, String sql);

    Map<String, Object> executeSQLStatementOrder(DbaseEntity dbaseEntity, String schemaName, String sql);
}
