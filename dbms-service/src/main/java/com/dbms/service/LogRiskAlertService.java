package com.dbms.service;

import com.dbms.entity.DbaseEntity;
import com.dbms.entity.LogRiskAlertEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author YSL
 * @since 2023-04-27
 */
public interface LogRiskAlertService extends IService<LogRiskAlertEntity> {
    List<LogRiskAlertEntity> selectLogList(LogRiskAlertEntity logRiskAlertEntity);

    //DbaseEntity sql语句 sql操作类型
    void LogRiskAlterForHbase(DbaseEntity dbaseEntity, String sql, String ip);

    void LogRiskAlterForHive(DbaseEntity dbaseEntity, String sql, String ip);

    void LogRiskAlterForES(DbaseEntity dbaseEntity, String sql, String shellsql,String ip);
}
