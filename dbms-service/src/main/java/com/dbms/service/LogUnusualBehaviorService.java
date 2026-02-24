package com.dbms.service;

import com.dbms.entity.DbaseEntity;
import com.dbms.entity.LogUnusualBehaviorEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.text.ParseException;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author HLH
 * @since 2023-06-15
 */
public interface LogUnusualBehaviorService extends IService<LogUnusualBehaviorEntity> {
    List<LogUnusualBehaviorEntity> selectLogList(LogUnusualBehaviorEntity logUnusualBehaviorEntity);

    void timeLimitCheck(DbaseEntity dbaseEntity) throws ParseException;
}
