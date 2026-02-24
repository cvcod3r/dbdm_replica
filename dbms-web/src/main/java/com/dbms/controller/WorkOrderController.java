package com.dbms.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.annotation.SysLogAnnotation;
import com.dbms.core.AjaxResult;
import com.dbms.core.BaseController;
import com.dbms.entity.DbaseEntity;
import com.dbms.entity.WorkOrderEntity;
import com.dbms.page.TableDataInfo;
import com.dbms.service.DbaseService;
import com.dbms.service.WorkOrderService;
import com.dbms.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author YSL
 * @since 2023-05-24
 */
@RestController
@RequestMapping("/work-order")
public class WorkOrderController extends BaseController {

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private DbaseService dbaseService;

    // TODO 分页查询
    @RequestMapping("/list")
    @SysLogAnnotation(moduleName = "工单模块", childModule = "所有工单", operation = "查询", procedureInfo = "查询工单列表")
    public TableDataInfo list(WorkOrderEntity workOrderEntity) {
        startPage();
        System.out.println("workOrderEntity: " + JSON.toJSONString(workOrderEntity));
        QueryWrapper<WorkOrderEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotNull(workOrderEntity.getUserId())){
            queryWrapper.eq(WorkOrderEntity.USER_ID, workOrderEntity.getUserId());
        }
        if (StringUtils.isNotNull(workOrderEntity.getUsername())){
            queryWrapper.eq(WorkOrderEntity.USERNAME, workOrderEntity.getUsername());
        }
        if (StringUtils.isNotNull(workOrderEntity.getDbId())){
            queryWrapper.eq(WorkOrderEntity.DB_ID, workOrderEntity.getDbId());
        }
        if (StringUtils.isNotNull(workOrderEntity.getDbUrl())){
            queryWrapper.like(WorkOrderEntity.DB_URL, workOrderEntity.getDbUrl());
        }
        if (StringUtils.isNotNull(workOrderEntity.getProcessStatus())){
            queryWrapper.eq(WorkOrderEntity.PROCESS_STATUS, workOrderEntity.getProcessStatus());
        }
        if (StringUtils.isNotNull(workOrderEntity.getProcessResult())){
            queryWrapper.eq(WorkOrderEntity.PROCESS_RESULT, workOrderEntity.getProcessResult());
        }
        queryWrapper.eq(WorkOrderEntity.IS_DELETE, 0);
        return getDataTable(workOrderService.list(queryWrapper));
    }

    @RequestMapping("/listByUserId")
    @SysLogAnnotation(moduleName = "工单模块", childModule = "我的工单", operation = "查询", procedureInfo = "查询工单列表")
    public TableDataInfo listByUserId(WorkOrderEntity workOrderEntity) {
        startPage();
        QueryWrapper<WorkOrderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(WorkOrderEntity.USER_ID, getUserId());
        if (StringUtils.isNotNull(workOrderEntity.getDbId())){
            queryWrapper.eq(WorkOrderEntity.DB_ID, workOrderEntity.getDbId());
        }
        if (StringUtils.isNotNull(workOrderEntity.getDbUrl())){
            queryWrapper.like(WorkOrderEntity.DB_URL, workOrderEntity.getDbUrl());
        }
        if (StringUtils.isNotNull(workOrderEntity.getProcessStatus())){
            queryWrapper.eq(WorkOrderEntity.PROCESS_STATUS, workOrderEntity.getProcessStatus());
        }
        if (StringUtils.isNotNull(workOrderEntity.getProcessResult())){
            queryWrapper.eq(WorkOrderEntity.PROCESS_RESULT, workOrderEntity.getProcessResult());
        }
        queryWrapper.eq(WorkOrderEntity.IS_DELETE, 0);
        return getDataTable(workOrderService.list(queryWrapper));
    }

    @RequestMapping("/listByProcessStatus")
    @SysLogAnnotation(moduleName = "工单模块", childModule = "待处理工单", operation = "查询", procedureInfo = "查询工单列表")
    public TableDataInfo listByProcessStatus(WorkOrderEntity workOrderEntity) {
        startPage();
        QueryWrapper<WorkOrderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(WorkOrderEntity.PROCESS_STATUS, 0);
        if (StringUtils.isNotNull(workOrderEntity.getDbId())){
            queryWrapper.eq(WorkOrderEntity.DB_ID, workOrderEntity.getDbId());
        }
        queryWrapper.eq(WorkOrderEntity.IS_DELETE, 0);
        return getDataTable(workOrderService.list(queryWrapper));
    }

    // TODO 新增
    @PostMapping("/addOrder")
    @SysLogAnnotation(moduleName = "工单模块", childModule = "我的工单", operation = "新增", procedureInfo = "新增工单")
    public AjaxResult addOrder(@RequestBody WorkOrderEntity workOrderEntity) {
//        System.out.println(JSON.toJSONString(workOrderEntity));
        DbaseEntity dbaseEntity = dbaseService.getById(workOrderEntity.getDbId());
        workOrderEntity.setDbUrl(dbaseEntity.getUrl());
        workOrderEntity.setDbType(dbaseEntity.getDbType());
        workOrderEntity.setUserId(getUserId());
        workOrderEntity.setUsername(getUsername());
        workOrderEntity.setRealName(getRealname());
        workOrderEntity.setProcessStatus(0); // 0 未处理 1 已处理
        workOrderEntity.setProcessResult(0); // 0 未通过 1 通过 2 驳回 3 撤销
        workOrderEntity.setIsDelete(0); // 0 未删除 1 已删除
        workOrderEntity.setCreateTime(LocalDateTime.now());
//        System.out.println(JSON.toJSONString(workOrderEntity));
        return toAjax(workOrderService.save(workOrderEntity));
    }
    // TODO 审核
    @PostMapping("/audit")
    @SysLogAnnotation(moduleName = "工单模块", childModule = "待处理工单", operation = "审核", procedureInfo = "审核工单")
    public AjaxResult audit(@RequestBody WorkOrderEntity workOrderEntity) {
        System.out.println(JSON.toJSONString(workOrderEntity));
        workOrderEntity.setProcessStatus(1); // 0 未处理 1 已处理
        workOrderEntity.setProcessTime(LocalDateTime.now());
        workOrderEntity.setProcessor(getUsername());
        workOrderEntity.setProcessorId(getUserId());
        return toAjax(workOrderService.updateById(workOrderEntity));
    }

    // TODO count
    @GetMapping("/countByProcessStatus")
//    @SysLogAnnotation(moduleName = "工单模块", childModule = "待处理工单", operation = "查询", procedureInfo = "查询待处理工单数量")
    public AjaxResult countByProcessStatus() {
        QueryWrapper<WorkOrderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(WorkOrderEntity.PROCESS_STATUS, 0);
        queryWrapper.eq(WorkOrderEntity.IS_DELETE, 0);
        return AjaxResult.success(workOrderService.count(queryWrapper));
    }

    // TODO 删除
    @GetMapping("/deleteOrder/{ids}")
    @SysLogAnnotation(moduleName = "工单模块", childModule = "我的工单/工单列表", operation = "删除", procedureInfo = "删除工单")
    public AjaxResult deleteOrder(@PathVariable Integer[] ids) {
        for (Integer id : ids) {
            WorkOrderEntity workOrderEntity = workOrderService.getById(id);
            workOrderEntity.setIsDelete(1);
            workOrderService.updateById(workOrderEntity);
        }
        return AjaxResult.success();
    }

    // TODO 修改
    @PostMapping("/editOrder")
    @SysLogAnnotation(moduleName = "工单模块", childModule = "我的工单/工单列表", operation = "修改", procedureInfo = "修改工单")
    public AjaxResult editOrder(@RequestBody WorkOrderEntity workOrderEntity){
        workOrderEntity.setProcessStatus(0);
        workOrderEntity.setProcessResult(0);
        return toAjax(workOrderService.updateById(workOrderEntity));
    }

    @GetMapping("/getWorkOrderByDbId/{dbId}")
    public AjaxResult getWorkOrderByDbId(@PathVariable Integer dbId){
        QueryWrapper<WorkOrderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(WorkOrderEntity.DB_ID, dbId);
        queryWrapper.eq(WorkOrderEntity.PROCESS_STATUS, 1);
        queryWrapper.eq(WorkOrderEntity.USER_ID, getUserId());
        queryWrapper.eq(WorkOrderEntity.PROCESS_RESULT, 1);
        queryWrapper.eq(WorkOrderEntity.IS_DELETE, 0);
        queryWrapper.eq(WorkOrderEntity.STATUS, 0);
        return AjaxResult.success(workOrderService.list(queryWrapper));
    }


}
