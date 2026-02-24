package com.dbms.core.dbase.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dbms.core.dbase.IElasticUtilService;
import com.dbms.entity.DbaseEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ElasticUtilServiceImpl implements IElasticUtilService {


    private static final Logger logger = LoggerFactory.getLogger(DataBaseUtilServiceImpl.class);

    public static final Map<String, RestClient> connMap = new ConcurrentHashMap<>();

    @Override
    public RestClient getConnection(DbaseEntity dbaseEntity) throws Exception{
        System.out.println("获取rest high level client");
        String[] hosts = dbaseEntity.getHost().split(",");
        HttpHost[] httpHosts = new HttpHost[hosts.length];
        for (int i=0;i<hosts.length;i++){
            httpHosts[i] = new HttpHost(hosts[i], Integer.parseInt(dbaseEntity.getPort()));
        }
        RestClient client = RestClient.builder(httpHosts).build();
//        GetRequest request = new GetRequest("elasticsearch");
//        if (client.exists(request, RequestOptions.DEFAULT)){
//            throw new Exception("获取Elastic连接失败！");
//        }
//        if (client. == null){
//            throw new Exception("获取Elastic连接失败！");
//        }
        return client;
    }

    @Override
    public RestClient getConnection(DbaseEntity dbaseEntity, String connKey) throws Exception{
        RestClient client = null;
        try{
            client = connMap.getOrDefault(connKey, null);
            if (client == null){
                String[] hosts = dbaseEntity.getHost().split(",");
                HttpHost[] httpHosts = new HttpHost[hosts.length];
                for (int i=0;i<hosts.length;i++){
                    httpHosts[i] = new HttpHost(hosts[i], Integer.parseInt(dbaseEntity.getPort()));
                }
                client = RestClient.builder(httpHosts).build();
//                // 连接延时配置

                //RestClientBuilder builder = RestClient.builder(httpHost);
//                builder.setRequestConfigCallback(requestConfigBuilder -> {
//                    requestConfigBuilder.setConnectTimeout(connectTimeOut);
//                    requestConfigBuilder.setSocketTimeout(socketTimeOut);
//                    requestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeOut);
//                    return requestConfigBuilder;
//                });
//                // 连接数配置
//                builder.setHttpClientConfigCallback(httpClientBuilder -> {
//                    httpClientBuilder.setMaxConnTotal(maxConnectNum);
//                    httpClientBuilder.setMaxConnPerRoute(maxConnectNumPerRoute);
//                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
//                    return httpClientBuilder;
//                });
                connMap.put(connKey, client);
            }
        }catch (Exception e){
            throw new Exception("获取Elastic连接失败！");
        }
        return client;
    }

    @Override
    public RestClient getConnectionByKey(String connKey) throws Exception{
        RestClient client = connMap.getOrDefault(connKey, null);
        if (client == null){
            throw new Exception("Elastic连接已关闭，请尝试重新连接！！");
        }
        return client;
    }

    @Override
    public void closeConn(String connKey) throws Exception {
        try {
            RestClient client = getConnectionByKey(connKey);
            if (client != null){
                client.close();
                connMap.remove(connKey);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> executeSqlByRestClient(DbaseEntity dbaseEntity, String connKey, String sql) throws Exception {
        RestClient restClient = getConnectionByKey(connKey);
        Request request = new Request("POST", "/_sql?format=json&pretty");
        String sqlJsonString = "{\"query\":" + "\"" + sql + "\"}";
        // @RequestBody String query
        request.setEntity(new NStringEntity(sqlJsonString, ContentType.APPLICATION_JSON));
        Response response = restClient.performRequest(request);
        System.out.println("===========");
        System.out.println(response.getEntity().getContent());
        String responseBody = EntityUtils.toString(response.getEntity());

        JSONObject result = JSONObject.parseObject(responseBody);
        // 列信息
        JSONArray columnsJSON = result.getJSONArray("columns");
        List<Map<String, Object>> columns = convertColumnsJsonToMapList(columnsJSON);
        // 结果集行信息
        JSONArray rowsJSON = result.getJSONArray("rows");
        List<Map<String, Object>> rows = convertRowsJsonToList(columns, rowsJSON);

        System.out.println("columns::::");
        System.out.println(JSON.toJSONString(columns));
        System.out.println("rows::::");
        System.out.println(JSON.toJSONString(rows));

        Map<String, Object> map = new HashMap<>();
        map.put("columns", columns);
        map.put("rows", rows);
        return map;
    }

    private List<Map<String, Object>> convertColumnsJsonToMapList(JSONArray columnsJSON) {
        List<Map<String, Object>> columns = (List<Map<String, Object>>) JSONArray.parse(columnsJSON.toJSONString());
        return columns;
    }

    private List<Map<String, Object>> convertRowsJsonToList(List<Map<String, Object>> columns, JSONArray rowsJSON) {
        List<Map<String, Object>> rowResult = new ArrayList<>();
        for (int i=0; i<rowsJSON.size(); i++){
            JSONArray row = rowsJSON.getJSONArray(i);
            Map<String, Object> item = new HashMap<>();
            for (int j=0; j<columns.size(); j++){
                String columnName = (String) columns.get(j).get("name");
                String itemString = String.valueOf(row.get(j));
                item.put(columnName, itemString);
            }
            rowResult.add(item);
        }
        return rowResult;
    }


}
