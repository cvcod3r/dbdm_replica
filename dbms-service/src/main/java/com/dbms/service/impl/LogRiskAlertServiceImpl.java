package com.dbms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.entity.DbaseEntity;
import com.dbms.entity.LogRiskAlertEntity;
import com.dbms.dao.LogRiskAlertDao;
import com.dbms.enums.SqlTypeEnum;
import com.dbms.service.LogRiskAlertService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dbms.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import static com.dbms.utils.SecurityUtils.getUsername;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author YSL
 * @since 2023-04-27
 */
@Service
public class LogRiskAlertServiceImpl extends ServiceImpl<LogRiskAlertDao, LogRiskAlertEntity> implements LogRiskAlertService {
    @Autowired
    LogRiskAlertDao logRiskAlertDao;

    @Autowired
    LogRiskAlertService logRiskAlertService;

    @Override
    public List<LogRiskAlertEntity> selectLogList(LogRiskAlertEntity logRiskAlertEntity) {
        QueryWrapper<LogRiskAlertEntity> queryWrapper=new QueryWrapper<>();

        if(StringUtils.isNotNull(logRiskAlertEntity.getUserName())){
            queryWrapper.like(LogRiskAlertEntity.USER_NAME,logRiskAlertEntity.getUserName());
        }
        if(StringUtils.isNotNull(logRiskAlertEntity.getDbUrl())){
            queryWrapper.like(LogRiskAlertEntity.DB_URL,logRiskAlertEntity.getDbUrl());
        }
        if(StringUtils.isNotNull(logRiskAlertEntity.getRiskLevel())){
            queryWrapper.like(LogRiskAlertEntity.RISK_LEVEL,logRiskAlertEntity.getRiskLevel());
        }
        queryWrapper.orderByDesc("CREATETIME");
        return logRiskAlertDao.selectList(queryWrapper);
    }

    @Override
    public void LogRiskAlterForHbase(DbaseEntity dbaseEntity, String sql, String ip) {
        LogRiskAlertEntity logRiskAlertEntity = new LogRiskAlertEntity();
        String sqlType=getsqlTypeForHbase(sql).getName();
        System.out.println(getsqlTypeForHbase((sql)));
        String shellSqlLow=sqlType.toLowerCase(Locale.ROOT);
        System.out.println("shellSqlLow"+shellSqlLow);
        switch (shellSqlLow){
            case "create_namespace":
            case "create":
                logRiskAlertEntity.setRuleType("普通规则");
                logRiskAlertEntity.setRiskLevel("关注行为");
                logRiskAlertEntity.setAlertId(null);
                logRiskAlertEntity.setDbUrl(dbaseEntity.getUrl());
                logRiskAlertEntity.setSchemaName(dbaseEntity.getSchemaName());
                logRiskAlertEntity.setUserName(getUsername());
                logRiskAlertEntity.setIpAddress(ip);
                logRiskAlertEntity.setSql(sql);
                logRiskAlertEntity.setSqlOperation(sqlType);
                logRiskAlertEntity.setCreateTime(LocalDateTime.now());
                logRiskAlertService.save(logRiskAlertEntity);
                break;
            case "drop_namespace":
            case "drop":
            case "truncate":
            case "grant":
            case "revoke":
            case "scan":
            case "disable":
                logRiskAlertEntity.setRuleType("普通规则");
                logRiskAlertEntity.setRiskLevel("高风险");
                logRiskAlertEntity.setAlertId(null);
                logRiskAlertEntity.setDbUrl(dbaseEntity.getUrl());
                logRiskAlertEntity.setSchemaName(dbaseEntity.getSchemaName());
                logRiskAlertEntity.setUserName(getUsername());
                logRiskAlertEntity.setIpAddress(ip);
                logRiskAlertEntity.setSql(sql);
                logRiskAlertEntity.setSqlOperation(sqlType);
                logRiskAlertEntity.setCreateTime(LocalDateTime.now());
                logRiskAlertService.save(logRiskAlertEntity);
                break;
            default:
                break;
        }
    }

    private SqlTypeEnum getsqlTypeForHbase(String sql){
        if (StringUtils.startsWith(sql.toUpperCase(), "SCAN")){
            return SqlTypeEnum.SCAN;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "SELECT")){
            return SqlTypeEnum.SELECT;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "INSERT")){
            return SqlTypeEnum.PUT;
        } else if(StringUtils.startsWith(sql.toUpperCase(),  "GET")){
            return SqlTypeEnum.GET;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "DROP")){
            return SqlTypeEnum.DROP;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "DELETE")){
            return SqlTypeEnum.DELETE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "DISABLE")){
            return SqlTypeEnum.DISABLE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "DELETEALL")){
            return SqlTypeEnum.DELETEALL;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "DESCRIBE")){
            return SqlTypeEnum.DESCRIBE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "GRANT")){
            return SqlTypeEnum.GRANT;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "LIST_NAMESPACE")){
            return SqlTypeEnum.LIST_NAMESPACE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "LIST_NAMESPACE_TABLES")){
            return SqlTypeEnum.LIST_NAMESPACE_TABLES;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "CREATE_NAMESPACE")){
            return SqlTypeEnum.CREATE_NAMESPACE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "DESCRIBE_NAMESPACE")){
            return SqlTypeEnum.DESCRIBE_NAMESPACE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "DROP_NAMESPACE")){
            return SqlTypeEnum.DROP_NAMESPACE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "REVOKE")){
            return SqlTypeEnum.REVOKE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "CREATE")){
            return SqlTypeEnum.CREATE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "LIST")){
            return SqlTypeEnum.LIST;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "DROP_ALL")){
            return SqlTypeEnum.DROP_ALL;
        } else {
            return SqlTypeEnum.NONE;
        }
    }
    private SqlTypeEnum getSqlTypeForHive(String sql){
        if (StringUtils.startsWith(sql.toUpperCase(), "ALTER")) {
            return SqlTypeEnum.ALTER;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "CREATE INDEX")) {
            return SqlTypeEnum.CREATEINDEX;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "CREATE ROLE")) {
            return SqlTypeEnum.CREATEROLE;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "CREATE DATABASE")) {
            return SqlTypeEnum.CREATEDATABASE;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "CREATE TABLE")) {
            return SqlTypeEnum.CREATETABLE;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "CREATE VIEW")) {
            return SqlTypeEnum.CREATEVIEW;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "DELETE")) {
            return SqlTypeEnum.DELETE;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "DROP TABLE")) {
            return SqlTypeEnum.DROPTABLE;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "DROP ROLE")) {
            return SqlTypeEnum.DROPROLE;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "DROP VIEW")) {
            return SqlTypeEnum.DROPVIEW;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "LOAD DATA")) {
            return SqlTypeEnum.LOADDATA;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "DROP")) {
            return SqlTypeEnum.DROP;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "EXECUTE")) {
            return SqlTypeEnum.EXECUTE;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "INSERT")) {
            return SqlTypeEnum.INSERT;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "MERGE")) {
            return SqlTypeEnum.MERGE;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "REPLACE")) {
            return SqlTypeEnum.REPLACE;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "SELECT")) {
            return SqlTypeEnum.SELECT;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "TRUNCATE")) {
            return SqlTypeEnum.TRUNCATE;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "UPDATE")) {
            return SqlTypeEnum.UPDATE;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "UPSERT")) {
            return SqlTypeEnum.UPSERT;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "SHOW")){
            return SqlTypeEnum.SHOW;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "DESCRIBE")){
            return SqlTypeEnum.DESCRIBE;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "DESC")){
            return SqlTypeEnum.DESC;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "DECLARE")){
            return SqlTypeEnum.DECLARE;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "RENAME")){
            return SqlTypeEnum.RENAME;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "EXPLAIN")){
            return SqlTypeEnum.EXPLAIN;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "COMMENT")){
            return SqlTypeEnum.COMMENT;
        }
        else {
//            System.out.println(sqlStmt.getClass());
            return SqlTypeEnum.NONE;
        }
    }


    @Override
    public void LogRiskAlterForHive(DbaseEntity dbaseEntity, String sql,String ip){
        LogRiskAlertEntity logRiskAlertEntity = new LogRiskAlertEntity();
        String sqlType=getSqlTypeForHive(sql).getName();
        String shellSqlLow=sqlType.toLowerCase(Locale.ROOT);
        switch (shellSqlLow){
            case "createdatabase":
            case "createtable":
            case "createview":
            case "show":
            case "desc":
            case "describe":
                logRiskAlertEntity.setRuleType("普通规则");
                logRiskAlertEntity.setRiskLevel("关注行为");
                logRiskAlertEntity.setAlertId(null);
                logRiskAlertEntity.setDbUrl(dbaseEntity.getUrl());
                logRiskAlertEntity.setSchemaName(dbaseEntity.getSchemaName());
                logRiskAlertEntity.setUserName(getUsername());
                logRiskAlertEntity.setIpAddress(ip);
                logRiskAlertEntity.setSql(sql);
                logRiskAlertEntity.setSqlOperation(sqlType);
                logRiskAlertEntity.setCreateTime(LocalDateTime.now());
                logRiskAlertService.save(logRiskAlertEntity);
                //表名还未获取 需等待sql解析
                break;
            case "dropdatabase":
            case "droptable":
            case "truncatetable":
            case "dropview":
            case "createrole":
            case "droprole":
            case "grant":
            case "revoke":
            case "altertable":
            case "loaddata":
                logRiskAlertEntity.setRuleType("普通规则");
                logRiskAlertEntity.setRiskLevel("高风险");
                logRiskAlertEntity.setAlertId(null);
                logRiskAlertEntity.setDbUrl(dbaseEntity.getUrl());
                logRiskAlertEntity.setSchemaName(dbaseEntity.getSchemaName());
                logRiskAlertEntity.setUserName(getUsername());
                logRiskAlertEntity.setIpAddress(ip);
                logRiskAlertEntity.setSql(sql);
                logRiskAlertEntity.setSqlOperation(sqlType);
                logRiskAlertEntity.setCreateTime(LocalDateTime.now());
                logRiskAlertService.save(logRiskAlertEntity);
                //表名还未获取 需等待sql解析
//                logRiskAlertEntity.setTableColumn(null);
                break;
            case "delete":
            case "update":
            case "select":
                if (sql.contains("where")||sql.contains("limit")){
                    logRiskAlertEntity.setRuleType("普通规则");
                    logRiskAlertEntity.setRiskLevel("高风险");
                    logRiskAlertEntity.setAlertId(null);
                    logRiskAlertEntity.setDbUrl(dbaseEntity.getUrl());
                    logRiskAlertEntity.setSchemaName(dbaseEntity.getSchemaName());
                    logRiskAlertEntity.setUserName(getUsername());
                    logRiskAlertEntity.setIpAddress(ip);
                    logRiskAlertEntity.setSql(sql);
                    logRiskAlertEntity.setSqlOperation(sqlType);
                    logRiskAlertEntity.setCreateTime(LocalDateTime.now());
                    logRiskAlertService.save(logRiskAlertEntity);
                    //表名还未获取 需等待sql解析
                    System.out.println("select成功");
                    break;
                }
        }
    }

    @Override
    public void LogRiskAlterForES(DbaseEntity dbaseEntity, String sql, String shellsql,String ip){
        LogRiskAlertEntity logRiskAlertEntity = null;
        //ES功能还未实现 敬请期待！！！
    }
}
