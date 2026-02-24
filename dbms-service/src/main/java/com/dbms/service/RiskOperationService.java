package com.dbms.service;

import com.dbms.entity.DbaseEntity;
import com.dbms.entity.RiskOperationEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dbms.entity.UserEntity;

import java.util.HashSet;
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
public interface RiskOperationService extends IService<RiskOperationEntity> {
    RiskOperationEntity getActionTablePri(List<RiskOperationEntity> resultRiskOperation);

    RiskOperationEntity getActionColumnSensitive(List<RiskOperationEntity> resultRiskOperation);

    RiskOperationEntity gettableRowLimit(List<RiskOperationEntity> resultRiskOperation);
}

