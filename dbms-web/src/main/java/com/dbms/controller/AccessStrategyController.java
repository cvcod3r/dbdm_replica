package com.dbms.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.annotation.CacheRemove;
import com.dbms.annotation.SysLogAnnotation;
import com.dbms.core.AjaxResult;
import com.dbms.core.BaseController;
import com.dbms.entity.AccessStrategyEntity;
import com.dbms.page.TableDataInfo;
import com.dbms.service.AccessStrategyService;
import com.dbms.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import javax.jdo.annotations.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author <a href="https://www.fengwenyi.com?code">YSL</a>
 * @since 2023-02-05
 */
@RestController
@RequestMapping("/access-strategy")
public class AccessStrategyController extends BaseController {

    @Autowired
    private AccessStrategyService accessStrategyService;

    @GetMapping("/listAccess")
    @SysLogAnnotation(moduleName = "访问策略模块", childModule = "访问控制策略", operation = "查询", procedureInfo = "查询访问控制策略")
    public TableDataInfo listAccess(AccessStrategyEntity accessStrategyEntity) {

        startPage();
        QueryWrapper<AccessStrategyEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotNull(accessStrategyEntity.getUrl())) {
            queryWrapper.like(AccessStrategyEntity.URL, accessStrategyEntity.getUrl());
        }
        if (StringUtils.isNotNull(accessStrategyEntity.getGroupName())) {
            queryWrapper.like(AccessStrategyEntity.GROUP_NAME, accessStrategyEntity.getGroupName());
        }
        if (StringUtils.isNotNull(accessStrategyEntity.getUsername())) {
            queryWrapper.like(AccessStrategyEntity.USERNAME, accessStrategyEntity.getUsername());
        }
        queryWrapper.eq(AccessStrategyEntity.IS_DELETE, 0);
        List<AccessStrategyEntity> list = accessStrategyService.list(queryWrapper);
//        System.out.println("调用这里了");
        return getDataTable(list);
    }


    @GetMapping(value = "/selectById/{strategyId}")
    @SysLogAnnotation(moduleName = "访问策略模块", childModule = "访问控制策略", operation = "查询", procedureInfo = "查询访问控制策略")
    public AjaxResult selectById(@PathVariable Integer strategyId)
    {
        return AjaxResult.success(accessStrategyService.getById(strategyId));
    }

    /**
     * 删除用户组
     */
    @DeleteMapping("/deleteAccess/{strategyIds}")
    @SysLogAnnotation(moduleName = "访问策略模块", childModule = "访问控制策略", operation = "删除", procedureInfo = "删除访问控制策略")
    @CacheRemove(value = {"risk_oper:*", "desensitization:*"})
    @Transactional
    public AjaxResult deleteAccess(@PathVariable Integer[] strategyIds) {
        if (strategyIds.length == 0) {
            return AjaxResult.error();
        } else {
            for (Integer strategyId : strategyIds) {
                AccessStrategyEntity accessStrategyEntity = accessStrategyService.getById(strategyId);
                accessStrategyEntity.setIsDelete(1);

                accessStrategyService.updateById(accessStrategyEntity);
            }
        }
        return AjaxResult.success();
    }


    /**
     * 新增用户组
     */
    @PostMapping(value = "/addAccess/{riskIds}")
    @SysLogAnnotation(moduleName = "访问策略模块", childModule = "访问控制策略", operation = "新增", procedureInfo = "新增访问控制策略")
    @CacheRemove(value = {"risk_oper:*", "desensitization:*"})
    @Transactional
    public AjaxResult add(@PathVariable Integer[] riskIds, @RequestBody AccessStrategyEntity accessStrategyEntity)
    {
//        System.out.println(JSON.toJSONString(riskIds));
        Map<Integer, String> actionTypeMap = JSON.parseObject(accessStrategyEntity.getRiskActionTypeMap(), new TypeReference<Map<Integer, String>>() {});
        if (riskIds != null && riskIds.length !=0){
            for (Integer riskId:riskIds){
                accessStrategyEntity.setStrategyId(null);
                accessStrategyEntity.setRiskId(riskId);
                accessStrategyEntity.setCreatetime(LocalDateTime.now());
                String actionType = actionTypeMap.getOrDefault(riskId, null);
                System.out.println(riskId + ":" + actionType);
                accessStrategyEntity.setActionType(actionType);
                accessStrategyService.save(accessStrategyEntity);
            }
            return AjaxResult.success();
        }
        return AjaxResult.error();
    }


    @PostMapping(value = "/addByDesensitization")
//    @SysLogAnnotation(moduleName = "访问策略模块", childModule = "访问控制策略", operation = "新增", procedureInfo = "新增访问控制策略")
//    @CacheRemove(value = {"risk_oper:*", "desensitization:*"})
//    @Transactional
    public AjaxResult addByDesensitization(@RequestBody AccessStrategyEntity accessStrategyEntity)
    {
        accessStrategyEntity.setStrategyId(null);
        accessStrategyEntity.setCreatetime(LocalDateTime.now());
        accessStrategyEntity.setActionType("C");
        accessStrategyEntity.setUsername(getUsername());
        accessStrategyEntity.setUserId(getUserId());
        accessStrategyEntity.setGroupId(getGroupId());
        System.out.println(JSON.toJSONString(accessStrategyEntity));
        return AjaxResult.success(accessStrategyService.save(accessStrategyEntity));
    }

    /**
     * 修改
     * @param accessStrategyEntity
     * @return
     */
    @PutMapping(value = "/updateAccess")
    @SysLogAnnotation(moduleName = "访问策略模块", childModule = "访问控制策略", operation = "修改", procedureInfo = "修改访问控制策略")
    @CacheRemove(value = {"risk_oper:*", "desensitization:*"})
    @Transactional
    public AjaxResult edit(@RequestBody AccessStrategyEntity accessStrategyEntity)
    {
//        if(riskIds != null || riskIds.length ==0){
//
//            for (Integer riskId:riskIds){
//                accessStrategyEntity.setRiskId(riskId);
//                accessStrategyEntity.setUpdatetime(LocalDateTime.now());
//                accessStrategyService.updateById(accessStrategyEntity);
//            }
//        }
//        accessStrategyEntity.setRiskId(riskIds);
        accessStrategyEntity.setUpdatetime(LocalDateTime.now());
//        accessStrategyService.updateById(accessStrategyEntity);
        //存储更改的用户信息

        return toAjax(accessStrategyService.saveOrUpdate(accessStrategyEntity));
    }
}
