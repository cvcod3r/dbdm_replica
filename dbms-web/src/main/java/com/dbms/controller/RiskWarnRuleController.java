package com.dbms.controller;

import com.dbms.core.BaseController;
import com.dbms.entity.LogRiskAlertEntity;
import com.dbms.entity.RiskWarnRuleEntity;
import com.dbms.page.TableDataInfo;
import com.dbms.service.LogRiskAlertService;
import com.dbms.service.RiskWarnRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author YSL
 * @since 2023-05-12
 */
@RestController
@RequestMapping("/risk-warn-rule")
public class RiskWarnRuleController extends BaseController {
    @Autowired
    private RiskWarnRuleService riskWarnRuleService;

    @GetMapping("/listLogAlertScan")
    public TableDataInfo listLogAlertScan(RiskWarnRuleEntity riskWarnRuleEntity)
    {
        startPage();
        List<RiskWarnRuleEntity> list = riskWarnRuleService.selectLogList(riskWarnRuleEntity);
        return getDataTable(list);
    }
}
