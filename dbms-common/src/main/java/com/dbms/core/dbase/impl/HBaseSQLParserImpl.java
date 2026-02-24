package com.dbms.core.dbase.impl;


import com.dbms.core.dbase.ISQLParser;
import com.dbms.enums.SqlTypeEnum;
import com.dbms.utils.StringUtils;
import com.github.CCweixiao.hbase.sdk.AbstractHBaseSqlTemplate;
import com.github.CCweixiao.hbase.sdk.common.lang.MyAssert;
import com.github.CCweixiao.hbase.sdk.common.model.row.HBaseDataRow;
import com.github.CCweixiao.hbase.sdk.common.model.row.HBaseDataSet;
import com.github.CCweixiao.hbase.sdk.hql.HBaseSQLExtendContextUtil;
import com.github.CCwexiao.hbase.sdk.dsl.antlr.HBaseSQLParser;
import com.github.CCwexiao.hbase.sdk.dsl.client.QueryExtInfo;
import com.github.CCwexiao.hbase.sdk.dsl.client.rowkey.RowKey;
import com.github.CCwexiao.hbase.sdk.dsl.context.HBaseSqlContext;
import com.github.CCwexiao.hbase.sdk.dsl.manual.HBaseSqlAnalysisUtil;
import com.github.CCwexiao.hbase.sdk.dsl.manual.RowKeyRange;
import com.github.CCwexiao.hbase.sdk.dsl.model.HBaseColumn;
import com.github.CCwexiao.hbase.sdk.dsl.model.HBaseTableSchema;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.exceptions.HBaseException;
import org.apache.hadoop.hbase.filter.Filter;

import java.util.*;

public class HBaseSQLParserImpl extends AbstractHBaseSqlTemplate implements ISQLParser{


    public HBaseSQLParserImpl(Properties properties) {
        super(properties);
    }

    @Override
    public SqlTypeEnum getSqlType(String sql){
        if (StringUtils.startsWith(sql.toUpperCase(), "SCAN")){
            return SqlTypeEnum.SCAN;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "SELECT")){
            return SqlTypeEnum.SELECT;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "INSERT")){
            return SqlTypeEnum.PUT;
        } else if(StringUtils.startsWith(sql.toUpperCase(),  "GET")){
            return SqlTypeEnum.GET;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "DROP")){
            return SqlTypeEnum.DROP;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "DELETE")){
            return SqlTypeEnum.DELETE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "DELETEALL")){
            return SqlTypeEnum.DELETEALL;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "DESCRIBE")){
            return SqlTypeEnum.DESCRIBE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "LIST_NAMESPACE")){
            return SqlTypeEnum.LIST_NAMESPACE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "LIST_NAMESPACE_TABLES")){
            return SqlTypeEnum.LIST_NAMESPACE_TABLES;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "CREATE_NAMESPACE")){
            return SqlTypeEnum.CREATE_NAMESPACE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "DESCRIBE_NAMESPACE")){
            return SqlTypeEnum.DESCRIBE_NAMESPACE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "DROP_NAMESPACE")){
            return SqlTypeEnum.DROP_NAMESPACE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "CREATE")){
            return SqlTypeEnum.CREATE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "LIST")){
            return SqlTypeEnum.LIST;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "DROP_ALL")){
            return SqlTypeEnum.DROP_ALL;
        } else {
            return SqlTypeEnum.NONE;
        }
    }

    @Override
    public Set<String> getSchemas(String sql, SqlTypeEnum sqlTypeEnum) throws Exception {
        Set<String> schemas = new HashSet<>();
        switch (sqlTypeEnum){
            case SELECT:
                break;
            case INSERT:
                break;
            case SCAN:
                break;
            case PUT:
                break;
            case GET:
                break;
            case DELETE:
                break;
        }
        return null;
    }

    /**
     * 获取tables表名,
     * Map格式：<key:别名，value:表名>，如果无别名，便均为表名
     *
     * @param sql
     * @return
     */
    @Override
    public HashMap<String, String> getTableMap(String sql, SqlTypeEnum sqlTypeEnum) throws HBaseException {
        HashMap<String , String> tableMap = new HashMap<String , String >();
        HBaseSQLParser.ProgContext progContext = parseProgContext(sql);
        String tableName = parseTableNameFromHql(progContext);
        switch (sqlTypeEnum){
            case SELECT:
                HBaseSQLParser.SelecthqlcContext selectHqlContext = HBaseSqlAnalysisUtil.parseSelectHqlContext(progContext);
                MyAssert.checkNotNull(selectHqlContext);
//                String tableName = parseTableNameFromHql(progContext);
                tableMap.put(tableName , tableName);
                break;
            case PUT:
                HBaseSQLParser.InserthqlcContext insertHqlContext = HBaseSqlAnalysisUtil.parseInsertHqlContext(progContext);
                MyAssert.checkNotNull(insertHqlContext);
//                String tableName = parseTableNameFromHql(progContext);
                tableMap.put(tableName , tableName);
                break;
            case DELETE:
                HBaseSQLParser.DeletehqlcContext deleteHqlContext = HBaseSqlAnalysisUtil.parseDeleteHqlContext(progContext);
//                String tableName = parseTableNameFromHql(progContext);
                tableMap.put(tableName , tableName);
                break;
        }
        return tableMap;
    }

    /**
     * 获取tables的列名表名hash
     * Map格式：<key:表名，value:列名列表>
     *     列名列表: 其中每个字段列是一个map，3个key值
     *          fullName:全名, 如 user.user_name;
     *          isUseAs:是否使用了别名, 如user.user_name as name;
     *          columnName:真实列名,如user.user_name,真实列名为user_name;
     * @param sql
     * @param sqlTypeEnum
     * @return
     */
    @Override
    public HashMap<String, ArrayList<Map<String, String>>> getTableColumnMap(String sql, SqlTypeEnum sqlTypeEnum) throws HBaseException {
        HashMap<String , ArrayList<Map<String, String>>> tableColumnMap = new HashMap<String , ArrayList<Map<String, String>>>();
        ArrayList<Map<String, String>> columNames = new ArrayList<Map<String, String>>();
        HBaseSQLParser.ProgContext progContext = parseProgContext(sql);
        HashMap<String, String> curMap = getTableMap(sql, sqlTypeEnum);
        String tableName = parseTableNameFromHql(progContext);
        switch(sqlTypeEnum){
            case SELECT:
                HBaseSQLParser.SelecthqlcContext selectHqlContext = HBaseSqlAnalysisUtil.parseSelectHqlContext(progContext);
                MyAssert.checkNotNull(selectHqlContext);
                HBaseTableSchema selectTableSchema = HBaseSqlContext.getTableSchema(tableName);
                HBaseSQLParser.SelectColListContext selectColListContext = selectHqlContext.selectColList();
                final List<HBaseColumn> selectQueryColumnSchemaList = HBaseSqlAnalysisUtil.extractColumnSchemaList(selectTableSchema, selectColListContext);
                MyAssert.checkArgument(!selectQueryColumnSchemaList.isEmpty(), "The column list of query is not empty.");
                for(HBaseColumn hBaseColumn : selectQueryColumnSchemaList){
                    String columName = hBaseColumn.getColumnName();
                    String fullName = tableName + "." + columName;
                    columNames.add(new HashMap<String, String>(){{
                        put("fullName", fullName);
                        put("isUseAs", columName);
                        put("columnName", columName);
                    }});
                }
                tableColumnMap.put(tableName , columNames);
                break;

            case PUT:
                HBaseSQLParser.InserthqlcContext insertHqlContext = HBaseSqlAnalysisUtil.parseInsertHqlContext(progContext);
                MyAssert.checkNotNull(insertHqlContext);
                HBaseTableSchema insertTableSchema = HBaseSqlContext.getTableSchema(tableName);
                List<HBaseColumn> insertColumnSchemaList = HBaseSqlAnalysisUtil.extractColumnSchemaList(insertTableSchema, insertHqlContext.colList());
                final List<HBaseSQLParser.InsertValueContext> insertValueContextList = insertHqlContext.insertValueList().insertValue();
                MyAssert.checkArgument(insertColumnSchemaList.size() == insertValueContextList.size(),
                        "The inserted fields length should be same as the values length.");
                for(HBaseColumn hBaseColumn : insertColumnSchemaList){
                    String columName = hBaseColumn.getColumnName();
                    String fullName = tableName + "." + columName;
                    columNames.add(new HashMap<String, String>(){{
                        put("fullName", fullName);
                        put("isUseAs", columName);
                        put("columnName", columName);
                    }});
                }
                tableColumnMap.put(tableName , columNames);
                break;
            case DELETE:
                HBaseSQLParser.DeletehqlcContext deleteHqlContext = HBaseSqlAnalysisUtil.parseDeleteHqlContext(progContext);
                HBaseTableSchema deleteTableSchema = HBaseSqlContext.getTableSchema(tableName);
                //delete col list
                HBaseSQLParser.SelectColListContext deleteColListContext = deleteHqlContext.selectColList();
                List<HBaseColumn> deleteColumnSchemaList = HBaseSqlAnalysisUtil.extractColumnSchemaList(deleteTableSchema, deleteColListContext);
                MyAssert.checkArgument(!deleteColumnSchemaList.isEmpty(), "The column will to be deleted must not be empty.");
                for(HBaseColumn hBaseColumn : deleteColumnSchemaList){
                    String columName = hBaseColumn.getColumnName();
                    String fullName = tableName + "." + columName;
                    columNames.add(new HashMap<String, String>(){{
                        put("fullName", fullName);
                        put("isUseAs", columName);
                        put("columnName", columName);
                    }});
                }
                tableColumnMap.put(tableName , columNames);
                break;
        }

        return tableColumnMap;
    }

    @Override
    protected Scan constructScan(String s, RowKey<?> rowKey, RowKey<?> rowKey1, Filter filter, QueryExtInfo queryExtInfo) {
        return null;
    }

    @Override
    public HBaseDataSet select(String s) {
        return null;
    }

    @Override
    public HBaseDataSet select(String s, Map<String, Object> map) {
        return null;
    }

    @Override
    public void insert(String s) {

    }

    @Override
    public void delete(String s) {

    }
}
