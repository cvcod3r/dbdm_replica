package com.dbms.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.annotation.SysLogAnnotation;
import com.dbms.core.AjaxResult;
import com.dbms.entity.DbaseAccountEntity;
import com.dbms.entity.DbaseEntity;
import com.dbms.entity.WorkOrderEntity;
import com.dbms.repository.BigDataOpService;
import com.dbms.service.*;
import com.dbms.utils.CryptoUtil;
import com.dbms.utils.DbInfoUtil;
import com.dbms.utils.GlobalMessageUtil;
import com.dbms.utils.ip.IpUtils;

import com.dbms.vo.DbaseEntityVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;

import static com.dbms.utils.SecurityUtils.getGroupId;
import static com.dbms.utils.SecurityUtils.getUserId;

@RestController
@RequestMapping("/dbms-bigdata")
public class BigDataOperController {

    private static final Logger logger = LoggerFactory.getLogger(DbaseController.class);

    @Autowired
    private DbaseService dbaseService;

    @Autowired
    private DbaseAccessService dbaseAccessService;

    @Autowired
    private DbaseAccountService dbaseAccountService;

    @Autowired
    private BigDataOpService bigDataOpService;

    @Autowired
    private LogDbaseService logDbaseService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private LogUnusualBehaviorService logUnusualBehaviorService;
    @Autowired
    HttpServletRequest request;

    /**
     * 根据用户Id或用户组Id获取数据源实体,主要为了获取数据源的账号
     * @param dbId
     * @return
     */
    public DbaseEntity getAccessDbaseEntityByDbId(Integer dbId){
        Integer userId = getUserId();
        Integer groupId = getGroupId();
        String dsType = "NoSQL";
        DbaseEntity accessDbaseEntity = dbaseAccessService.getAccessDbaseEntity(userId, groupId, dbId, dsType);
        return accessDbaseEntity;
    }


    /** 获取数据库列表
     * @param dsType NoSQL
     * @return
     */
    @GetMapping("/getAccessibleDbase/{dsType}")
    public AjaxResult getAccessibleDbase(@PathVariable String dsType){
        Integer userId = getUserId();
        Integer groupId = getGroupId();
        List<DbaseEntityVo> dbaseEntityVoList = dbaseAccessService.getAccessibleDbaseVo(userId, groupId, dsType);
        return AjaxResult.success(dbaseEntityVoList);
    }

    @PostMapping("/testBigDataConn")
    public AjaxResult testConn(@RequestBody DbaseEntity dbaseEntity){
        String mess;
        String status;
        boolean flag = bigDataOpService.testConn(dbaseEntity);
        if (flag)
        {
            mess = "连接成功！";
            status = "success";
        }
        else
        {
            mess = "连接失败！";
            status = "fail";
        }
        AjaxResult ajaxResult = AjaxResult.success(mess);
        ajaxResult.put("mess", mess);
        ajaxResult.put("status", status);
        return ajaxResult;
    }

    /**
     * 保存连接，先测试是否连接成功
     *
     * @param dbaseEntity
     * @return
     */
    @PostMapping("/saveBigDataConn")
    @ResponseBody
    public AjaxResult saveConn(@RequestBody DbaseEntity dbaseEntity) {
        String mess;
        if (bigDataOpService.testConn(dbaseEntity)) {
            // 数据库连接成功
            String encodePassword = CryptoUtil.encode(dbaseEntity.getPassword());
            dbaseEntity.setPassword(encodePassword);
            dbaseEntity.setUrl(DbInfoUtil.getURL(dbaseEntity));
            dbaseEntity.setCreatetime(LocalDateTime.now());
//            System.out.println(JSON.toJSONString(dbaseEntity));
            QueryWrapper<DbaseEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(DbaseEntity.URL, dbaseEntity.getUrl());
            queryWrapper.eq(DbaseEntity.IS_DELETE, 0);
            if (dbaseService.count(queryWrapper) > 0){
                mess = GlobalMessageUtil.dbSaveFailure;
            } else {
                boolean flag = dbaseService.save(dbaseEntity);
                if (flag) {
                    DbaseAccountEntity dbaseAccountEntity = new DbaseAccountEntity();
                    dbaseAccountEntity.setUrl(dbaseEntity.getUrl());
                    dbaseAccountEntity.setConnName(dbaseEntity.getConnName());
                    dbaseAccountEntity.setDbId(dbaseEntity.getDbId());
                    dbaseAccountEntity.setUsername(dbaseEntity.getUsername());
                    dbaseAccountEntity.setPassword(dbaseEntity.getPassword());
                    dbaseAccountService.save(dbaseAccountEntity);
                    mess = GlobalMessageUtil.saveSuccess;
                } else {
                    mess = GlobalMessageUtil.saveFailure;
                }
            }
        } else {
            // 连接失败
            mess = GlobalMessageUtil.dbConnFailure;
        }
        AjaxResult ajaxResult = AjaxResult.success(mess);
        return ajaxResult;
    }

    /**
     * 修改连接，先测试是否连接成功
     *
     * @param dbaseEntity
     * @return
     */
    @PostMapping("/editBigDataConn")
    @ResponseBody
    public AjaxResult editConn(@RequestBody DbaseEntity dbaseEntity) {
        if (bigDataOpService.testConn(dbaseEntity)) {
            // 数据库连接成功
            String encodePassword = CryptoUtil.encode(dbaseEntity.getPassword());
            dbaseEntity.setPassword(encodePassword);
            dbaseEntity.setUrl(DbInfoUtil.getURL(dbaseEntity));
            dbaseEntity.setCreatetime(LocalDateTime.now());
            System.out.println(JSON.toJSONString(dbaseEntity));
            if (dbaseService.saveOrUpdate(dbaseEntity)) {
                return AjaxResult.success(GlobalMessageUtil.editSuccess);
            } else {
                return AjaxResult.error(GlobalMessageUtil.editFailure);
            }
        } else {
            // 连接失败
            return AjaxResult.error(GlobalMessageUtil.dbConnFailure);
        }
    }

    /**打开窗口，获取连接，获取表
     * 根据dbId获取数据库元数据
     *
     * @param dbId
     * @return
     */
    @RequestMapping("/getBigDataInfo/{dbId}")
    public AjaxResult getDbInfo(@PathVariable Integer dbId) {
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
//        System.out.println(JSON.toJSONString(dbaseEntity));
//        System.out.println("closeConn");
        bigDataOpService.closeConn(dbaseEntity);
//        System.out.println("获取连接");
        bigDataOpService.getConnectionSession(dbaseEntity);
        Map<String, List<String>> tableMap = null;
        try {
            tableMap = bigDataOpService.getTables(dbaseEntity);
        } catch (Exception e) {
            return AjaxResult.error(GlobalMessageUtil.dbOpenConnFailure);
        }
        AjaxResult ajaxResult = AjaxResult.success();
        ajaxResult.put("tableMap", tableMap);
        return ajaxResult;
    }

    /** 执行语句
     *  分流 ，HBASE还是ES
     * @param dbId
     * @param sql
     * @return
     */
    @RequestMapping("/executeSQLStatement/{dbId}")
    public AjaxResult executeSQLStatement(@PathVariable Integer dbId, @RequestBody String sql){
//        System.out.println(sql);
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        Map<String, Object> resultMap = null;
        String dbType = dbaseEntity.getDbType();
        String ip = IpUtils.getIpAddress(request);
        try {
            if (dbType.equals("HBaseShell")){
                resultMap = bigDataOpService.executeSQLStatementForHBase(dbaseEntity, sql);
                if (resultMap != null && resultMap.size() != 0) {
                    logDbaseService.saveLog(resultMap, dbaseEntity, ip);
                    logUnusualBehaviorService.timeLimitCheck(dbaseEntity);
    //                    System.out.println(JSON.toJSONString(resultMap));
                    return AjaxResult.success(GlobalMessageUtil.statusSuccess, resultMap);
                }
            } else if (dbType.equals("ElasticRest")) {
                sql = sql.replaceAll("#new_line#", "\n");
                resultMap = bigDataOpService.executeSQLStatementForElasticRestful(dbaseEntity, sql);
                if (resultMap != null && resultMap.size() != 0) {
                    logDbaseService.saveLog(resultMap, dbaseEntity, ip);
                    logUnusualBehaviorService.timeLimitCheck(dbaseEntity);
                    return AjaxResult.success(GlobalMessageUtil.statusSuccess, resultMap);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return AjaxResult.error(GlobalMessageUtil.execStatusFailure);
    }

    @RequestMapping("/executeSQLStatementOrderBigData/{dbId}")
    public AjaxResult executeSQLStatementOrderBigData(@PathVariable Integer dbId, @RequestBody WorkOrderEntity workOrderEntity){
//        System.out.println(sql);
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        Map<String, Object> resultMap = null;
        String dbType = dbaseEntity.getDbType();
        String ip = IpUtils.getIpAddress(request);
        String sql = workOrderEntity.getSqlStatement();
        try {
            if (workOrderEntity.getLimitCount()!=null && workOrderEntity.getLimitCount()>0){
                if (dbType.equals("HBaseShell")){
                    resultMap = bigDataOpService.executeSQLStatementForHBaseOrder(dbaseEntity, sql);
                    if (resultMap != null && resultMap.size() != 0) {
                        logDbaseService.saveLog(resultMap, dbaseEntity, ip);
                        logUnusualBehaviorService.timeLimitCheck(dbaseEntity);
                        // 工单限制次数减1
                        WorkOrderEntity temp = new WorkOrderEntity();
                        temp.setLimitCount(workOrderEntity.getLimitCount()-1);
                        QueryWrapper<WorkOrderEntity> queryWrapper = new QueryWrapper<>();
                        queryWrapper.eq(WorkOrderEntity.WORK_ORDER_ID, workOrderEntity.getWorkOrderId());
                        workOrderService.update(temp, queryWrapper);
    //                    System.out.println(JSON.toJSONString(resultMap));
                        return AjaxResult.success(GlobalMessageUtil.statusSuccess, resultMap);
                    }
                } else if (dbType.equals("ElasticRest")) {
                    sql = sql.replaceAll("#new_line#", "\n");
                    resultMap = bigDataOpService.executeSQLStatementForElasticRestful(dbaseEntity, sql);
                    if (resultMap != null && resultMap.size() != 0) {
                        logDbaseService.saveLog(resultMap, dbaseEntity, ip);
                        logUnusualBehaviorService.timeLimitCheck(dbaseEntity);
                        // 工单限制次数减1
                        WorkOrderEntity temp = new WorkOrderEntity();
                        temp.setLimitCount(workOrderEntity.getLimitCount()-1);
                        QueryWrapper<WorkOrderEntity> queryWrapper = new QueryWrapper<>();
                        queryWrapper.eq(WorkOrderEntity.WORK_ORDER_ID, workOrderEntity.getWorkOrderId());
                        workOrderService.update(temp, queryWrapper);
                        return AjaxResult.success(GlobalMessageUtil.statusSuccess, resultMap);
                    }
                }
            }
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        }
        return AjaxResult.error(GlobalMessageUtil.execStatusFailure);
    }

    /**
     *
     * @param dbId
     * @return
     */
    @RequestMapping("/getBigDataLikeSchema/{dbId}")
    public AjaxResult getBigDataLikeSchema(@PathVariable Integer dbId){
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        String dbType = dbaseEntity.getDbType();
        List<String> schemaList = null;
        if (dbType.equals("HBaseShell")){
            try {
                schemaList = bigDataOpService.getHBaseNameSpace(dbaseEntity);
            } catch (Exception e) {
                return AjaxResult.error(GlobalMessageUtil.dbSchemaFailure);
            }
        } else if (dbType.equals("ElasticRest")){
            try {
                schemaList = Collections.singletonList("elasticsearch");
//                        bigDataOpService.getElasticIndex(dbaseEntity);
            } catch (Exception e) {
                return AjaxResult.error(GlobalMessageUtil.dbSchemaFailure);
            }
        }
        AjaxResult ajaxResult = AjaxResult.success();
        ajaxResult.put("schemaList", schemaList);
        return ajaxResult;
    }


    /**
     *
     * @param dbId
     * @return
     */
    @RequestMapping("/getBigDataTable/{dbId}/{schemaName}")
    public AjaxResult getBigDataTable(@PathVariable Integer dbId, @PathVariable String schemaName){
        DbaseEntity dbaseEntity = getAccessDbaseEntityByDbId(dbId);
        String dbType = dbaseEntity.getDbType();
        List<Map<String, Object>> tableMeta = null;
        if (dbType.equals("HBaseShell")){
            try {
                tableMeta = bigDataOpService.getHBaseTableMetas(dbaseEntity, schemaName);
            } catch (Exception e) {
                return AjaxResult.error("获取表失败");
            }
        } else if (dbType.equals("ElasticRest")){
            try {
                tableMeta = bigDataOpService.getElasticIndexMetas(dbaseEntity, schemaName);
            } catch (Exception e) {
                return AjaxResult.error("获取索引失败");
            }
        }
//        System.out.println(JSON.toJSONString(tableMeta));
        AjaxResult ajaxResult = AjaxResult.success();
        ajaxResult.put("tableMeta", tableMeta);
        return ajaxResult;
    }


}
