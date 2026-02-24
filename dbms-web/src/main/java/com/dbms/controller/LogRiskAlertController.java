package com.dbms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.core.AjaxResult;
import com.dbms.core.BaseController;
import com.dbms.entity.LogRiskAlertEntity;
import com.dbms.entity.LogSystemEntity;
import com.dbms.page.TableDataInfo;
import com.dbms.service.LogRiskAlertService;
import com.dbms.service.LogSystemService;
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
 * @since 2023-04-27
 */
@RestController
@RequestMapping("/log-risk")
public class LogRiskAlertController extends BaseController {
    @Autowired
    private LogRiskAlertService logRiskAlertService;

    @GetMapping("/list")
    public TableDataInfo list(LogRiskAlertEntity logRiskAlertEntity)
    {
        startPage();
        System.out.println(logRiskAlertEntity.getUserName());
        System.out.println(logRiskAlertEntity.getRiskLevel());
        List<LogRiskAlertEntity> list = logRiskAlertService.selectLogList(logRiskAlertEntity);
        return getDataTable(list);
    }

    @GetMapping("/getUrl")
    public AjaxResult getUrl(){
        QueryWrapper<LogRiskAlertEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT DB_URL");
        List<LogRiskAlertEntity> logRiskAlertEntities = logRiskAlertService.list(queryWrapper);
        return AjaxResult.success(logRiskAlertEntities);
    }
}
