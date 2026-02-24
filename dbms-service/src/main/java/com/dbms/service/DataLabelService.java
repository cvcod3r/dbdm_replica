package com.dbms.service;

import com.dbms.entity.DataLabelEntity;
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
public interface DataLabelService extends IService<DataLabelEntity> {
    boolean deleteByIds(Integer[] groupIds);

    Map<String,Map<String, Object>> extractTableInfo(DbaseEntity dbaseEntity, String schemaName, Map<String, List<Map<String, Integer>>> tableInfo) throws Exception;

    Map<String,Map<String, Object>> extractTableInfoForHBase(DbaseEntity dbaseEntity, String schemaName, Map<String, List<Map<String, Integer>>> tableInfo) ;
}
