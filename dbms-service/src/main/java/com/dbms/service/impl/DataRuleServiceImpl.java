package com.dbms.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.core.dbase.DataBaseUtilService;
import com.dbms.dao.SensitiveScanResultDao;
import com.dbms.entity.DataRuleEntity;
import com.dbms.dao.DataRuleDao;
import com.dbms.entity.DbaseEntity;
import com.dbms.entity.SensitiveScanResultEntity;
import com.dbms.repository.impl.DbopServiceImpl;
import com.dbms.service.DataRuleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static com.dbms.utils.SecurityUtils.*;
import com.dbms.utils.DbInfoUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author <a href="https://www.fengwenyi.com?code">YSL</a>
 * @since 2023-02-05
 */
@Service
public class DataRuleServiceImpl extends ServiceImpl<DataRuleDao, DataRuleEntity> implements DataRuleService {

    @Autowired
    private DataRuleDao dataRuleDao;

    @Autowired
    private SensitiveScanResultDao sensitiveScanResultDao;

    @Autowired
    private DataBaseUtilService dataBaseUtilService;
    private static final Logger logger = LoggerFactory.getLogger(DbopServiceImpl.class);

    public boolean deleteByIds(Integer[] groupIds) {
        if (groupIds.length==0){
            return false;
        }else{
            for (Integer groupId : groupIds) {
                DataRuleEntity dataRuleEntity = dataRuleDao.selectById(groupId);
                dataRuleEntity.setIsDelete(1);
                dataRuleDao.updateById(dataRuleEntity);
            }
        }
        return true;
    }

    /**
     * 正则表达式判断函数，若满足则返回true
     * @param pattern
     * @param str
     * @return
     */
    public boolean match(String pattern,String str)
    {
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(str);
        return m.matches();
    }

    /**
     * 获得rule表中Id对正则表达式的映射
     * @return
     */
    public Map<Integer,String> getRuleOptions()
    {
        QueryWrapper<DataRuleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DataRuleEntity.IS_DELETE,0);
        List<DataRuleEntity> res = list(queryWrapper);
        Map<Integer,String> map = new HashMap<>();
        for(DataRuleEntity dataRuleEntity : res)
        {
            map.put(dataRuleEntity.getRuleId(),dataRuleEntity.getRuleReg());
        }
        return map;
    }

    /**
     * 敏感数据扫描函数
     * @param dbaseEntity
     * @param schemaName
     * @param tableNames
     * @param dataRuleList
     * @return
     */
    public Map<String,List<Map<String,List<Integer>>>> sensitiveDataScan(DbaseEntity dbaseEntity, String schemaName, List<String> tableNames, List<List<Integer>> dataRuleList)
    {
        // 单表操作，每次只查一张表的所有列，然后进行敏感数据扫描，将扫描的结果填入三个Map中，其结构为<列名，[敏感数据标签id]>，分别代表列名称，列内容和列备注
        // 最后的总结构为<表名，[三个Map]>
        // 上方想法只是暂定，后面的具体结果可以等表建出来以后根据字段存入即可

        // 存返回结果映射map
        Map<String, Object> map = new HashMap<>();
        System.out.println("------------敏感数据扫描---------------");
        String connKey = DbInfoUtil.getConnKey(dbaseEntity, getUserId(), schemaName);
        // 最终结果
        Map<String,List<Map<String,List<Integer>>>> result = new HashMap<>();
        // 准备工作1
        // 建立规则ID和规则正则表达式的映射
        Map<Integer,String> ruleIdToReg = getRuleOptions();
        System.out.println("ruleIdToReg: " + ruleIdToReg);
        // 遍历每张表
        for(String tableName : tableNames) {
            System.out.println("-----------------" + tableName + "-------------------");
            // 后续需要写一个函数专门用于填充sql字符串***
            String sql = "";
            //拼接sql
            if(dbaseEntity.getDbType().equals("DM") || dbaseEntity.getDbType().equals("KingBase") || dbaseEntity.getDbDriver().equals("com.gbase.jdbc.Driver") ) {
                sql = String.format("select * from \"%s\".\"%s\" limit 1", schemaName, tableName);
                System.out.println("执行SQL语句: " + sql);
                map.put("sql", sql);
            } else if (dbaseEntity.getDbType().equals("Oscar")){
                sql = String.format("select * from %s.%s limit 1", schemaName, tableName);
                System.out.println("执行SQL语句: " + sql);
                map.put("sql", sql);
            } else if (dbaseEntity.getDbDriver().equals("com.gbasedbt.jdbc.Driver")) {
                sql = String.format("select * from %s:%s limit 1", schemaName, tableName);
                System.out.println("执行SQL语句: " + sql);
                map.put("sql", sql);
            } else if (dbaseEntity.getDbType().equals("Hive")) {
                sql = String.format("select * from %s limit 1", tableName);
                System.out.println("执行SQL语句: " + sql);
                map.put("sql", sql);
            }
            System.out.println("======执行SQL语句: " + sql);
            map.put(tableName, sql);
            // 存储三个Map
            List<Map<String,List<Integer>>> tableObjectList = new ArrayList<>();
            try {
                // 获取sql执行结果集
                List<Map<String, Object>> list = dataBaseUtilService.queryForList(dbaseEntity, sql, connKey);
                System.out.println("list: " + list);
                map.put(tableName+"_list", list);
                // 建立所谓的三个Map，结构<列名，[敏感数据标签id]>
                Map<String,List<Integer>> columnNameIdList = new HashMap<>();
                Map<String,List<Integer>> columnContentIdList = new HashMap<>();
                Map<String,List<Integer>> columnRemarkIdList = new HashMap<>();
                // 目前没有实现对不同对象操作
                // columnObject中0，1，2分别对应列名称，列内容，列备注
                int columnObject = 0;

                // 敏感数据扫描操作
                // 针对三种匹配对象：列名称，列内容，列备注
                for(List<Integer> dataRule : dataRuleList)
                {
                    // 针对三个对象的各个规则
                    for(Integer dataRuleId : dataRule)
                    {
                        // 针对该表中每个列对象进行匹配
                        for(Map.Entry<String, Object> entry : list.get(0).entrySet())
                        {

                            String pattern = ruleIdToReg.get(dataRuleId);
                            String str = "null";
                            if(entry.getValue() != null)
                            {
                                str = entry.getValue().toString();
                            }else{
//                                System.out.println("str_error: " + entry.getValue());
                            }
//                            System.out.println("pattern: " + pattern);
//                            System.out.println("str: " + str);
                            boolean Flag = match(pattern,str);
                            if(Flag)
                            {
                                if(columnObject == 0) {
                                    if(!columnNameIdList.containsKey(entry.getKey())) {
                                        columnNameIdList.put(entry.getKey(),new ArrayList<>());
                                    }
                                    columnNameIdList.get(entry.getKey()).add(dataRuleId);
                                } else if (columnObject == 1) {
                                    if(!columnContentIdList.containsKey(entry.getKey())) {
                                        columnContentIdList.put(entry.getKey(),new ArrayList<>());
                                    }
                                    columnContentIdList.get(entry.getKey()).add(dataRuleId);
                                } else {
                                    if(!columnRemarkIdList.containsKey(entry.getKey())) {
                                        columnRemarkIdList.put(entry.getKey(),new ArrayList<>());
                                    }
                                    columnRemarkIdList.get(entry.getKey()).add(dataRuleId);
                                }
                            }
                        }
                    }
                    columnObject++;
                }
                tableObjectList.add(columnNameIdList);
                tableObjectList.add(columnContentIdList);
                tableObjectList.add(columnRemarkIdList);
                System.out.println("-----扫描结束------");
                System.out.println("columnNameIdList:" + columnNameIdList);
                System.out.println("columnContentIdList:" + columnContentIdList);
                System.out.println("columnRemarkIdList:" + columnRemarkIdList);
                System.out.println("tableName: " + tableName + " :: " + "tableObjectList: " + tableObjectList);
                System.out.println("sql successs");
                // 存储扫描结果
                SensitiveScanResultEntity temp = new SensitiveScanResultEntity();
                temp.setDbId(dbaseEntity.getDbId());
                temp.setTableName(tableName);
                temp.setUrl(dbaseEntity.getUrl());
                temp.setScanTime(LocalDateTime.now());
                temp.setSchemaName(schemaName);
                // 遍历每列元素，如果有查询结果则放入并保存
                boolean flag;
                for(Map.Entry<String, Object> entry : list.get(0).entrySet()){
                    flag = false;
                    temp.setColumnName(entry.getKey());
                    if(columnNameIdList.containsKey(entry.getKey())) {
                        temp.setColumnNameResult(JSON.toJSONString(columnNameIdList.get(entry.getKey())));
                        flag = true;
                    } else {
                        temp.setColumnNameResult("");
                    }
                    if(columnContentIdList.containsKey(entry.getKey())) {
                        temp.setColumnRemarkResult(JSON.toJSONString(columnContentIdList.get(entry.getKey())));
                        flag = true;
                    } else {
                        temp.setColumnRemarkResult("");
                    }
                    if(columnRemarkIdList.containsKey(entry.getKey())) {
                        temp.setColumnContentResult(JSON.toJSONString(columnRemarkIdList.get(entry.getKey())));
                        flag = true;
                    } else {
                        temp.setColumnRemarkResult("");
                    }
                    if (flag){
                        sensitiveScanResultDao.insert(temp);
                    }
                }
            } catch (Exception e) {
                System.out.println("sql error");
                logger.error(e.getMessage(), e);
            }
            result.put(tableName,tableObjectList);
        }
        System.out.println("result:" + result);
        return result;
    }
}
