package com.dbms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.core.AjaxResult;
import com.dbms.core.BaseController;
import com.dbms.entity.LogRiskAlertEntity;
import com.dbms.entity.LogUnusualBehaviorEntity;
import com.dbms.page.TableDataInfo;
import com.dbms.service.LogRiskAlertService;
import com.dbms.service.LogUnusualBehaviorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.dbms.utils.PageUtils.startPage;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author HLH
 * @since 2023-06-15
 */
@RestController
@RequestMapping("/log-unusual-behavior")
public class LogUnusualBehaviorController extends BaseController {

    @Autowired
    private LogUnusualBehaviorService logUnusualBehaviorService;

    @GetMapping("/list")
    public TableDataInfo list(LogUnusualBehaviorEntity logUnusualBehaviorEntity)
    {
        startPage();
        System.out.println(logUnusualBehaviorEntity.getUsername());
        System.out.println(logUnusualBehaviorEntity.getUrl());
        List<LogUnusualBehaviorEntity> list = logUnusualBehaviorService.selectLogList(logUnusualBehaviorEntity);
        return getDataTable(list);
    }

}
