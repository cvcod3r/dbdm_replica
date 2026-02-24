package com.dbms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.annotation.SysLogAnnotation;
import com.dbms.core.AjaxResult;
import com.dbms.core.BaseController;
import com.dbms.core.dbase.DataBaseUtilService;
import com.dbms.core.dbase.IHBaseUtilService;
import com.dbms.entity.DataLabelEntity;
import com.dbms.entity.DataRuleEntity;
import com.dbms.entity.DbaseEntity;
import com.dbms.entity.UserGroupEntity;
import com.dbms.page.TableDataInfo;
import com.dbms.repository.BigDataOpService;
import com.dbms.repository.DbopService;
import com.dbms.service.*;
import com.dbms.utils.CryptoUtil;
import com.dbms.utils.DbInfoUtil;
import com.dbms.utils.GlobalMessageUtil;
import com.dbms.utils.StringUtils;
import com.dbms.utils.ip.IpUtils;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author <a href="https://www.fengwenyi.com?code">YSL</a>
 * @since 2023-02-05
 */
@RestController
@RequestMapping("/data-label")
public class DataLabelController extends BaseController {
    @Autowired
    private DataLabelService dataLabelService;

    @Autowired
    private DbaseAccessService dbaseAccessService;

    @Autowired
    HttpServletRequest request;

    @Autowired
    private DbopService dbopService;

    @Autowired
    private LogDbaseService logDbaseService;

    @Autowired
    private DbaseService dbaseService;

    @Autowired
    private BigDataOpService bigDataOpService;


    @Autowired
    private DataBaseUtilService dataBaseUtilService;

    @Autowired
    private IHBaseUtilService hBaseUtilService;

    public static Map<Integer, List<TableDescriptor>> hbaseCloneTable = new ConcurrentHashMap<>();
    /**
     * 查询敏感数据标签
     * @param dataLabelEntity
     * @return
     */
    @GetMapping("/listDatalabel")
    @SysLogAnnotation(moduleName = "脱敏模块", childModule = "敏感数据标签", operation = "查询", procedureInfo = "查询敏感数据标签")
    public TableDataInfo listDatalabel(DataLabelEntity dataLabelEntity)
    {
        startPage();
        QueryWrapper<DataLabelEntity> queryWrapper=new QueryWrapper<>();
        if (StringUtils.isNotNull(dataLabelEntity.getLabelName())) {
            queryWrapper.like(DataLabelEntity.LABEL_NAME,dataLabelEntity.getLabelName());
        }
        if(StringUtils.isNotNull(dataLabelEntity.getStatus())){
            queryWrapper.like(DataLabelEntity.STATUS,dataLabelEntity.getStatus());
        }
        queryWrapper.eq(DataLabelEntity.IS_DELETE,0);
        List<DataLabelEntity> list = dataLabelService.list(queryWrapper);
        return getDataTable(list);
    }

    /**
     * 根据dbId查询是大数据NoSQL还是国产数据库SQL
     * @param dbId
     * @return
     */
    public String getDsTypeByDbId(Integer dbId){
        QueryWrapper<DbaseEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DbaseEntity.DB_ID,dbId);
        List<DbaseEntity> list= dbaseService.list(queryWrapper);
        return list.get(0).getDsType();
    }

    /**
     * 根据用户Id或用户组Id获取数据源实体,主要为了获取数据源的账号
     * @param dbId
     * @return
     */
    public DbaseEntity getAccessDbaseEntityByDbId(Integer dbId){
        Integer userId = getUserId();
        Integer groupId = getGroupId();
        String dsType = getDsTypeByDbId(dbId);
        DbaseEntity accessDbaseEntity = dbaseAccessService.getAccessDbaseEntity(userId, groupId, dbId, dsType);
        return accessDbaseEntity;
    }

    public void setDbaseEntityInfo(DbaseEntity dbaseEntity, String schemaName){
        dbaseEntity.setSchemaName(schemaName);
        String pw = CryptoUtil.decode(dbaseEntity.getPassword());
        dbaseEntity.setPassword(pw);
        if (StringUtils.isNotEmpty(dbaseEntity.getSchemaName())){
//            String dir = "/";
//            if (dbaseEntity.getDbType().equals("DM") || dbaseEntity.getDbType().equals("Oracle")){
//                dir = ":";
//            } else if (dbaseEntity.getDbType().equals("MSSQL")){
//                dir = ";database=";
//            }
            if (dbaseEntity.getDbType().equals("GBase")||dbaseEntity.getDbType().equals("Oracle")||dbaseEntity.getDbType().equals("ElasticSearch")){

            } else {
                dbaseEntity.setUrl(dbaseEntity.getUrl() + dbaseEntity.getUrlDir() + dbaseEntity.getSchemaName());
            }
        }
    }

    /**
     * 提取数据并脱敏
     * @param dbId
     * @param schemaName
     * @param tableInfo
     * @return
     */
    @PostMapping("/getTableInfo/{dbId}/{schemaName}")
//    @SysLogAnnotation(moduleName = "脱敏模块", childModule = "敏感数据标签", operation = "查询", procedureInfo = "查询敏感数据标签")
//            Map<String, List<Map<String, Integer>>> tableInfo
    public AjaxResult getTableInfo(@PathVariable Integer dbId,@PathVariable String schemaName,@RequestBody Map<String, List<Map<String, Integer>>> tableInfo) {
        System.out.println("---------------开始提取数据----------------");
        System.out.println("dbId:" + dbId);
        System.out.println("schemaName:" + schemaName);
        System.out.println("tableInfo:" + tableInfo);
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        setDbaseEntityInfo(dbaseEntity, schemaName);
        Map<String, Map<String, Object>> resultMap = new HashMap<>();
        String dbType = dbaseEntity.getDbType();
        System.out.println("dbType:" + dbType);
        System.out.println("DbDriver:" + dbaseEntity.getDbDriver());
        String ip = IpUtils.getIpAddress(request);
        if (dbType.equals("DM") || dbType.equals("KingBase") || dbType.equals("GBase") || dbType.equals("Hive") || dbType.equals("Oscar")) {
            try {
                resultMap = dataLabelService.extractTableInfo(dbaseEntity, schemaName, tableInfo);
                System.out.println("resultMap: " + resultMap);
                if (resultMap != null && resultMap.size() != 0) {
                    //                    logDbaseService.saveLog(resultMap, dbaseEntity, ip);
                    //                    System.out.println(JSON.toJSONString(resultMap));
                    return AjaxResult.success(GlobalMessageUtil.statusSuccess, resultMap);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else if (dbType.equals("HBaseShell")) {
            try {
                resultMap = dataLabelService.extractTableInfoForHBase(dbaseEntity, schemaName, tableInfo);
                System.out.println("resultMap: " + resultMap);
                if (resultMap != null && resultMap.size() != 0) {
                    List<TableDescriptor> Td = new ArrayList<>();
                    for(Map.Entry<String, Map<String, Object>> entry : resultMap.entrySet()){
                        Td.add((TableDescriptor)entry.getValue().get("create"));
                    }
                    System.out.println("Td: " + Td);
                    hbaseCloneTable.put(getUserId(),Td);
    //                logDbaseService.saveLog(resultMapBigData, dbaseEntity, ip);
    //                    System.out.println(JSON.toJSONString(resultMap));
                    return AjaxResult.success(GlobalMessageUtil.statusSuccess, resultMap);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else if (dbType.equals("ElasticSearch")
                    || dbType.equals("MySql")
                    || dbType.equals("MSSQL")
                    || dbType.equals("Oracle")
                    || dbType.equals("HBasePhoenix")) {
                System.out.println("敬请期待");
        }
            System.out.println("---------------提取失败数据----------------");
            return AjaxResult.error(GlobalMessageUtil.statusFailure);
    }


        /**
         * 查询所有敏感数据标签
         * @return
         */
        @GetMapping("/allDataLabel")
        @SysLogAnnotation(moduleName = "脱敏模块", childModule = "敏感数据标签", operation = "获取", procedureInfo = "获取敏感数据标签列表")
        public AjaxResult allDataLabel ()
        {
            QueryWrapper<DataLabelEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(DataLabelEntity.IS_DELETE, 0);
            List<DataLabelEntity> res = dataLabelService.list(queryWrapper);
            return AjaxResult.success(res);
        }

        /**
         * 校验敏感数据标签名是否重复
         */
        @GetMapping(value = "/checkLabelName/{labelID}/{labelName}")
        public AjaxResult checkLabelName (@PathVariable Integer labelID, @PathVariable String labelName)
        {
            QueryWrapper<DataLabelEntity> queryWrapper = new QueryWrapper<>();
            if (labelID != -1) {
                queryWrapper.ne(DataLabelEntity.LABEL_ID, labelID);
            }
            queryWrapper.eq(DataLabelEntity.LABEL_NAME, labelName);
            queryWrapper.ne(DataLabelEntity.IS_DELETE, 1);
            List<DataLabelEntity> DataRuleEntities = dataLabelService.list(queryWrapper);
            if (DataRuleEntities != null && DataRuleEntities.size() != 0) {
                return AjaxResult.success(GlobalMessageUtil.statusSuccess);
            }
            return AjaxResult.success(GlobalMessageUtil.statusFailure);
        }

        /**
         * 新增敏感数据标签
         */
        @PostMapping(value = "/addDataLabel")
        @SysLogAnnotation(moduleName = "脱敏模块", childModule = "敏感数据标签", operation = "新增", procedureInfo = "新增敏感数据标签")
        public AjaxResult addDataLabel (@Validated @RequestBody DataLabelEntity dataLabelEntity)
        {
            //存储创建的数据识别规则信息
            dataLabelEntity.setIsDelete(0);
            dataLabelEntity.setCreatetime(LocalDateTime.now());
            dataLabelEntity.setModify(1);
            return toAjax(dataLabelService.save(dataLabelEntity));
        }

        @GetMapping(value = "/selectById/{LabelID}")
        @SysLogAnnotation(moduleName = "脱敏模块", childModule = "敏感数据标签", operation = "查询", procedureInfo = "根据ID查询敏感数据标签")
        public AjaxResult selectById (@PathVariable Integer LabelID)
        {
            return AjaxResult.success(dataLabelService.getById(LabelID));
        }
        /**
         * 修改敏感数据标签
         */
        @PutMapping(value = "/updateDataLabel")
        @SysLogAnnotation(moduleName = "脱敏模块", childModule = "敏感数据标签", operation = "修改", procedureInfo = "修改敏感数据标签")
        public AjaxResult updateDataLabel (@Validated @RequestBody DataLabelEntity dataLabelEntity)
        {
            dataLabelEntity.setUpdatetime(LocalDateTime.now());
            return toAjax(dataLabelService.saveOrUpdate(dataLabelEntity));
        }

        /**
         * 删除敏感数据标签
         */
        @DeleteMapping("/deleteDataLabel/{labelIds}")
        @SysLogAnnotation(moduleName = "脱敏模块", childModule = "敏感数据标签", operation = "删除", procedureInfo = "删除敏感数据标签")
        public AjaxResult deleteDataRule (@PathVariable Integer[]labelIds)
        {
            return toAjax(dataLabelService.deleteByIds(labelIds));
        }

    /**
     * 向其他模式中导入数据
     * @return
     */
    @PostMapping(value = "/importTable/{oldSchemaName}/{newSchemaName}/{dbId}")
//    ordSchemaName是旧数据库 newSchemaName是新数据库
    public AjaxResult importTable(@PathVariable String oldSchemaName,@PathVariable String newSchemaName,@PathVariable Integer dbId,@Validated @RequestBody List<String> createTableSql)
    {
        System.out.println("旧模式" + oldSchemaName);
        System.out.println("新模式" + newSchemaName);
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        String psw = dbaseEntity.getPassword();
        dbaseEntity.setPassword(CryptoUtil.decode(psw));
        for (String singleCreateTableSql : createTableSql){
//            建表语句 非插入数据
            String newSingleCreateTableSql = singleCreateTableSql.replaceFirst(oldSchemaName,newSchemaName);
            try {
                int x = dataBaseUtilService.execUpdate(dbaseEntity, newSingleCreateTableSql);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return AjaxResult.success();
    }

    @PostMapping(value = "/importTableSql/{oldSchemaName}/{newSchemaName}/{dbId}")
//    ordSchemaName是旧数据库 newSchemaName是新数据库
//    insertTableSql 是 {表名：<插入sql语句>,表名：<插入sql语句>}
    public AjaxResult importTableSql(@PathVariable String oldSchemaName,@PathVariable String newSchemaName,@PathVariable Integer dbId,@Validated @RequestBody Map<String,List<String>> insertTableSql)
    {
        System.out.println("------------导入数据-------------");
        Map<String, Map<String, Object>> importResult = new HashMap<>();
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        System.out.println("Dbtype: " + dbaseEntity.getDbType());
        System.out.println("insertTableSql: " + insertTableSql);
        String psw = dbaseEntity.getPassword();
        dbaseEntity.setPassword(CryptoUtil.decode(psw));
        for (String tableName : insertTableSql.keySet()){
            System.out.println("tableName： " + tableName);
//            存放replace后的sql语句
            List<String> newInsertSqlList = insertTableSql.get(tableName);
//            for (String singleInsertSql :insertTableSql.get(tableName)){
//                String newSingleInsertSql = singleInsertSql.replace(oldSchemaName,newSchemaName);
//                newInsertSqlList.add(newSingleInsertSql);
//            }
            System.out.println("newInsertSqlList: " + newInsertSqlList);
            try {
                Map<String, Object> resultMap = dataBaseUtilService.execUpdateList(dbaseEntity, newInsertSqlList);
                importResult.put(tableName,resultMap);
                System.out.println("resultMap: " + resultMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return AjaxResult.success(importResult);
    }

    @PostMapping(value = "/importTableForBigData/{dbId}")
    public AjaxResult importTableForBigData(@PathVariable Integer dbId)
    {
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbIdForBigData(dbId);
        String psw = dbaseEntity.getPassword();
        dbaseEntity.setPassword(CryptoUtil.decode(psw));
        // 获取连接key
        String connKey = DbInfoUtil.getConnKey(dbaseEntity, getUserId());
        try {
            List<TableDescriptor> Td = hbaseCloneTable.get(getUserId());
            System.out.println(Td);
            hBaseUtilService.createNewTable(Td,connKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AjaxResult.success();
    }

    public DbaseEntity getAccessDbaseEntityByDbIdForBigData(Integer dbId){
        Integer userId = getUserId();
        Integer groupId = getGroupId();
        String dsType = "NoSQL";
        DbaseEntity accessDbaseEntity = dbaseAccessService.getAccessDbaseEntity(userId, groupId, dbId, dsType);
        return accessDbaseEntity;
    }

    @PostMapping(value = "/importTableSqlForBigData/{oldSchemaName}/{newSchemaName}/{dbId}")
//    ordSchemaName是旧数据库 newSchemaName是新数据库
//    insertTableSql 是 {表名：<插入sql语句>,表名：<插入sql语句>}
    public AjaxResult importTableSqlForBigData(@PathVariable String oldSchemaName,@PathVariable String newSchemaName,@PathVariable Integer dbId,@Validated @RequestBody Map<String,List<String>> insertTableSql)
    {
        System.out.println("------------导入数据-------------");
        Map<String, Map<String, Object>> importResult = new HashMap<>();
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbIdForBigData(dbId);
        System.out.println("Dbtype: " + dbaseEntity.getDbType());
        System.out.println("insertTableSql: " + insertTableSql);
        String psw = dbaseEntity.getPassword();
        dbaseEntity.setPassword(CryptoUtil.decode(psw));
        // 获取连接key
        String connKey = DbInfoUtil.getConnKey(dbaseEntity, getUserId());
        for (String tableName : insertTableSql.keySet()){
            System.out.println("tableName： " + tableName);
//            存放replace后的sql语句
            List<String> newInsertSqlList = insertTableSql.get(tableName);
//            for (String singleInsertSql :insertTableSql.get(tableName)){
//                String newSingleInsertSql = singleInsertSql.replace(oldSchemaName,newSchemaName);
//                newInsertSqlList.add(newSingleInsertSql);
//            }
            System.out.println("newInsertSqlList: " + newInsertSqlList);
            try {
                Map<String, Object> resultMap = hBaseUtilService.putDataList(newInsertSqlList , connKey);
                importResult.put(tableName,resultMap);
                System.out.println("resultMap: " + resultMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return AjaxResult.success(importResult);
    }
}
