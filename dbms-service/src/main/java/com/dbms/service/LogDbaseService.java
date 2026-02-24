package com.dbms.service;

import com.dbms.entity.DbaseEntity;
import com.dbms.entity.LogDbaseEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author <a href="https://www.fengwenyi.com?code">YSL</a>
 * @since 2023-02-05
 */
public interface LogDbaseService extends IService<LogDbaseEntity> {

    List<LogDbaseEntity> selectLogList(LogDbaseEntity logDbaseEntity);

    void saveLog(Map<String, Object> map, DbaseEntity dbaseEntity, String ip);

    List<Map<String, Object>> getSqlTypeCountMonitor(Integer dbId, String dbType,Date beginTime, Date endTime);

    Long getSelectDataCount(Integer dbId, Date beginTime, Date endTime);
}
