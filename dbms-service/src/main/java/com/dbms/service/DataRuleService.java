package com.dbms.service;

import com.dbms.entity.DataRuleEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dbms.entity.DbaseEntity;

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
public interface DataRuleService extends IService<DataRuleEntity> {
    boolean deleteByIds(Integer[] groupIds);
    Map<String,List<Map<String,List<Integer>>>> sensitiveDataScan(DbaseEntity dbaseEntity, String schemaName, List<String> tableNames, List<List<Integer>> dataRuleList);
    Map<Integer,String> getRuleOptions();
    boolean match(String pattern,String str);
}
