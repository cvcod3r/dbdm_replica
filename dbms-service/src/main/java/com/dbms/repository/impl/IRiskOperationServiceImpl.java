package com.dbms.repository.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dbms.bean.HBaseRiskBean;
import com.dbms.bean.RiskBean;
import com.dbms.constant.CacheConstants;
import com.dbms.core.RedisCache;
import com.dbms.core.SQLParserUtil;
import com.dbms.core.dbase.HBaseShellMeta;
import com.dbms.core.dbase.impl.DruidSQLParserImpl;
import com.dbms.entity.AccessStrategyEntity;
import com.dbms.entity.DbaseEntity;
import com.dbms.entity.RiskOperationEntity;
import com.dbms.enums.SqlTypeEnum;
import com.dbms.repository.IRiskOperationService;
import net.sf.jsqlparser.JSQLParserException;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.dbms.dao.*;
import org.stringtemplate.v4.ST;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class IRiskOperationServiceImpl implements IRiskOperationService {


    @Autowired
    private RiskOperationDao riskOperationDao;

    @Autowired
    private AccessStrategyDao accessStrategyDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private DruidSQLParserImpl druidSQLParser;

    public List<RiskOperationEntity> getRiskOperations(Integer userId, Integer groupId, Integer dbId){
        List<RiskOperationEntity> riskOperationEntities = null;
        String redisKey = CacheConstants.RISK_OPER + userId + ":" + groupId + ":" + dbId;
        if (redisTemplate.hasKey(redisKey)){
            System.out.println("risk::Redis");
            riskOperationEntities = redisCache.getCacheObject(redisKey);
            return riskOperationEntities;
        }
        System.out.println("risk::Database");
        QueryWrapper<AccessStrategyEntity> qw = new QueryWrapper<>();
        qw.eq(AccessStrategyEntity.DB_ID, dbId)
                .and(wr -> wr.eq(AccessStrategyEntity.GROUP_ID, groupId)
                .or().eq(AccessStrategyEntity.USER_ID, userId))
                .eq(AccessStrategyEntity.STATUS, 0)
                .eq(AccessStrategyEntity.IS_DELETE, 0);
        List<AccessStrategyEntity> accessStrategyEntities = accessStrategyDao.selectList(qw);
        List<Integer> riskIds = new ArrayList<>();
        for (AccessStrategyEntity accessStrategyEntity:accessStrategyEntities){
            Integer riskId = accessStrategyEntity.getRiskId();
//            System.out.println("122222"+riskId);
            riskIds.add(riskId);
        }
        if (!riskIds.isEmpty()){
            QueryWrapper<RiskOperationEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.in(RiskOperationEntity.RISK_ID, riskIds);
            queryWrapper.eq(RiskOperationEntity.LABEL_STATUS, 0);
            queryWrapper.eq(RiskOperationEntity.IS_DELETE, 0);
            queryWrapper.eq(RiskOperationEntity.STATUS, 0);
            queryWrapper.in(RiskOperationEntity.ACTION_TYPE, ("A"), ("B"));
            riskOperationEntities = riskOperationDao.selectList(queryWrapper);
//            System.out.println("**********"+ riskOperationEntities.size());
        }
        if (riskOperationEntities!= null && riskOperationEntities.size()!=0){
            // 存入redis并设置过期时间
            redisCache.setCacheObject(redisKey, riskOperationEntities, 60*10, TimeUnit.SECONDS);
        }
        return riskOperationEntities;
    }

    /**
     * 验证权限，如验证权限通过返回true,可以执行SQL
     * @param sql
     * @param userId
     * @param groupId
     * @param schemaName
     * @param dbaseEntity
     * @return 返回riskBean 验证结果，动作类型，操作类型，模式集合，表集合，表字段map
     * @throws JSQLParserException
     */
    @Override
    public RiskBean checkRiskOperation(String sql, Integer userId, Integer groupId, String schemaName, DbaseEntity dbaseEntity) throws Exception {

        Integer dbId = dbaseEntity.getDbId();
        String dbType = dbaseEntity.getDbType();
//        System.out.println("-------验证权限--------");
        if (sql.contains("[")||sql.contains("]")){
            sql = sql.replace("[","");
            sql = sql.replace("]","");
        }
//        System.out.println("sql:" + sql+"userId:" + userId +"groupId:"+ groupId +"dbId:" +dbId + "schemaName" +schemaName );
//        SqlTypeEnum 是sql 类型 如update

        SqlTypeEnum sqlTypeEnum;
        Set<String> schemaSet = null;
        Map<String, String> tableMap = null;
        Map<String, ArrayList<String>> tableColumnMap = null;
        if(dbType.equals("Hive")){
            sqlTypeEnum = druidSQLParser.getSqlType(sql);
            schemaSet = druidSQLParser.getSchemas(sql, sqlTypeEnum);
            tableColumnMap = druidSQLParser.getTableColumnList(sql, sqlTypeEnum);
            tableMap = druidSQLParser.getTableMap(sql, sqlTypeEnum);
        } else {
            sqlTypeEnum = SQLParserUtil.getSqlType(sql);
            schemaSet = SQLParserUtil.getSchemas(sql, sqlTypeEnum);
            tableColumnMap = SQLParserUtil.getTableColumnList(sql, sqlTypeEnum);
            tableMap = SQLParserUtil.getTableMap(sql, sqlTypeEnum);
        }
        String sqlType = sqlTypeEnum.getName();
        List<RiskOperationEntity> riskOperationEntities = getRiskOperations(userId, groupId, dbId);
//        System.out.println(JSON.toJSONString(riskOperationEntities));
//        System.out.println("sqlType::" + sqlType);
        // SQL解析，获取数据库模式集合
//        Set<String>
//        System.out.println("schemaSet:"+schemaSet);
//        // 获取数据库表集合
//        Map<String, String>
//        System.out.println("tableMap::"+JSON.toJSONString(tableMap));
//        // 获取数据库表和字段集合
//        Map<String, ArrayList<String>>
//        System.out.println("tableColumnMap::"+JSON.toJSONString(tableColumnMap));
        // 数据库模式集合转为字符串
        String schemas = JSON.toJSONString(schemaSet);
        // 遍历tableMap tabelMap 是你查询的表名
        Set<String> tableSet = new HashSet<>();
        if (tableMap!=null&&!tableMap.isEmpty()){
            for (String value : tableMap.values()){
//                System.out.println(value);
                tableSet.add(value);
            }
        }
        // 表集合转换为字符串  schemaSet是库名
        String tables = JSON.toJSONString(tableSet);
        // 表、字段map转换为字符串 tableSet是表名
        String tableColumns = JSON.toJSONString(tableColumnMap);
//        System.out.println("tableColumns"+tableColumns);
        if (riskOperationEntities==null||riskOperationEntities.isEmpty()){
            return new RiskBean(true, null, sqlType, schemas,tables,tableColumns);
        }
        // 库权限、表权限以及列权限验证标记
//        String currentSchemaName = null;
//        boolean schemaFlag = true;
//        boolean tableFlag = true;
//        boolean columnFlag = true;
        if (schemaSet == null || schemaSet.isEmpty()){
            schemaSet = new HashSet<>();
            schemaSet.add(schemaName);
        }
        // 校验库权限
        for (String schema : schemaSet){
            for (RiskOperationEntity riskOperation:riskOperationEntities){
//                System.out.println("riskOperation"+riskOperation);
                String currSchema = riskOperation.getSchemaName();
                // 是否命中模式名 currSchema是库名
                if(schema.toLowerCase().equals(currSchema.toLowerCase())||schema.toUpperCase().equals(currSchema.toUpperCase())){
                    riskOperation.stringToObject();
                    List<String> schemaPrivileges = riskOperation.getSchemaPrivilegeList();
                    // 如果命中模式权限直接返回
                    if (schemaPrivileges!=null&&!schemaPrivileges.isEmpty()){
                        // 如果权限列表包含当前SQL操作类型
                        if (schemaPrivileges.contains(sqlType)||schemaPrivileges.contains("ALL")){
//                            System.out.println("schemaPrivileges:"+schemaPrivileges);
//                            System.out.println("-------命中库权限--------");
                            return new RiskBean(false, riskOperation.getActionType(), sqlType, schemas,tables,tableColumns);
                        }
                    }
                    // SQL 语句中未解析到表名，跳出当前循环
                    if (tableMap == null||tableMap.isEmpty()) continue;
                    // 校验表权限
                    List<String> tableNameList = riskOperation.getTableNameList();
                    Map<String, List<String>> tablePrivilegeMap = riskOperation.getTablePrivilegeMap();
                    Map<String, Map<String, List<String>>> columnPrivileges = riskOperation.getColumnPrivilegeMap();
                    // 遍历表名
                    for (String tableAlias:tableMap.keySet()){
//                        table是高危操作列表中的表  tableName是该sql语句中执行的表
                        String tableName = tableMap.get(tableAlias);
                        for (String currTable:tableNameList){
//                            System.out.println("tableName"+tableName);
//                            System.out.println("currTable"+currTable);
                            // 判断当前表名是否命中
                            if (tableName.toLowerCase().equals(currTable.toLowerCase())||tableName.toUpperCase().equals(currTable.toUpperCase())){
                                List<String> currTablePrivileges = null;
                                if (tablePrivilegeMap != null && !tablePrivilegeMap.isEmpty()){
                                    currTablePrivileges = tablePrivilegeMap.getOrDefault(currTable, null);
//                                    System.out.println("currTablePrivileges:"+currTablePrivileges);
                                }
                                if(currTablePrivileges != null && !currTablePrivileges.isEmpty()){
                                    // 如果权限列表包含当前SQL操作类型
                                    if (currTablePrivileges.contains(sqlType)||currTablePrivileges.contains("ALL")){
//                                        System.out.println("-------命中表权限--------");
                                        return new RiskBean(false, riskOperation.getActionType(), sqlType, schemas,tables,tableColumns);
                                    }
                                }
                                List<String> currTableColumns = tableColumnMap.getOrDefault(currTable, null);
                                if (currTableColumns != null && !currTableColumns.isEmpty()){
                                    Map<String, List<String>> columnPriMap = columnPrivileges.getOrDefault(currTable, null);
                                    // 如果列权限
                                    if (columnPriMap == null || columnPriMap.isEmpty()){
                                        continue;
                                    }
                                    for (String column:currTableColumns){
                                        List<String> currColumnPri = columnPriMap.getOrDefault(column, null);
                                        if (currColumnPri!=null&&!currColumnPri.isEmpty()){
                                            if (currColumnPri.contains(sqlType) || currColumnPri.contains("ALL")){
                                                System.out.println("-------命中列权限--------");
                                                return new RiskBean(false, riskOperation.getActionType(), sqlType, schemas,tables,tableColumns);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
//        System.out.println("-------验证权限结束--------");
        return new RiskBean(true, null, sqlType, schemas,tables,tableColumns);
    }


    public HBaseRiskBean checkHBaseShellRiskOperation(HBaseShellMeta shellMeta,String sqlType, Integer userId, Integer groupId, Integer dbId){
//        System.out.println("-------验证权限--------");
//        System.out.println(JSON.toJSONString(shellMeta));
//        System.out.println("userId"+userId+"groupId"+groupId+"dbId"+dbId);
//        获取表名
        if (shellMeta ==null){
            return new HBaseRiskBean(true,null,sqlType,null,null);
        }
        Map<String,String> tableMap=shellMeta.getTableName();
//        System.out.println("sqlTypeEnum" + sqlTypeEnum);
        String sqlTypeEnums = shellMeta.getSqlType().getName();
//        System.out.println("sqlTypeEnums" + sqlTypeEnums);
        Set<String> tableSet=new HashSet<>();
        // 获取数据库表和字段集合
        HashMap<String, ArrayList<Map<String, String>>> tableColumnMap =shellMeta.getColumnFamily();;
//        System.out.println(tableColumnMap);
        String tableColumns = null;
        String tables = null;
        Map<String,ArrayList<String>> tablecolumn  =new HashMap<>();
        if (tableColumnMap != null){
            for(String key:tableColumnMap.keySet()){
                ArrayList<Map<String,String>> aList =tableColumnMap.get(key);
                ArrayList<String> collist =new ArrayList<>();
                for (Map<String,String> maps : aList){
                    for(String mapkey :maps.keySet()){
                        collist.add(String.valueOf(maps.keySet()));
                    }
                }
                tablecolumn.put(key,collist);
            }
            tableColumns = JSON.toJSONString(tablecolumn);
            if(tableColumns.contains("=")){
                tableColumns=tableColumns.replace("=","");
            }
            if(tableMap!=null || !tableMap.isEmpty()){
                for (String value :tableMap.values()){
                    tableSet.add(value);
                }
            }
            tables = JSON.toJSONString(tableSet);
        }
//        System.out.println("userId"+userId+"groupId"+groupId+"dbId"+dbId);
//        检验是否有高危操作权限
        List<RiskOperationEntity> riskOperationEntities=getRiskOperations(userId,groupId,dbId);
//        System.out.println("riskOperationEntities"+JSON.toJSONString(riskOperationEntities));
        if (riskOperationEntities==null || riskOperationEntities.isEmpty()){
            return  new HBaseRiskBean(true,null,sqlTypeEnums,tables,tableColumns);
        }
//        如果有高危操作 则验证表权限和列权限
        for (RiskOperationEntity riskOperationEntity:riskOperationEntities){
            System.out.println("riskid"+riskOperationEntity.getRiskId());
            riskOperationEntity.stringToObject();
            List<String> tableNameList = riskOperationEntity.getTableNameList();
//            获取表权限
            Map<String, List<String>> tablePrivilegeMap = riskOperationEntity.getTablePrivilegeMap();
//            获取列权限
            Map<String, Map<String, List<String>>> columnPrivileges = riskOperationEntity.getColumnPrivilegeMap();
//            System.out.println("tableMap:"+tableMap);
            for (String tablealias:tableMap.keySet()){
//                System.out.println("tablealias:"+tablealias);
                String tableName =tableMap.get(tablealias);
                tableName=tableName.toLowerCase();
//                System.out.println("tableName"+tableName);
                if (shellMeta.getColumnFamily() == null){
                    for (String currTable:tableNameList){
//                    System.out.println("currTable"+currTable);
                        if(tableName.equals(currTable)){
                            List<String> currTablePrivileges=new ArrayList<>();
                            if(tablePrivilegeMap!=null && !tablePrivilegeMap.isEmpty()){
                                currTablePrivileges = tablePrivilegeMap.getOrDefault(currTable,null);
                            }
                            if(currTablePrivileges !=null && !currTablePrivileges.isEmpty()){
                                if(currTablePrivileges.contains(sqlTypeEnums)){
//                                System.out.println("----------命中表权限----------");
                                    return new HBaseRiskBean(false,riskOperationEntity.getActionType(),sqlTypeEnums,tables,tableColumns);
                                }
                            }
                            List<String> currTableColumns = tablecolumn.getOrDefault(currTable, null);
                            if (currTableColumns !=null && !currTableColumns.isEmpty()){
                                Map<String,List<String>> columnPriMap =columnPrivileges.getOrDefault(currTable,null);
                                if(columnPriMap==null || columnPriMap.isEmpty()){
                                    continue;
                                }
                                for (String columne:currTableColumns){
                                    List<String> currColumnPri =columnPriMap.getOrDefault(columne,null);
                                    if (currColumnPri!=null || !currColumnPri.isEmpty()){
                                        if(currColumnPri.contains(sqlTypeEnums)){
//                                        System.out.println("----------命中列权限----------");
                                            return  new HBaseRiskBean(false,riskOperationEntity.getActionType(),sqlTypeEnums,tables,tableColumns);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return new HBaseRiskBean(true,riskOperationEntity.getActionType(),sqlTypeEnums,tables,tableColumns);
                }else {
                    for (String currTable:tableNameList){
//                    System.out.println("currTable"+currTable);
                        if(tableName.equals(currTable)){
                            List<String> currTablePrivileges=new ArrayList<>();
                            if(tablePrivilegeMap!=null && !tablePrivilegeMap.isEmpty()){
                                currTablePrivileges = tablePrivilegeMap.getOrDefault(currTable,null);
                            }
//                        System.out.println("currTablePrivileges"+currTablePrivileges);
//                        System.out.println("sqlTypeEnums"+sqlTypeEnums);
                            if(currTablePrivileges !=null && !currTablePrivileges.isEmpty()){
//                            System.out.println("2222222222222");
                                if(currTablePrivileges.contains(sqlTypeEnums)){
//                                System.out.println("----------命中表权限----------");
                                    return new HBaseRiskBean(false,riskOperationEntity.getActionType(),sqlTypeEnums,tables,tableColumns);
                                }
                            }
                            List<String> currTableColumns = tablecolumn.getOrDefault(currTable, null);
                            if (currTableColumns !=null && !currTableColumns.isEmpty()){
                                Map<String,List<String>> columnPriMap =columnPrivileges.getOrDefault(currTable,null);
                                if(columnPriMap==null || columnPriMap.isEmpty()){
                                    continue;
                                }
                                for (String columne:currTableColumns){
                                    List<String> currColumnPri =columnPriMap.getOrDefault(columne,null);
                                    if (currColumnPri!=null || !currColumnPri.isEmpty()){
                                        if(currColumnPri.contains(sqlTypeEnums)){
//                                        System.out.println("----------命中列权限----------");
                                            return  new HBaseRiskBean(false,riskOperationEntity.getActionType(),sqlTypeEnums,tables,tableColumns);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
//        System.out.println("-------验证权限结束--------");
        return new HBaseRiskBean(true, null,sqlTypeEnums, tables,tableColumns);
    }


    public List<RiskOperationEntity> getTableRowLimits(Integer userId, Integer groupId, Integer dbId){
        List<RiskOperationEntity> riskOperationEntities = null;
        String redisKey = CacheConstants.RISK_TABLE_ROW_LIMIT + userId + ":" + groupId + ":" + dbId;
        if (redisTemplate.hasKey(redisKey)){
            System.out.println("risk_table_row_limits::Redis");
            riskOperationEntities = redisCache.getCacheObject(redisKey);
            return riskOperationEntities;
        }
        System.out.println("risk::Database");
        QueryWrapper<AccessStrategyEntity> qw = new QueryWrapper<>();
        qw.eq(AccessStrategyEntity.DB_ID, dbId)
                .and(wr -> wr.eq(AccessStrategyEntity.GROUP_ID, groupId)
                        .or().eq(AccessStrategyEntity.USER_ID, userId))
                .eq(AccessStrategyEntity.STATUS, 0)
                .eq(AccessStrategyEntity.IS_DELETE, 0);
        List<AccessStrategyEntity> accessStrategyEntities = accessStrategyDao.selectList(qw);
        List<Integer> riskIds = new ArrayList<>();
        for (AccessStrategyEntity accessStrategyEntity:accessStrategyEntities){
            Integer riskId = accessStrategyEntity.getRiskId();
//            System.out.println("122222"+riskId);
            riskIds.add(riskId);
        }
        if (!riskIds.isEmpty()){
            QueryWrapper<RiskOperationEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.in(RiskOperationEntity.RISK_ID, riskIds);
            queryWrapper.eq(RiskOperationEntity.LABEL_STATUS, 0);
            queryWrapper.eq(RiskOperationEntity.IS_DELETE, 0);
            queryWrapper.eq(RiskOperationEntity.STATUS, 0);
            queryWrapper.in(RiskOperationEntity.ACTION_TYPE, ("D"));
            riskOperationEntities = riskOperationDao.selectList(queryWrapper);
//            System.out.println("**********"+ riskOperationEntities.size());
        }
        if (riskOperationEntities!= null && riskOperationEntities.size()!=0){
            // 存入redis并设置过期时间
            redisCache.setCacheObject(redisKey, riskOperationEntities, 60*10, TimeUnit.SECONDS);
        }
        return riskOperationEntities;
    }

    @Override
    public Integer checkTableRowLimits(Integer userId, Integer groupId, String schemaName, String tables, DbaseEntity dbaseEntity) {
        List<RiskOperationEntity> riskOperationEntities = getTableRowLimits(userId, groupId, dbaseEntity.getDbId());
        System.out.println("schema:" + schemaName);
//        System.out.println("rowLimits: " + JSON.toJSONString(riskOperationEntities));
        if (riskOperationEntities == null || riskOperationEntities.isEmpty()){
            return -1;
        }
        List<String> tableList = JSON.parseArray(tables, String.class);
//        System.out.println("tableList"+tableList.toString());
        // Integer的最大值;
        Integer rowCountLimit = Integer.MAX_VALUE;
        for (RiskOperationEntity riskOperationEntity:riskOperationEntities){
            if (!riskOperationEntity.getSchemaName().equals(schemaName)){
                continue;
            }
            String tableRowLimit = riskOperationEntity.getTableRowLimit();
            Map<String, Integer> tableRowLimitMap = JSON.parseObject(tableRowLimit, new TypeReference<Map<String, Integer>>() {});
            for (String tableName:tableList){
                Integer currRowLimit = tableRowLimitMap.getOrDefault(tableName, Integer.MAX_VALUE);
                if (currRowLimit < rowCountLimit){
                    rowCountLimit = currRowLimit;
                }
            }
        }
        if (rowCountLimit == Integer.MAX_VALUE){
            return -1;
        }
        return rowCountLimit;
    }
}
