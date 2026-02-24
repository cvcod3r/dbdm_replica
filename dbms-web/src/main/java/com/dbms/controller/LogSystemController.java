package com.dbms.controller;

import com.dbms.core.BaseController;
import com.dbms.entity.LogSystemEntity;
import com.dbms.page.TableDataInfo;
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
 * @author <a href="https://www.fengwenyi.com?code">YSL</a>
 * @since 2023-02-05
 */
@RestController
@RequestMapping("/log-system")
public class LogSystemController extends BaseController {

    @Autowired
    private LogSystemService logSystemService;

    @GetMapping("/list")
    public TableDataInfo list(LogSystemEntity logSystemEntity)
    {
        startPage();
        List<LogSystemEntity> list = logSystemService.selectLogList(logSystemEntity);
        return getDataTable(list);
    }
}
