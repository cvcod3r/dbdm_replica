package com.dbms.core.dbase;

import com.dbms.entity.DbaseEntity;

import java.util.List;
import java.util.Map;

public interface IElasticRestfulService {
    Map<String, Object> executeStatementRestHttp(DbaseEntity dbaseEntity, String sql) throws Exception;

    Map<String, String> parseQuery(String query) throws Exception;

    String executeRestRequest(DbaseEntity dbaseEntity, String method, String endpoint, String data) throws Exception;

    String executeRequest(DbaseEntity dbaseEntity, String method, String endpoint, String data) throws Exception;

    Map<String, List<String>> getTables(DbaseEntity dbaseEntity) throws Exception;

    List<String> getIndices(DbaseEntity dbaseEntity) throws Exception;

    List<Map<String, Object>> getIndicesMetas(DbaseEntity dbaseEntity) throws Exception;
}
