package com.dbms.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.annotation.SysLogAnnotation;
import com.dbms.core.AjaxResult;
import com.dbms.core.BaseController;
import com.dbms.entity.*;
import com.dbms.page.TableDataInfo;
import com.dbms.service.RiskOperationService;
import com.dbms.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author <a href="https://www.fengwenyi.com?code">YSL</a>
 * @since 2023-02-05
 */
@RestController
@RequestMapping("/risk-operation")
public class RiskOperationController extends BaseController {

    @Autowired
    private RiskOperationService riskOperationService;

    @GetMapping("/listRisk")
//    @SysLogAnnotation(moduleName = "高危操作模块", childModule = "高危操作列表", operation = "查询", procedureInfo = "查询高危操作")
    public TableDataInfo listRisk(RiskOperationEntity riskOperationEntity)
    {
        startPage();
        QueryWrapper<RiskOperationEntity> queryWrapper=new QueryWrapper<>();
        if (riskOperationEntity.getDbId()!=null){
            queryWrapper.eq(RiskOperationEntity.DB_ID,riskOperationEntity.getDbId());
        }
        if (StringUtils.isNotNull(riskOperationEntity.getUrl())) {
            queryWrapper.like(RiskOperationEntity.URL,riskOperationEntity.getUrl());
        }
        if(StringUtils.isNotNull(riskOperationEntity.getStatus())){
            queryWrapper.like(RiskOperationEntity.STATUS,riskOperationEntity.getStatus());
        }
        queryWrapper.eq(RiskOperationEntity.IS_DELETE,0);
        List<RiskOperationEntity> list = riskOperationService.list(queryWrapper);
        return getDataTable(list);
    }

    @GetMapping("/getRiskId")
//    @SysLogAnnotation(moduleName = "高危操作模块", childModule = "高危操作列表", operation = "查询", procedureInfo = "查询高危操作")
    public AjaxResult getRiskId(RiskOperationEntity riskOperationEntity)
    {
        QueryWrapper<RiskOperationEntity> queryWrapper=new QueryWrapper<>();
        if (riskOperationEntity.getDbId()!=null){
            System.out.println("DB_ID: " + riskOperationEntity.getDbId());
            queryWrapper.eq(RiskOperationEntity.DB_ID,riskOperationEntity.getDbId());
        }
        if (StringUtils.isNotNull(riskOperationEntity.getUrl())) {
            System.out.println("URL: " + riskOperationEntity.getUrl());
            queryWrapper.like(RiskOperationEntity.URL,riskOperationEntity.getUrl());
        }
        if (StringUtils.isNotNull(riskOperationEntity.getTableName())) {
            System.out.println("TABLE_NAME: " + riskOperationEntity.getTableName());
            queryWrapper.like(RiskOperationEntity.TABLE_NAME,riskOperationEntity.getTableName());
        }
        if (StringUtils.isNotNull(riskOperationEntity.getSchemaName())) {
            System.out.println("SCHEMA_NAME: " + riskOperationEntity.getSchemaName());
            queryWrapper.like(RiskOperationEntity.SCHEMA_NAME,riskOperationEntity.getSchemaName());
        }
        if(StringUtils.isNotNull(riskOperationEntity.getColumnSensitive())){
            System.out.println("COLUMN_SENSITIVE: " + riskOperationEntity.getColumnSensitive());
            queryWrapper.like(RiskOperationEntity.COLUMN_SENSITIVE,riskOperationEntity.getColumnSensitive());
        }
        queryWrapper.eq(RiskOperationEntity.IS_DELETE,0);
        List<RiskOperationEntity> list = riskOperationService.list(queryWrapper);
        System.out.println("riskId: " + list.get(list.size()-1).getRiskId());
        return AjaxResult.success(list.get(list.size()-1).getRiskId());
    }

    /**
     * 删除高危操作列表
     */
    @DeleteMapping("/deleteRisk/{riskIds}")
    @SysLogAnnotation(moduleName = "高危操作模块", childModule = "高危操作列表", operation = "删除", procedureInfo = "删除高危操作列表")
    public AjaxResult deleteListRisk(@PathVariable Integer[] riskIds)
    {
        if(riskIds.length == 0){
            return AjaxResult.error();
        }else{
            for (Integer riskId:riskIds){
                RiskOperationEntity riskOperationEntity = riskOperationService.getById(riskId);
                riskOperationEntity.setIsDelete(1);
                riskOperationService.updateById(riskOperationEntity);
            }
        }
        return AjaxResult.success();
    }

    @PostMapping("/saveRiskSet")
    @SysLogAnnotation(moduleName = "高危操作模块", childModule = "高危操作配置", operation = "新增", procedureInfo = "新增高危操作配置")
    public AjaxResult saveRiskSet(@RequestBody RiskOperationEntity riskOperationEntity){
//        System.out.println(JSON.toJSONString(riskOperationEntity));
        if(riskOperationEntity.getActionType().equals("C")){
            riskOperationEntity.setLabelStatus(1);
        }
        riskOperationEntity.setCreatetime(LocalDateTime.now());

        return AjaxResult.success(riskOperationService.save(riskOperationEntity));
    }

    @GetMapping("/allRisk")
    @SysLogAnnotation(moduleName = "高危操作模块", childModule = "高危操作配置", operation = "获取", procedureInfo = "获取高危操作")
    public AjaxResult allRisk()
    {
        QueryWrapper<RiskOperationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(RiskOperationEntity.IS_DELETE,0);
        return AjaxResult.success(riskOperationService.list(queryWrapper));
    }

    @GetMapping("/getRiskSet/{riskId}")
    public AjaxResult getRiskSet(@PathVariable Integer riskId){
        RiskOperationEntity riskOperationEntity = riskOperationService.getById(riskId);
        riskOperationEntity.stringToObject();
//        System.out.println(riskOperationEntity.getColumnPrivilege());
        return AjaxResult.success(riskOperationEntity);
    }

    @GetMapping("/getRiskListByDbId/{dbId}")
    public AjaxResult getRiskListByDbId(@PathVariable Integer dbId){
        QueryWrapper<RiskOperationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(RiskOperationEntity.DB_ID, dbId);
        queryWrapper.eq(RiskOperationEntity.IS_DELETE, 0);
        List<RiskOperationEntity> riskList= riskOperationService.list(queryWrapper);
        return AjaxResult.success(riskList);
    }

    @PutMapping("/updateRiskSet")
//    @SysLogAnnotation(moduleName = "脱敏模块", childModule = "动态脱敏", operation = "修改", procedureInfo = "修改高危操作配置中的脱敏字段")
    public AjaxResult updateRiskSet(@Validated @RequestBody RiskOperationEntity riskOperationEntity)
    {
        riskOperationEntity.setCreatetime(LocalDateTime.now());
        return toAjax(riskOperationService.save(riskOperationEntity));
    }
}
