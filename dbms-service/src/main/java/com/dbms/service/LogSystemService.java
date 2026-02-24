package com.dbms.service;

import com.dbms.entity.LogSystemEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author <a href="https://www.fengwenyi.com?code">YSL</a>
 * @since 2023-02-05
 */
public interface LogSystemService extends IService<LogSystemEntity> {

    List<LogSystemEntity> selectLogList(LogSystemEntity logSystemEntity);
}
