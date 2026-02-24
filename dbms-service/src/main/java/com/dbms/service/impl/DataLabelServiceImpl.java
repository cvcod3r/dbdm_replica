package com.dbms.service.impl;

import com.dbms.bean.HBaseRiskBean;
import com.dbms.bean.RiskBean;
import com.dbms.bean.SensitiveBean;
import com.dbms.core.AjaxResult;
import com.dbms.core.dbase.DataBaseUtilService;
import com.dbms.core.dbase.HBaseShellMeta;
import com.dbms.core.dbase.IHBaseShellParser;
import com.dbms.core.dbase.IHBaseUtilService;
import com.dbms.dao.DataRuleDao;
import com.dbms.entity.DataLabelEntity;
import com.dbms.dao.DataLabelDao;
import com.dbms.entity.DataRuleEntity;
import com.dbms.entity.DbaseEntity;
import com.dbms.enums.SqlTypeEnum;
import com.dbms.repository.IDesensitizationService;
import com.dbms.repository.impl.DbopServiceImpl;
import com.dbms.service.DataLabelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dbms.utils.CryptoUtil;
import com.dbms.utils.DbInfoUtil;
import com.dbms.utils.ip.IpUtils;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dbms.utils.SecurityUtils.getGroupId;
import static com.dbms.utils.SecurityUtils.getUserId;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author <a href="https://www.fengwenyi.com?code">YSL</a>
 * @since 2023-02-05
 */
@Service
public class DataLabelServiceImpl extends ServiceImpl<DataLabelDao, DataLabelEntity> implements DataLabelService {

    @Autowired
    private DataLabelDao dataLabelDao;

    @Autowired
    private DataBaseUtilService dataBaseUtilService;

    @Autowired
    private IDesensitizationService desensitizationService;

    @Autowired
    IHBaseShellParser hbaseShellParser;

    @Autowired
    private IHBaseUtilService hbaseUtilService;


    private static final Logger logger = LoggerFactory.getLogger(DbopServiceImpl.class);

    public boolean deleteByIds(Integer[] groupIds) {
        if (groupIds.length==0){
            return false;
        }else{
            for (Integer groupId : groupIds) {
                DataLabelEntity dataLabelEntity = dataLabelDao.selectById(groupId);
                dataLabelEntity.setIsDelete(1);
                dataLabelDao.updateById(dataLabelEntity);
            }
        }
        return true;
    }

    public Map<String,Map<String, Object>> extractTableInfo(DbaseEntity dbaseEntity, String schemaName, Map<String, List<Map<String, Integer>>> tableInfo) throws Exception {
        // 总：提取每张表的数据，并进行脱敏
        // 1.先拼接select语句读取相应数据
        // 2.再进行脱敏
        // 3.获取建表语句以及列信息

        System.out.println("---------开始查询各表信息----------");
        String connKey = DbInfoUtil.getConnKey(dbaseEntity, getUserId(), schemaName);
        Map<String,Map<String, Object>> resultMap = new HashMap<>();
        try {
        // 分别提取表格内容并脱敏
            for(Map.Entry<String, List<Map<String, Integer>>> tableEntry : tableInfo.entrySet()){
                Map<String, Object> map = new HashMap<>();
                //结果集
                List<Map<String, Object>> list = null;
                //列信息集
                List<Map<String, Object>> columns = null;
                //表名
                String tableName = tableEntry.getKey();
                System.out.println("tableName: " + tableName);
                //表信息集
                List<Map<String, Integer>> tableInfoNow = tableEntry.getValue();
                System.out.println("tableInfoNow: " + tableInfoNow);
                //limit
                int limitLow = tableInfoNow.get(0).get("limit_low");
                int limitHigh = tableInfoNow.get(0).get("limit_high");
                //脱敏配置
                Map<String, Integer> desensitizationOption = tableInfoNow.get(1);
                System.out.println("desensitizationOption: " + desensitizationOption);
                String sql = "";
                //拼接sql
                if(dbaseEntity.getDbType().equals("DM") || dbaseEntity.getDbType().equals("KingBase") || dbaseEntity.getDbDriver().equals("com.gbase.jdbc.Driver") ) {
                    sql = String.format("select * from \"%s\".\"%s\" limit %d offset %d", schemaName, tableName, limitHigh - limitLow + 1, limitLow - 1);
                    System.out.println("执行SQL语句: " + sql);
                    map.put("sql", sql);
                } else if (dbaseEntity.getDbType().equals("Oscar")){
                    sql = String.format("select * from %s.%s limit %d offset %d", schemaName, tableName, limitHigh - limitLow + 1, limitLow - 1);
                    System.out.println("执行SQL语句: " + sql);
                    map.put("sql", sql);
                } else if (dbaseEntity.getDbDriver().equals("com.gbasedbt.jdbc.Driver")) {
                    sql = String.format("select * from %s:%s limit %d offset %d", schemaName, tableName, limitHigh - limitLow + 1, limitLow - 1);
                    System.out.println("执行SQL语句: " + sql);
                    map.put("sql", sql);
                } else if (dbaseEntity.getDbType().equals("Hive")) {
                    sql = String.format("select * from %s limit %d offset %d", tableName, limitHigh - limitLow + 1, limitLow - 1);
                    System.out.println("执行SQL语句: " + sql);
                    map.put("sql", sql);
                } else if (dbaseEntity.getDbType().equals("HBaseShell"))
                {
                    sql = String.format("scan '%s';",tableName);
                    System.out.println("执行SQL语句: " + sql);
                    map.put("sql", sql);
                }
                // 获取原始结果集
                list = dataBaseUtilService.queryForList(dbaseEntity, sql, connKey);
                System.out.println("list: " + list);
                // 脱敏
                SensitiveBean sensitiveBean = desensitizationService.getSensitiveOptionsForStatic(tableName, desensitizationOption);
                list = desensitizationService.dynamicDesensitization(list, sensitiveBean);
                System.out.println("list_D: " + list);
                // 获取结果集列信息
                if (dbaseEntity.getDbType().equals("Oracle")){
                    sql = "select * from (" + sql + ") where rownum = 1 ";
                }
                //3.获取建表语句以及列信息
                columns = dataBaseUtilService.executeSqlForColumns(dbaseEntity, sql, connKey);
                System.out.println(columns);
                // 除了DM，Hive数据库暂时都不支持拿建表语句
                if (dbaseEntity.getDbType().equals("DM")) {
                    sql = String.format("CALL SP_TABLEDEF(\'%s\',\'%s\')", schemaName , tableName);
//                    System.out.println("查询建表语句: " + sql);
                    List<Map<String, Object>> createTable = dataBaseUtilService.queryForList(dbaseEntity, sql, connKey);
//                    System.out.println("createTable: " + createTable.get(0).get("COLUMN_VALUE"));
                    map.put("create",createTable.get(0).get("COLUMN_VALUE"));
                } else if (dbaseEntity.getDbType().equals("Hive")) {
                    sql = String.format("show create table `%s`.`%s`", schemaName , tableName);
//                    System.out.println("查询建表语句: " + sql);
                    List<Map<String, Object>> createTable = dataBaseUtilService.queryForList(dbaseEntity, sql, connKey);
                    String createTableSql = "";
                    for (Map<String, Object> cte : createTable) {
//                        System.out.println(cte.get("createtab_stmt"));
                        if (cte.get("createtab_stmt").toString().contains("ROW FORMAT"))
                        {
                            break;
                        } else {
                            createTableSql += cte.get("createtab_stmt").toString();
                        }
                    }
//                    System.out.println("createTable: " + createTableSql);
                    map.put("create",createTableSql);
                } else if (dbaseEntity.getDbDriver().equals("com.gbase.jdbc.Driver")) {
                    sql = String.format("show create table \"%s\".\"%s\"", schemaName , tableName);
//                    System.out.println("查询建表语句: " + sql);
                    List<Map<String, Object>> createTable = dataBaseUtilService.queryForList(dbaseEntity, sql, connKey);
//                    System.out.println("createTable: " + createTable);
                    String createTableSql = createTable.get(0).get("Create Table").toString();
                    // gbase8a的建表语句需提取）前部分，丢弃后半部分，并且在前端处理时插入新模式名
                    String[] arrOfStr = createTableSql.split("ENGINE");
                    map.put("create",arrOfStr[0]);
                    arrOfStr[0] = arrOfStr[0].replace("\n","");
//                    System.out.println("createTable: " + arrOfStr[0]);
                } else if(dbaseEntity.getDbType().equals("HBaseShell")){
                    sql = String.format("dump '%s';",tableName);
//                    System.out.println("查询建表语句: " + sql);
                    List<Map<String, Object>> createTable = dataBaseUtilService.queryForList(dbaseEntity, sql, connKey);
//                    System.out.println("createTable: " + createTable);
                    map.put("create",null);
                }else if (dbaseEntity.getDbType().equals("Oscar")){
                    sql = String.format("select SYS_GET_TABLEDEF from v_sys_table where tablename='%s';", tableName);
//                    System.out.println("查询建表语句: " + sql);
                    List<Map<String, Object>> createTable = dataBaseUtilService.queryForList(dbaseEntity, sql, connKey);
//                    System.out.println("createTable: " + createTable);
//                    System.out.println("createTable: " + createTable.get(0).get("SYS_GET_TABLEDEF"));
                    map.put("create",createTable.get(0).get("SYS_GET_TABLEDEF"));
                } else {
                    map.put("create",null);
                }
                map.put("result", list);
                map.put("columns", columns);
                map.put("schemas", schemaName);
                resultMap.put(tableName, map);
            }
        } catch (Exception e) {
            System.out.println("sql error");
            logger.error(e.getMessage(), e);
        }
        //-----------------
//        String str1 = "root";
//        String str2 = "huanglaihao";
//        String str3 = "abc123johnabc";
//
//        System.out.println("随机化算法:");
//        System.out.println(desensitizationService.randomize(str1));
//        System.out.println(desensitizationService.randomize(str2));
//        System.out.println(desensitizationService.randomize(str3));
//        System.out.println("AES对称加密算法:");
//        System.out.println(desensitizationService.encrypt(str1));
//        System.out.println(desensitizationService.encrypt(str2));
//        System.out.println(desensitizationService.encrypt(str3));
//        System.out.println("SHA-256算法:");
//        System.out.println(desensitizationService.hash256(str1));
//        System.out.println(desensitizationService.hash256(str2));
//        System.out.println(desensitizationService.hash256(str3));
//        System.out.println("字符串剪切算法:");
//        System.out.println(desensitizationService.cut(str1));
//        System.out.println(desensitizationService.cut(str2));
//        System.out.println(desensitizationService.cut(str3));
//        System.out.println("字符串填充算法:");
//        System.out.println(desensitizationService.pad(str1));
//        System.out.println(desensitizationService.pad(str2));
//        System.out.println(desensitizationService.pad(str3));
//        System.out.println("字符串随机打乱算法:");
//        System.out.println(desensitizationService.shuffle(str1));
//        System.out.println(desensitizationService.shuffle(str2));
//        System.out.println(desensitizationService.shuffle(str3));
//        System.out.println("字符串随机添加算法:");
//        System.out.println(desensitizationService.add(str1));
//        System.out.println(desensitizationService.add(str2));
//        System.out.println(desensitizationService.add(str3));
//        System.out.println("字符串转十六进制编码算法:");
//        System.out.println(desensitizationService.toHex(str1));
//        System.out.println(desensitizationService.toHex(str2));
//        System.out.println(desensitizationService.toHex(str3));
//        System.out.println("Base64编码算法:");
//        System.out.println(desensitizationService.toBase64(str1));
//        System.out.println(desensitizationService.toBase64(str2));
//        System.out.println(desensitizationService.toBase64(str3));
        //-----------------
        return resultMap;
    }

    public Map<String,Map<String, Object>> extractTableInfoForHBase(DbaseEntity dbaseEntity, String schemaName, Map<String, List<Map<String, Integer>>> tableInfo) {
        Map<String,Map<String, Object>> resultMap = new HashMap<>();
        // 获取连接key
        String connKey = DbInfoUtil.getConnKey(dbaseEntity, getUserId());
        // 获取操作类型
        HBaseShellMeta shellMeta;
        try {
            for(Map.Entry<String, List<Map<String, Integer>>> tableEntry : tableInfo.entrySet()) {
                // 每张表的结果集合
                Map<String, Object> map = new HashMap<>();
                List<Map<String, Object>> resultList = null;
                List<Map<String, Object>> columns = null;
                //表名
                String tableName = tableEntry.getKey();
                System.out.println("tableName: " + tableName);
                //表信息集
                List<Map<String, Integer>> tableInfoNow = tableEntry.getValue();
                System.out.println("tableInfoNow: " + tableInfoNow);
                //limit
                int limitLow = tableInfoNow.get(0).get("limit_low");
                int limitHigh = tableInfoNow.get(0).get("limit_high");
                //脱敏配置
                Map<String, Integer> desensitizationOption = tableInfoNow.get(1);
                System.out.println("desensitizationOption: " + desensitizationOption);
                String sql = "";
                //拼接sql
                if(dbaseEntity.getDbType().equals("HBaseShell")) {
                    sql = String.format("scan %s", tableName);
                    System.out.println("执行SQL语句: " + sql);
                    map.put("sql", sql);
                }
                //            通过sql语句获取shell命令
                shellMeta = hbaseShellParser.getHBaseShellMeta(sql);
                // 脱敏（Hbase的静态脱敏未完成）
                System.out.println("sql: " + sql);
                hbaseUtilService.getConnection(dbaseEntity,connKey);
                resultList = hbaseUtilService.scanData(sql, connKey);
                System.out.println("resultList: " + resultList);
//                System.out.println("————获取表格结构————");
                TableDescriptor sourceTableDescriptor = hbaseUtilService.getTableStructure(tableName,connKey);
                SensitiveBean sensitiveBeanScan = desensitizationService.getSensitiveOptionsForHbaseForStatic(shellMeta, desensitizationOption);
                resultList = desensitizationService.dynamicDesensitizationForHbase(resultList, sensitiveBeanScan);
                columns = hbaseUtilService.listSomeColumns(Arrays.asList("ROWKEY", "COLUMN", "CELL"));
                map.put("create", sourceTableDescriptor);
                map.put("result", resultList);
                map.put("columns", columns);
                map.put("schemas", schemaName);
                resultMap.put(tableName, map);
            }
        } catch (Exception e) {
            System.out.println("sql error");
            logger.error(e.getMessage(), e);
        }
        return resultMap;
    }
}


