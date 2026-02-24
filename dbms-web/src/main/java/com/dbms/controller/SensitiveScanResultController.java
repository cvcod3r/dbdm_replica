package com.dbms.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dbms.core.BaseController;
import com.dbms.core.dbase.DataBaseUtilService;
import com.dbms.core.dbase.IHBaseUtilService;
import com.dbms.core.dbase.impl.HBaseUtilServiceImpl;
import com.dbms.entity.DbaseEntity;
import com.dbms.entity.UserGroupEntity;
import com.dbms.service.*;
import com.dbms.utils.CryptoUtil;
import com.dbms.utils.DbInfoUtil;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.annotation.SysLogAnnotation;
import com.dbms.core.AjaxResult;
import com.dbms.core.BaseController;
import com.dbms.entity.DataLabelEntity;
import com.dbms.entity.SensitiveScanResultEntity;
import com.dbms.page.TableDataInfo;
import com.dbms.utils.GlobalMessageUtil;
import com.dbms.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.dbms.utils.SecurityUtils.getUserId;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author
 * @since 2023-03-10
 */
@RestController
@RequestMapping("/sensitive-scan-result")
public class SensitiveScanResultController extends BaseController {

    @Autowired
    private SensitiveScanResultService sensitiveScanResultService;

    /**
     * 查询所有敏感数据标签
     * @return
     */
    @GetMapping("/allScanResult/{dbId}/{schemaName}")
//    @SysLogAnnotation(moduleName = "脱敏模块", childModule = "敏感数据标签", operation = "获取", procedureInfo = "获取敏感数据标签列表")
    public AjaxResult allScanResult(@PathVariable Integer dbId, @PathVariable String schemaName)
    {
        QueryWrapper<SensitiveScanResultEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SensitiveScanResultEntity.DB_ID, dbId);
        queryWrapper.eq(SensitiveScanResultEntity.SCHEMA_NAME, schemaName);
        List<SensitiveScanResultEntity> res = sensitiveScanResultService.list(queryWrapper);
        for (SensitiveScanResultEntity r : res)
        {
            r.stringToObject();
        }
        return AjaxResult.success(res);
    }


    @GetMapping("/selectScanResultByTableName")
//    @SysLogAnnotation(moduleName = "用户模块", childModule = "用户组", operation = "查询", procedureInfo = "查询用户组")
    public TableDataInfo selectScanResultByTableName(SensitiveScanResultEntity sensitiveScanResultEntity)
    {
        System.out.println("----------查询结果----------");
        System.out.println(JSON.toJSONString(sensitiveScanResultEntity));
        startPage();
        QueryWrapper<SensitiveScanResultEntity> queryWrapper=new QueryWrapper<>();
        if (StringUtils.isNotNull(sensitiveScanResultEntity.getSchemaName())) {
            queryWrapper.like(SensitiveScanResultEntity.SCHEMA_NAME,sensitiveScanResultEntity.getSchemaName());
        }
        if(StringUtils.isNotNull(sensitiveScanResultEntity.getTableName())){
            queryWrapper.like(SensitiveScanResultEntity.TABLE_NAME,sensitiveScanResultEntity.getTableName());
        }
        List<SensitiveScanResultEntity> list = sensitiveScanResultService.list(queryWrapper);
        for (SensitiveScanResultEntity r : list)
        {
            r.stringToObject();
        }
        return getDataTable(list);
    }

    /**
     * 查询特定敏感数据标签
     * @return
     */
    @GetMapping("/selectScanResult/{dbId}/{schemaName}")
//    @SysLogAnnotation(moduleName = "脱敏模块", childModule = "敏感数据标签", operation = "获取", procedureInfo = "获取敏感数据标签列表")
    public AjaxResult selectScanResult(@PathVariable Integer dbId,@PathVariable String schemaName)
    {
        System.out.println("-----------dbId: " + dbId + " schemaName: " + schemaName + "---------------");
        QueryWrapper<SensitiveScanResultEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SensitiveScanResultEntity.DB_ID,dbId);
        queryWrapper.eq(SensitiveScanResultEntity.SCHEMA_NAME,schemaName);
        List<SensitiveScanResultEntity> res = sensitiveScanResultService.list(queryWrapper);
        for (SensitiveScanResultEntity r : res)
        {
            r.stringToObject();
        }
        return AjaxResult.success(res);
    }

    /**
     * 新增敏感数据标签
     */
    @PostMapping(value = "/addScanResult")
//    @SysLogAnnotation(moduleName = "脱敏模块", childModule = "敏感数据标签", operation = "新增", procedureInfo = "新增敏感数据标签")
    public AjaxResult addScanResult(@Validated @RequestBody SensitiveScanResultEntity sensitiveScanResultEntity)
    {
        //存储创建的数据识别规则信息
        sensitiveScanResultEntity.setScanTime(LocalDateTime.now());
        return toAjax(sensitiveScanResultService.save(sensitiveScanResultEntity));
    }

    /**
     * 删除扫描结果
     */
    @DeleteMapping("/delScanResult/{dbId}/{schemaName}")
//    @SysLogAnnotation(moduleName = "脱敏模块", childModule = "敏感数据标签", operation = "删除", procedureInfo = "删除敏感数据标签")
    public AjaxResult deleteScanResult(@PathVariable Integer dbId,@PathVariable String schemaName)
    {
        System.out.println("------------deleteScanResult----------");
        QueryWrapper<SensitiveScanResultEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SensitiveScanResultEntity.DB_ID,dbId);
        queryWrapper.eq(SensitiveScanResultEntity.SCHEMA_NAME,schemaName);
        long count = sensitiveScanResultService.count(queryWrapper);
        // 如果没有相关计数则count为0，不用删除
        System.out.println("count: " + count);
        if (count != 0) {
            return toAjax(sensitiveScanResultService.remove(queryWrapper));
        }
        return AjaxResult.success("not find delete_object");
    }

}
