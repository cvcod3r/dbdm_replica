package com.dbms.repository;

import com.dbms.entity.DbaseEntity;

import java.util.List;
import java.util.Map;

public interface BigDataOpService {


    // 执行语句for hbase, 返回值一样
    /**
     * 0.hbase语句解析，get, put, scan, create
     *  不同的操作，不同的执行
     *
     *
     */
    Map<String, Object> executeSQLStatementForHBase(DbaseEntity dbaseEntity, String sql);

    // 执行语句for es,
    //  rest-api
    Map<String, Object> executeSQLStatementForElastic(DbaseEntity dbaseEntity, String sql);

    void closeConn(DbaseEntity dbaseEntity);

    void getConnectionSession(DbaseEntity dbaseEntity);

    Map<String, List<String>> getTables(DbaseEntity dbaseEntity);

    boolean testConn(DbaseEntity dbaseEntity);

    List<String> getHBaseNameSpace(DbaseEntity dbaseEntity) throws Exception;

    List<Map<String, Object>> getHBaseTableMetas(DbaseEntity dbaseEntity, String schemaName) throws Exception;

    Map<String, Object> executeSQLStatementForElasticRestful(DbaseEntity dbaseEntity, String sql);

    List<String> getElasticIndex(DbaseEntity dbaseEntity) throws Exception;

    List<Map<String, Object>> getElasticIndexMetas(DbaseEntity dbaseEntity, String schemaName) throws Exception;

    Map<String, Object> executeSQLStatementForHBaseOrder(DbaseEntity dbaseEntity, String sql);
}
