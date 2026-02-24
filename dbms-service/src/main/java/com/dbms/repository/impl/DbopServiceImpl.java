package com.dbms.repository.impl;

import com.alibaba.fastjson.JSON;
import com.dbms.bean.RiskBean;
import com.dbms.bean.SensitiveBean;
import com.dbms.config.GlobalConfig;
import com.dbms.core.BusiDataBaseUtil;
import com.dbms.core.dbase.DataBaseUtilService;
import com.dbms.core.dbase.IHBaseShellParser;
import com.dbms.entity.*;
import com.dbms.enums.SqlTypeEnum;
import com.dbms.repository.DbopService;
import com.dbms.repository.IDesensitizationService;
import com.dbms.repository.IRiskOperationService;
import com.dbms.service.LogRiskAlertService;
import com.dbms.utils.DbInfoUtil;
import com.dbms.utils.GlobalMessageUtil;
import com.dbms.utils.ip.IpUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import com.dbms.domain.dbase.MetaInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.dbms.utils.SecurityUtils.*;


@Service
public class DbopServiceImpl implements DbopService {

    private static final Logger logger = LoggerFactory.getLogger(DbopServiceImpl.class);

    @Autowired
    private IDesensitizationService desensitizationService;

    @Autowired
    private IRiskOperationService riskOperationService;

    @Autowired
    private LogRiskAlertService logRiskAlertService;

    @Autowired
    private DataBaseUtilService dataBaseUtilService;

    @Autowired
    private GlobalConfig globalConfig;


    public static List<String> selectOperation = Arrays.asList("SELECT", "SHOW", "DESC", "DESCRIBE");


    @Override
    public boolean testConn(DbaseEntity dbaseEntity) {
//        dbaseEntity.setUrl(DbInfoUtil.getURL(dbaseEntity));
        return dataBaseUtilService.testConnection(dbaseEntity);
    }


    @Override
    public List<String> getSchemas(DbaseEntity dbaseEntity) {
        return dataBaseUtilService.getSchemas(dbaseEntity);
    }

    @Override
    public List<String> getTables(DbaseEntity dbaseEntity, String schemaName) {
        String connKey = DbInfoUtil.getConnKey(dbaseEntity, getUserId(), schemaName);
        return dataBaseUtilService.getTables(dbaseEntity, connKey);
    }

    @Override
    public List<String> getViews(DbaseEntity dbaseEntity, String schemaName) {
        String connKey = DbInfoUtil.getConnKey(dbaseEntity,getUserId(), schemaName);
        return dataBaseUtilService.getViews(dbaseEntity, connKey);
    }

    @Override
    public List<Map<String, Object>> getTableMetas(DbaseEntity dbaseEntity,String schemaName) {
        String connKey = DbInfoUtil.getConnKey(dbaseEntity,getUserId(), schemaName);
        return dataBaseUtilService.getTableMetas(dbaseEntity, connKey);
    }

    @Override
    public List<Map<String, Object>> getFuncs(DbaseEntity dbaseEntity, String schemaName) {
        String connKey = DbInfoUtil.getConnKey(dbaseEntity,getUserId(), schemaName);
        return dataBaseUtilService.getFuncs(dbaseEntity, connKey);
    }
    @Autowired
    HttpServletRequest request;

    @Override
    public List<String> getTablesByType(DbaseEntity dbaseEntity, String schemaName) {

        List<String> tableList = new ArrayList<>();
        List<Map<String, Object>> tables = new ArrayList<>();
        String dbType = dbaseEntity.getDbType();
        BusiDataBaseUtil db = new BusiDataBaseUtil(dbaseEntity, schemaName);
        String connKey = DbInfoUtil.getConnKey(dbaseEntity,getUserId(), schemaName);
        try {
            if (dbType.equals("DM")) {
                String sql = " select TABLE_NAME from all_tables where owner='" + dbaseEntity.getDbName() + "'";
                tables =  db.queryForList(sql,connKey);
            }
            if (dbType.equals("GBase")) {
                String sql = " select tabname as TABLE_NAME from systables,systabauth where systables.tabid=systabauth.tabid and tabtype='T' and systabauth.tabauth like 'su%' and  systabauth.grantee='public' and systables.owner='" + dbaseEntity.getUsername() + "';";
                tables =  db.queryForList(sql,connKey);
            }
            if (dbType.equals("KingBase")) {
                String sql = " SELECT TABLE_NAME from information_schema.TABLES WHERE table_schema='public' and table_type='BASE TABLE';";
                tables =  db.queryForList(sql,connKey);

            }
            if (dbType.equals("MySql")) {
                String sql = " select TABLE_NAME from information_schema.TABLES where table_schema='" + dbaseEntity.getDbName() + "' and table_type='BASE TABLE';";
                tables =  db.queryForList(sql,connKey);
            }
            if (dbType.equals("Oracle")) {
                String sql = " select TABLE_NAME from dba_tables where owner='" + dbaseEntity.getUsername() + "'";
                tables = db.queryForList(sql,connKey);
            }
            if (dbType.equals("MSSQL")) {
                String sql = " SELECT Name as TABLE_NAME FROM " + dbaseEntity.getDbName() + "..SysObjects Where XType='U' ORDER BY Name ";
                tables = db.queryForList(sql,connKey);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
        for (Map<String,Object> table: tables) {
            String table_name = (String) table.getOrDefault("TABLE_NAME", null);
            if (table_name == null){
                table_name = (String) table.get("table_name");
            }
            tableList.add(table_name);
        }
        return tableList;
    }

    @Override
    public List<String> getViewsByType(DbaseEntity dbaseEntity, String schemaName) {
        List<String> views = new ArrayList<>();
        List<Map<String, Object>> viewList = new ArrayList<>();
        BusiDataBaseUtil db = new BusiDataBaseUtil(dbaseEntity, schemaName);
        String connKey = DbInfoUtil.getConnKey(dbaseEntity,getUserId(), schemaName);
        String dbType = dbaseEntity.getDbType();
        try {
            if (dbType.equals("MySql")) {
                String sql = " select TABLE_NAME from information_schema.TABLES where table_schema='" + dbaseEntity.getDbName() + "' and table_type='VIEW' ";
                viewList =  db.queryForList(sql,connKey);
            }else if (dbType.equals("Oracle")) {
                String sql = "select view_name as TABLE_NAME from all_views where owner='" + dbaseEntity.getUsername() + "'";
                viewList = db.queryForList(sql,connKey);
            }else if (dbType.equals("MSSQL")) {
                String sql = " SELECT NAME AS TABLE_NAME FROM sysobjects where XTYPE ='V'";
                viewList = db.queryForList(sql,connKey);
            }else if (dbType.equals("DM")) {
                String sql = "select view_name as TABLE_NAME from user_views where owner='" + dbaseEntity.getDbName() + "'";
                viewList = db.queryForList(sql,connKey);
            }else if (dbType.equals("GBase")) {
                String sql = "select tabname as TABLE_NAME from systables where tabtype='V' and owner='" + dbaseEntity.getUsername() + "'";
                viewList = db.queryForList(sql,connKey);
            }else if (dbType.equals("KingBase")) {
                String sql = " SELECT TABLE_NAME from information_schema.TABLES WHERE table_schema='public' and table_type='VIEW'";
                viewList = db.queryForList(sql,connKey);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
        for (Map<String,Object> view: viewList) {
            String view_name = (String) view.getOrDefault("TABLE_NAME", null);
            if (view_name == null){
                view_name = (String) view.get("table_name");
            }
            views.add(view_name);
        }
        return views;
    }

    @Override
    public List<Map<String, Object>> getFuncsByType(DbaseEntity dbaseEntity, String schemaName) {

        List<Map<String, Object>> funcs = new ArrayList<>();
        String connKey = DbInfoUtil.getConnKey(dbaseEntity,getUserId(), schemaName);
        BusiDataBaseUtil db = new BusiDataBaseUtil(dbaseEntity, schemaName);
        String dbType = dbaseEntity.getDbType();
        try {
            if (dbType.equals("MySql")) {
                String sql = " select ROUTINE_NAME,ROUTINE_TYPE from information_schema.ROUTINES where routine_schema='" + dbaseEntity.getDbName() + "' ";
                funcs =  db.queryForList(sql,connKey);
            } else if (dbType.equals("Oracle")) {
                String sql = " select object_name as ROUTINE_NAME,object_type as ROUTINE_TYPE from user_objects where object_type='PROCEDURE' OR object_type='FUNCTION' ";
                funcs = db.queryForList(sql,connKey);
            } else if (dbType.equals("MSSQL")) {
                String sql = " SELECT name as ROUTINE_NAME,(CASE XTYPE WHEN 'P' THEN 'PROCEDURE' ELSE 'FUNCTION' END) as ROUTINE_TYPE FROM sysobjects where XTYPE='P' OR XTYPE='FN'";
                funcs = db.queryForList(sql,connKey);
            } else if (dbType.equals("DM")) {
                String sql = " select object_name as ROUTINE_NAME,object_type as ROUTINE_TYPE from user_objects where object_type='PROCEDURE' OR object_type='FUNCTION' ";
                funcs = db.queryForList(sql,connKey);
            } else if (dbType.equals("GBase")) {
                String sql = " select object_name as ROUTINE_NAME,object_type as ROUTINE_TYPE from user_objects where object_type='PROCEDURE' OR object_type='FUNCTION' ";
                funcs = db.queryForList(sql,connKey);
            } else if (dbType.equals("KingBase")) {
                String sql = " select ROUTINE_NAME,ROUTINE_TYPE from information_schema.ROUTINES where routine_schema='public'; ";
                funcs = db.queryForList(sql,connKey);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
        return funcs;
    }

    @Override
    public List<Map<String, Object>> getTableMeta(DbaseEntity dbaseEntity, String schemaName) {
        BusiDataBaseUtil db = new BusiDataBaseUtil(dbaseEntity, schemaName);
        List<Map<String, Object>> tableMetaList = null;
        String dbType = dbaseEntity.getDbType();
        try {
            if (dbType.equals("MySql")) {
                String sql = "select table_name,column_name from information_schema.columns where table_schema='" + dbaseEntity.getDbName() + "' ";
                tableMetaList =  db.queryForList(sql);
                if (tableMetaList!=null){
                    return tableMetaList;
                }
            } else if (dbType.equals("Oracle")) {
                String sql = "select TABLE_NAME,COLUMN_NAME from all_tab_columns where owner='" + dbaseEntity.getUsername() + "'";
                tableMetaList = db.queryForList(sql);
                if (tableMetaList!=null){
                    return tableMetaList;
                }
            } else if (dbType.equals("MSSQL")) {
                String sql = "select TABLE_NAME,COLUMN_NAME from information_schema.columns where table_schema='dbo' ";
                tableMetaList = db.queryForList(sql);
                if (tableMetaList!=null){
                    return tableMetaList;
                }
            } else if (dbType.equals("DM")) {
                String sql = "select TABLE_NAME,COLUMN_NAME from all_tab_columns where owner='" + dbaseEntity.getDbName() + "'";
                tableMetaList = db.queryForList(sql);
                if (tableMetaList!=null){
                    return tableMetaList;
                }
            } else if (dbType.equals("GBase")) {
                String sql = "select tabname as TABLE_NAME,colname as COLUMN_NAME from systables,systabauth,syscolumns where systables.tabid=systabauth.tabid and systables.tabid=syscolumns.tabid and tabtype='T' and systabauth.tabauth like 'su%' and  systabauth.grantee='public' and systables.owner='" + dbaseEntity.getUsername() + "'";
                tableMetaList = db.queryForList(sql);
                if (tableMetaList!=null){
                    return tableMetaList;
                }
            } else if (dbType.equals("KingBase")) {
                String sql = "select A.TABLE_NAME,COLUMN_NAME from information_schema.columns as A,information_schema.tables as B where A .table_schema = 'public' and A .table_name = B.table_name and B.table_type = 'BASE TABLE';";
                tableMetaList = db.queryForList(sql);
                if (tableMetaList!=null){
                    return tableMetaList;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> getTableColumns(DbaseEntity dbaseEntity, String schemaName, String tableName) {
        List<Map<String, Object>> tableMetaList = null;
        BusiDataBaseUtil db = new BusiDataBaseUtil(dbaseEntity, schemaName);
        String dbType = dbaseEntity.getDbType();
        try {
            if (dbType.equals("DM")) {
                String sql = "select COLUMN_NAME from all_tab_columns where TABLE_NAME='" + tableName +"'";
                tableMetaList = db.queryForList(sql);
                if (tableMetaList!=null){
                    return tableMetaList;
                }
            } else if (dbType.equals("GBase")) {
                String sql = "select colname as COLUMN_NAME from systables, syscolumns where systables.tabid=syscolumns.tabid and tabtype='T' and systables.owner='" + dbaseEntity.getUsername() + "' AND tabname='" + tableName +"';";
                tableMetaList = db.queryForList(sql);
                if (tableMetaList!=null){
                    return tableMetaList;
                }
            } else if (dbType.equals("KingBase")) {
                String sql = "select COLUMN_NAME from all_tab_columns where TABLE_NAME='" + tableName.toUpperCase() +"' and owner='" + dbaseEntity.getUsername().toUpperCase() + "'";
                tableMetaList = db.queryForList(sql);
                if (tableMetaList!=null){
                    return tableMetaList;
                }
            }  else if (dbType.equals("MySql")) {
                String sql = " select column_name from information_schema.columns where table_schema='"
                        + dbaseEntity.getDbName() + "' and table_name='" + tableName +"'";
                tableMetaList =  db.queryForList(sql);
                if (tableMetaList!=null){
                    return tableMetaList;
                }
            }  else if (dbType.equals("Oracle")) {
                String sql = "select COLUMN_NAME from user_tab_columns where TABLE_NAME='" + tableName +"'";
                tableMetaList = db.queryForList(sql);
                if (tableMetaList!=null){
                    return tableMetaList;
                }
            } else if (dbType.equals("MSSQL")) {
                String sql = " select COLUMN_NAME from information_schema.columns where table_schema='dbo' " +
                        "and TABLE_NAME='" + tableName + "'";
                tableMetaList = db.queryForList(sql);
                if (tableMetaList!=null){
                    return tableMetaList;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
        return null;
    }

    @Override
    public Map<String, Object> executeSQLStatement(DbaseEntity dbaseEntity, String schemaName, String sql) {
        String connKey = DbInfoUtil.getConnKey(dbaseEntity, getUserId(), schemaName);
//        System.out.println("======执行SQL语句: " + sql);
        Map<String, Object> map = new HashMap<>();
        map.put("sql", sql);
        String mess;
        String status;
        String actionType = null;
        String sensitiveRuleName = null;
        String ip = IpUtils.getIpAddress(request);
        int rowCount = 0;
        Integer rowLimitCount = -1;
        // SQL高危操作验证
        try {
//            System.out.println("============================高危操作验证============================");
            RiskBean riskBean = riskOperationService.checkRiskOperation(sql, getUserId(),getGroupId(), schemaName, dbaseEntity);
            String dbType = dbaseEntity.getDbType();
            String shellsql=riskBean.getSqlType();
            System.out.println("dpType"+dbType);
            if(dbType.equals("Hive")){
                logRiskAlertService.LogRiskAlterForHive(dbaseEntity,sql,ip);
            }
            else if(dbType.equals("ElasticSearch")){
                logRiskAlertService.LogRiskAlterForES(dbaseEntity,sql,shellsql,ip);
            }
            boolean agentFlag = globalConfig.isRiskFlag();
            if (riskBean.isRiskFlag()||!agentFlag) {
                String sqlOp =  riskBean.getSqlType();
                if (selectOperation.contains(sqlOp)) {
                    StopWatch clock = new StopWatch();
                    clock.start();
                    List<Map<String, Object>> list=null;
                    List<Map<String, Object>> columns=null;
                    try {
                        // 获取sql执行结果集
                        list = dataBaseUtilService.queryForList(dbaseEntity, sql, connKey);
                        //新增脱敏操作
//                        System.out.println("============================动态脱敏操作============================");
                        SensitiveBean sensitiveBean = desensitizationService.getSensitiveOptions(getUserId(),getGroupId(),dbaseEntity.getDbId(),schemaName,sql);
                        if (sensitiveBean.getRuleNames()!=null){
                            sensitiveRuleName = sensitiveBean.getRuleNames().toString();
                        }
                        list = desensitizationService.dynamicDesensitization(list,sensitiveBean);
//                        System.out.println("list: "+list);
                        // 获取结果集列信息
//                        if (dbaseEntity.getDbType().equals("Oracle")){
//                            sql = "select * from (" + sql + ") where rownum = 1 ";
//                        }
                        columns = dataBaseUtilService.getColumnsFromResultList(list);
                        // 获取影响行数
                        rowCount = list.size();
                        rowLimitCount = riskOperationService.checkTableRowLimits(getUserId(), getGroupId(), schemaName, riskBean.getTables(), dbaseEntity);
                        mess = "执行成功！";
                        status = "success";
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        mess = e.getMessage();
                        status = "fail";
                    }
                    clock.stop();
//                    System.out.println("行数判定");
                    if (rowLimitCount != -1 && rowCount > rowLimitCount && agentFlag) {
//                        System.out.println("行数判定失败");
                        mess = "数据量限制验证失败, 结果集超过" + rowLimitCount + "行, 已拦截!";
                        status = "fail";
                        map.put("rowCount", rowCount);
                        map.put("time", clock.getTotalTimeMillis());
                        actionType = "D";
                    } else {
                        map.put("result", list);
                        map.put("columns", columns);
                        map.put("rowCount", rowCount);
                        map.put("time", clock.getTotalTimeMillis());
                    }
                } else {
//                        if(StringUtils.startsWithAny(sql.toLowerCase(), "update","delete","insert"))
                    StopWatch clock = new StopWatch();
                    clock.start();
                    try
                    {
                        rowCount = dataBaseUtilService.execUpdate(dbaseEntity, sql, connKey);
                        mess = "执行成功！";
                        status = "success";
                    }
                    catch (Exception e)
                    {
                        logger.error(e.getMessage(), e);
                        mess = e.getMessage();
                        status = "fail";
                    }
                    clock.stop();
                    map.put("rowCount", rowCount);
                    map.put("time", clock.getTotalTimeMillis());
                }
            }else{
                mess = GlobalMessageUtil.operationIntercept;
                status = "fail";
                actionType = riskBean.getActionType();
                // 如果高危操作是会话阻断，则断开连接
                if (actionType.equals("A")){
                    mess = GlobalMessageUtil.sessionClosedMessage;
                    closeConn(dbaseEntity);
                }
            }
            map.put("operator",riskBean.getSqlType());
            map.put("schemas", riskBean.getSchemas());
            map.put("tables", riskBean.getTables());
            map.put("tableColumns", riskBean.getTableColumns());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            mess = e.getMessage();
            status = "fail";
        }
        map.put("mess", mess);
        map.put("status", status);
        map.put("actionType", actionType);
        map.put("dataLabels", sensitiveRuleName);
        return map;
    }

    @Override
    public Map<String, Object> executeSQLStatementOrder(DbaseEntity dbaseEntity, String schemaName, String sql) {
        String connKey = DbInfoUtil.getConnKey(dbaseEntity, getUserId(), schemaName);
        System.out.println("======执行工单SQL语句: " + sql);
        Map<String, Object> map = new HashMap<>();
        map.put("sql", sql);
        String mess;
        String status;
        String sensitiveRuleName = null;
        int rowCount = 0;
        // SQL高危操作验证
        try {
            RiskBean riskBean = riskOperationService.checkRiskOperation(sql, getUserId(),getGroupId(), schemaName, dbaseEntity);
            String sqlOp =  riskBean.getSqlType();
            if (selectOperation.contains(sqlOp)) {
                StopWatch clock = new StopWatch();
                clock.start();
                List<Map<String, Object>> list=null;
                List<Map<String, Object>> columns=null;
                try {
                    // 获取sql执行结果集
                    list = dataBaseUtilService.queryForList(dbaseEntity, sql, connKey);
                    //新增脱敏操作
//                        System.out.println("============================动态脱敏操作============================");
                    SensitiveBean sensitiveBean = desensitizationService.getSensitiveOptions(getUserId(),getGroupId(),dbaseEntity.getDbId(),schemaName,sql);
                    if (sensitiveBean.getRuleNames()!=null){
                        sensitiveRuleName = sensitiveBean.getRuleNames().toString();
                    }
                    list = desensitizationService.dynamicDesensitization(list, sensitiveBean);
//                        System.out.println("list: "+list);
                    // 获取结果集列信息
//                    if (dbaseEntity.getDbType().equals("Oracle")){
//                        sql = "select * from (" + sql + ") where rownum = 1 ";
//                    }
//                    columns = dataBaseUtilService.executeSqlForColumns(dbaseEntity, sql, connKey);
//                        System.out.println(columns);
                    columns = dataBaseUtilService.getColumnsFromResultList(list);
                    // 获取影响行数
                    rowCount = list.size();
//                    rowLimitCount = riskOperationService.checkTableRowLimits(getUserId(), getGroupId(), schemaName, riskBean.getTables(), dbaseEntity);
                    mess = "执行成功！";
                    status = "success";
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    mess = e.getMessage();
                    status = "fail";
                }
                clock.stop();
                map.put("result", list);
                map.put("columns", columns);
                map.put("rowCount", rowCount);
                map.put("time", clock.getTotalTimeMillis());
            } else {
//                        if(StringUtils.startsWithAny(sql.toLowerCase(), "update","delete","insert"))
                StopWatch clock = new StopWatch();
                clock.start();
                try
                {
                    rowCount = dataBaseUtilService.execUpdate(dbaseEntity, sql, connKey);
                    mess = "执行成功！";
                    status = "success";
                }
                catch (Exception e)
                {
                    logger.error(e.getMessage(), e);
                    mess = e.getMessage();
                    status = "fail";
                }
                clock.stop();
                map.put("rowCount", rowCount);
                map.put("time", clock.getTotalTimeMillis());
            }
            map.put("operator",riskBean.getSqlType());
            map.put("schemas", riskBean.getSchemas());
            map.put("tables", riskBean.getTables());
            map.put("tableColumns", riskBean.getTableColumns());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            mess = e.getMessage();
            status = "fail";
        }
        map.put("mess", mess);
        map.put("status", status);
        map.put("dataLabels", sensitiveRuleName);
        return map;
    }

    @Override
    public List<Map<String, Object>> executeSQLQueueForDM(DbaseEntity dbaseEntity, String schemaName, List<String> sqlList, Integer limitSize){
        List<Map<String,Object>> result = new ArrayList<>();
        String connKey = DbInfoUtil.getConnKey(dbaseEntity, getUserId(), schemaName);
//        System.out.println(111);
        // 影响行数
//        int currentRowCount = 0;
        System.out.println("执行SQL语句");
        for (String sql:sqlList){
            Map<String, Object> map = new HashMap<>();
            map.put("sql", sql);
            String mess;
            String status;
            String actionType = null;
            int rowCount = 0;
            // SQL高危操作验证
            try {
                System.out.println("高危操作验证");
                RiskBean riskBean = riskOperationService.checkRiskOperation(sql, getUserId(),getGroupId(), schemaName, dbaseEntity);
                if (riskBean.isRiskFlag()){
                    if (StringUtils.startsWithAny(sql.toLowerCase(), "select", "show","desc","describe")) {
                        String sql2 = sql;
                        if ((sql.indexOf("show") == 0) || (sql.indexOf("SHOW") == 0)) {
                            sql2 = sql;
                        }

                        StopWatch clock = new StopWatch();
                        clock.start();
                        List<Map<String, Object>> list=null;
                        List<Map<String, Object>> columns=null;
                        try {
                            // 获取sql执行结果集
                            list = dataBaseUtilService.queryForList(dbaseEntity, sql2, connKey);
                            //新增脱敏操作
//                            System.out.println("============================动态脱敏操作============================");
                            SensitiveBean sensitiveBean = desensitizationService.getSensitiveOptions(getUserId(),getGroupId(),dbaseEntity.getDbId(),schemaName,sql);
                            list = desensitizationService.dynamicDesensitization(list,sensitiveBean);
//                            System.out.println("list: "+list);
                            // 获取结果集列信息
                            columns = dataBaseUtilService.executeSqlForColumns(dbaseEntity, sql2, connKey);
//                            System.out.println(columns);
                            // 获取影响行数
                            rowCount = list.size();
                            mess = "执行成功！";
                            status = "success";
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                            mess = e.getMessage();
                            status = "fail";
                        }
                        clock.stop();
                        map.put("resultList",list);
                        map.put("columns",columns);
                        map.put("rowCount",rowCount);
                        map.put("time", clock.getTotalTimeMillis());
                    } else {
//                        if(StringUtils.startsWithAny(sql.toLowerCase(), "update","delete","insert"))
                        StopWatch clock = new StopWatch();
                        clock.start();
                        try
                        {
                            rowCount = dataBaseUtilService.execUpdate(dbaseEntity, sql,connKey);
                            mess = "执行成功！";
                            status = "success";
                        }
                        catch (Exception e)
                        {
                            logger.error(e.getMessage(), e);
                            mess = e.getMessage();
                            status = "fail";
                        }
                        clock.stop();
                        map.put("rowCount", rowCount);
                        map.put("time", clock.getTotalTimeMillis());
                    }
                }else{
                    mess = "权限验证失败，无法执行";
                    status = "fail";
                    actionType = riskBean.getActionType();
                }
                map.put("operator",riskBean.getSqlType());
                map.put("schemas", riskBean.getSchemas());
                map.put("tables", riskBean.getTables());
                map.put("tableColumns", riskBean.getTableColumns());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                mess = e.getMessage();
                status = "fail";
            }
            map.put("mess", mess);
            map.put("status", status);
            map.put("actionType", actionType);
            result.add(map);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> executeSQLQueueForDMRisk(DbaseEntity dbaseEntity, String schemaName, List<String> sqlList, Integer limitSize){
        BusiDataBaseUtil db = new BusiDataBaseUtil(dbaseEntity, schemaName);
        List<Map<String,Object>> result = new ArrayList<>();
        String connKey = DbInfoUtil.getConnKey(dbaseEntity, getUserId(), schemaName);
        // 影响行数
//        int currentRowCount = 0;
        for (String sql:sqlList){
            Map<String, Object> map = new HashMap<>();
            map.put("sql", sql);
            String mess;
            String status;
            int rowCount = 0;
            // SQL高危操作验证
            if (StringUtils.startsWithAny(sql.toLowerCase(), "select", "show","desc","describe")) {
                String sql2 = sql;
                if ((sql.indexOf("show") == 0) || (sql.indexOf("SHOW") == 0)) {
                    sql2 = sql;
                }
                StopWatch clock = new StopWatch();
                clock.start();
                List<Map<String, Object>> list=null;
                List<Map<String, Object>> columns=null;
                try {
                    // 获取sql执行结果集
                    list = db.queryForList(sql2,connKey);
                    // 获取结果集列信息
                    columns = db.executeSqlForColumns(sql2,connKey);
                    // 获取影响行数
                    rowCount = list.size();
                    mess = "执行成功！";
                    status = "success";
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    mess = e.getMessage();
                    status = "fail";
                }
                clock.stop();
                map.put("operator", "select");
                map.put("resultList",list);
                map.put("columns",columns);
                map.put("rowCount",rowCount);
                map.put("time", clock.getTotalTimeMillis());
            } else if(StringUtils.startsWithAny(sql.toLowerCase(), "update","delete","insert")){
                StopWatch clock = new StopWatch();
                clock.start();
                try
                {
                    rowCount = db.execUpdate(sql,connKey);
                    mess = "执行成功！";
                    status = "success";
                }
                catch (Exception e)
                {
                    logger.error(e.getMessage(), e);
                    mess = e.getMessage();
                    status = "fail";
                }
                clock.stop();
                map.put("operator", "update");
                map.put("rowCount", rowCount);
                map.put("time", clock.getTotalTimeMillis());
            }
            else {
                // "create","drop","declare","alter","truncate"
                StopWatch clock = new StopWatch();
                clock.start();
                try
                {
                    rowCount = db.execUpdate(sql);
                    mess = "执行成功！";
                    status = "success";
                }
                catch (Exception e)
                {
                    logger.error(e.getMessage(), e);
                    mess = e.getMessage();
                    status = "fail";
                }
                clock.stop();
                map.put("operator","create or alter:DDL");
                map.put("rowCount", rowCount);
                map.put("time", clock.getTotalTimeMillis());
            }
//                }else{
//                    mess = "权限验证失败，无法执行";
//                    status = "fail";
//                }
//            } catch (Exception e) {
//                logger.error(e.getMessage(), e);
//                mess = e.getMessage();
//                status = "fail";
//            }
            map.put("mess", mess);
            map.put("status", status);
            result.add(map);
        }
        // 如果sqlList含有update操作
//        if (currentRowCount>0){
//            Map<String, Object> map = updateOverflow(currentRowCount, limitSize, dbaseEntity);
//            result.add(map);
//        }
        return result;
    }

    @Override
    public List<Map<String, Object>> executeSQLQueueForGBase(DbaseEntity dbaseEntity, String schemaName, List<String> sqlList, Integer limitSize) {
        BusiDataBaseUtil db = new BusiDataBaseUtil(dbaseEntity, schemaName);
        List<Map<String,Object>> result = new ArrayList<>();
        String connKey = DbInfoUtil.getConnKey(dbaseEntity,getUserId(), schemaName);
        // 影响行数
//        int currentRowCount = 0;
        for (String sql:sqlList){
            Map<String, Object> map = new HashMap<>();
            map.put("sql", sql);
            String mess;
            String status;
            int rowCount = 0;
            if (StringUtils.startsWithAny(sql.toLowerCase(), "select", "show","desc","describe")) {
                String sql2 = sql;
                if ((sql.indexOf("show") == 0) || (sql.indexOf("SHOW") == 0)) {
                    sql2 = sql;
                }
                StopWatch clock = new StopWatch();
                clock.start();
                List<Map<String, Object>> list=null;
                List<Map<String, Object>> columns=null;
                try {
                    // 获取sql执行结果集
                    list = db.queryForList(sql2,connKey);
                    // 获取结果集列信息
                    columns = db.executeSqlForColumns(sql2,connKey);
                    // 获取影响行数
                    rowCount = list.size();
                    mess = "执行成功！";
                    status = "success";
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    mess = e.getMessage();
                    status = "fail";
                }
                clock.stop();
                map.put("operator","select or show:DQL");
                map.put("resultList",list);
                map.put("columns",columns);
                map.put("rowCount",rowCount);
                map.put("time", clock.getTotalTimeMillis());
            }
            else if(StringUtils.startsWithAny(sql.toLowerCase(), "update","delete","insert")){
                StopWatch clock = new StopWatch();
                clock.start();
                try
                {
                    rowCount = db.execUpdate(sql,connKey);
                    mess = "执行成功！";
                    status = "success";
                }
                catch (Exception e)
                {
                    logger.error(e.getMessage(), e);
                    mess = e.getMessage();
                    status = "fail";
                }
                clock.stop();
                map.put("operator","update operator:DML");
                map.put("rowCount", rowCount);
                map.put("time", clock.getTotalTimeMillis());
            }
            else {
                // "create","drop","declare","alter","truncate"
                StopWatch clock = new StopWatch();
                clock.start();
                try
                {
                    rowCount = db.execUpdate(sql);
                    mess = "执行成功！";
                    status = "success";
                }
                catch (Exception e)
                {
                    logger.error(e.getMessage(), e);
                    mess = e.getMessage();
                    status = "fail";
                }
                clock.stop();
                map.put("operator","create or alter:DDL");
                map.put("rowCount", rowCount);
                map.put("time", clock.getTotalTimeMillis());
            }
            map.put("mess", mess);
            map.put("status", status);
            result.add(map);
        }
        // 如果sqlList含有update操作
//        if (currentRowCount>0){
//            Map<String, Object> map = updateOverflow(currentRowCount, limitSize, dbaseEntity);
//            result.add(map);
//        }
        return result;
    }

    @Override
    public List<Map<String, Object>> executeSQLQueueForKingBase(DbaseEntity dbaseEntity, String schemaName, List<String> sqlList, Integer limitSize) {
        BusiDataBaseUtil db = new BusiDataBaseUtil(dbaseEntity, schemaName);
        List<Map<String,Object>> result = new ArrayList<>();
        String connKey = DbInfoUtil.getConnKey(dbaseEntity, getUserId(), schemaName);
        // 影响行数
//        int currentRowCount = 0;
        for (String sql:sqlList){
            Map<String, Object> map = new HashMap<>();
            map.put("sql", sql);
            String mess;
            String status;
            int rowCount = 0;
            if (StringUtils.startsWithAny(sql.toLowerCase(), "select", "show","desc","describe")) {
                String sql2 = sql;
                if ((sql.indexOf("show") == 0) || (sql.indexOf("SHOW") == 0)) {
                    sql2 = sql;
                }
                StopWatch clock = new StopWatch();
                clock.start();
                List<Map<String, Object>> list=null;
                List<Map<String, Object>> columns=null;
                try {
                    // 获取sql执行结果集
                    list = db.queryForList(sql2,connKey);
                    // 获取结果集列信息
                    columns = db.executeSqlForColumns(sql2,connKey);
                    // 获取影响行数
                    rowCount = list.size();
                    mess = "执行成功！";
                    status = "success";
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    mess = e.getMessage();
                    status = "fail";
                }
                clock.stop();
                map.put("operator","select or show:DQL");
                map.put("resultList",list);
                map.put("columns",columns);
                map.put("rowCount",rowCount);
                map.put("time", clock.getTotalTimeMillis());
            }
            else if(StringUtils.startsWithAny(sql.toLowerCase(), "update","delete","insert")){
                StopWatch clock = new StopWatch();
                clock.start();
                try
                {
                    rowCount = db.execUpdate(sql,connKey);
                    mess = "执行成功！";
                    status = "success";
                }
                catch (Exception e)
                {
                    logger.error(e.getMessage(), e);
                    mess = e.getMessage();
                    status = "fail";
                }
                clock.stop();
                map.put("operator","update operator:DML");
                map.put("rowCount", rowCount);
                map.put("time", clock.getTotalTimeMillis());
            }
            else {
                // "create","drop","declare","alter","truncate"
                StopWatch clock = new StopWatch();
                clock.start();
                try
                {
                    rowCount = db.execUpdate(sql);
                    mess = "执行成功！";
                    status = "success";
                }
                catch (Exception e)
                {
                    logger.error(e.getMessage(), e);
                    mess = e.getMessage();
                    status = "fail";
                }
                clock.stop();
                map.put("operator","create or alter:DDL");
                map.put("rowCount", rowCount);
                map.put("time", clock.getTotalTimeMillis());
            }
            map.put("mess", mess);
            map.put("status", status);
            result.add(map);
        }
        // 如果sqlList含有update操作
//        if (currentRowCount>0){
//            Map<String, Object> map = updateOverflow(currentRowCount, limitSize, dbaseEntity);
//            result.add(map);
//        }
        return result;
    }

    @Override
    public Map<String,Object> commit(DbaseEntity dbaseEntity, String schemaName) {
        Map<String, Object> map = new HashMap<>();
        String mess = "";
        String status = "";
        String key = DbInfoUtil.getConnKey(dbaseEntity, getUserId(), schemaName);
        StopWatch clock = new StopWatch();
        clock.start();
        try {
            dataBaseUtilService.commit(key);
            mess = "事务提交执行成功！";
            status = "success";
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            mess = e.getMessage();
            status = "fail";
        }
        clock.stop();
        map.put("sql","COMMIT");
        map.put("operator","COMMIT");
        map.put("time",clock.getTotalTimeMillis());
        map.put("mess", mess);
        map.put("status", status);
        return map;
    }

    @Override
    public Map<String,Object> rollback(DbaseEntity dbaseEntity, String schemaName) {
        Map<String,Object> map = new HashMap<>();
        String mess = "";
        String status = "";
        String key = DbInfoUtil.getConnKey(dbaseEntity,getUserId(), schemaName);
        StopWatch clock = new StopWatch();
        clock.start();
        try {
            dataBaseUtilService.rollback(key);
            mess = "事务回滚执行成功！";
            status = "success";
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            mess = e.getMessage();
            status = "fail";
        }
        clock.stop();
        map.put("sql","ROLLBACK");
        map.put("operator","ROLLBACK");
        map.put("time", clock.getTotalTimeMillis());
        map.put("mess", mess);
        map.put("status", status);
        return map;
    }

    @Override
    public List<Map<String, Object>> executeUpdate(DbaseEntity dbaseEntity, String schemaName, List<String> sqlList) {
        List<Map<String,Object>> result = new ArrayList<>();
        Map<String,Object> map = new HashMap<>();
        String mess;
        String status;
        StopWatch clock = new StopWatch();
        clock.start();
        int rowCount = 0;
        try {
            rowCount = dataBaseUtilService.updateExecuteBatch(dbaseEntity, sqlList);
            mess = "执行成功！";
            status = "success";
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            mess = e.getMessage();
            status = "fail";
        }
        clock.stop();
        String sqls = "";
        for (String sql:sqlList){
            sqls += sql + "\n";
        }
        map.put("sql", sqls);
        map.put("operator","update batch:DDL or DML");
        map.put("rowCount", rowCount);
        map.put("time", clock.getTotalTimeMillis());
        map.put("mess", mess);
        map.put("status", status);
        result.add(map);
        return result;
    }

    @Override
    public void getConnectionSession(DbaseEntity dbaseEntity) {
        String connKey = DbInfoUtil.getConnKey(dbaseEntity, getUserId(), dbaseEntity.getSchemaName());
        try {
            dataBaseUtilService.getConnection(dbaseEntity, connKey);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void closeConn(DbaseEntity dbaseEntity) {
        String connKey = DbInfoUtil.getConnKey(dbaseEntity, getUserId(), dbaseEntity.getSchemaName());
        try {
            dataBaseUtilService.closeConn(connKey);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> executeSQLFunction(DbaseEntity dbaseEntity, String schemaName, String sql) {
        List<Map<String,Object>> result = new ArrayList<>();
        List<Map<String, Object>> list=null;
        Map<String, Object> map = new HashMap<>();
        int rowCount=0;
        String mess;
        String status;
        StopWatch clock = new StopWatch();
        clock.start();
        try {
            if (dbaseEntity.getDbType().equals("Oracle")){
                list = dataBaseUtilService.executeSQL(dbaseEntity, sql);
            }else{
                list = dataBaseUtilService.queryForList(dbaseEntity, sql);
            }
            // 获取影响行数
            rowCount = list.size();
            mess = "执行成功！";
            status = "success";
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            mess = e.getMessage();
            status = "fail";
        }
        clock.stop();
        map.put("operator", "Function EXEC");
        map.put("resultList",list);
        map.put("rowCount",rowCount);
        map.put("time", clock.getTotalTimeMillis());
        map.put("status",status);
        map.put("mess", mess);
        map.put("sql", sql);
        result.add(map);
        return result;
    }

    @Override
    public List<Map<String, Object>> executeSelectSQL(DbaseEntity dbaseEntity, String schemaName, List<String> sqlList) {
        List<Map<String,Object>> result = new ArrayList<>();
        for (String sql:sqlList){
            List<Map<String, Object>> list=null;
            List<Map<String, Object>> columns=null;
            Map<String, Object> map = new HashMap<>();
            int rowCount=0;
            String mess;
            String status;
            StopWatch clock = new StopWatch();
            clock.start();
            try {
                list = dataBaseUtilService.queryForList(dbaseEntity, sql);
                if (dbaseEntity.getDbType().equals("Oracle")){
                    String sqlColumns = "select * from (" + sql + ") where rownum = 1 ";
                    columns = dataBaseUtilService.executeSqlForColumns(dbaseEntity, sqlColumns);
                }else{
                    columns = dataBaseUtilService.executeSqlForColumns(dbaseEntity, sql);
                }
                // 获取影响行数
                rowCount = list.size();
                mess = "执行成功！";
                status = "success";
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                mess = e.getMessage();
                status = "fail";
            }
            clock.stop();
            map.put("operator","select or show:DQL");
            map.put("resultList",list);
            map.put("columns",columns);
            map.put("rowCount",rowCount);
            map.put("time", clock.getTotalTimeMillis());
            map.put("status",status);
            map.put("mess", mess);
            map.put("sql", sql);
            result.add(map);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getAccount(DbaseEntity dbaseEntity) {
        List<Map<String, Object>> accounts = new ArrayList<>();
        String dbType = dbaseEntity.getDbType();
        try {
            if (dbType.equals("DM")) {
                String sql = "select username from dba_users;";
                accounts =  dataBaseUtilService.queryForList(dbaseEntity, sql);
            }
            else if (dbType.equals("GBase")) {
                if (dbaseEntity.getDbDriver().equals("com.gbasedbt.jdbc.Driver")){
                    String sql = "select username from sysusers;";
                    accounts =  dataBaseUtilService.queryForList(dbaseEntity, sql);
                } else if (dbaseEntity.getDbDriver().equals("com.gbase.jdbc.Driver")){
                    String sql = "select `User` as USERNAME from gbase.user;";
                    accounts =  dataBaseUtilService.queryForList(dbaseEntity, sql);
                }
            }
            else if (dbType.equals("KingBase")) {
                String sql = "select usename AS USERNAME from sys_user;";
                accounts =  dataBaseUtilService.queryForList(dbaseEntity, sql);
            }
            else if (dbType.equals("Oscar")) {
                String sql = "SELECT USERNAME FROM dba_users;";
                accounts =  dataBaseUtilService.queryForList(dbaseEntity, sql);
            }
            else if (dbType.equals("MySql")) {
                String sql = "SELECT `User` as USERNAME FROM mysql.user;";
                accounts =  dataBaseUtilService.queryForList(dbaseEntity, sql);
            }
            else if (dbType.equals("Oracle")) {
                String sql = "SELECT USERNAME FROM ALL_USERS;";
                accounts = dataBaseUtilService.queryForList(dbaseEntity, sql);
            }
            else if (dbType.equals("MSSQL")) {
                String sql = "SELECT name as USERNAME FROM sys.sql_logins;";
                accounts = dataBaseUtilService.queryForList(dbaseEntity, sql);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
//        System.out.println(JSON.toJSONString(accounts));
//        List<Map<String, Object>> accounts = new ArrayList<>();
        for (Map<String, Object> account:accounts){
            if(account.getOrDefault("USERNAME", null) == null){
                account.put("USERNAME", account.getOrDefault("username", null));
            }
        }
        return accounts;
    }
    @Override
    public MetaInfo getDataBaseMetaInfo(DbaseEntity dbaseEntity) {
        return dataBaseUtilService.getDataBaseMeta(dbaseEntity);
    }
}
