package com.dbms.core;

import com.alibaba.fastjson.JSON;
import com.dbms.enums.SqlTypeEnum;
import com.dbms.utils.StringUtils;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.RenameTableStatement;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.function.CreateFunction;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.procedure.CreateProcedure;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.CreateView;

import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.io.StringReader;
import java.util.*;

public class SQLParserUtil {

    public static SqlTypeEnum getSqlType(String sql) throws JSQLParserException {
        if (sql.toUpperCase().startsWith("SHOW CREATE TABLE") || sql.toUpperCase().startsWith("SELECT PASSWORD")) {
            return SqlTypeEnum.SHOW;
        }
        Statement sqlStmt = CCJSqlParserUtil.parse(new StringReader(sql));
        if (sqlStmt instanceof Alter) {
            return SqlTypeEnum.ALTER;
        } else if (sqlStmt instanceof CreateIndex) {
            return SqlTypeEnum.CREATEINDEX;
        } else if (sqlStmt instanceof CreateTable) {
            return SqlTypeEnum.CREATETABLE;
        } else if (sqlStmt instanceof CreateView) {
            return SqlTypeEnum.CREATEVIEW;
        } else if (sqlStmt instanceof CreateSchema) {
            return SqlTypeEnum.CREATESCHEMA;
        } else if (sqlStmt instanceof CreateFunction){
            return SqlTypeEnum.CREATEFUNCTION;
        } else if (sqlStmt instanceof CreateProcedure) {
            return SqlTypeEnum.CREATEPROCEDURE;
        } else if (sqlStmt instanceof Delete) {
            return SqlTypeEnum.DELETE;
        } else if (sqlStmt instanceof Drop) {
            return SqlTypeEnum.DROP;
        } else if (sqlStmt instanceof Execute) {
            return SqlTypeEnum.EXECUTE;
        } else if (sqlStmt instanceof Insert) {
            return SqlTypeEnum.INSERT;
        } else if (sqlStmt instanceof Merge) {
            return SqlTypeEnum.MERGE;
        } else if (sqlStmt instanceof Replace) {
            return SqlTypeEnum.REPLACE;
        } else if (sqlStmt instanceof Select) {
            return SqlTypeEnum.SELECT;
        } else if (sqlStmt instanceof Truncate) {
            return SqlTypeEnum.TRUNCATE;
        } else if (sqlStmt instanceof Update) {
            return SqlTypeEnum.UPDATE;
        } else if (sqlStmt instanceof Upsert) {
            return SqlTypeEnum.UPSERT;
        } else if (sqlStmt instanceof ShowStatement){
            return SqlTypeEnum.SHOW;
        } else if (sqlStmt instanceof DescribeStatement){
            return SqlTypeEnum.DESCRIBE;
        } else if (sqlStmt instanceof DeclareStatement){
            return SqlTypeEnum.DECLARE;
        } else if (sqlStmt instanceof RenameTableStatement){
            return SqlTypeEnum.RENAME;
        } else if (sqlStmt instanceof ExplainStatement){
            return SqlTypeEnum.EXPLAIN;
        } else if (sqlStmt instanceof Comment){
            return SqlTypeEnum.COMMENT;
        } else if (sqlStmt instanceof ShowTablesStatement){
            return SqlTypeEnum.SHOW;
        } else if (sqlStmt instanceof Grant){
            return SqlTypeEnum.GRANT;
        } else if (sqlStmt instanceof Commit){
            return SqlTypeEnum.COMMIT;
        } else if (sqlStmt instanceof RollbackStatement){
            return SqlTypeEnum.ROLLBACK;
        }
        else {
//            System.out.println(sqlStmt.getClass());
            return SqlTypeEnum.NONE;
        }
    }

    /**
     * 获取sql操作接口,与上面类型判断结合使用
     *
     * @param sql
     * @return
     * @throws JSQLParserException
     */
    public static Statement getStatement(String sql) throws JSQLParserException {
        Statement sqlStmt = CCJSqlParserUtil.parse(new StringReader(sql));
        return sqlStmt;
    }

    /**
     * 获取tables表名
     *
     * @param sql
     * @return
     */
    public static List<String> getTableList(String sql, SqlTypeEnum sqlTypeEnum) throws JSQLParserException {
        List<String> tableList = null;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        Table table;
        String tableName;
        switch (sqlTypeEnum){
            case SELECT:
                Select select = (Select) CCJSqlParserUtil.parse(new StringReader(sql));
                tableList = tablesNamesFinder.getTableList(select);
                break;
            case INSERT:
                Insert insert = (Insert) CCJSqlParserUtil.parse(new StringReader(sql));
                table = insert.getTable();
                tableName = table.getName();
                tableList = Arrays.asList(tableName);
                break;
            case UPDATE:
                Update update = (Update) CCJSqlParserUtil.parse(new StringReader(sql));
                table = update.getTable();
                tableName = table.getName();
                tableList = Arrays.asList(tableName);
                break;
            case DELETE:
                Delete delete = (Delete) CCJSqlParserUtil.parse(new StringReader(sql));
                table = delete.getTable();
                tableName = table.getName();
                tableList = Arrays.asList(tableName);
                break;
            case UPSERT:
                break;
            case DROP:
            case ALTER:
            case CREATEINDEX:
            case CREATETABLE:
            case CREATEVIEW:
            case REPLACE:
            case TRUNCATE:
            case EXECUTE:
                break;
            default:
                return null;
        }

        return tableList;
    }

    /**
     * 获取tables的列名
     *
     * @param sql
     * @param sqlTypeEnum
     * @return
     */
    public static List<String> getColumnList(String sql, SqlTypeEnum sqlTypeEnum) throws JSQLParserException {
        List<String> columnList = new ArrayList<>();
        List<Column> columns = null;
        switch (sqlTypeEnum){
            case SELECT:
                Select select = (Select) CCJSqlParserUtil.parse(new StringReader(sql));
                PlainSelect selectBody = (PlainSelect) select.getSelectBody();
                List<SelectItem> selectItemlist = selectBody.getSelectItems();
                SelectExpressionItem selectExpressionItem = null;
                AllTableColumns allTableColumns = null;
                Alias alias = null;
                SimpleNode node = null;
                if (selectItemlist != null) {
                    for (SelectItem selectItem : selectItemlist) {
                        if (selectItem instanceof SelectExpressionItem) {
                            selectExpressionItem = (SelectExpressionItem) selectItem;
//                            alias = selectExpressionItem.getAlias();
                            node = selectExpressionItem.getExpression().getASTNode();
                            Object value = node.jjtGetValue();
                            String columnName;
                            if (value instanceof Column) {
                                columnName = ((Column) value).getColumnName();
                            } else if (value instanceof Function) {
                                columnName = ((Function) value).toString();
                            } else {
                                // 增加对select 'aaa' from table; 的支持
                                columnName = (String) value;
                                columnName = columnName.replace("'", "");
                                columnName = columnName.replace("\"", "");
                                columnName = columnName.replace("[","");
                                columnName = columnName.replace("]","");
                            }
                            columnList.add(columnName);
                        } else if (selectItem instanceof AllTableColumns) {
                            allTableColumns = (AllTableColumns) selectItem;
                            columnList.add(allTableColumns.toString());
                        } else {
                            columnList.add(selectItem.toString());
                        }
                    }
                }
                break;
            case INSERT:
                Insert insert = (Insert) CCJSqlParserUtil.parse(new StringReader(sql));
//                System.out.println("insert = " + JSON.toJSONString(insert));
                columns = insert.getColumns();
                for (Column column:columns){
                    String columnName = column.getColumnName();
                    columnList.add(columnName);
                }
                break;
            case UPDATE:
                Update update = (Update) CCJSqlParserUtil.parse(new StringReader(sql));
                columns = update.getColumns();
                for (Column column:columns){
                    String columnName = column.getColumnName();
                    columnList.add(columnName);
                }
                break;
            default:
                return null;
        }
        return columnList;
    }



    public static Set<String> getSchemas(String sql, SqlTypeEnum sqlTypeEnum) throws JSQLParserException {
        Set<String> schemas = new HashSet<>();
        Table table;
        switch (sqlTypeEnum){
            case SELECT:
                Select select = (Select) CCJSqlParserUtil.parse(new StringReader(sql));
                SelectBody selectBody = select.getSelectBody();
                PlainSelect plainSelect = (PlainSelect)selectBody;
                while(plainSelect.getFromItem() instanceof SubSelect){
                    SubSelect subSelect = (SubSelect) plainSelect.getFromItem();
                    SelectBody selectBody1 = subSelect.getSelectBody();
                    plainSelect = (PlainSelect) selectBody1;
                }
                table = (Table)plainSelect.getFromItem();
                if (StringUtils.isNotEmpty(table.getSchemaName())){
                    schemas.add(replace(table.getSchemaName()));
                }
                if (plainSelect.getJoins()!=null){
                    for(Join join : plainSelect.getJoins()){
                        Table table1 = (Table)join.getRightItem();
                        schemas.add(replace(table1.getSchemaName()));
                    }
                }
                break;
            case INSERT:
                Insert insert = (Insert) CCJSqlParserUtil.parse(new StringReader(sql));
                table = insert.getTable();
                if (StringUtils.isNotEmpty(table.getSchemaName())){
                    schemas.add(replace(table.getSchemaName()));
                }
                break;
            case UPDATE:
                Update update = (Update) CCJSqlParserUtil.parse(new StringReader(sql));
                table = update.getTable();
                if (StringUtils.isNotEmpty(table.getSchemaName())){
                    schemas.add(replace(table.getSchemaName()));
                }
                break;
            case DELETE:
                Delete delete = (Delete) CCJSqlParserUtil.parse(new StringReader(sql));
                table = delete.getTable();
                if (StringUtils.isNotEmpty(table.getSchemaName())){
                    schemas.add(replace(table.getSchemaName()));
                }
                break;
            case UPSERT:
                Upsert upsert = (Upsert) CCJSqlParserUtil.parse(new StringReader(sql));
                table = upsert.getTable();
                if (StringUtils.isNotEmpty(table.getSchemaName())){
                    schemas.add(replace(table.getSchemaName()));
                }
                break;
            case DROP:
                Drop drop = (Drop) CCJSqlParserUtil.parse(new StringReader(sql));
                table = drop.getName();
                if (StringUtils.isNotEmpty(table.getSchemaName())){
                    schemas.add(replace(table.getSchemaName()));
                }
                break;
            case ALTER:
                Alter alter = (Alter) CCJSqlParserUtil.parse(new StringReader(sql));
                table = alter.getTable();
                if (StringUtils.isNotEmpty(table.getSchemaName())){
                    schemas.add(replace(table.getSchemaName()));
                }
                break;
            case TRUNCATE:
                Truncate truncate = (Truncate) CCJSqlParserUtil.parse(new StringReader(sql));
                table = truncate.getTable();
                if (StringUtils.isNotEmpty(table.getSchemaName())){
                    schemas.add(replace(table.getSchemaName()));
                }
                break;
//            case COMMENT:
//
//                Comment comment = (Comment) CCJSqlParserUtil.parse(new StringReader(sql));
//                table = comment.getTable();
//                System.out.println("comment::"+comment.toString());
//                tableName = replace(table.getName());
//                tableMap.put(tableName,tableName);
//                break;
            case DESCRIBE:
                DescribeStatement describe = (DescribeStatement) CCJSqlParserUtil.parse(new StringReader(sql));
                table = describe.getTable();
                if (StringUtils.isNotEmpty(table.getSchemaName())){
                    schemas.add(replace(table.getSchemaName()));
                }
                break;
            case EXECUTE:
                break;
            default:
                return null;
        }

        return schemas;
    }
    /**
     * 获取tables表名,
     * Map格式：<key:别名，value:表名>，如果无别名，便均为表名
     *
     * @param sql
     * @return
     */
    public static HashMap<String,String> getTableMap(String sql, SqlTypeEnum sqlTypeEnum) throws JSQLParserException {
        HashMap<String, String> tableMap = new HashMap<>();
        Table table;
        String tableName;
        switch (sqlTypeEnum){
            case SELECT:
                Select select = (Select) CCJSqlParserUtil.parse(new StringReader(sql));
                SelectBody selectBody = select.getSelectBody();
                PlainSelect plainSelect = (PlainSelect)selectBody;
                while(plainSelect.getFromItem() instanceof SubSelect){
                    SubSelect subSelect = (SubSelect) plainSelect.getFromItem();
                    SelectBody selectBody1 = subSelect.getSelectBody();
                    plainSelect = (PlainSelect) selectBody1;
                }
                table = (Table)plainSelect.getFromItem();
                if(table.getAlias() != null){
                    tableMap.put(replace(table.getAlias().getName()), replace(table.getName()));
                }else{
                    tableMap.put(replace(table.getName()),replace(table.getName()));
                }
                if (plainSelect.getJoins()!=null){
                    for(Join join : plainSelect.getJoins()){
                        Table table1 = (Table)join.getRightItem();
                        if(table1.getAlias()!=null){
                            tableMap.put(replace(table1.getAlias().getName()), replace(table1.getName()));
                        }else{
                            tableMap.put(replace(table1.getName()), replace(table1.getName()));
                        }
                    }
                }
                break;
            case INSERT:
                Insert insert = (Insert) CCJSqlParserUtil.parse(new StringReader(sql));
                table = insert.getTable();
                tableName = replace(table.getName());
                tableMap.put(tableName,tableName);
                break;
            case UPDATE:
                Update update = (Update) CCJSqlParserUtil.parse(new StringReader(sql));
                table = update.getTable();
                tableName = replace(table.getName());
                tableMap.put(tableName,tableName);
                break;
            case DELETE:
                Delete delete = (Delete) CCJSqlParserUtil.parse(new StringReader(sql));
                table = delete.getTable();
                tableName = replace(table.getName());
                tableMap.put(tableName,tableName);
                break;
            case UPSERT:
                Upsert upsert = (Upsert) CCJSqlParserUtil.parse(new StringReader(sql));
                table = upsert.getTable();
                tableName = replace(table.getName());
                tableMap.put(tableName,tableName);
                break;
            case DROP:
                Drop drop = (Drop) CCJSqlParserUtil.parse(new StringReader(sql));
                table = drop.getName();
                tableName = replace(table.getName());
                tableMap.put(tableName,tableName);
                break;
            case ALTER:
                Alter alter = (Alter) CCJSqlParserUtil.parse(new StringReader(sql));
                table = alter.getTable();
                tableName = replace(table.getName());
                tableMap.put(tableName,tableName);
                break;
            case TRUNCATE:
                Truncate truncate = (Truncate) CCJSqlParserUtil.parse(new StringReader(sql));
                table = truncate.getTable();
                tableName = replace(table.getName());
                tableMap.put(tableName,tableName);
                break;
//            case COMMENT:
//
//                Comment comment = (Comment) CCJSqlParserUtil.parse(new StringReader(sql));
//                table = comment.getTable();
//                System.out.println("comment::"+comment.toString());
//                tableName = replace(table.getName());
//                tableMap.put(tableName,tableName);
//                break;
            case DESCRIBE:
                DescribeStatement describe = (DescribeStatement) CCJSqlParserUtil.parse(new StringReader(sql));
                table = describe.getTable();
                tableName = replace(table.getName());
                tableMap.put(tableName,tableName);
                break;
            case EXECUTE:
                break;
            default:
                return null;
        }
        return tableMap;
    }

    /**
     * 获取tables的列名表名hash
     * Map格式：<key:表名，value:列名列表>
     * @param sql
     * @param sqlTypeEnum
     * @return
     */
    public static HashMap<String, ArrayList<String>> getTableColumnList(String sql, SqlTypeEnum sqlTypeEnum) throws JSQLParserException {
        List<String> columnList = new ArrayList<>();
        HashMap<String, String> tablemap = getTableMap(sql, sqlTypeEnum);
//        System.out.println("tablemap = " + JSON.toJSONString(tablemap));
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<Column> columns = null;
        HashMap<String, ArrayList<String>> mapList = new HashMap<String, ArrayList<String>>();;
        String tableName = "";
        switch (sqlTypeEnum){
            case SELECT:
                Select select = (Select) CCJSqlParserUtil.parse(new StringReader(sql));
                PlainSelect selectBody = (PlainSelect) select.getSelectBody();
                while(selectBody.getFromItem() instanceof SubSelect){
                    SubSelect subSelect = (SubSelect) selectBody.getFromItem();
                    SelectBody selectBody1 = subSelect.getSelectBody();
                    selectBody = (PlainSelect) selectBody1;
                }
                List<SelectItem> selectItemlist = selectBody.getSelectItems();
                SelectExpressionItem selectExpressionItem = null;
                AllTableColumns allTableColumns = null;
//                Alias alias = null;
                SimpleNode node = null;
                if (selectItemlist != null) {
                    for (SelectItem selectItem : selectItemlist) {
                        if (selectItem instanceof SelectExpressionItem) {
                            selectExpressionItem = (SelectExpressionItem) selectItem;
//                            alias = selectExpressionItem.getAlias();
//                            System.out.println(JSON.toJSONString(alias));
                            node = selectExpressionItem.getExpression().getASTNode();
                            Object value = node.jjtGetValue();
                            String columnName;
                            String currentTableName = "";
                            if (value instanceof Column) {
                                columnName = ((Column) value).getFullyQualifiedName();
                                columnName = replace(columnName);
                                String []str = columnName.split("\\.");
                                if(str.length == 1){
                                    List<String> tableList = tablesNamesFinder.getTableList(select);
                                    currentTableName = replace(tableList.get(0));
                                    String[] temp = currentTableName.split("\\.");
                                    currentTableName = temp[temp.length - 1];
                                    if(!(mapList.containsKey(currentTableName))){
                                        mapList.put(currentTableName, new ArrayList<String>());
                                    }
                                    mapList.get(currentTableName).add(str[0]);;
                                    continue;
                                }
                                String tableNameMap  = tablemap.getOrDefault(str[0], null);
                                if(tableNameMap != null) currentTableName = tableNameMap;
//                                System.out.println(mapList.getOrDefault(str[0], null));
                                if(!(mapList.containsKey(str[0]))){
                                    mapList.put(currentTableName, new ArrayList<String>());
                                }
                                mapList.get(currentTableName).add(str[str.length - 1]);;
                            } else if (value instanceof Function) {
                                columnName = ((Function) value).toString();
                            } else {
                                // 增加对select 'aaa' from table; 的支持
                                columnName = (String) value;
                                columnName = columnName.replace("'", "");
                                columnName = columnName.replace("\"", "");
                                columnName = columnName.replace("[","");
                                columnName = columnName.replace("]","");
                            }
                            columnList.add(columnName);
                        } else if (selectItem instanceof AllTableColumns) {
                            allTableColumns = (AllTableColumns) selectItem;
                            columnList.add(allTableColumns.toString());
                        } else {
                            columnList.add(selectItem.toString());
                        }
                    }
                }
                break;
            case INSERT:
                Insert insert = (Insert) CCJSqlParserUtil.parse(new StringReader(sql));
                columns = insert.getColumns();
                tableName = replace(insert.getTable().getName());
                mapList.put(tableName, new ArrayList<String>());
                for (Column column:columns){
                    String columnName = replace(column.getColumnName());
                    mapList.get(tableName).add(columnName);
                }
                break;
            case UPDATE:
                Update update = (Update) CCJSqlParserUtil.parse(new StringReader(sql));
                columns = update.getColumns();
                tableName = replace(update.getTable().getName());
                mapList.put(tableName, new ArrayList<String>());
                for (Column column:columns){
                    String columnName = replace(column.getColumnName());
                    mapList.get(tableName).add(columnName);
                }
                break;
            default:
                return mapList;
        }
        return mapList;
    }

    private static String replace(String str){
        if (StringUtils.isNull(str)){
            return null;
        }
        str = str.replace("`", "");
        str = str.replace("\"", "");
        str = str.replace("[","");
        str = str.replace("]","");
        return str;
    }

    /**
     * 获取tables的列名表名hash
     * Map格式：<key:表名，value:列名列表>
     *     列名列表: 其中每个字段列是一个map，3个key值
     *          fullName:全名, 如 user.user_name;
     *          isUseAs:是否使用了别名, 如user.user_name as name;
     *          asName: 别名;
     *          columnName:真实列名,如user.user_name,真实列名为user_name;
     * @param sql
     * @param sqlTypeEnum
     * @return
     */
    public static HashMap<String, ArrayList<Map<String, String>>> getTableColumnMap(String sql, SqlTypeEnum sqlTypeEnum) throws JSQLParserException {
        List<String> columnList = new ArrayList<>();
        HashMap<String, String> tableMap = getTableMap(sql, sqlTypeEnum);
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        HashMap<String, ArrayList<Map<String,String>>> columnTableMap = new HashMap<>();
        Select select = (Select) CCJSqlParserUtil.parse(new StringReader(sql));
        PlainSelect selectBody = (PlainSelect) select.getSelectBody();
        while(selectBody.getFromItem() instanceof SubSelect){
            SubSelect subSelect = (SubSelect) selectBody.getFromItem();
            SelectBody selectBody1 = subSelect.getSelectBody();
            selectBody = (PlainSelect) selectBody1;
        }
        List<SelectItem> selectItemlist = selectBody.getSelectItems();
        SelectExpressionItem selectExpressionItem = null;
        AllTableColumns allTableColumns = null;
        Alias alias = null;
        SimpleNode node = null;
        if (selectItemlist != null) {
            for (SelectItem selectItem : selectItemlist) {
                if (selectItem instanceof SelectExpressionItem) {
                    selectExpressionItem = (SelectExpressionItem) selectItem;
                    alias = selectExpressionItem.getAlias();
                    node = selectExpressionItem.getExpression().getASTNode();
                    Object value = node.jjtGetValue();
                    String columnName;
                    if (value instanceof Column) {
                        columnName = ((Column) value).getFullyQualifiedName();
//                                String alias = ;
                        Map<String, String> columnMap = new HashMap<>();
                        columnMap.put("fullName",columnName);
                        if (alias != null){
                            columnMap.put("isUseAs", String.valueOf(alias.isUseAs()));
                            columnMap.put("asName", alias.getName());
                        }else{
                            columnMap.put("isUseAs", "false");
                        }
                        columnName = replace(columnName);
                        String []str = columnName.split("\\.");
                        // 如果列名不是table.column
                        String currentTableName;
                        if(str.length == 1){
                            columnMap.put("columnName", str[0]);
                            List<String> tableList = tablesNamesFinder.getTableList(select);
                            currentTableName = replace(tableList.get(0));
                            if (!columnTableMap.containsKey(currentTableName)){
                                columnTableMap.put(currentTableName, new ArrayList<Map<String,String>>());
                            }
                            columnTableMap.get(currentTableName).add(columnMap);
                            continue;
                        }
                        // 列名是table.column
                        currentTableName  = tableMap.getOrDefault(str[0], null);
                        if(currentTableName != null) {
                            columnMap.put("columnName",str[1]);
                            if (!columnTableMap.containsKey(currentTableName)){
                                columnTableMap.put(currentTableName, new ArrayList<Map<String,String>>());
                            }
                            columnTableMap.get(currentTableName).add(columnMap);
                        }
                    } else if (value instanceof Function) {
                        columnName = ((Function) value).toString();
                    } else {
                        // 增加对select 'aaa' from table; 的支持
                        columnName = (String) value;
                        columnName = columnName.replace("'", "");
                        columnName = columnName.replace("\"", "");
                        columnName = columnName.replace("[","");
                        columnName = columnName.replace("]","");
                    }
                    columnList.add(columnName);
                } else if (selectItem instanceof AllTableColumns) {
                    allTableColumns = (AllTableColumns) selectItem;
                    columnList.add(allTableColumns.toString());
                } else {
                    columnList.add(selectItem.toString());
                }
            }
        }
        return columnTableMap;
    }

    /**
     * 获取子查询
     *
     * @param selectBody
     * @return
     */
    public static SubSelect getSubSelect(SelectBody selectBody) {
        if (selectBody instanceof PlainSelect) {
            FromItem fromItem = ((PlainSelect) selectBody).getFromItem();
            if (fromItem instanceof SubSelect) {
                return ((SubSelect) fromItem);
            }
        } else if (selectBody instanceof WithItem) {
            SQLParserUtil.getSubSelect(((WithItem) selectBody));
        }
        return null;
    }

    /**
     * 判断是否为多级子查询
     *
     * @param selectBody
     * @return
     */
    public static boolean isMultiSubSelect(SelectBody selectBody) {
        if (selectBody instanceof PlainSelect) {
            FromItem fromItem = ((PlainSelect) selectBody).getFromItem();
            if (fromItem instanceof SubSelect) {
                SelectBody subBody = ((SubSelect) fromItem).getSelectBody();
                if (subBody instanceof PlainSelect) {
                    FromItem subFromItem = ((PlainSelect) subBody).getFromItem();
                    if (subFromItem instanceof SubSelect) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 获取查询字段
     *
     * @param selectBody
     * @return
     */
    public static List<SelectItem> getSelectItems(SelectBody selectBody) {
        if (selectBody instanceof PlainSelect) {
            List<SelectItem> selectItems = ((PlainSelect) selectBody).getSelectItems();
            return selectItems;
        }
        return null;
    }
}
