package com.dbms.service.impl;

import com.dbms.core.AjaxResult;
import com.dbms.entity.RiskOperationEntity;
import com.dbms.dao.RiskOperationDao;
import com.dbms.service.RiskOperationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author <a href="https://www.fengwenyi.com?code">YSL</a>
 * @since 2023-02-05
 */
@Service
public class RiskOperationServiceImpl extends ServiceImpl<RiskOperationDao, RiskOperationEntity> implements RiskOperationService {
    @Override
    public RiskOperationEntity getActionTablePri(List<RiskOperationEntity> resultRiskOperation){

        try {
            RiskOperationEntity results=new RiskOperationEntity();
            Map<String,List<String>> mergedMap=new HashMap<>();
            Map<String, Map<String, List<String>>> mergeColMap=new HashMap<>();
            if (resultRiskOperation.isEmpty()){
                return new RiskOperationEntity();
            }
            for (RiskOperationEntity singleRisk:resultRiskOperation){
                singleRisk.stringToObject();
                Map<String,List<String>> tablePriMap=singleRisk.getTablePrivilegeMap();
                Map<String,Map<String,List<String>>> columnPriMap=singleRisk.getColumnPrivilegeMap();
                if(!tablePriMap.isEmpty()){
                    for (Map.Entry<String, List<String>> entry : tablePriMap.entrySet()) {
                        String key = entry.getKey();
                        List<String> value = entry.getValue();
                        if (!mergedMap.containsKey(key)) {
                            mergedMap.put(key, value);
                        } else {
                            List<String> existingValue = mergedMap.get(key);
                            Set<String> combinedSet = new HashSet<>(existingValue);
                            combinedSet.addAll(value);
                            List<String> combinedList = new ArrayList<>(combinedSet);
                            mergedMap.put(key, combinedList);
                        }
                    }
                }
                if(!columnPriMap.isEmpty()){
                    for (Map.Entry<String, Map<String, List<String>>> columnMap : columnPriMap.entrySet()) {
                        //表名
                        String key = columnMap.getKey();
                        Map<String, List<String>> valueMap = columnMap.getValue();
                        if (!mergeColMap.containsKey(key)) {
                            mergeColMap.put(key, valueMap);
                        } else {
                            Map<String, List<String>> existingValueMap = mergeColMap.get(key);
                            for (Map.Entry<String, List<String>> valueEntry : valueMap.entrySet()) {
//                            列名
                                String valueKey = valueEntry.getKey();
                                List<String> valueList = valueEntry.getValue();
                                if (!existingValueMap.containsKey(valueKey)) {
                                    existingValueMap.put(valueKey, valueList);
                                } else {
                                    List<String> existingValueList = existingValueMap.get(valueKey);
                                    Set<String> combinedSet = new HashSet<>(existingValueList);
                                    combinedSet.addAll(valueList);
                                    List<String> combinedList = new ArrayList<>(combinedSet);
                                    existingValueMap.put(valueKey, combinedList);
                                }
                            }
                        }
                    }
                }
            }
            results.setTablePrivilegeMap(mergedMap);
            results.setColumnPrivilegeMap(mergeColMap);
            return results;
        }
        catch (Exception e){
            return null;
        }

    }
    @Override
    public  RiskOperationEntity getActionColumnSensitive(List<RiskOperationEntity> resultRiskOperation){
        try {
            if(resultRiskOperation.isEmpty()){
                return  new RiskOperationEntity();
            }
            RiskOperationEntity riskOperationEntity=new RiskOperationEntity();
            Map<String, Map<String, Integer>> result = new HashMap<>();
            List<Map<String, Map<String, Integer>>> maps = new ArrayList<>();
            for (RiskOperationEntity entity : resultRiskOperation){
                entity.stringToObject();
                maps.add(entity.getColumnSensitiveMap());
            }
            if (!maps.isEmpty()){
                for (Map<String, Map<String, Integer>> map : maps) {
                    for (Map.Entry<String, Map<String, Integer>> entry : map.entrySet()) {
                        String key = entry.getKey();
                        Map<String, Integer> value = entry.getValue();
                        if (result.containsKey(key)) {
                            continue;
                        }
                        result.put(key, value);
                    }
                }
            }
            riskOperationEntity.setColumnSensitiveMap(result);
            return riskOperationEntity;
        }
        catch (Exception e){
            return null;
        }

    }

    @Override
    public  RiskOperationEntity gettableRowLimit(List<RiskOperationEntity> resultRiskOperation){
        try{
            RiskOperationEntity riskresult=new RiskOperationEntity();
            if(resultRiskOperation.isEmpty()){
                return  new RiskOperationEntity();
            }
            Map<String,Integer> result =new HashMap<>();
            for (RiskOperationEntity riskentity:resultRiskOperation){
                if(!riskentity.getTableRowLimitMap().isEmpty()){
                    for (Map.Entry<String,Integer> map:riskentity.getTableRowLimitMap().entrySet()){
                        String key=map.getKey();
                        Integer value=map.getValue();
                        if (!result.containsKey(key)){
                            result.put(key,value);
                        }
                        else{
                            if(result.get(key)>value){
                                result.put(key,value);
                            }
                        }
                    }
                }
            }
            riskresult.setTableRowLimitMap(result);
            return riskresult;
        }
        catch (Exception e){
            return new RiskOperationEntity();
        }

    }
}
