package com.dbms.core.dbase;

import com.dbms.entity.DbaseEntity;
import org.elasticsearch.client.RestClient;

import java.util.Map;

public interface IElasticUtilService {


    RestClient getConnection(DbaseEntity dbaseEntity) throws Exception;

    RestClient getConnection(DbaseEntity dbaseEntity, String connKey) throws Exception;

    RestClient getConnectionByKey(String connKey) throws Exception;

    void closeConn(String connKey) throws Exception;

    Map<String, Object> executeSqlByRestClient(DbaseEntity dbaseEntity, String connKey, String sql) throws Exception;

}
