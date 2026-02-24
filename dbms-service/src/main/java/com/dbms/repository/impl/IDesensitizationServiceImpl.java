package com.dbms.repository.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.constant.CacheConstants;
import com.dbms.core.RedisCache;
import com.dbms.core.SQLParserUtil;
import com.dbms.core.dbase.HBaseShellMeta;
import com.dbms.dao.*;
import com.dbms.entity.*;
import com.dbms.enums.SqlTypeEnum;
import com.dbms.repository.DbopService;
import com.dbms.repository.IDesensitizationService;
import net.sf.jsqlparser.JSQLParserException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.dbms.bean.SensitiveBean;
import com.dbms.utils.*;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
public class IDesensitizationServiceImpl implements IDesensitizationService {
    @Autowired
    private RiskOperationDao riskOperationDao;

    @Autowired
    private DataLabelDao dataLabelDao;

    @Autowired
    private DbopService dbopService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private AccessStrategyDao accessStrategyDao;

    public List<RiskOperationEntity> getDesensitizationOperations(Integer userId, Integer groupId, Integer dbId){
        List<RiskOperationEntity> riskOperationEntities = null;
        String redisKey = CacheConstants.DESENSITIZATION + userId + ":" + groupId + ":" + dbId;
        if (redisTemplate.hasKey(redisKey)){
            riskOperationEntities = redisCache.getCacheObject(redisKey);
            return riskOperationEntities;
        }
        QueryWrapper<AccessStrategyEntity> qw = new QueryWrapper<>();
        qw.eq(AccessStrategyEntity.DB_ID, dbId)
                .and(wr -> wr.eq(AccessStrategyEntity.GROUP_ID, groupId)
                        .or().eq(AccessStrategyEntity.USER_ID, userId))
                .eq(AccessStrategyEntity.IS_DELETE, 0)
                .eq(AccessStrategyEntity.STATUS, 0);
        List<AccessStrategyEntity> accessStrategyEntities = accessStrategyDao.selectList(qw);
        List<Integer> riskIds = new ArrayList<>();
        for (AccessStrategyEntity accessStrategyEntity:accessStrategyEntities){
            Integer riskId = accessStrategyEntity.getRiskId();
            riskIds.add(riskId);
        }
        if (!riskIds.isEmpty()){
            QueryWrapper<RiskOperationEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.in(RiskOperationEntity.RISK_ID, riskIds);
            queryWrapper.eq(RiskOperationEntity.LABEL_STATUS, 1);
            queryWrapper.eq(RiskOperationEntity.IS_DELETE, 0);
            queryWrapper.eq(RiskOperationEntity.STATUS, 0);
            riskOperationEntities = riskOperationDao.selectList(queryWrapper);
        }
        if (riskOperationEntities!= null && riskOperationEntities.size()!=0){
            // 存入redis并设置过期时间
            redisCache.setCacheObject(redisKey, riskOperationEntities, 60*5, TimeUnit.SECONDS);
        }
        return riskOperationEntities;
    }

    public String strtos(String str){
        String s=str;
        if (s.contains("\"")){
            s = s.replace("\"","");
        }
        return s;
    }

    //取对应表的全部脱敏配置
    public SensitiveBean getSensitiveOptions(Integer userId, Integer groupId, Integer dbId, String schemaName,String sql) throws JSQLParserException {
        List<RiskOperationEntity> riskOperationEntities = getDesensitizationOperations(userId,groupId,dbId);
        if(riskOperationEntities== null || riskOperationEntities.size()==0){
            return new SensitiveBean();
        }
        List<Map<String,Map<String, Integer>>> tableColumnToLabelId = new ArrayList<>();
        for (RiskOperationEntity riskOperationEntity : riskOperationEntities){
            //取出高危操作表实体中所需内容，并存入map中
            riskOperationEntity.stringToObject();
            tableColumnToLabelId.add(riskOperationEntity.getColumnSensitiveMap());
        }
        if(tableColumnToLabelId == null || tableColumnToLabelId.size() == 0){
            return new SensitiveBean();
        }
        // 取这些表对应列的脱敏配置，并组合为<表名，<列名，脱敏配置实体其中包含关键参数如正则表达式和参数>>
        // 先获取所有敏感标签实体并建立map<ID,实体>，方便后续取用
        QueryWrapper<DataLabelEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DataLabelEntity.IS_DELETE,0);
        List<DataLabelEntity> response = dataLabelDao.selectList(queryWrapper);
        Map<Integer, DataLabelEntity> IdtoEntity = new HashMap<>();
        if(response == null || response.size() == 0){
            return new SensitiveBean();
        }
        for(DataLabelEntity element : response){
            IdtoEntity.put(element.getLabelId(),element);
        }
        // 建立set用于存储规则名
        Set<String> ruleSet = new HashSet<>();
        List<Map<String,Map<String, DataLabelEntity>>> columnSensitivelist = new ArrayList<>();
        //按照映射逐层去除map，并根据最里层的map去找对应的标签id，同时去除对应的敏感标签实体，重新合并为新的双层map，并返回
        for(Map<String,Map<String, Integer>> labelMap:tableColumnToLabelId) {
//            Map<String,Map<String, Integer>> labelMap = labelList.get(i);
            Map<String,Map<String, DataLabelEntity>> rsMap = new HashMap<>();
            for(Map.Entry<String,Map<String, Integer>> entry : labelMap.entrySet()) {
//                System.out.println("外： key = " + entry.getKey() + ", value = " + entry.getValue());
                Map<String, Integer> labelMap2 = entry.getValue();
                Map<String, DataLabelEntity> rsMap2 = new HashMap<>();
                for (Map.Entry<String, Integer> entry2 : labelMap2.entrySet()){
//                    System.out.println("内： key = " + entry2.getKey() + ", value = " + entry2.getValue());
//                    QueryWrapper<DataLabelEntity> queryWrapper = new QueryWrapper<>();
//                    queryWrapper.eq(DataLabelEntity.LABEL_ID,entry2.getValue());
//                    queryWrapper.eq(DataLabelEntity.IS_DELETE,0);
//                    List<DataLabelEntity> res = dataLabelDao.selectList(queryWrapper);
//                    System.out.println(JSON.toJSONString(res));
                    rsMap2.put(entry2.getKey(),IdtoEntity.get(entry2.getValue()));
                    ruleSet.add(IdtoEntity.get(entry2.getValue()).getLabelName());
                }
                rsMap.put(entry.getKey(),rsMap2);
            }
            columnSensitivelist.add(rsMap);
        }
        // 将规则名set转换为list，其中存储了所有的使用的规则名
        List<String> ruleList = new ArrayList<>(ruleSet);
        System.out.println("ruleList:" + ruleList);
//        System.out.println("columnSensitivelist:" + columnSensitivelist);
        //取表的别名和列的别名
        if (sql.contains("[")||sql.contains("]")){
            sql = sql.replace("[","");
            sql = sql.replace("]","");
        }
        SqlTypeEnum sqlTypeEnum = SQLParserUtil.getSqlType(sql);
        // 获取数据库表的别名map<别名，真名>
        Map<String, String> tableMapRes = SQLParserUtil.getTableMap(sql, sqlTypeEnum);
        Map<String, String> tableMap = new HashMap<>();
        for(Map.Entry<String, String> tableEntry : tableMapRes.entrySet()){
            tableMap.put(strtos(tableEntry.getKey()),strtos(tableEntry.getValue()));
        }
//        System.out.println("tableMap:"+JSON.toJSONString(tableMap));
        Set<String> tableSet = new HashSet<>();
        if (tableMap!=null&&!tableMap.isEmpty()){
            for (String value : tableMap.values()){
                tableSet.add(value);
            }
        }
        //表名列表
        List<String> tableNames = new ArrayList<>(tableSet);
//        System.out.println("tableNames:"+tableNames);
        Map<String, ArrayList<Map<String, String>>> tableColumnMap = SQLParserUtil.getTableColumnMap(sql, sqlTypeEnum);
//        System.out.println("tableColumnMap:" + tableColumnMap);
        // 获取列别名map<别名，真名>
        Map<String,String> columnMap = new HashMap<>();
        for (Map.Entry<String, ArrayList<Map<String, String>>> entryColumn : tableColumnMap.entrySet()) {
//            System.out.println(entryColumn.getKey()+"::"+entryColumn.getValue());
            for(Map<String, String> columnInfo : entryColumn.getValue()){
//                System.out.println(columnInfo);
                if(columnInfo.get("isUseAs") == "true"){
                    columnMap.put(strtos(columnInfo.get("asName")),strtos(columnInfo.get("columnName")));
                }else{
                    columnMap.put(strtos(columnInfo.get("columnName")),strtos(columnInfo.get("columnName")));
                }
            }
        }
//        System.out.println("columnMap:" + columnMap);
//        System.out.println("---获取完所有脱敏配置---");
        //将所有数据添入bean类，并返回
        return new SensitiveBean(columnSensitivelist,tableMap,columnMap,"C",tableNames,ruleList);
    }

    //取对应表的全部脱敏配置(静态脱敏时)
    public SensitiveBean getSensitiveOptionsForStatic( String tableName, Map<String, Integer> tableInfoNow) throws JSQLParserException {
        System.out.println("----获取脱敏配置----");
        if(tableInfoNow== null || tableInfoNow.size()==0){
            return new SensitiveBean();
        }
        // <表名，<列名，脱敏配置实体其中包含关键参数如正则表达式和参数>>
        // 先获取所有敏感标签实体并建立map<ID,实体>，方便后续取用
        QueryWrapper<DataLabelEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DataLabelEntity.IS_DELETE,0);
        List<DataLabelEntity> response = dataLabelDao.selectList(queryWrapper);
        Map<Integer, DataLabelEntity> IdtoEntity = new HashMap<>();
        if(response == null || response.size() == 0){
            return new SensitiveBean();
        }
        for(DataLabelEntity element : response){
            IdtoEntity.put(element.getLabelId(),element);
        }

        List<Map<String,Map<String, DataLabelEntity>>> columnSensitivelist = new ArrayList<>();
        // 直接去转换表脱敏配置为适合脱敏函数的结构
        Map<String,Map<String, DataLabelEntity>> rsMap = new HashMap<>();
        Map<String, DataLabelEntity> rsMap2 = new HashMap<>();
        for (Map.Entry<String, Integer> entry : tableInfoNow.entrySet()){
            rsMap2.put(entry.getKey(),IdtoEntity.get(entry.getValue()));
        }
        rsMap.put(tableName,rsMap2);
        columnSensitivelist.add(rsMap);
        System.out.println("columnSensitivelist:" + columnSensitivelist);

        // 表别名map<别名，真名>
        Map<String, String> tableMap = new HashMap<>();
        tableMap.put(tableName,tableName);

        // 表名列表
        List<String> tableNames = new ArrayList<>();
        tableNames.add(tableName);

        // 列别名map<别名，真名>
        Map<String,String> columnMap = null;

        System.out.println("----获取脱敏配置完成----");
        // 将所有数据添入bean类，并返回
        return new SensitiveBean(columnSensitivelist,tableMap,columnMap,"C",tableNames,null);
    }

    //取对应表的全部脱敏配置（Hbase时）
    public SensitiveBean getSensitiveOptionsForHbase(HBaseShellMeta shellMeta,Integer userId, Integer groupId, Integer dbId) throws JSQLParserException {
//        System.out.println("------------获取脱敏配置--------------");
//        System.out.println("shellMeta： " + shellMeta);
//        System.out.println("userId: " + userId + " groupId： " + groupId + " dbId: " + dbId);
//        System.out.println(shellMeta.getTableName());
//        System.out.println(shellMeta.getColumnFamily());
        List<RiskOperationEntity> riskOperationEntities = getDesensitizationOperations(userId,groupId,dbId);
        if(riskOperationEntities== null || riskOperationEntities.size()==0){
            return new SensitiveBean();
        }
        List<Map<String,Map<String, Integer>>> tableColumnToLabelId = new ArrayList<>();
        for (RiskOperationEntity riskOperationEntity : riskOperationEntities){
            //取出高危操作表实体中所需内容，并存入map中
            riskOperationEntity.stringToObject();
            tableColumnToLabelId.add(riskOperationEntity.getColumnSensitiveMap());
        }
        if(tableColumnToLabelId == null || tableColumnToLabelId.size() == 0){
            return new SensitiveBean();
        }
        // 先获取所有敏感标签实体并建立map<ID,实体>，方便后续取用
        QueryWrapper<DataLabelEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DataLabelEntity.IS_DELETE,0);
        List<DataLabelEntity> response = dataLabelDao.selectList(queryWrapper);
        Map<Integer, DataLabelEntity> IdtoEntity = new HashMap<>();
        if(response == null || response.size() == 0){
            return new SensitiveBean();
        }
        for(DataLabelEntity element : response){
            IdtoEntity.put(element.getLabelId(),element);
        }
        // 建立set用于存储规则名
        Set<String> ruleSet = new HashSet<>();
        // 取这些表对应列的脱敏配置，并组合为<表名，<列簇名:列名，脱敏配置实体其中包含关键参数如正则表达式和参数>>
        List<Map<String,Map<String, DataLabelEntity>>> columnSensitivelist = new ArrayList<>();
        //按照映射逐层去除map，并根据最里层的map去找对应的标签id，同时去除对应的敏感标签实体，重新合并为新的双层map，并返回
        for(Map<String,Map<String, Integer>> labelMap:tableColumnToLabelId) {
//            Map<String,Map<String, Integer>> labelMap = labelList.get(i);
            Map<String,Map<String, DataLabelEntity>> rsMap = new HashMap<>();
            for(Map.Entry<String,Map<String, Integer>> entry : labelMap.entrySet()) {
//                System.out.println("外： key = " + entry.getKey() + ", value = " + entry.getValue());
                Map<String, Integer> labelMap2 = entry.getValue();
                Map<String, DataLabelEntity> rsMap2 = new HashMap<>();
                for (Map.Entry<String, Integer> entry2 : labelMap2.entrySet()){
//                    System.out.println("内： key = " + entry2.getKey() + ", value = " + entry2.getValue());
//                    QueryWrapper<DataLabelEntity> queryWrapper = new QueryWrapper<>();
//                    queryWrapper.eq(DataLabelEntity.LABEL_ID,entry2.getValue());
//                    queryWrapper.eq(DataLabelEntity.IS_DELETE,0);
//                    List<DataLabelEntity> res = dataLabelDao.selectList(queryWrapper);
//                    System.out.println(JSON.toJSONString(res));
                    rsMap2.put(entry2.getKey(),IdtoEntity.get(entry2.getValue()));
                    ruleSet.add(IdtoEntity.get(entry2.getValue()).getLabelName());
                }
                rsMap.put(entry.getKey(),rsMap2);
            }
            columnSensitivelist.add(rsMap);
        }
//        System.out.println("columnSensitivelist:" + columnSensitivelist);
        // 将规则名set转换为list，其中存储了所有的使用的规则名
        List<String> ruleList = new ArrayList<>(ruleSet);
        System.out.println("ruleList:" + ruleList);
        //取表的别名和列的别名
        // 获取数据库表的别名map<别名，真名>
        Map<String, String> tableMap = shellMeta.getTableName();
        //表名列表
        List<String> tableNames = new ArrayList<>();
        for (Map.Entry<String, String> entry3 : tableMap.entrySet()){
            tableNames.add(entry3.getKey());
        }
//        System.out.println("tableNames:"+tableNames);
        // 获取列别名map<别名，真名>
        Map<String,String> columnMap = null;
//        System.out.println("columnMap:" + columnMap);
//        System.out.println("---获取完所有脱敏配置---");
        //将所有数据添入bean类，并返回
        return new SensitiveBean(columnSensitivelist,tableMap,columnMap,"C",tableNames,ruleList);
    }

    //取对应表的全部脱敏配置（Hbase时）
    public SensitiveBean getSensitiveOptionsForHbaseForStatic(HBaseShellMeta shellMeta,Map<String, Integer> tableInfoNow) throws JSQLParserException {
//        System.out.println("------------获取脱敏配置--------------");
//        System.out.println("shellMeta： " + shellMeta);
//        System.out.println("userId: " + userId + " groupId： " + groupId + " dbId: " + dbId);
//        System.out.println(shellMeta.getTableName());
//        System.out.println(shellMeta.getColumnFamily());
        System.out.println("----获取脱敏配置----");
        if(tableInfoNow== null || tableInfoNow.size()==0){
            return new SensitiveBean();
        }
        // 先获取所有敏感标签实体并建立map<ID,实体>，方便后续取用
        QueryWrapper<DataLabelEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DataLabelEntity.IS_DELETE,0);
        List<DataLabelEntity> response = dataLabelDao.selectList(queryWrapper);
        Map<Integer, DataLabelEntity> IdtoEntity = new HashMap<>();
        if(response == null || response.size() == 0){
            return new SensitiveBean();
        }
        for(DataLabelEntity element : response){
            IdtoEntity.put(element.getLabelId(),element);
        }

        List<Map<String,Map<String, DataLabelEntity>>> columnSensitivelist = new ArrayList<>();
        // 直接去转换表脱敏配置为适合脱敏函数的结构
        Map<String,Map<String, DataLabelEntity>> rsMap = new HashMap<>();
        Map<String, DataLabelEntity> rsMap2 = new HashMap<>();
        for (Map.Entry<String, Integer> entry : tableInfoNow.entrySet()){
            rsMap2.put(entry.getKey(),IdtoEntity.get(entry.getValue()));
        }
        // 获取数据库表的别名map<别名，真名>
        Map<String, String> tableMap = shellMeta.getTableName();
        String tableName = "";
        //表名列表
        List<String> tableNames = new ArrayList<>();
        for (Map.Entry<String, String> entry3 : tableMap.entrySet()){
            tableNames.add(entry3.getKey());
            tableName = entry3.getKey();
        }
        rsMap.put(tableName,rsMap2);
        columnSensitivelist.add(rsMap);
        System.out.println("columnSensitivelist:" + columnSensitivelist);

        // 获取列别名map<别名，真名>
        Map<String,String> columnMap = null;
//        System.out.println("columnMap:" + columnMap);
//        System.out.println("---获取完所有脱敏配置---");
        //将所有数据添入bean类，并返回
        return new SensitiveBean(columnSensitivelist,tableMap,columnMap,"C",tableNames,null);
    }

    //用于脱敏替换的核心函数，传入原始字符串，正则表达式和参数
    public String desensitizationCore(String str,String reg,String params){
        if (StringUtils.isNotEmpty(str)) {
            String regex = String.format(reg,params);
//            System.out.println("替换表达式："+str+" "+ reg +" "+ params);
            str = str.replaceAll(regex,params);
        }
        return str;
    }


    /*注：本函数仅实现了带有别名的多表查询，后续可加强的方向：
    1.可尝试在取配置阶段，将带别名的列名直接替换掉，这样就不用保存再在后续传入
    2.可尝试在配置阶段取出配置的表名，比对后若发现当前的表名不属于其中则可以直接跳出脱敏
    */
    //根据对应表，对应列的脱敏配置，替换list中对应内容，并返回
    public List<Map<String, Object>> dynamicDesensitization(List<Map<String, Object>> list ,SensitiveBean sensitiveBean) throws Exception{
        if(sensitiveBean.getColumnSensitivelist() == null) {
//            System.out.println("---该库未设置脱敏或配置错误---");
            return list;
        }
//        System.out.println("---开始脱敏操作---");
        List<String> tableNameList = sensitiveBean.getTableNames();
//        System.out.println("tableNameList:"+tableNameList);
//        Map<String,String> tableAliastoTrue = sensitiveBean.getTableAliastoTrue();
//        System.out.println("tableAliastoTrue"+tableAliastoTrue);
        Map<String,String> columnAliastoTrue = sensitiveBean.getColumnAliastoTrue();
//        System.out.println("columnAliastoTrue"+columnAliastoTrue);
        //逐层展开list，替换对应元素，最外层是所有相关的脱敏配置
        for (Map<String, Map<String, DataLabelEntity>> columnSensitive : sensitiveBean.getColumnSensitivelist()) {
//            System.out.println("columnSensitive:"+columnSensitive);
            //从本层开始，第一层拿各个表所对应的敏感标签，第二层取整个list的每一行替换，第三层是查每一列中是否有相应的脱敏对象
            for (String tableName : tableNameList) {
                //每一张表的脱敏配置columnMap
                Map<String, DataLabelEntity> columnMap = columnSensitive.get(tableName);
//                String tableTrueName = tableAliastoTrue.get(tableName);
//                System.out.println("tableTrueName:"+tableTrueName+" tableName:"+tableName);
//                System.out.println("columnMap:"+columnMap);
                //注意，得先判断colunmap中是否为null，再判断是否为空，然后再判断是否包含这个key值，需要替换
                if (columnMap != null && !columnMap.isEmpty()) {
                    for (int j = 0; j < list.size(); j++) {
                        //第j行的元素存于map中
                        Map<String, Object> map = list.get(j);
//                        System.out.println("map:"+map);
                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                            String columnName = entry.getKey();
                            String columnTrueName = columnName;
                            if(columnAliastoTrue != null && !columnAliastoTrue.isEmpty()) {
                                columnTrueName = columnAliastoTrue.get(columnName);
                            }
//                            System.out.println("columnTrueName:"+columnTrueName+" columnName:"+columnName);
                            if (columnMap.containsKey(columnTrueName) && columnMap.get(columnTrueName) != null) {
                                if (columnMap.get(columnTrueName).getModify() == 1) {
                                    String reg = columnMap.get(columnTrueName).getLabelReg();
                                    String params = columnMap.get(columnTrueName).getParam();
                                    if (entry.getValue() != null) {
                                        String str = entry.getValue().toString();
//                                System.out.println("reg:"+reg+" params:"+params+" str:"+str);
                                        String temp = desensitizationCore(str, reg, params);
                                        map.replace(entry.getKey(), temp);
                                    }
//                                System.out.println("替换元素："+entry.getKey()+"   "+entry.getValue());
                                } else {
                                    String labelName = columnMap.get(columnTrueName).getLabelName();
                                    String temp = "";
                                    if (entry.getValue() != null) {
                                        String str = entry.getValue().toString();
                                        System.out.println("str: " + str);
                                        System.out.println("执行：" + labelName);
                                        switch (labelName) {
                                            case "随机化算法" :
                                                temp = DesensitizationUtil.randomize(str);
                                                break;
                                            case "AES对称加密算法" :
                                                temp = DesensitizationUtil.encrypt(str);
                                                break;
                                            case "SHA-256算法" :
                                                temp = DesensitizationUtil.hash256(str);
                                                break;
                                            case "字符串剪切算法" :
                                                temp = DesensitizationUtil.cut(str);
                                                break;
                                            case "字符串填充算法" :
                                                temp = DesensitizationUtil.pad(str);
                                                break;
                                            case "字符串随机打乱算法" :
                                                temp = DesensitizationUtil.shuffle(str);
                                                break;
                                            case "字符串随机添加算法" :
                                                temp = DesensitizationUtil.add(str);
                                                break;
                                            case "转十六进制编码算法" :
                                                temp = DesensitizationUtil.toHex(str);
                                                break;
                                            case "Base64编码算法" :
                                                temp = DesensitizationUtil.toBase64(str);
                                                break;
                                            case "身份证号64脱敏" :
                                                temp = DesensitizationUtil.maskIDCardNumber(str);
                                                break;
                                            case "银行卡号前四脱敏" :
                                                temp = DesensitizationUtil.maskBankCardNumber(str);
                                                break;
                                            case "手机号乱序" :
                                                temp = DesensitizationUtil.scramblePhoneNumber(str);
                                                break;
                                            case "姓名脱敏" :
                                                temp = DesensitizationUtil.maskNameOnlySurname(str);
                                                break;
                                            case "银行卡号后四脱敏" :
                                                temp = DesensitizationUtil.maskBankCardNumberKeepLastFour(str);
                                                break;
                                            case "虚拟姓名" :
                                                temp = DesensitizationUtil.generateVirtualName();
                                                break;
                                            case "邮箱前缀脱敏" :
                                                temp = DesensitizationUtil.maskEmail(str);
                                                break;
                                            case "地址脱敏" :
                                                temp = DesensitizationUtil.maskAddress(str);
                                                break;
                                            case "密码脱敏" :
                                                temp = DesensitizationUtil.maskPassword(str);
                                                break;
                                            case "IP地址脱敏" :
                                                temp = DesensitizationUtil.maskIPAddress(str);
                                                break;
                                            case "日期脱敏" :
                                                temp = DesensitizationUtil.maskDate(str);
                                                break;
                                            case "职位脱敏" :
                                                temp = DesensitizationUtil.maskJobTitle(str);
                                                break;
                                            case "URL脱敏" :
                                                temp = DesensitizationUtil.maskUrl(str);
                                                break;
                                            case "姓名拼音脱敏" :
                                                temp = DesensitizationUtil.maskPinyinName(str);
                                                break;
                                            case "驾驶证号脱敏" :
                                                temp = DesensitizationUtil.maskDriverLicenseNumber(str);
                                                break;
                                            case "健康卡号脱敏" :
                                                temp = DesensitizationUtil.maskHealthCardNumber(str);
                                                break;
                                            case "年龄范围脱敏" :
                                                temp = DesensitizationUtil.maskAgeRange(str);
                                                break;
                                            case "时间偏移脱敏" :
                                                temp = DesensitizationUtil.maskDateTime(str);
                                                break;
                                            case "缩短字符串" :
                                                temp = DesensitizationUtil.shortenString(str);
                                                break;
                                            case "异或加密" :
                                                temp = DesensitizationUtil.xorEncrypt(str);
                                                break;
                                            case "文件路径脱敏" :
                                                temp = DesensitizationUtil.hidePath(str);
                                                break;
                                            case "隐藏用户名" :
                                                temp = DesensitizationUtil.hideUsername(str);
                                                break;
                                            case "隐藏域名" :
                                                temp = DesensitizationUtil.hideDomain(str);
                                                break;
                                            case "随机增减空格" :
                                                temp = DesensitizationUtil.addRandomWhitespace(str);
                                                break;
                                            case "位置扰动" :
                                                temp = DesensitizationUtil.distortCoordinates(str);
                                                break;
                                            case "DSA" :
                                                temp = DesensitizationUtil.signAndConvertToString(str);
                                                break;
                                            case "RSA" :
                                                temp = DesensitizationUtil.encryptRSA(str);
                                                break;
                                        }
                                        System.out.println("temp: " + temp);
                                        map.replace(entry.getKey(), temp);
                                    }
                                }
                            }
                        }
                        list.set(j, map);
                    }
                }
            }
        }
//        System.out.println(desensitizationCore("一二三四五六","([\\u4e00-\\u9fa5]{1})[\\u4e00-\\u9fa5]*","$1***"));
//        System.out.println("---脱敏操作结束---");
        return list;
    }

    //根据对应表，对应列的脱敏配置，替换list中对应内容，并返回
    public List<Map<String, Object>> dynamicDesensitizationForHbase(List<Map<String, Object>> resultList ,SensitiveBean sensitiveBean) throws Exception {
        if (sensitiveBean.getColumnSensitivelist() == null) {
//            System.out.println("---该库未设置脱敏或配置错误---");
            return resultList;
        }
//        System.out.println("---开始脱敏操作---");
        List<String> tableNameList = sensitiveBean.getTableNames();
//        System.out.println("tableNameList:" + tableNameList);
//        Map<String,String> tableAliastoTrue = sensitiveBean.getTableAliastoTrue();
//        System.out.println("tableAliastoTrue"+tableAliastoTrue);
        Map<String, String> columnAliastoTrue = sensitiveBean.getColumnAliastoTrue();
//        System.out.println("columnAliastoTrue" + columnAliastoTrue);
        //逐层展开list，替换对应元素，最外层是所有相关的脱敏配置
        for (Map<String, Map<String, DataLabelEntity>> columnSensitive : sensitiveBean.getColumnSensitivelist()) {
//            System.out.println("columnSensitive:" + columnSensitive);
            for (String tableName : tableNameList) {
                //每一张表的脱敏配置columnMap
                Map<String, DataLabelEntity> columnMap = columnSensitive.get(tableName);
                //注意，得先判断colunmap中是否为null，再判断是否为空，然后再判断是否包含这个key值，需要替换
                if (columnMap != null && !columnMap.isEmpty()) {
                    for (Map<String, Object> result : resultList) {
                        if (columnMap.containsKey(result.get("COLUMN"))) {
//                            System.out.println(result.get("CELL"));
                            String str = result.get("CELL").toString();
                            String[] str1 = str.split(",");
//                            System.out.println(str1[1]);
                            String[] str2 = str1[1].split("=");
//                            System.out.println(str2[1]);
                            if (columnMap.get(result.get("COLUMN")).getModify() == 1) {
                                String reg = columnMap.get(result.get("COLUMN")).getLabelReg();
                                String params = columnMap.get(result.get("COLUMN")).getParam();
                                if (str2[1] != null) {
                                    String temp = desensitizationCore(str2[1], reg, params);
                                    result.replace("CELL", str1[0] + "," + str2[0] + "=" + temp);
                                }
                            } else {
                                String labelName = columnMap.get(result.get("COLUMN")).getLabelName();
                                String temp = "";
                                if (str2[1] != null) {
                                    String s = str2[1];
                                    System.out.println("str: " + s);
                                    System.out.println("执行：" + labelName);
                                    switch (labelName) {
                                        case "随机化算法" :
                                            temp = DesensitizationUtil.randomize(str);
                                            break;
                                        case "AES对称加密算法" :
                                            temp = DesensitizationUtil.encrypt(str);
                                            break;
                                        case "SHA-256算法" :
                                            temp = DesensitizationUtil.hash256(str);
                                            break;
                                        case "字符串剪切算法" :
                                            temp = DesensitizationUtil.cut(str);
                                            break;
                                        case "字符串填充算法" :
                                            temp = DesensitizationUtil.pad(str);
                                            break;
                                        case "字符串随机打乱算法" :
                                            temp = DesensitizationUtil.shuffle(str);
                                            break;
                                        case "字符串随机添加算法" :
                                            temp = DesensitizationUtil.add(str);
                                            break;
                                        case "转十六进制编码算法" :
                                            temp = DesensitizationUtil.toHex(str);
                                            break;
                                        case "Base64编码算法" :
                                            temp = DesensitizationUtil.toBase64(str);
                                            break;
                                        case "身份证号64脱敏" :
                                            temp = DesensitizationUtil.maskIDCardNumber(str);
                                            break;
                                        case "银行卡号前四脱敏" :
                                            temp = DesensitizationUtil.maskBankCardNumber(str);
                                            break;
                                        case "手机号乱序" :
                                            temp = DesensitizationUtil.scramblePhoneNumber(str);
                                            break;
                                        case "姓名脱敏" :
                                            temp = DesensitizationUtil.maskNameOnlySurname(str);
                                            break;
                                        case "银行卡号后四脱敏" :
                                            temp = DesensitizationUtil.maskBankCardNumberKeepLastFour(str);
                                            break;
                                        case "虚拟姓名" :
                                            temp = DesensitizationUtil.generateVirtualName();
                                            break;
                                        case "邮箱前缀脱敏" :
                                            temp = DesensitizationUtil.maskEmail(str);
                                            break;
                                        case "地址脱敏" :
                                            temp = DesensitizationUtil.maskAddress(str);
                                            break;
                                        case "密码脱敏" :
                                            temp = DesensitizationUtil.maskPassword(str);
                                            break;
                                        case "IP地址脱敏" :
                                            temp = DesensitizationUtil.maskIPAddress(str);
                                            break;
                                        case "日期脱敏" :
                                            temp = DesensitizationUtil.maskDate(str);
                                            break;
                                        case "职位脱敏" :
                                            temp = DesensitizationUtil.maskJobTitle(str);
                                            break;
                                        case "URL脱敏" :
                                            temp = DesensitizationUtil.maskUrl(str);
                                            break;
                                        case "姓名拼音脱敏" :
                                            temp = DesensitizationUtil.maskPinyinName(str);
                                            break;
                                        case "驾驶证号脱敏" :
                                            temp = DesensitizationUtil.maskDriverLicenseNumber(str);
                                            break;
                                        case "健康卡号脱敏" :
                                            temp = DesensitizationUtil.maskHealthCardNumber(str);
                                            break;
                                        case "年龄范围脱敏" :
                                            temp = DesensitizationUtil.maskAgeRange(str);
                                            break;
                                        case "时间偏移脱敏" :
                                            temp = DesensitizationUtil.maskDateTime(str);
                                            break;
                                        case "缩短字符串" :
                                            temp = DesensitizationUtil.shortenString(str);
                                            break;
                                        case "异或加密" :
                                            temp = DesensitizationUtil.xorEncrypt(str);
                                            break;
                                        case "文件路径脱敏" :
                                            temp = DesensitizationUtil.hidePath(str);
                                            break;
                                        case "隐藏用户名" :
                                            temp = DesensitizationUtil.hideUsername(str);
                                            break;
                                        case "隐藏域名" :
                                            temp = DesensitizationUtil.hideDomain(str);
                                            break;
                                        case "随机增减空格" :
                                            temp = DesensitizationUtil.addRandomWhitespace(str);
                                            break;
                                        case "位置扰动" :
                                            temp = DesensitizationUtil.distortCoordinates(str);
                                            break;
                                        case "DSA" :
                                            temp = DesensitizationUtil.signAndConvertToString(str);
                                            break;
                                        case "RSA" :
                                            temp = DesensitizationUtil.encryptRSA(str);
                                            break;
                                    }
                                    System.out.println("temp: " + temp);
                                    result.replace("CELL", str1[0] + "," + str2[0] + "=" + temp);
                                }
                            }
                        }
                    }
                }
            }
//        System.out.println(desensitizationCore("一二三四五六","([\\u4e00-\\u9fa5]{1})[\\u4e00-\\u9fa5]*","$1***"));
        }
//        System.out.println("---脱敏操作结束---");
        return resultList;
    }
}
