package com.dbms.core.dbase;

import com.dbms.enums.SqlTypeEnum;
import net.sf.jsqlparser.JSQLParserException;

import java.util.*;

public interface ISQLParser {

    SqlTypeEnum getSqlType(String sql) throws Exception;

//    /**
//     * 获取tables表名
//     *
//     * @param sql
//     * @return
//     */
//    List<String> getTableList(String sql, SqlTypeEnum sqlTypeEnum) throws Exception;
//    /**
//     * 获取tables的列名
//     *
//     * @param sql
//     * @param sqlTypeEnum
//     * @return
//     */
//    List<String> getColumnList(String sql, SqlTypeEnum sqlTypeEnum) throws Exception;

    Set<String> getSchemas(String sql, SqlTypeEnum sqlTypeEnum) throws Exception;
    /**
     * 获取tables表名,
     * Map格式：<key:别名，value:表名>，如果无别名，便均为表名
     *
     * @param sql
     * @return
     */
    HashMap<String,String> getTableMap(String sql, SqlTypeEnum sqlTypeEnum) throws Exception;

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
    HashMap<String, ArrayList<Map<String, String>>> getTableColumnMap(String sql, SqlTypeEnum sqlTypeEnum) throws Exception;
}
