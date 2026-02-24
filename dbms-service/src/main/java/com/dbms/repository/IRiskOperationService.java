package com.dbms.repository;

import com.dbms.bean.HBaseRiskBean;
import com.dbms.bean.RiskBean;
import com.dbms.core.dbase.HBaseShellMeta;
import com.dbms.entity.DbaseEntity;
import com.dbms.entity.RiskOperationEntity;

import java.util.List;

public interface IRiskOperationService {
    RiskBean checkRiskOperation(String sql, Integer userId, Integer groupId, String schemaName, DbaseEntity dbaseEntity) throws Exception;
    List<RiskOperationEntity> getRiskOperations(Integer userId, Integer groupId, Integer dbId);
    HBaseRiskBean checkHBaseShellRiskOperation(HBaseShellMeta shellMeta, String shellType, Integer userId, Integer groupId, Integer dbId);

    List<RiskOperationEntity> getTableRowLimits(Integer userId, Integer groupId, Integer dbId);

    Integer checkTableRowLimits(Integer userId, Integer groupId, String schemaName, String tables, DbaseEntity dbaseEntity);
}
