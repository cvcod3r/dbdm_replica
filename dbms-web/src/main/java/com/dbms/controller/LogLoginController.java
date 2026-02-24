package com.dbms.controller;

import com.dbms.core.BaseController;
import com.dbms.entity.LogDbaseEntity;
import com.dbms.entity.LogLoginEntity;
import com.dbms.page.TableDataInfo;
import com.dbms.service.LogLoginService;
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
 * @author <a href="https://www.fengwenyi.com?code">YSL</a>
 * @since 2023-02-05
 */
@RestController
@RequestMapping("/log-login")
public class LogLoginController extends BaseController {

    @Autowired
    private LogLoginService logLoginService;

    @GetMapping("/list")
    public TableDataInfo list(LogLoginEntity logLoginEntity)
    {
        startPage();
        List<LogLoginEntity> list = logLoginService.selectLogList(logLoginEntity);
        return getDataTable(list);
    }

    // TODO 登录信息：分析用户的登录信息，包括登录时间、IP地址、登录账号等，以便管理员能够及时监控用户的登录行为
    @GetMapping("/getMonitorUserLogin/{userId}")
    public TableDataInfo getMonitorUserLogin(LogLoginEntity logLoginEntity)
    {
        startPage();
        List<LogLoginEntity> list = logLoginService.selectLogList(logLoginEntity);
        return getDataTable(list);
    }


}
