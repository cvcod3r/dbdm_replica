package com.dbms.repository.impl;

import com.dbms.bean.SensitiveBean;
import com.dbms.config.GlobalConfig;
import com.dbms.core.dbase.*;
import com.dbms.entity.DbaseEntity;
import com.dbms.enums.SqlTypeEnum;
import com.dbms.repository.BigDataOpService;
import com.dbms.repository.IDesensitizationService;
import com.dbms.service.LogRiskAlertService;
import com.dbms.utils.DbInfoUtil;
import com.dbms.utils.GlobalMessageUtil;
import com.dbms.utils.ip.IpUtils;
import org.apache.hadoop.hbase.client.Connection;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import com.dbms.bean.HBaseRiskBean;
import com.dbms.repository.IRiskOperationService;


import javax.servlet.http.HttpServletRequest;

import static com.dbms.utils.SecurityUtils.getGroupId;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dbms.utils.SecurityUtils.getUserId;

@Service
public class BigDataOpServiceImpl implements BigDataOpService {

    private static final Logger logger = LoggerFactory.getLogger(DbopServiceImpl.class);

    @Autowired
    private IHBaseUtilService hbaseUtilService;

    @Autowired
    private IElasticUtilService elasticUtilService;

    @Autowired
    private IElasticRestfulService elasticRestfulService;

    @Autowired
    private LogRiskAlertService logRiskAlertService;

    @Autowired
    private IHBaseShellParser hbaseShellParser;

    @Autowired
    private IRiskOperationService iriskOperationService;

    @Autowired
    private IDesensitizationService desensitizationService;

    @Autowired
    private GlobalConfig globalConfig;

    @Autowired
    HttpServletRequest request;
    // 执行语句
    /**
     * 分流， HBASE还是ES
     *
     */
    @Override
    public Map<String, Object> executeSQLStatementForHBase(DbaseEntity dbaseEntity, String sql)  {
        // 获取连接key
        String connKey = DbInfoUtil.getConnKey(dbaseEntity, getUserId());
        // 获取操作类型
        SqlTypeEnum sqlTypeEnum = null;
        HBaseShellMeta shellMeta;
        Map<String, Object> map = new HashMap<>();
//        List<Map<String, Object>>
        String mess = null;
        String status = null;
        String actionType = null;
        List<Map<String, Object>> resultList = null;
        List<Map<String, Object>> columns = null;
        StopWatch clock = new StopWatch();
        String ip = IpUtils.getIpAddress(request);
        try {
            System.out.println("============================高危操作验证============================");
            sqlTypeEnum = hbaseShellParser.getSqlType(sql);
            map.put("operator", sqlTypeEnum.getName());
            System.out.println("sqlType:::" + sqlTypeEnum.getName());
            String shellsql = sqlTypeEnum.getName();
            //            通过sql语句获取shell命令
            shellMeta = hbaseShellParser.getHBaseShellMeta(sql);
            // 高危操作
            HBaseRiskBean hBaseRiskBean = iriskOperationService.checkHBaseShellRiskOperation(shellMeta,shellsql,getUserId(),getGroupId(),dbaseEntity.getDbId());
            //Hbase风险告警
            logRiskAlertService.LogRiskAlterForHbase(dbaseEntity,sql,ip);
            boolean agentFlag = globalConfig.isRiskFlag();
            if (hBaseRiskBean.isRiskFlag() || !agentFlag) {
                clock.start();
                mess = "";
                switch (sqlTypeEnum){
                    case CREATE:
                        hbaseUtilService.createTable(sql, connKey);
                        break;
                    case PUT:
                        hbaseUtilService.putData(sql, connKey);
                        break;
                    case GET:
                        resultList = hbaseUtilService.getData(sql, connKey);
                        SensitiveBean sensitiveBeanGet = desensitizationService.getSensitiveOptionsForHbase(shellMeta,getUserId(),getGroupId(),dbaseEntity.getDbId());
                        resultList = desensitizationService.dynamicDesensitizationForHbase(resultList,sensitiveBeanGet);
                        columns = hbaseUtilService.listSomeColumns(Arrays.asList("ROWKEY", "COLUMN", "CELL"));
                        // 脱敏
                        break;
                    case DROP:
                        hbaseUtilService.dropTable(sql, connKey);
                        break;
                    case ALTER:
                        hbaseUtilService.alterData(sql, connKey);
                        break;
                    case SCAN:
                        // 脱敏
                        resultList = hbaseUtilService.scanData(sql, connKey);
                        SensitiveBean sensitiveBeanScan = desensitizationService.getSensitiveOptionsForHbase(shellMeta,getUserId(),getGroupId(),dbaseEntity.getDbId());
                        resultList = desensitizationService.dynamicDesensitizationForHbase(resultList,sensitiveBeanScan);
                        columns = hbaseUtilService.listSomeColumns(Arrays.asList("ROWKEY", "COLUMN", "CELL"));
                        break;
                    case LIST:
                        resultList = hbaseUtilService.listTable(sql, connKey);
                        columns = hbaseUtilService.listColumns("TABLE");
                        break;
                    case LIST_NAMESPACE:
                        resultList = hbaseUtilService.listNameSpaces(connKey);
                        columns = hbaseUtilService.listColumns("NAMESPACE");
                        break;
                    case LIST_NAMESPACE_TABLES:
                        resultList = hbaseUtilService.listNameSpaceTables(sql, connKey);
                        columns = hbaseUtilService.listColumns("TABLE");
                        break;
                    case DISABLE:
                        hbaseUtilService.disableTable(sql, connKey);
                        break;
                    case ENABLE:
                        hbaseUtilService.enableTable(sql, connKey);
                        break;
                    case DESCRIBE:
                    case DESC:
                        Map<String, Object> descResult = hbaseUtilService.descTable(sql, connKey);
                        resultList = (List<Map<String, Object>>) descResult.getOrDefault("resultList", null);
                        columns = (List<Map<String, Object>>) descResult.getOrDefault("columns", null);
                        break;
                    case EXIST:
                        boolean res = hbaseUtilService.existTable(sql, connKey);
                        if(res)
                            mess += "表存在，语句";
                        else
                            mess += "表不存在，语句";
                        break;
                    case TRUNCATE:
                        boolean trunRes = hbaseUtilService.truncateTable(sql, connKey);
                        if(trunRes){
                            mess += "表清空成功，语句";
                        }
                        else{
                            mess += "表清空失败，语句";
                        }
                        break;
                    case GRANT:
                        boolean grantRes = hbaseUtilService.grant(sql, connKey);
                        if(grantRes){
                            mess += "权限赋予成功，语句";
                        }
                        else{
                            mess += "权限赋予失败，语句";
                        }
                        break;
                    case REVOKE:
                        boolean revokeRes = hbaseUtilService.revoke(sql, connKey);
                        if(revokeRes){
                            mess += "权限回收成功，语句";
                        }
                        else{
                            mess += "权限回收失败，语句";
                        }
                        break;
                    case CREATE_NAMESPACE:
                        boolean createNameSpaceRes = hbaseUtilService.createNameSpace(sql, connKey);
                        if(createNameSpaceRes){
                            mess += "创建命名空间成功，语句";
                        }
                        else{
                            mess += "创建命名空间失败，语句";
                        }
                        break;
                    case DROP_NAMESPACE:
                        boolean dropNameSpaceRes = hbaseUtilService.dropNameSpace(sql, connKey);
                        if(dropNameSpaceRes){
                            mess += "删除命名空间成功，语句";
                        }
                        else{
                            mess += "删除命名空间失败，语句";
                        }
                        break;
                    default:
                        break;
                }
                clock.stop();
                mess += "执行成功！";
                status = "success";
            }
            else {
                mess = GlobalMessageUtil.operationIntercept;
                status= "fail";
                actionType=hBaseRiskBean.getActionType();
                if (actionType.equals("A")){
//                    System.out.println("断开连接");
                    mess = GlobalMessageUtil.sessionClosedMessage;
                    closeConn(dbaseEntity);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            mess = e.getMessage();
            status = "fail";
        }
        map.put("time", clock.getTotalTimeMillis());
//        map.put("tables", riskBean.getTables());
//        map.put("tableColumns", riskBean.getTableColumns());
        map.put("result", resultList);
        map.put("columns", columns);
        map.put("mess", mess);
        map.put("status", status);
        map.put("actionType", actionType);
        map.put("sql", sql);
        return map;
    }

    @Override
    public Map<String, Object> executeSQLStatementForHBaseOrder(DbaseEntity dbaseEntity, String sql) {
        // 获取连接key
        String connKey = DbInfoUtil.getConnKey(dbaseEntity, getUserId());
        // 获取操作类型
        SqlTypeEnum sqlTypeEnum = null;
        HBaseShellMeta shellMeta;
        Map<String, Object> map = new HashMap<>();
//        List<Map<String, Object>>
        String mess = null;
        String status = null;
        String actionType = null;
        List<Map<String, Object>> resultList = null;
        List<Map<String, Object>> columns = null;
        StopWatch clock = new StopWatch();
        String ip = IpUtils.getIpAddress(request);
        try {
            System.out.println("============================高危操作验证============================");
            sqlTypeEnum = hbaseShellParser.getSqlType(sql);
            map.put("operator", sqlTypeEnum.getName());
//            System.out.println("sqlType:::" + sqlTypeEnum.getName());
            String shellsql = sqlTypeEnum.getName();
            //            通过sql语句获取shell命令
            shellMeta = hbaseShellParser.getHBaseShellMeta(sql);
            // 高危操作
            HBaseRiskBean hBaseRiskBean = iriskOperationService.checkHBaseShellRiskOperation(shellMeta,shellsql,getUserId(),getGroupId(),dbaseEntity.getDbId());
            actionType = hBaseRiskBean.getActionType();
            //Hbase风险告警
//            logRiskAlertService.LogRiskAlterForHbase(dbaseEntity,sql,ip);
            clock.start();
            mess = "";
            switch (sqlTypeEnum){
                case CREATE:
                    hbaseUtilService.createTable(sql, connKey);
                    break;
                case PUT:
                    hbaseUtilService.putData(sql, connKey);
                    break;
                case GET:
                    resultList = hbaseUtilService.getData(sql, connKey);
                    SensitiveBean sensitiveBeanGet = desensitizationService.getSensitiveOptionsForHbase(shellMeta,getUserId(),getGroupId(),dbaseEntity.getDbId());
                    resultList = desensitizationService.dynamicDesensitizationForHbase(resultList,sensitiveBeanGet);
                    columns = hbaseUtilService.listSomeColumns(Arrays.asList("ROWKEY", "COLUMN", "CELL"));
                    // 脱敏
                    break;
                case DROP:
                    hbaseUtilService.dropTable(sql, connKey);
                    break;
                case ALTER:
                    hbaseUtilService.alterData(sql, connKey);
                    break;
                case SCAN:
                    // 脱敏
                    resultList = hbaseUtilService.scanData(sql, connKey);
                    SensitiveBean sensitiveBeanScan = desensitizationService.getSensitiveOptionsForHbase(shellMeta,getUserId(),getGroupId(),dbaseEntity.getDbId());
                    resultList = desensitizationService.dynamicDesensitizationForHbase(resultList,sensitiveBeanScan);
                    columns = hbaseUtilService.listSomeColumns(Arrays.asList("ROWKEY", "COLUMN", "CELL"));
                    break;
                case LIST:
                    resultList = hbaseUtilService.listTable(sql, connKey);
                    columns = hbaseUtilService.listColumns("TABLE");
                    break;
                case LIST_NAMESPACE:
                    resultList = hbaseUtilService.listNameSpaces(connKey);
                    columns = hbaseUtilService.listColumns("NAMESPACE");
                    break;
                case LIST_NAMESPACE_TABLES:
                    resultList = hbaseUtilService.listNameSpaceTables(sql, connKey);
                    columns = hbaseUtilService.listColumns("TABLE");
                    break;
                case DISABLE:
                    hbaseUtilService.disableTable(sql, connKey);
                    break;
                case ENABLE:
                    hbaseUtilService.enableTable(sql, connKey);
                    break;
                case DESCRIBE:
                case DESC:
                    Map<String, Object> descResult = hbaseUtilService.descTable(sql, connKey);
                    resultList = (List<Map<String, Object>>) descResult.getOrDefault("resultList", null);
                    columns = (List<Map<String, Object>>) descResult.getOrDefault("columns", null);
                    break;
                case EXIST:
                    boolean res = hbaseUtilService.existTable(sql, connKey);
                    if(res)
                        mess += "表存在，语句";
                    else
                        mess += "表不存在，语句";
                    break;
                case TRUNCATE:
                    boolean trunRes = hbaseUtilService.truncateTable(sql, connKey);
                    if(trunRes){
                        mess += "表清空成功，语句";
                    }
                    else{
                        mess += "表清空失败，语句";
                    }
                    break;
                case GRANT:
                    boolean grantRes = hbaseUtilService.grant(sql, connKey);
                    if(grantRes){
                        mess += "权限赋予成功，语句";
                    }
                    else{
                        mess += "权限赋予失败，语句";
                    }
                    break;
                case REVOKE:
                    boolean revokeRes = hbaseUtilService.revoke(sql, connKey);
                    if(revokeRes){
                        mess += "权限回收成功，语句";
                    }
                    else{
                        mess += "权限回收失败，语句";
                    }
                    break;
                case CREATE_NAMESPACE:
                    boolean createNameSpaceRes = hbaseUtilService.createNameSpace(sql, connKey);
                    if(createNameSpaceRes){
                        mess += "创建命名空间成功，语句";
                    }
                    else{
                        mess += "创建命名空间失败，语句";
                    }
                    break;
                case DROP_NAMESPACE:
                    boolean dropNameSpaceRes = hbaseUtilService.dropNameSpace(sql, connKey);
                    if(dropNameSpaceRes){
                        mess += "删除命名空间成功，语句";
                    }
                    else{
                        mess += "删除命名空间失败，语句";
                    }
                    break;
                default:
                    break;
            }
            clock.stop();
            mess += "执行成功！";
            status = "success";
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            mess = e.getMessage();
            status = "fail";
        }
        map.put("time", clock.getTotalTimeMillis());
//        map.put("tables", riskBean.getTables());
//        map.put("tableColumns", riskBean.getTableColumns());
        map.put("result", resultList);
        map.put("columns", columns);
        map.put("mess", mess);
        map.put("status", status);
        map.put("actionType", actionType);
        map.put("sql", sql);
        return map;
    }

    @Override
    public Map<String, Object> executeSQLStatementForElastic(DbaseEntity dbaseEntity, String sql) {
// 获取连接key
        String connKey = DbInfoUtil.getConnKey(dbaseEntity, getUserId());
        // 获取操作类型

        Map<String, Object> map = new HashMap<>();
        map.put("sql", sql);
//        map.put("operator", sqlTypeEnum.getName());
//        List<Map<String, Object>>
        String mess = null;
        String status = null;
        String actionType = null;

        List<Map<String, Object>> resultList = null;
        List<Map<String, Object>> columns = null;
        StopWatch clock = new StopWatch();
        clock.start();
        try {
            // 执行语句
            Map<String, Object> result = elasticUtilService.executeSqlByRestClient(dbaseEntity, connKey, sql);
            // 结果集
            resultList = (List<Map<String, Object>>) result.getOrDefault("rows", null);
            // 列信息
            columns = (List<Map<String, Object>>) result.getOrDefault("columns", null);
            mess = "执行成功！";
            status = "success";
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            mess = e.getMessage();
            status = "fail";
        }
        clock.stop();
        map.put("time", clock.getTotalTimeMillis());
//        map.put("tables", riskBean.getTables());
//        map.put("tableColumns", riskBean.getTableColumns());
        map.put("result", resultList);
        map.put("columns", columns);
        map.put("mess", mess);
        map.put("status", status);
        map.put("actionType", actionType);
        return map;
    }

    @Override
    public Map<String, Object> executeSQLStatementForElasticRestful(DbaseEntity dbaseEntity, String sql) {
        Map<String, Object> map = new HashMap<>();
        map.put("sql", sql);
        String mess = null;
        String status = null;
        String actionType = null;
        String resultString = null;
        String endpoint = null;
        StopWatch clock = new StopWatch();
        clock.start();
        try {
            // 执行语句
            Map<String, Object> result = elasticRestfulService.executeStatementRestHttp(dbaseEntity, sql);
            // 结果集
            resultString = (String) result.getOrDefault("result", null);
            actionType = (String) result.getOrDefault("method", null);
            endpoint = (String) result.getOrDefault("endpoint", null);
            // 列信息
            mess = "执行成功！";
            status = "success";
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            mess = e.getMessage();
            status = "fail";
        }
        clock.stop();
        map.put("time", clock.getTotalTimeMillis());
        map.put("result", resultString);
//        map.put("columns", columns);
        map.put("mess", mess);
        map.put("status", status);
        map.put("schemas", endpoint);
        map.put("operator", actionType);
        return map;
    }


    @Override
    public void closeConn(DbaseEntity dbaseEntity) {
        String connKey = DbInfoUtil.getConnKey(dbaseEntity, getUserId());
        if (dbaseEntity.getDbType().equals("HBaseShell")){
            try {
                hbaseUtilService.closeConn(connKey);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else if (dbaseEntity.getDbType().equals("ElasticRest")){
            try {
//                elasticUtilService.closeConn(connKey);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void getConnectionSession(DbaseEntity dbaseEntity) {
        String connKey = DbInfoUtil.getConnKey(dbaseEntity, getUserId());
        if (dbaseEntity.getDbType().equals("HBaseShell")){
            try {
                hbaseUtilService.getConnection(dbaseEntity, connKey);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else if (dbaseEntity.getDbType().equals("ElasticRest")){
            try {
//                elasticUtilService.getConnection(dbaseEntity, connKey);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public Map<String, List<String>> getTables(DbaseEntity dbaseEntity) {
        Map<String, List<String>> tables = null;
        String connKey = DbInfoUtil.getConnKey(dbaseEntity, getUserId());
        if (dbaseEntity.getDbType().equals("HBaseShell")){
            try {
                tables = hbaseUtilService.getTables(connKey);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else if (dbaseEntity.getDbType().equals("ElasticRest")){
            try {
                tables = elasticRestfulService.getTables(dbaseEntity);
//                tables = elasticUtilService.getTables(connKey);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return tables;
    }

    @Override
    public boolean testConn(DbaseEntity dbaseEntity) {
        if (dbaseEntity.getDbType().equals("HBaseShell")){
            try {
                hbaseUtilService.setHost(dbaseEntity.getHost(), dbaseEntity.getDbName());
                Connection connection = hbaseUtilService.getConnection(dbaseEntity);
                return connection != null;
            } catch (Exception e) {

            }
        } else if (dbaseEntity.getDbType().equals("ElasticRest")){
            try {
                RestClient client = elasticUtilService.getConnection(dbaseEntity);
                return client != null;
            } catch (Exception e) {

            }
        }
        return false;
    }
    @Override
    public List<String> getHBaseNameSpace(DbaseEntity dbaseEntity) throws Exception {
        return hbaseUtilService.listNameSpacesByDbaseEntity(dbaseEntity);
    }

    @Override
    public List<Map<String, Object>> getHBaseTableMetas(DbaseEntity dbaseEntity, String schemaName) throws Exception {
        return hbaseUtilService.getHBaseTableMetas(dbaseEntity, schemaName);
    }

    @Override
    public List<String> getElasticIndex(DbaseEntity dbaseEntity) throws Exception {
        return elasticRestfulService.getIndices(dbaseEntity);
    }

    @Override
    public List<Map<String, Object>> getElasticIndexMetas(DbaseEntity dbaseEntity, String schemaName) throws Exception {

        return elasticRestfulService.getIndicesMetas(dbaseEntity);
    }
}
