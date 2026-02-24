package com.dbms.service;

import com.dbms.entity.RiskWarnRuleEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author YSL
 * @since 2023-05-12
 */
public interface RiskWarnRuleService extends IService<RiskWarnRuleEntity> {

    List<RiskWarnRuleEntity> selectLogList(RiskWarnRuleEntity riskWarnRuleEntity);
}
