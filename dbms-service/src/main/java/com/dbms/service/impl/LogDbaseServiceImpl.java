package com.dbms.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.entity.DbaseEntity;
import com.dbms.entity.LogDbaseEntity;
import com.dbms.dao.LogDbaseDao;
import com.dbms.service.LogDbaseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dbms.utils.DbInfoUtil;
import com.dbms.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

import static com.dbms.utils.SecurityUtils.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author <a href="https://www.fengwenyi.com?code">YSL</a>
 * @since 2023-02-05
 */
@Service
public class LogDbaseServiceImpl extends ServiceImpl<LogDbaseDao, LogDbaseEntity> implements LogDbaseService {

    @Autowired
    private LogDbaseDao logDbaseDao;

    @Override
    public List<LogDbaseEntity> selectLogList(LogDbaseEntity logDbaseEntity) {
        QueryWrapper<LogDbaseEntity> queryWrapper=new QueryWrapper<>();
        if (StringUtils.isNotNull(logDbaseEntity.getSqlOperation())) {
            queryWrapper.like(LogDbaseEntity.SQL_OPERATION,logDbaseEntity.getSqlOperation());
        }
        if(StringUtils.isNotNull(logDbaseEntity.getUserName())){
            queryWrapper.like(LogDbaseEntity.USER_NAME,logDbaseEntity.getUserName());
        }
        if(StringUtils.isNotNull(logDbaseEntity.getDbUrl())){
            queryWrapper.like(LogDbaseEntity.DB_URL,logDbaseEntity.getDbUrl());
        }
        queryWrapper.eq(LogDbaseEntity.IS_DELETE,0);
        queryWrapper.orderByDesc("CREATETIME");
        return logDbaseDao.selectList(queryWrapper);
    }

    @Override
    public void saveLog(Map<String, Object> map, DbaseEntity dbaseEntity, String ip) {
        LogDbaseEntity logDbaseEntity = new LogDbaseEntity();
        String url = DbInfoUtil.getURL(dbaseEntity);
        logDbaseEntity.setDbUrl(url);
        logDbaseEntity.setUserId(getUserId());
        logDbaseEntity.setDbId(dbaseEntity.getDbId());
        logDbaseEntity.setDbConnName(dbaseEntity.getConnName());
        logDbaseEntity.setUserName(getUsername());
        logDbaseEntity.setRealName(getRealname());
        logDbaseEntity.setIpAddress(ip);
        logDbaseEntity.setSqlCommand((String) map.getOrDefault("sql", null));
        logDbaseEntity.setSqlOperation((String) map.getOrDefault("operator", null));
        logDbaseEntity.setSchemaName((String) map.getOrDefault("schemas", null));
        logDbaseEntity.setTableName((String) map.getOrDefault("tables", null));
        logDbaseEntity.setTableColumn((String) map.getOrDefault("tableColumns", null));
        logDbaseEntity.setRowCount((Integer) map.getOrDefault("rowCount", null));
        logDbaseEntity.setExecTime((Long) map.getOrDefault("time", null));
        logDbaseEntity.setDataLabels((String) map.getOrDefault("dataLabels", null));
        logDbaseEntity.setActionType((String) map.getOrDefault("actionType", null));
        logDbaseEntity.setCreatetime(LocalDateTime.now());
        //logEntity.setResult(JSON.toJSONString(map.getOrDefault("resultList","[null]")));
//        String resultDetail = "[命令]:" + map.get("sql") + "\n";
//        resultDetail += "[信息]:" + map.get("mess") + "\n";
//        if (map.getOrDefault("rowCount", null) != null) {
//            resultDetail += "[影响行数]:" + map.get("rowCount") + "\n";
//        }
//        resultDetail += "[执行时间]:" + map.get("time") + "\n";
        logDbaseEntity.setResultDetail((String) map.getOrDefault("mess", null));
        logDbaseDao.insert(logDbaseEntity);
    }

    @Override
    public List<Map<String, Object>> getSqlTypeCountMonitor(Integer dbId,String dbType,Date beginTime,Date endTime) {
        List<String> sqlTypes;
        if(dbType.contains("HBase")){
            sqlTypes = Arrays.asList("CREATE_NAMESPACE", "DROP_NAMESPACE", "CREATE","DROP","DISABLE","TRUNCATE","GRANT","REVOKE","SCAN", "PUT", "GET", "LIST");
        }
        else if(dbType.contains("Hive")){
            sqlTypes = Arrays.asList("CREATEDATABASE", "DROPDATABASE", "CREATETABLE","DROPTABLE","TRUNCATE","CREATEVIEW","DROPVIEW","SHOW","DESCRIBLE","DESC","INSERT","UPDATE","DELETE","SELETE","CREATEROLE",
                    "DROPROLE","GREANT","REVOKE","ALTERTABLE","LOADDATA");
        }
        else {
            sqlTypes = Arrays.asList("SELECT", "UPDATE", "INSERT","ALTER","CREATEINDEX","CREATETABLE","CREATEVIEW","DELETE","DROP","EXECUTE","MERGE","REPLACE","TRUNCATE","UPSERT","NONE","SHOW",
                    "DESCRIBE","DECLARE","RENAME","EXPLAIN","COMMENT","GRANT","REVOKE","COMMIT","ROLLBACK");
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (String sqlType:sqlTypes){
            Map<String, Object> map = getSqlTypeMap(dbId, sqlType,beginTime,endTime);
            if (map != null){
                result.add(map);
            }
        }
        return result;
    }

    private Map<String, Object> getSqlTypeMap(Integer dbId, String sqlType,Date beginTime,Date endTime) {
        Map<String, Object> map = new HashMap<>();
        try {
            QueryWrapper<LogDbaseEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(LogDbaseEntity.DB_ID, dbId);
            queryWrapper.eq(LogDbaseEntity.SQL_OPERATION, sqlType);
            queryWrapper.ge(LogDbaseEntity.CREATETIME,beginTime);
            queryWrapper.le(LogDbaseEntity.CREATETIME,endTime);
            Long sqlTypeCount = logDbaseDao.selectCount(queryWrapper);
            map.put("sqlType", sqlType);
            map.put("count", sqlTypeCount);
        }catch (Exception e){
            return null;
        }
        return map;
    }

    @Override
    public Long getSelectDataCount(Integer dbId, Date beginTime, Date endTime) {
        long count = 0;
//        System.out.println(JSON.toJSONString(beginTime)+ JSON.toJSONString(endTime));
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String formatBegin = dateFormat.format(beginTime);
//        String formatEnd = dateFormat.format(endTime);
        try {
            QueryWrapper<LogDbaseEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(LogDbaseEntity.DB_ID, dbId);
            queryWrapper.eq(LogDbaseEntity.SQL_OPERATION, "SELECT");
            queryWrapper.ge(LogDbaseEntity.CREATETIME, beginTime);
            queryWrapper.le(LogDbaseEntity.CREATETIME, endTime);
            List<LogDbaseEntity> logDbaseEntities = logDbaseDao.selectList(queryWrapper);
            for (LogDbaseEntity logDbaseEntity:logDbaseEntities){
                if (logDbaseEntity.getRowCount() != null){
                    count += logDbaseEntity.getRowCount();
                }
            }
        } catch (Exception e){

        }
        return count;
    }
}
