package com.dbms.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.annotation.SysLogAnnotation;
import com.dbms.core.AjaxResult;
import com.dbms.core.BaseController;
import com.dbms.entity.AccessStrategyEntity;
import com.dbms.entity.RiskOperationEntity;
import com.dbms.service.AccessStrategyService;
import com.dbms.service.RiskOperationService;
import com.dbms.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author <a href="https://www.fengwenyi.com?code">YSL</a>
 * @since 2023-02-05
 */
@RestController
@RequestMapping("/access-strategy-review")
public class AccessStrategyReviewController extends BaseController {

    @Autowired
    private AccessStrategyService accessStrategyService;
    @Autowired
    private RiskOperationService riskOperationService;

    @GetMapping("/getAccessReview/{schemaName}")
    @SysLogAnnotation(moduleName = "访问策略模块", childModule = "访问控制策略", operation = "查询", procedureInfo = "查询访问控制策略")
    public AjaxResult getAccessReview(AccessStrategyEntity accessStrategyEntity,String schemaName) {
            Map<String,RiskOperationEntity> result = new HashMap<>();
            QueryWrapper<AccessStrategyEntity> queryWrapperAS = new QueryWrapper<>();
            if (StringUtils.isNotNull(accessStrategyEntity.getDbId())) {
                queryWrapperAS.eq(AccessStrategyEntity.DB_ID, accessStrategyEntity.getDbId());
            }
            if (StringUtils.isNotNull(accessStrategyEntity.getGroupId())) {
                queryWrapperAS.eq(AccessStrategyEntity.GROUP_ID, accessStrategyEntity.getGroupId());
            }
            if (StringUtils.isNotNull(accessStrategyEntity.getUserId())) {
                queryWrapperAS.eq(AccessStrategyEntity.USER_ID, accessStrategyEntity.getUserId());
            }
            List<AccessStrategyEntity> list = accessStrategyService.list(queryWrapperAS);
            List<Integer> riskIds = new ArrayList<>();
            if(list.isEmpty()){
                return AjaxResult.error("当前选项未配置高危操作");
            }
        for (AccessStrategyEntity singleAccess:list){
            riskIds.add(singleAccess.getRiskId());
            System.out.println(singleAccess.getRiskId());
        }
            QueryWrapper<RiskOperationEntity> queryWrapperRO = new QueryWrapper<>();
            queryWrapperRO.in(RiskOperationEntity.RISK_ID, riskIds);
            queryWrapperRO.eq(RiskOperationEntity.IS_DELETE, 0);
            queryWrapperRO.eq(RiskOperationEntity.STATUS,0);
            queryWrapperRO.eq(RiskOperationEntity.SCHEMA_NAME,schemaName);
            queryWrapperRO.eq(RiskOperationEntity.ACTION_TYPE, "A");
            List<RiskOperationEntity> resultRiskOperationAs=riskOperationService.list(queryWrapperRO);

            QueryWrapper<RiskOperationEntity> queryWrapperR1 = new QueryWrapper<>();
            queryWrapperR1.in(RiskOperationEntity.RISK_ID, riskIds);
            queryWrapperR1.eq(RiskOperationEntity.IS_DELETE, 0);
            queryWrapperR1.eq(RiskOperationEntity.STATUS,0);
            queryWrapperR1.eq(RiskOperationEntity.SCHEMA_NAME,schemaName);
            queryWrapperR1.eq(RiskOperationEntity.ACTION_TYPE, "B");
            List<RiskOperationEntity> resultRiskOperationBs=riskOperationService.list(queryWrapperR1);

            QueryWrapper<RiskOperationEntity> queryWrapperR2 = new QueryWrapper<>();
            queryWrapperR2.in(RiskOperationEntity.RISK_ID, riskIds);
            queryWrapperR2.eq(RiskOperationEntity.IS_DELETE, 0);
            queryWrapperR2.eq(RiskOperationEntity.STATUS,0);
            queryWrapperR2.eq(RiskOperationEntity.SCHEMA_NAME,schemaName);
            queryWrapperR2.eq(RiskOperationEntity.ACTION_TYPE, "C");
            List<RiskOperationEntity> resultRiskOperationCs=riskOperationService.list(queryWrapperR2);

            QueryWrapper<RiskOperationEntity> queryWrapperR3 = new QueryWrapper<>();
            queryWrapperR3.in(RiskOperationEntity.RISK_ID, riskIds);
            queryWrapperR3.eq(RiskOperationEntity.IS_DELETE, 0);
            queryWrapperR3.eq(RiskOperationEntity.STATUS,0);
            queryWrapperR3.eq(RiskOperationEntity.SCHEMA_NAME,schemaName);
            queryWrapperR3.eq(RiskOperationEntity.ACTION_TYPE, "D");
            List<RiskOperationEntity> resultRiskOperationDs=riskOperationService.list(queryWrapperR3);

            RiskOperationEntity riskOperationForTablePriA=riskOperationService.getActionTablePri(resultRiskOperationAs);
            RiskOperationEntity riskOperationForTablePriB=riskOperationService.getActionTablePri(resultRiskOperationBs);
            RiskOperationEntity riskOperationForTablePriC=riskOperationService.getActionColumnSensitive(resultRiskOperationCs);
            RiskOperationEntity riskOperationForTablePriD=riskOperationService.gettableRowLimit(resultRiskOperationDs);

            if (riskOperationForTablePriA!=null){
                riskOperationForTablePriA.setActionType("A");
                riskOperationForTablePriA.setLabelStatus(0);
                riskOperationForTablePriA.copyToString();
                riskOperationForTablePriA.stringToObject();
                System.out.println(JSON.toJSONString(resultRiskOperationAs));
                result.put("A",riskOperationForTablePriA);
            }
            if (riskOperationForTablePriB!=null){
                riskOperationForTablePriB.setActionType("B");
                riskOperationForTablePriB.setLabelStatus(0);
                riskOperationForTablePriB.copyToString();
                riskOperationForTablePriB.stringToObject();
                System.out.println(JSON.toJSONString(resultRiskOperationBs));
                result.put("B",riskOperationForTablePriB);
            }
            if (riskOperationForTablePriC!=null){
                riskOperationForTablePriC.setActionType("C");
                riskOperationForTablePriC.setLabelStatus(1);
                riskOperationForTablePriC.copyToString();
                riskOperationForTablePriC.stringToObject();
                System.out.println(JSON.toJSONString(resultRiskOperationCs));
                result.put("C",riskOperationForTablePriC);
            }
            if (riskOperationForTablePriD!=null){
                riskOperationForTablePriD.setActionType("D");
                riskOperationForTablePriD.setLabelStatus(0);
                riskOperationForTablePriD.copyToString();
                riskOperationForTablePriD.stringToObject();
                System.out.println(JSON.toJSONString(resultRiskOperationDs));
                result.put("D",riskOperationForTablePriD);

            }
            if(riskOperationForTablePriA==null&&riskOperationForTablePriB==null&&riskOperationForTablePriC==null&&riskOperationForTablePriD==null){
                return AjaxResult.error("当前选项未配置高危操作");
            }
            return AjaxResult.success(result);
    }


    @GetMapping(value = "/selectById/{strategyId}")
    @SysLogAnnotation(moduleName = "访问策略模块", childModule = "访问控制策略", operation = "查询", procedureInfo = "查询访问控制策略")
    public AjaxResult selectById(@PathVariable Integer strategyId)
    {
        return AjaxResult.success(accessStrategyService.getById(strategyId));
    }


}
