package com.dbms.core.dbase.impl;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.hive.visitor.HiveSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlExportParameterVisitor;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.stat.TableStat.Column;
import com.dbms.core.dbase.ISQLParser;
import com.dbms.enums.SqlTypeEnum;
import com.dbms.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

@Service
public class DruidSQLParserImpl implements ISQLParser {
    @Override
    public SqlTypeEnum getSqlType(String sql) throws Exception {
        if (StringUtils.startsWith(sql.toUpperCase(), "ALTER")) {
            return SqlTypeEnum.ALTER;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "CREATEINDEX")) {
            return SqlTypeEnum.CREATEINDEX;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "CREATETABLE")) {
            return SqlTypeEnum.CREATETABLE;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "CREATEVIEW")) {
            return SqlTypeEnum.CREATEVIEW;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "DELETE")) {
            return SqlTypeEnum.DELETE;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "DROP")) {
            return SqlTypeEnum.DROP;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "EXECUTE")) {
            return SqlTypeEnum.EXECUTE;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "INSERT")) {
            return SqlTypeEnum.INSERT;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "MERGE")) {
            return SqlTypeEnum.MERGE;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "REPLACE")) {
            return SqlTypeEnum.REPLACE;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "SELECT")) {
            return SqlTypeEnum.SELECT;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "TRUNCATE")) {
            return SqlTypeEnum.TRUNCATE;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "UPDATE")) {
            return SqlTypeEnum.UPDATE;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "UPSERT")) {
            return SqlTypeEnum.UPSERT;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "SHOW")){
            return SqlTypeEnum.SHOW;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "DESCRIBE")){
            return SqlTypeEnum.DESCRIBE;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "DECLARE")){
            return SqlTypeEnum.DECLARE;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "RENAME")){
            return SqlTypeEnum.RENAME;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "EXPLAIN")){
            return SqlTypeEnum.EXPLAIN;
        } else if (StringUtils.startsWith(sql.toUpperCase(), "COMMENT")){
            return SqlTypeEnum.COMMENT;
        }
        else {
//            System.out.println(sqlStmt.getClass());
            return SqlTypeEnum.NONE;
        }
    }

    @Override
    public Set<String> getSchemas(String sql, SqlTypeEnum sqlTypeEnum) throws Exception {
        return null;
    }

    /**
     * 获取tables表名,
     * Map格式：<key:别名，value:表名>，如果无别名，便均为表名
     * @param hql
     * @return
     */
    @Override
    public HashMap<String, String> getTableMap(String hql, SqlTypeEnum sqlTypeEnum) throws Exception {
        HashMap<String, String> tableMap = new HashMap<String, String>();
        SQLStatement statement = SQLUtils.parseSingleStatement(hql, DbType.hive);
        SchemaStatVisitor visitor = new HiveSchemaStatVisitor();
        statement.accept(visitor);
        Map<TableStat.Name, TableStat> tableStatMap = visitor.getTables();
        tableStatMap.forEach((key, value) -> {
            String tableFullName = key.toString();
            tableMap.put(parseTableOthName(hql, tableFullName), parseTableName(hql, tableFullName));
        });
        return tableMap;
    }

    /**
     * 按表全名解析表名
     * @param hql
     * @param tableFullName
     * @return
     */
    public String parseTableName(String hql, String tableFullName){
        String[] splitedTabFullName = tableFullName.split("\\.");
        String tableName;
        tableName = splitedTabFullName[splitedTabFullName.length - 1];
        return tableName;
    }

    /**
     * 按表全名解析表别名
     * @param hql
     * @param tableFullName
     * @return
     */
    public String parseTableOthName(String hql, String tableFullName){
        String[] splitedTabFullName = tableFullName.split("\\.");
        String tableName;
        tableName = splitedTabFullName[splitedTabFullName.length - 1];
        if(!hql.contains(tableFullName)) return "";
        hql = hql.substring(0, hql.length() - 1);
        hql = hql.replace(",", " ");
        hql = hql.replace(".", " ");
        String[] splitedHQL = hql.split(" ");
        boolean afterFrom = false;
        for(int i = 0; i < splitedHQL.length; ++i) {
            String str = splitedHQL[i];
            if(str.equals("FROM") || str.equals("from")) afterFrom = true;
            if(!afterFrom) continue;
            if(str.equals(tableName)) return splitedHQL[i + 1];//hive的表别名之前没有AS，直接+1取
        }
        return "";
    }

    /**
     * 获取tables表名,和表对应操作类型
     * Map格式：<key:表名，value:操作类型>
     * 更改：因为涉及到INSERT与SELECT合并使用的情况（单重多重插入），这里返回不再使用<别名，表名>的结构
     * @param hql
     * @param sqlTypeEnum
     * @return
     */
    public HashMap<String, String> getTableMapAndSQLType(String hql, SqlTypeEnum sqlTypeEnum) throws Exception {
        HashMap<String, String> tableMap = new HashMap<String, String>();
        SQLStatement statement = SQLUtils.parseSingleStatement(hql, DbType.hive);
        SchemaStatVisitor visitor = new HiveSchemaStatVisitor();
        statement.accept(visitor);
        Map<TableStat.Name, TableStat> tableStatMap = visitor.getTables();
        tableStatMap.forEach((key, value) -> {
            tableMap.put(key.toString(), value.toString());
        });
        return tableMap;
    }

    /**
     * 获取tables的列名表名hash
     * Map格式：<key:表名，value:列名列表>
     *     列名列表: 其中每个字段列是一个map，3个key值
     *          fullName:全名, 如 user.user_name;
     *          isUseAs:是否使用了别名, 如user.user_name as name;
     *          asName: 别名;
     *          columnName:真实列名,如user.user_name,真实列名为user_name;
     *
     * @param hql
     * @param sqlTypeEnum
     * @return
     */
    @Override
    public HashMap<String, ArrayList<Map<String, String>>> getTableColumnMap(String hql, SqlTypeEnum sqlTypeEnum) throws Exception {
        HashMap<String, ArrayList<Map<String, String>>> tableColumnMap = new HashMap<String, ArrayList<Map<String, String>>>();
        SQLStatement statement = SQLUtils.parseSingleStatement(hql, DbType.hive);
        SchemaStatVisitor visitor = new HiveSchemaStatVisitor();
        statement.accept(visitor);
        Map<TableStat.Name, TableStat> tableStatMap = visitor.getTables();
        Collection<TableStat.Column> tableColumns = visitor.getColumns();
        for(Column tableColumn : tableColumns){
            String tableFullName = tableColumn.getTable();
            String tableName = parseTableName(hql, tableFullName);
            if(!tableColumnMap.containsKey(tableName)){
                String columnFullName = tableColumn.getFullName();
                String columnName = tableColumn.getName();
                String othName = parseColumnOthName(hql, columnFullName);
                ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
                Map<String, String> map = new HashMap<String, String>();
                map.put("fullName", columnFullName);
                map.put("columnName", columnName);
                if(othName == ""){
                    map.put("isUseAs", "false");
                }
                else{
                    map.put("isUseAs", "true");
                    map.put("asName", othName);
                }
                list.add(map);
                tableColumnMap.put(tableName, list);
            }
            else{
                String columnFullName = tableColumn.getFullName();
                String columnName = tableColumn.getName();
                String othName = parseColumnOthName(hql, columnFullName);
                ArrayList<Map<String, String>> list = tableColumnMap.get(tableName);
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("fullName", columnFullName);
                map.put("isUseAs", othName);
                map.put("columnName", columnName);
                list.add(map);
                tableColumnMap.put(tableName, list);
            }
        }
        return tableColumnMap;
    }


    /**
     * 获取tables的列名表名hash
     * Map格式：<key:表名，value:列名列表>
     * @param sql
     * @param sqlTypeEnum
     * @return
     */
    public Map<String, ArrayList<String>> getTableColumnList(String sql, SqlTypeEnum sqlTypeEnum) {
        Map<String, ArrayList<String>> tableColumnList = new HashMap<String, ArrayList<String>>();
        SQLStatement statement = SQLUtils.parseSingleStatement(sql, DbType.hive);
        SchemaStatVisitor visitor = new HiveSchemaStatVisitor();
        statement.accept(visitor);
        Map<TableStat.Name, TableStat> tableStatMap = visitor.getTables();
        Collection<TableStat.Column> tableColumns = visitor.getColumns();
        for(Column tableColumn : tableColumns){
            String tableFullName = tableColumn.getTable();
            String tableName = parseTableName(sql, tableFullName);
            if(!tableColumnList.containsKey(tableName)){
//                String columnFullName = tableColumn.getFullName();
                String columnName = tableColumn.getName();
//                String othName = parseColumnOthName(sql, columnFullName);
                ArrayList<String> list = new ArrayList<String>();
//                Map<String, String> map = new HashMap<String, String>();
//                map.put("fullName", columnFullName);
//                map.put("isUseAs", othName);
                list.add(columnName);
                tableColumnList.put(tableName, list);
            }
            else{
//                String columnFullName = tableColumn.getFullName();
                String columnName = tableColumn.getName();
//                String othName = parseColumnOthName(sql, columnFullName);
                ArrayList<String > list = tableColumnList.get(tableName);
//                HashMap<String, String> map = new HashMap<String, String>();
//                map.put("fullName", columnFullName);
//                map.put("isUseAs", othName);
//                map.put("columnName", columnName);
                list.add(tableName);
                tableColumnList.put(tableName, list);
            }
        }
        return tableColumnList;
    }


    /**
     * 按列属性全名解析别名
     * @param hql
     * @param colFullName
     * @return
     */
    private String parseColumnOthName(String hql, String colFullName){
        String[] splitedColFullName = colFullName.split("\\.");
        String colName;
        if(splitedColFullName.length == 1) colName = colFullName;
        else colName = splitedColFullName[splitedColFullName.length - 2] + "." + splitedColFullName[splitedColFullName.length - 1];
        if(!hql.contains(colName)) return "";
        hql = hql.substring(0, hql.length() - 1);
        hql = hql.replace(",", " ");
        String[] splitedHQL = hql.split(" ");
        for(int i = 0; i < splitedHQL.length; ++i) {
            String str = splitedHQL[i];
            if (str.contains(colName)) {
                if (i + 1 == splitedHQL.length || !splitedHQL[i + 1].toUpperCase().equals("AS")) return "";//hive的列别名前有AS关键字
                else return splitedHQL[i + 2];
            }
        }
        return "";
    }

    /**
     * 获取插入值
     * Map格式：<key:插入条目编号，value:插入值map>
     * 插入值map格式：<key:属性， value:插入值>
     * @param hql
     * @return
     */
    public HashMap<Integer, Map<String, String>> getInsertValuesMap(String hql){
        if(!StringUtils.startsWith(hql.toUpperCase(), "INSERT")) {
            log.println("非插入语句");
            return null;
        }
        if(!(hql.contains("VALUES") || hql.contains("values"))) {
            log.println("插入参数为空");
            return null;
        }
        HashMap<Integer, Map<String, String>> insertValuesMap = new HashMap<Integer, Map<String, String>>();
        SQLStatement statement = SQLUtils.parseSingleStatement(hql, DbType.hive);
        HiveSchemaStatVisitor visitor = new HiveSchemaStatVisitor();
        MySqlExportParameterVisitor pVisitor= new MySqlExportParameterVisitor();
        statement.accept(visitor);
        statement.accept(pVisitor);
        int valuesSize = visitor.getColumns().size();
        String str = visitor.getColumns().toString();
        String[] columnKeys = str.split(". ");
        if(pVisitor.getParameters() == null) {
            log.println("插入参数为空");
            return null;
        }
        List<Object> valuesList = pVisitor.getParameters();
        int index = 0;
        while(index < valuesList.size()){
            ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
            Map<String, String> map = new HashMap<String, String>();
            {map.put(columnKeys[index % valuesSize], valuesList.get(index).toString());
                index += 1;}
            {map.put(columnKeys[index % valuesSize], valuesList.get(index).toString());
                index += 1;}
            {map.put(columnKeys[index % valuesSize], valuesList.get(index).toString());
                index += 1;}
            {map.put(columnKeys[index % valuesSize], valuesList.get(index).toString());
                index += 1;}
            insertValuesMap.put(index / valuesSize , map);
        }
        return insertValuesMap;
    }

    /**
     * 获取判断条件
     * Map格式：<key:表名.列属性，value:判断值>
     * @param hql
     * @return
     */
    public HashMap<String, String> getConditionsMap(String hql){
        HashMap<String, String> tableConditionsMap = new HashMap<String, String>();
        SQLStatement statement = SQLUtils.parseSingleStatement(hql, DbType.hive);
        SchemaStatVisitor visitor = new HiveSchemaStatVisitor();
        statement.accept(visitor);
        if(visitor.getConditions() == null){
            log.println("判断条件为空");
            return null;
        }
        List<TableStat.Condition> conditions = visitor.getConditions();
        for(TableStat.Condition condition : conditions){
            String str = condition.toString();
            String[] splitedCondition = str.split(" = ");
            tableConditionsMap.put(splitedCondition[0], splitedCondition[1]);
        }
        return tableConditionsMap;
    }


}
