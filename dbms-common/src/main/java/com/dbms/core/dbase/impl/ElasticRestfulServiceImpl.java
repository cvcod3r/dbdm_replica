package com.dbms.core.dbase.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dbms.core.dbase.IElasticRestfulService;
import com.dbms.entity.DbaseEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ElasticRestfulServiceImpl implements IElasticRestfulService {

    @Override
    public Map<String, Object> executeStatementRestHttp(DbaseEntity dbaseEntity, String query) throws Exception {
//        System.out.println(query);
        Map<String, String> parsedQuery = parseQuery(query);
        String method = parsedQuery.get("method");
        String endpoint = parsedQuery.get("endpoint");
        String data = parsedQuery.getOrDefault("data", null);
//        System.out.println(method);
//        System.out.println(endpoint);
//        System.out.println(data);
        String result = executeRestRequest(dbaseEntity, method, endpoint, data);
//        System.out.println(result);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("result", result);
        resultMap.put("method", method);
        resultMap.put("endpoint", endpoint);
        return resultMap;
    }

    @Override
    public Map<String, String> parseQuery(String query) throws Exception{
        Map<String, String> result = new HashMap<>();
        String[] parts = query.split("\\{", 2);
        String methodEndpoint = parts[0];
        String[] methodEndpointParts = methodEndpoint.split("\\s", 2);
        String method = methodEndpointParts[0];
        String endpoint = methodEndpointParts[1].trim();
        if (!query.contains("{")){
            result.put("method", method);
            result.put("endpoint", endpoint);
            result.put("data", null);
            return result;
        }
        String data = "{" + parts[1];
        result.put("method", method);
        result.put("endpoint", endpoint);
        result.put("data", data);
        return result;
    }
    @Override
    public String executeRestRequest(DbaseEntity dbaseEntity, String method, String endpoint, String data) throws Exception {
        RestClient client = RestClient.builder(
                new HttpHost(dbaseEntity.getHost(), Integer.parseInt(dbaseEntity.getPort()))).build();
        Request request = new Request(method, endpoint);

        if (data != null) {
            request.setEntity(new NStringEntity(data, ContentType.APPLICATION_JSON));
        }

        Response response = client.performRequest(request);
        String responseBody = EntityUtils.toString(response.getEntity());

        client.close();

        return responseBody;
    }

    @Override
    public String executeRequest(DbaseEntity dbaseEntity, String method, String endpoint, String data) throws Exception {
        String url = dbaseEntity.getUrl() + "/" + endpoint;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        // 设置请求方法
        con.setRequestMethod(method);
        // 添加请求头
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        if (data != null) {
            // 发送请求数据
            con.setDoOutput(true);
            con.getOutputStream().write(data.getBytes(StandardCharsets.UTF_8));
        }

        // 获取响应结果
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    // 获取索引
    @Override
    public List<String> getIndices(DbaseEntity dbaseEntity) throws Exception {
        String result = executeRestRequest(dbaseEntity, "GET", "_cat/indices?format=json", null);
        List<String> indices = new ArrayList<>();
        JSONArray jsonArray = JSONArray.parseArray(result);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String index = jsonObject.getString("index");
            indices.add(index);
        }
//        System.out.println("All indices: " + indices.toString());
        return indices;
    }

    @Override
    public List<Map<String, Object>> getIndicesMetas(DbaseEntity dbaseEntity) throws Exception {
        List<Map<String, Object>> indicesMetas = new ArrayList<>();
        List<String> indices = getIndices(dbaseEntity);
        for (String index : indices) {
            String result = executeRestRequest(dbaseEntity, "GET", index + "/_mapping", null);
            JSONObject jsonObject = JSONObject.parseObject(result);
            JSONObject mappingsProperties = jsonObject.getJSONObject(index).getJSONObject("mappings").getJSONObject("properties");
            for (String key : mappingsProperties.keySet()) {
                Map<String, Object> indexMeta = new HashMap<>();
                indexMeta.put("TABLE_NAME", index);
                indexMeta.put("COLUMN_NAME", key);
                indicesMetas.add(indexMeta);
            }
        }
//        System.out.println("All indices metas: " + indicesMetas.toString());
        return indicesMetas;
    }

    /**
     * 获取索引和文档，类似数据库和表
     * @param dbaseEntity
     * @return
     */
    @Override
    public Map<String, List<String>> getTables(DbaseEntity dbaseEntity) throws Exception{
        List<String> indices = getIndices(dbaseEntity);
        Map<String, List<String>> indicesMap = new HashMap<>();
        indicesMap.put("elasticsearch", indices);
        System.out.println("All indexes: " + indicesMap.toString());
        return indicesMap;
    }
}
