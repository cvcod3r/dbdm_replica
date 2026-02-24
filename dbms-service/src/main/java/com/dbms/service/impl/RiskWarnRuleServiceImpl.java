package com.dbms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.entity.RiskWarnRuleEntity;
import com.dbms.dao.RiskWarnRuleDao;
import com.dbms.service.RiskWarnRuleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dbms.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author YSL
 * @since 2023-05-12
 */
@Service
public class RiskWarnRuleServiceImpl extends ServiceImpl<RiskWarnRuleDao, RiskWarnRuleEntity> implements RiskWarnRuleService {
    @Autowired
    RiskWarnRuleDao riskWarnRuleDao;

    @Autowired
    RiskWarnRuleService riskWarnRuleService;

    @Override
    public List<RiskWarnRuleEntity> selectLogList(RiskWarnRuleEntity riskWarnRuleEntity) {
        QueryWrapper<RiskWarnRuleEntity> queryWrapper=new QueryWrapper<>();

        if(StringUtils.isNotNull(riskWarnRuleEntity.getRuleName())){
            queryWrapper.like(RiskWarnRuleEntity.RULE_NAME,riskWarnRuleEntity.getRuleName());
        }
        if(StringUtils.isNotNull(riskWarnRuleEntity.getRiskLevel())){
            queryWrapper.like(RiskWarnRuleEntity.RISK_LEVEL,riskWarnRuleEntity.getRiskLevel());
        }
        return riskWarnRuleDao.selectList(queryWrapper);
    }
}
