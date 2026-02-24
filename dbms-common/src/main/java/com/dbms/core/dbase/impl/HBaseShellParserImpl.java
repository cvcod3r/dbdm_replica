package com.dbms.core.dbase.impl;

import co.cask.tephra.TxConstants;
import com.alibaba.druid.sql.visitor.functions.Char;
import com.dbms.core.dbase.HBaseShellMeta;
import com.dbms.core.dbase.IHBaseShellParser;
import com.dbms.enums.SqlTypeEnum;
import com.dbms.utils.StringUtils;
import com.github.CCweixiao.hbase.sdk.common.model.ColumnFamilyDesc;
import com.github.CCweixiao.hbase.sdk.common.model.HTableDesc;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.KeepDeletedCells;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.io.compress.Compression;
import org.apache.hadoop.hbase.io.encoding.DataBlockEncoding;
import org.apache.hadoop.hbase.regionserver.BloomType;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HBaseShellParserImpl implements IHBaseShellParser {

    /**
     * 获取操作类型
     * @param sql
     * @return
     */
    @Override
    public SqlTypeEnum getSqlType(String sql) throws Exception{
        if (StringUtils.startsWith(sql.toUpperCase(), "SCAN ")){
            return SqlTypeEnum.SCAN;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "SELECT ")){
            return SqlTypeEnum.SELECT;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "PUT ")){
            return SqlTypeEnum.PUT;
        } else if(StringUtils.startsWith(sql.toUpperCase(),  "GET ")){
            return SqlTypeEnum.GET;
        } else if(StringUtils.startsWith(sql.toUpperCase(),  "ALTER ")){
            return SqlTypeEnum.ALTER;
        } else if(StringUtils.startsWith(sql.toUpperCase(),  "DISABLE ")){
            return SqlTypeEnum.DISABLE;
        } else if(StringUtils.startsWith(sql.toUpperCase(),  "ENABLE ")){
            return SqlTypeEnum.ENABLE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "DROP ")){
            return SqlTypeEnum.DROP;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "TRUNCATE ")){
            return SqlTypeEnum.TRUNCATE;
        }else if(StringUtils.startsWith(sql.toUpperCase(), "EXIST ")){
            return SqlTypeEnum.EXIST;
        }else if(StringUtils.startsWith(sql.toUpperCase(), "GRANT ")){
            return SqlTypeEnum.GRANT;
        }else if(StringUtils.startsWith(sql.toUpperCase(), "REVOKE ")){
            return SqlTypeEnum.REVOKE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "DELETE ")){
            return SqlTypeEnum.DELETE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "DELETEALL ")){
            return SqlTypeEnum.DELETEALL;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "DESCRIBE ")){
            return SqlTypeEnum.DESCRIBE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "DESC ")){
            return SqlTypeEnum.DESC;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "LIST_NAMESPACE_TABLES ")){
            return SqlTypeEnum.LIST_NAMESPACE_TABLES;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "LIST_NAMESPACE")){
            return SqlTypeEnum.LIST_NAMESPACE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "LIST")){
            return SqlTypeEnum.LIST;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "CREATE_NAMESPACE ")){
            return SqlTypeEnum.CREATE_NAMESPACE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "DESCRIBE_NAMESPACE ")){
            return SqlTypeEnum.DESCRIBE_NAMESPACE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "DROP_NAMESPACE ")){
            return SqlTypeEnum.DROP_NAMESPACE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "CREATE ")){
            return SqlTypeEnum.CREATE;
        } else if(StringUtils.startsWith(sql.toUpperCase(), "DROP_ALL ")){
            return SqlTypeEnum.DROP_ALL;
        } else {
            throw new Exception("语句错误，请检查后重试！");
        }
    }


    /**
     * 获取HBase Shell元数据（即解析HBase Shell结构，获取相应的元素）
     * @param shell
     * @return
     */
    @Override
    public HBaseShellMeta getHBaseShellMeta(String shell) throws Exception {
        HBaseShellMeta shellMeta = null;
        SqlTypeEnum sqlType = getSqlType(shell);
        switch (sqlType){
            case CREATE_NAMESPACE:
                shellMeta = createNameSpaceStatement(sqlType, shell);
                break;
            case DROP_NAMESPACE:
                shellMeta = dropNameSpaceStatement(sqlType, shell);
                break;
            case CREATE:
                shellMeta = createTableStatement(sqlType, shell);
                break;
            case PUT:
                shellMeta = putTableStatement(sqlType, shell);
                break;
            case GET:
                shellMeta = getTableStatement(sqlType, shell);
                break;
            case DROP:
                shellMeta = DropTableStatement(sqlType, shell);
                break;
            case ALTER:
                shellMeta = alterTableStatement(sqlType, shell);
                break;
            case DISABLE:
                shellMeta = disableTableStatement(sqlType, shell);
                break;
            case ENABLE:
                shellMeta = enableTableStatement(sqlType, shell);
                break;
            case SCAN:
                shellMeta = scanTableStatement(sqlType, shell);
                break;
            case LIST_NAMESPACE_TABLES:
                shellMeta = listNamespaceTablesStatement(sqlType, shell);
                break;
            case DESCRIBE:
            case DESC:
                shellMeta = descTablesStatement(sqlType, shell);
                break;
            case EXIST:
                shellMeta = existTablesStatement(sqlType, shell);
                break;
            case TRUNCATE:
                shellMeta = truncateTableStatement(sqlType, shell);
                break;
            case GRANT:
                shellMeta = grantStatement(sqlType, shell);
                break;
            case REVOKE:
                shellMeta = revokeStatement(sqlType, shell);
                break;
//            case CREATE_NAMESPACE:
//                shellMeta = createNameSpaceStatement(sqlType, shell);
//                break;
//            case DROP_NAMESPACE:
//                shellMeta = dropNameSpaceStatement(sqlType, shell);
//                break;
            case NONE:
                throw new Exception("语句出错，请仔细检查");
            default:
                return shellMeta;
        }
        return shellMeta;
    }




    /**
     * 截取第一个空格之前的字符
     * @param str
     * @return
     */
    private String subPrefix(String str){
        return str.substring(0, str.indexOf(" "));
    }

    /**
     * 截取第一个空格之后的字符
     * @param str
     * @return
     */
    private String subSuffix(String str){
        int index = str.indexOf(" ");
        return str.substring(index);
    }

    private String filterChar(String str){
        return str != null ? str.replace("'", "").trim() : null;
    }

    /**
     * 从 'tableName' ,中拿到表名
     * @param str
     * @return
     */
    private String getTableName(String str){
        return str != null ? str.replace(",", "").replace("'", "").trim() : null;
    }

    /**
     * 过滤右括号
     * @param str
     * @return
     */
    private String filterRight(String str){
        return str != null ? str.replace("}", "").trim() : null;
    }

    /**
     * 获取 => 右边的内容
     * 如 NAME => 'f2' 中的 f2
     * {NAME => 'f2', DATA_BLOCK_ENCODING => 'NONE', BLOOMFILTER => 'ROW', REPLICATION_SCOPE => '0', VERSIONS => '1', COMPRESSION => 'NONE', MIN_VERSIONS => '0', TTL => '2147483647', KEEP_DELETED_CELLS => 'false', BLOCKSIZE => '65536', IN_MEMORY => 'false', BLOCKCACHE => 'true'}
     * @param str
     * @return
     */
    private String getColumnMeta(String str){
        int index = str.indexOf(">");
        return str.substring(index+1).replace("'", "").trim();
    }


    /**
     * 解析create语句
     * create ‘表名’,{NAME=>‘列簇名’},{NAME=>‘列簇名’}…
     * create 'customer',{NAME=>'addr'},{NAME=>'order'}
     *
     * @param sqlType
     * @param shell
     * @return
     */
    private HBaseShellMeta createTableStatement(SqlTypeEnum sqlType, String shell) {
        HBaseShellMeta shellMeta = new HBaseShellMeta();
        HashMap<String, String> tableName = new HashMap<String, String>();
        HashMap<String, ArrayList<Map<String, String>>> columnFamilies = new HashMap<String, ArrayList<Map<String, String>>>();
        List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
        if (shell.contains("{")){
            System.out.println("列族有其他描述信息");
            String meta = subSuffix(shell);
            String[] metaList = meta.split("\\{");
            String tableNameStr = getTableName(metaList[0]);
            tableName.put(tableNameStr, tableNameStr);
            // 解析列簇配置
            for(int i = 1; i < metaList.length; i++){
                // 拆之前先去掉右括号
                String[] columns = filterRight(metaList[i]).split(",");
                ColumnFamilyDescriptorBuilder build = null;
                for (String column : columns){
//                    System.out.println(column);
                    if (column.toUpperCase().contains("NAME")){
                        build = ColumnFamilyDescriptorBuilder.newBuilder(getColumnMeta(column).getBytes());
//                        columnFamilies.add(getColumnMeta(column));
                        columnFamilies.put(tableNameStr, new ArrayList<Map<String, String>>(){{
                            add(new HashMap<String, String>(){{
                                put(getColumnMeta(column), "");
                            }});
                        }});
                    } else if (column.toUpperCase().contains("DATA_BLOCK_ENCODING")){
                        build.setDataBlockEncoding(DataBlockEncoding.valueOf(getColumnMeta(column)));
                    } else if (column.toUpperCase().contains("BLOOMFILTER")){
                        build.setBloomFilterType(BloomType.valueOf(getColumnMeta(column)));
                    } else if (column.toUpperCase().contains("REPLICATION_SCOPE")){
                        build.setScope(Integer.valueOf(getColumnMeta(column)));
                    } else if (column.toUpperCase().contains("VERSIONS")){
                        if (column.toUpperCase().contains("MIN_VERSIONS")){
                            build.setMinVersions(Integer.valueOf(getColumnMeta(column)));
                        } else {
                            build.setMaxVersions(Integer.valueOf(getColumnMeta(column)));
                        }
                    } else if (column.toUpperCase().contains("COMPRESSION")){
                        build.setCompressionType(Compression.Algorithm.valueOf(getColumnMeta(column)));
                    } else if (column.toUpperCase().contains("TTL")){
                        build.setTimeToLive(Integer.valueOf(getColumnMeta(column)));
                    } else if (column.toUpperCase().contains("KEEP_DELETED_CELLS")){
                        build.setKeepDeletedCells(KeepDeletedCells.getValue(getColumnMeta(column)));
                    } else if (column.toUpperCase().contains("BLOCKSIZE")){
                        build.setBlocksize(Integer.valueOf(getColumnMeta(column)));
                    } else if (column.toUpperCase().contains("IN_MEMORY")){
                        build.setInMemory(Boolean.valueOf(getColumnMeta(column)));
                    } else if (column.toUpperCase().contains("BLOCKCACHE")){
                        build.setBlockCacheEnabled(Boolean.valueOf(getColumnMeta(column)));
                    }
                }
                if (StringUtils.isNotEmpty(build.getNameAsString())){
                    columnFamilyDescriptors.add(build.build());
                }
            }
            // 解析表配置，表配置位于最后一个{}中，只需要解析metaList的最后一个元素
            // 拆之前先去掉右括号
            // String[] tableMetas = filterRight(metaList[metaList.length - 1]).split(",");
            TableDescriptor tableDescriptor = TableDescriptorBuilder.newBuilder(TableName.valueOf(tableNameStr))
                    .setColumnFamilies(columnFamilyDescriptors)
                    .build();
            // 给shellMeta成员赋值
            shellMeta.setSqlType(sqlType);
            shellMeta.setTableName(tableName);
            shellMeta.setColumnFamily(columnFamilies);
            shellMeta.setColumnFamilyDescriptors(columnFamilyDescriptors);
            shellMeta.setTableDescriptor(tableDescriptor);
        } else {
            System.out.println("最简单的形式");
            // 获取表名
            String meta = subSuffix(shell);
            String[] metaList = meta.split(",");
            String tableNameStr = filterChar(metaList[0]);
            tableName.put(tableNameStr, tableNameStr);
            List<String> columnList = new ArrayList<String>();
            // 获取列簇，要把第一个表名去掉
            for (String str : metaList){
                columnList.add(filterChar(str.trim()));
            }
            columnList.remove(0);
            ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
            // hbase-sdk ColumnFamilyDesc
            for (String column : columnList){
                ColumnFamilyDescriptor columnFamilyDescriptor = ColumnFamilyDescriptorBuilder.newBuilder(column.getBytes()).build();
                columnFamilyDescriptors.add(columnFamilyDescriptor);
                list.add(new HashMap<String, String>(){{
                    put(column, "");
                }});
            }
            columnFamilies.put(tableNameStr, list);
            TableDescriptor tableDescriptor = TableDescriptorBuilder.newBuilder(TableName.valueOf(tableNameStr))
                    .setColumnFamilies(columnFamilyDescriptors)
                    .build();
            // 给shellMeta成员赋值
            shellMeta.setSqlType(sqlType);
            shellMeta.setTableName(tableName);
            shellMeta.setColumnFamily(columnFamilies);
            shellMeta.setColumnFamilyDescriptors(columnFamilyDescriptors);
            shellMeta.setTableDescriptor(tableDescriptor);
        }
        return shellMeta;
    }

    /**
     * PUT语句解析函数
     * @param sqlType
     * @param shell
     * @return
     */
    private HBaseShellMeta putTableStatement(SqlTypeEnum sqlType, String shell) {
        //初始化
        HBaseShellMeta shellMeta = new HBaseShellMeta();
        HashMap<String, ArrayList<Map<String, String>>> columnFamilies = new HashMap<String, ArrayList<Map<String, String>>>();
        //解析
        shell = shell.replace(" ", ",");
        String[] splitedShell = shell.split(",");
        //表名
        String tableNameStr = splitedShell[1];
        tableNameStr = getBetweenQuot(tableNameStr);
        HashMap<String, String> tableName = new HashMap<String, String>();
        tableName.put(tableNameStr, tableNameStr);
        //rowKey
        String rowKey = splitedShell[2];
        rowKey = getBetweenQuot(rowKey);
        //列簇、列
        String[] colMeta = splitedShell[3].split(":");
        String colFamliy = colMeta[0];
        String colName = colMeta[1];
        colFamliy = getBetweenQuot(colFamliy);
        colName = getBetweenQuot(colName);
        //值
        String putValue = splitedShell[4];
        putValue = getBetweenQuot(putValue);
        //时间戳
        Long timeStamp = null;
        if(splitedShell.length == 5){
            System.out.println("无时间戳，系统将自动添加");
        }
        else{
            String timeStampStr = splitedShell[5];
            timeStamp = (long)Integer.valueOf(timeStampStr);
        }
        Put put = new Put(Bytes.toBytes(rowKey));
        if(splitedShell.length == 5) {
            put.addColumn(Bytes.toBytes(colFamliy), Bytes.toBytes(colName), Bytes.toBytes(putValue));
        }
        else{
            put.addColumn(Bytes.toBytes(colFamliy), Bytes.toBytes(colName), timeStamp, Bytes.toBytes(putValue));
        }
        //columnFamilies
        ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
        Map<String, String> map = new HashMap<String, String>();
        map.put(colFamliy, colName);
        list.add(map);
        columnFamilies.put(tableNameStr, list);
        //shellMeta赋值
        shellMeta.setSqlType(sqlType);
        shellMeta.setPut(put);
        shellMeta.setPutValue(putValue);
        shellMeta.setTableName(tableName);
        shellMeta.setColumnFamily(columnFamilies);
        return shellMeta;
    }

    private HBaseShellMeta alterTableStatement(SqlTypeEnum sqlType, String shell) {
        //初始化
        HBaseShellMeta shellMeta = new HBaseShellMeta();
        HashMap<String, ArrayList<Map<String, String>>> columnFamilies = new HashMap<String, ArrayList<Map<String, String>>>();
        List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
        HashMap<String, String> tableName = new HashMap<String, String>();
        List<String> columnList = new ArrayList<String>();
        boolean del_flag = false;
        //解析
        if (shell.contains("METHOD") && shell.contains("delete")){
            del_flag = true;
        }
        if (shell.contains("{")) {
            // 有大括号，则涉及多个列族，按照大括号做分割
            System.out.println("列族有其他描述信息");
            String meta = subSuffix(shell);
            String[] metaList = meta.split("\\{");
            String tableNameStr = getTableName(metaList[0]);
            tableName.put(tableNameStr, tableNameStr);
            // 解析列簇配置
            for (int i = 1; i < metaList.length; i++) {
                // 拆之前先去掉右括号
                String[] columns = filterRight(metaList[i]).split(",");
                ColumnFamilyDescriptorBuilder build = null;
                for (String column : columns) {
//                    System.out.println(column);
                    if (column.toUpperCase().contains("NAME")) {
                        build = ColumnFamilyDescriptorBuilder.newBuilder(getColumnMeta(column).getBytes());
//                        columnFamilies.add(getColumnMeta(column));
                        columnFamilies.put(tableNameStr, new ArrayList<Map<String, String>>() {{
                            add(new HashMap<String, String>() {{
                                put(getColumnMeta(column), "");
                            }});
                        }});
                    }else if (column.toUpperCase().contains("VERSIONS")){
                        build.setMaxVersions(Integer.valueOf(getColumnMeta(column)));
                    }else if (column.toUpperCase().contains("KEEP_DELETED_CELLS")){
                        build.setKeepDeletedCells(KeepDeletedCells.getValue(getColumnMeta(column)));
                    }
                }
                if (StringUtils.isNotEmpty(build.getNameAsString())){
                    columnFamilyDescriptors.add(build.build());
                }
            }

        }else{
            // 无大括号，只有一个列族，直接解析
            System.out.println("无大括号的形式，无附加信息，只有列族名");
            shell = shell.replace(" ", ",");
            String[] splitedShell = shell.split(",");
            String tableNameStr = filterChar(splitedShell[1]);
            tableName.put(tableNameStr, tableNameStr);
            String colFamilyName = filterChar(splitedShell[2].replace("NAME=>", ""));
            columnList.add(colFamilyName);

            ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
            // hbase-sdk ColumnFamilyDesc
            for (String column : columnList){
                ColumnFamilyDescriptor columnFamilyDescriptor = ColumnFamilyDescriptorBuilder.newBuilder(column.getBytes()).build();
                columnFamilyDescriptors.add(columnFamilyDescriptor);
                list.add(new HashMap<String, String>(){{
                    put(column, "");
                }});
            }
            columnFamilies.put(tableNameStr, list);
        }

        // 给shellMeta成员赋值
        shellMeta.setSqlType(sqlType);
        shellMeta.setTableName(tableName);
        shellMeta.setColumnFamily(columnFamilies);
        shellMeta.setColumnFamilyDescriptors(columnFamilyDescriptors);
        shellMeta.setAlterDelete(del_flag);

        return shellMeta;
    }

    private HBaseShellMeta disableTableStatement(SqlTypeEnum sqlType, String shell) {
        // 初始化
        HBaseShellMeta shellMeta = new HBaseShellMeta();
        HashMap<String, String> tableName = new HashMap<String, String>();
        // 解析
        shell = shell.replace(" ", ",");
        String[] splitedShell = shell.split(",");
        String tableNameStr = filterChar(splitedShell[1]);
        tableName.put(tableNameStr, tableNameStr);
        // 赋值
        shellMeta.setSqlType(sqlType);
        shellMeta.setTableName(tableName);
        return shellMeta;
    }

    private HBaseShellMeta enableTableStatement(SqlTypeEnum sqlType, String shell) {
        // 初始化
        HBaseShellMeta shellMeta = new HBaseShellMeta();
        HashMap<String, String> tableName = new HashMap<String, String>();
        // 解析
        shell = shell.replace(" ", ",");
        String[] splitedShell = shell.split(",");
        String tableNameStr = filterChar(splitedShell[1]);
        tableName.put(tableNameStr, tableNameStr);
        // 赋值
        shellMeta.setSqlType(sqlType);
        shellMeta.setTableName(tableName);
        return shellMeta;
    }

    private HBaseShellMeta descTablesStatement(SqlTypeEnum sqlType, String shell) {
        // 初始化
        HBaseShellMeta shellMeta = new HBaseShellMeta();
        HashMap<String, String> tableName = new HashMap<String, String>();
        // 解析
        shell = shell.replace(" ", ",");
        String[] splitedShell = shell.split(",");
        String tableNameStr = filterChar(splitedShell[1]);
        tableName.put(tableNameStr, tableNameStr);
        // 赋值
        shellMeta.setSqlType(sqlType);
        shellMeta.setTableName(tableName);
        return shellMeta;
    }

    private HBaseShellMeta existTablesStatement(SqlTypeEnum sqlType, String shell) {
        // 初始化
        HBaseShellMeta shellMeta = new HBaseShellMeta();
        HashMap<String, String> tableName = new HashMap<String, String>();
        // 解析
        shell = shell.replace(" ", ",");
        String[] splitedShell = shell.split(",");
        String tableNameStr = filterChar(splitedShell[1]);
        tableName.put(tableNameStr, tableNameStr);
        // 赋值
        shellMeta.setSqlType(sqlType);
        shellMeta.setTableName(tableName);
        return shellMeta;
    }

    private HBaseShellMeta DropTableStatement(SqlTypeEnum sqlType, String shell) {
        HBaseShellMeta shellMeta = new HBaseShellMeta();
        shell = shell.replace(" ", ",");
        String[] splitedShell = shell.split(",");
        //表名
        String tableNameStr = filterChar(splitedShell[1]);
        tableNameStr = getBetweenQuot(tableNameStr);
        HashMap<String, String> tableName = new HashMap<String, String>();
        tableName.put(tableNameStr,tableNameStr);
        shellMeta.setSqlType(sqlType);
        shellMeta.setTableName(tableName);
        //System.out.println(shellMeta);
        return shellMeta;
    }

    private HBaseShellMeta listNamespaceTablesStatement(SqlTypeEnum sqlType, String shell) {
        HBaseShellMeta shellMeta = new HBaseShellMeta();
        String namespace = filterChar(subSuffix(shell));
        shellMeta.setSqlType(sqlType);
        shellMeta.setNameSpace(namespace);
        return shellMeta;
    }

    /**
     * 拿单引号中的字符串
     * @param str
     * @return
     */
    private String getBetweenQuot(String str){
        return str != null ? str.replace(",", "").replace("'", "").trim() : null;
    }

    /**
     * 拿双引号中的字符串
     * @param str
     * @return
     */
    private String getBetweenDoubleQuot(String str){
        return str != null ? str.replace("\"", "").trim() : null;
    }

    /**
     * 拿括号中的字符串
     * @param str
     * @return
     */
    private String getBetweenBrackes(String str){
        return str != null ? str.replace("(", "").replace(")", "").trim() : null;
    }

    /**
     * 拿第二个单引号位置，用于定位切割字符串
     * @return
     */
    private int getEndIndexOfQuot(int beginIndex, String meta){
        int endIndex = beginIndex;
        boolean flag = true;
        while(flag || meta.charAt(endIndex) != '\''){
            if (meta.charAt(endIndex) == '\''){
                flag = false;
            }
            endIndex += 1;
            if(endIndex >= meta.length()) break;
        }
        return endIndex;
    }

    /**
     * 拿第二个双引号位置，用于定位切割字符串
     * @return
     */
    private int getEndIndexOfDoubleQuot(int beginIndex, String meta){
        int endIndex = beginIndex;
        boolean flag = true;
        while(flag || meta.charAt(endIndex) != '\"'){
            if (meta.charAt(endIndex) == '\"'){
                flag = false;
            }
            endIndex += 1;
            if(endIndex >= meta.length()) break;
        }
        return endIndex;
    }

    /**
     * 拿第二个方括号位置，用于定位切割字符串
     * @return
     */
    private int getEndIndexOfSqrBracket(int beginIndex, String meta){
        int endIndex = beginIndex;
        while(meta.charAt(endIndex) != ']'){
            endIndex += 1;
            if(endIndex >= meta.length()) break;
        }
        return endIndex;
    }

    /**
     * 拿第二个括号位置，用于定位切割字符串
     * @return
     */
    private int getEndIndexOfBracket(int beginIndex, String meta){
        int endIndex = beginIndex;
        while(meta.charAt(endIndex) != ')'){
            endIndex += 1;
            if(endIndex >= meta.length()) break;
        }
        return endIndex;
    }

    /**
     * 切割boolean
     * @return
     */
    private int getEndIndexOfBoolean(int beginIndex, String meta){
        int endIndex = beginIndex;
        while(Character.isAlphabetic(meta.charAt(endIndex))){
            endIndex += 1;
            if(endIndex >= meta.length()) break;
        }
        return endIndex;
    }

    /**
     * 切割数字
     * @return
     */
    private int getEndIndexOfInt(int beginIndex, String meta){
        int endIndex = beginIndex;
        while(Character.isDigit(meta.charAt(endIndex))){
            endIndex += 1;
            if(endIndex >= meta.length()) break;
        }
        return endIndex;
    }

    /**
     * SCAN语句解析函数结构：
     *          if      *COLUMNS*  列查找
     *          elseif  *TIMERANGE  时间戳查找
     *          elseif  *STARTROW/STOPROW*  行查找
     *          elseif  *ROWPREFIXFILTER*  行前缀查找
     *          elseif  *FILTER* :  过滤器：
     *                  if  *ColumnPrefixFilter* （列前缀过滤）
     *                  elseif  *PrefixFilter*  （行前缀过滤）
     *                  elseif  *QualifierFilter*  （列关键字过滤）
     *                  elseif  *ValueFilter*  （值过滤）
     *                  elseif  *FamilyFilter*  （列簇过滤）
     *                  elseif  *TimestampsFilter*  （时间戳过滤）
     *                  if      *FirstKeyOnlyFilter*  （只拿出key中的第一个column的第一个version）
     *                  if      *KeyOnlyFilter*  （只要key,不要value）
     *          if      *REVERSED*  （反转）
     *          if      *RAW*  （全部数据）
     *          if      *LIMIT*  （显示行数）
     *          if      *ALL_METRICS*  （全部指标）   ------------<这条不确定函数用的对不对>------------
     * @param sqlType
     * @param shell
     * @return
     * @throws Exception
     */
    private HBaseShellMeta scanTableStatement(SqlTypeEnum sqlType, String shell) throws Exception {
        HBaseShellMeta shellMeta = new HBaseShellMeta();
        HashMap<String, String> tableName = new HashMap<String, String>();
        HashMap<String, ArrayList<Map<String, String>>> columnFamilies = new HashMap<String, ArrayList<Map<String, String>>>();
        shell = subSuffix(shell);//去除操作类型
        if(shell.contains("{")){
            String[] metaList = shell.split("\\{");
            if(metaList.length > 2) throw new Exception("语句出错，请仔细检查");
            //表名
            String tableNameStr = getTableName(metaList[0]);
            tableName.put(tableNameStr, tableNameStr);
            //Scan类
            Scan scan = new Scan();
            //右侧{}内的字段
            String meta = filterRight(metaList[1]);
            //按列查找    ****COLUMNS=>****
            if(meta.contains("COLUMNS")){
                //切割出COLUMNS=>后面的值
                String newMeta = meta.replace(" ", "");
                int beginIndex = 0;
                int endIndex = 0;
                //单列簇，无'[]'
                if(newMeta.charAt(newMeta.indexOf("COLUMNS") + 9) != '['){
                    beginIndex = newMeta.indexOf("COLUMNS") + 9;
                    endIndex = getEndIndexOfQuot(beginIndex, newMeta);
                }
                //多列簇,含'[]'
                else{
                    beginIndex = newMeta.indexOf("COLUMNS") + 11;
                    endIndex = getEndIndexOfSqrBracket(beginIndex, newMeta);
                }
                newMeta = newMeta.substring(beginIndex, endIndex);
                String[] columnMetaList = newMeta.split(",");
                for(String columnMetas : columnMetaList){
                    columnMetas = getBetweenQuot(columnMetas);
                    String[] columnMeta = columnMetas.split(":");
                    scan.addColumn(Bytes.toBytes(columnMeta[0]), Bytes.toBytes(columnMeta[1]));
                }
                System.out.println(scan);
            }
            //按时间查找    ****TIMERANGE=>****
            else if(meta.contains("TIMERANGE")){
                //切割出TIMERANGE=>后面的值
                String newMeta = meta.replace(" ", "");
                int beginIndex = newMeta.indexOf("COLUMNS") + 13;
                int endIndex = getEndIndexOfSqrBracket(beginIndex, newMeta);
                String[] timeStamps = newMeta.substring(beginIndex, endIndex).split(",");
                String minTimeStr = timeStamps[0];
                String maxTimeStr = timeStamps[1];
                long minTime = (long)Integer.valueOf(minTimeStr);
                long maxTime = (long)Integer.valueOf(maxTimeStr);
                scan.setTimeRange(minTime, maxTime);
                System.out.println(scan);
            }
            //按rowKey的范围查找     ****STARTROW=> && STOPROW=>****
            else if(meta.contains("STARTROW") && meta.contains("STOPROW")){
                String newMeta = meta.replace(" ", "");
                //startRow
                int beginIndex = newMeta.indexOf("STARTROW") + 10;
                int endIndex = getEndIndexOfQuot(beginIndex, newMeta);
                String startRow = newMeta.substring(beginIndex, endIndex);
                startRow = getBetweenQuot(startRow);
                //stopRow
                beginIndex = newMeta.indexOf("STOPROW") + 9;
                endIndex = getEndIndexOfQuot(beginIndex, newMeta);
                String stopRow = newMeta.substring(beginIndex, endIndex);
                stopRow = getBetweenQuot(stopRow);
                scan.setStartRow(Bytes.toBytes(startRow));
                scan.setStopRow(Bytes.toBytes(stopRow));
                System.out.println(scan);
            }
            //指定开头的rowKey查找     ****ROWPREFIXFILTER=>****
            else if(meta.contains("ROWPREFIXFILTER")){
                String newMeta = meta.replace(" ", "");
                int beginIndex = newMeta.indexOf("ROWPREFIXFILTER") + 17;
                int endIndex = getEndIndexOfQuot(beginIndex, newMeta);
                newMeta = newMeta.substring(beginIndex, endIndex);
                newMeta = getBetweenQuot(newMeta);
                scan.setRowPrefixFilter(Bytes.toBytes(newMeta));
                System.out.println(scan);
            }
            //按FILTER查找    ****FILTER=>****
            //FILTER包含多种类Filter以及组合使用
            else if(meta.contains("FILTER")){
                String newMeta = meta.replace(" ", "");
                int beginIndex = newMeta.indexOf("FILTER") + 8;
                int endIndex = getEndIndexOfDoubleQuot(beginIndex, newMeta);
                newMeta = newMeta.substring(beginIndex, endIndex);
                newMeta = getBetweenDoubleQuot(newMeta);
                FilterList finalFilter = new FilterList(FilterList.Operator.MUST_PASS_ALL);
                //以AND切割字段
                String[] columnMetaList_AND = newMeta.split("AND");
                for(String columnMetaList : columnMetaList_AND){
                    //多值过滤
                    FilterList filters = new FilterList(FilterList.Operator.MUST_PASS_ONE);
                    //以OR切割字段
                    String[] columnMetaList_OR = columnMetaList.split("OR");
                    for(String columnMeta : columnMetaList_OR){
                        //指定前缀的列过滤器
                        if(columnMeta.contains("ColumnPrefixFilter")){
                            columnMeta = columnMeta.replace(" ", "");
                            beginIndex = columnMeta.indexOf("ColumnPrefixFilter") + 18;
                            endIndex = getEndIndexOfQuot(beginIndex, columnMeta);
                            columnMeta = columnMeta.substring(beginIndex, endIndex);
                            columnMeta = getBetweenQuot(columnMeta);
                            columnMeta = getBetweenBrackes(columnMeta);
                            columnMeta = columnMeta.replace(",", "");
                            ColumnPrefixFilter filter = new ColumnPrefixFilter(Bytes.toBytes(columnMeta));
                            filters.addFilter(filter);
                            System.out.println(filter);
                        }
                        //指定前缀的rowKey过滤器
                        else if(columnMeta.contains("PrefixFilter")){
                            columnMeta = columnMeta.replace(" ", "");
                            beginIndex = columnMeta.indexOf("PrefixFilter") + 12;
                            endIndex = getEndIndexOfQuot(beginIndex, columnMeta);
                            columnMeta = columnMeta.substring(beginIndex, endIndex);
                            columnMeta = getBetweenQuot(columnMeta);
                            columnMeta = getBetweenBrackes(columnMeta);
                            columnMeta = columnMeta.replace(",", "");
                            PrefixFilter filter = new PrefixFilter(Bytes.toBytes(columnMeta));
                            filters.addFilter(filter);
                            System.out.println(filter);
                        }
                        //按列查找，可以指定某一确定的列或列的范围。binary是确定的参数，substring是参数中含有的值。
                        else if(columnMeta.contains("QualifierFilter")){
                            columnMeta = columnMeta.replace(" ", "");
                            beginIndex = columnMeta.indexOf("QualifierFilter") + 15;
                            endIndex = getEndIndexOfBracket(beginIndex, columnMeta);
                            columnMeta = columnMeta.substring(beginIndex, endIndex);
                            columnMeta = getBetweenBrackes(columnMeta);
                            columnMeta = columnMeta.replace(",", "");
                            String[] splitedColumnMeta = columnMeta.split("\'");
                            String opSymbol = splitedColumnMeta[0];
                            String[]  splitedColumnMeta1 = splitedColumnMeta[1].split(":");
                            String binOrsub = splitedColumnMeta1[0];
                            String value = splitedColumnMeta1[1];
                            QualifierFilter qualifierFilter;

                            System.out.println(opSymbol);
                            System.out.println(binOrsub);
                            System.out.println(value);

                            //字段为binary
                            if(binOrsub.equals("binary")){
                                if(opSymbol.equals("<")){
                                    qualifierFilter = new QualifierFilter(CompareOperator.LESS, new BinaryComparator(Bytes.toBytes(value)));
                                }
                                else if(opSymbol.equals("<=")){
                                    qualifierFilter = new QualifierFilter(CompareOperator.LESS_OR_EQUAL, new BinaryComparator(Bytes.toBytes(value)));
                                }
                                else if(opSymbol.equals(">")){
                                    qualifierFilter = new QualifierFilter(CompareOperator.GREATER, new BinaryComparator(Bytes.toBytes(value)));
                                }
                                else if(opSymbol.equals(">=")){
                                    qualifierFilter = new QualifierFilter(CompareOperator.GREATER_OR_EQUAL, new BinaryComparator(Bytes.toBytes(value)));
                                }
                                else if(opSymbol.equals("=")){
                                    qualifierFilter = new QualifierFilter(CompareOperator.EQUAL, new BinaryComparator(Bytes.toBytes(value)));
                                }
                                else{
                                    throw new Exception("QualifierFilter:运算符字段错误");
                                }
                            }
                            //字段为substring
                            else if(binOrsub.equals("substring")){
                                if(opSymbol.equals("<")){
                                    qualifierFilter = new QualifierFilter(CompareOperator.LESS, new SubstringComparator(value));
                                }
                                else if(opSymbol.equals("<=")){
                                    qualifierFilter = new QualifierFilter(CompareOperator.LESS_OR_EQUAL, new SubstringComparator(value));
                                }
                                else if(opSymbol.equals(">")){
                                    qualifierFilter = new QualifierFilter(CompareOperator.GREATER, new SubstringComparator(value));
                                }
                                else if(opSymbol.equals(">=")){
                                    qualifierFilter = new QualifierFilter(CompareOperator.GREATER_OR_EQUAL, new SubstringComparator(value));
                                }
                                else if(opSymbol.equals("=")){
                                    qualifierFilter = new QualifierFilter(CompareOperator.EQUAL, new SubstringComparator(value));
                                }
                                else{
                                    throw new Exception("QualifierFilter:运算符字段错误");
                                }
                            }
                            else{
                                throw new Exception("QualifierFilter:binary/substring字段错误");
                            }
                            filters.addFilter(qualifierFilter);
                            System.out.println(qualifierFilter);
                        }
                        //按值查找，判断条件同上
                        else if(columnMeta.contains("ValueFilter")){
                            columnMeta = columnMeta.replace(" ", "");
                            beginIndex = columnMeta.indexOf("ValueFilter") + 11;
                            endIndex = getEndIndexOfBracket(beginIndex, columnMeta);
                            columnMeta = columnMeta.substring(beginIndex, endIndex);
                            columnMeta = getBetweenBrackes(columnMeta);
                            columnMeta = columnMeta.replace(",", "");
                            String[] splitedColumnMeta = columnMeta.split("\'");
                            String opSymbol = splitedColumnMeta[0];
                            String[]  splitedColumnMeta1 = splitedColumnMeta[1].split(":");
                            String binOrsub = splitedColumnMeta1[0];
                            String value = splitedColumnMeta1[1];
                            ValueFilter valueFilter;

                            System.out.println(opSymbol);
                            System.out.println(binOrsub);
                            System.out.println(value);

                            //字段为binary
                            if(binOrsub.equals("binary")){
                                if(opSymbol.equals("<")){
                                    valueFilter = new ValueFilter(CompareOperator.LESS, new BinaryComparator(Bytes.toBytes(value)));
                                }
                                else if(opSymbol.equals("<=")){
                                    valueFilter = new ValueFilter(CompareOperator.LESS_OR_EQUAL, new BinaryComparator(Bytes.toBytes(value)));
                                }
                                else if(opSymbol.equals(">")){
                                    valueFilter = new ValueFilter(CompareOperator.GREATER, new BinaryComparator(Bytes.toBytes(value)));
                                }
                                else if(opSymbol.equals(">=")){
                                    valueFilter = new ValueFilter(CompareOperator.GREATER_OR_EQUAL, new BinaryComparator(Bytes.toBytes(value)));
                                }
                                else if(opSymbol.equals("=")){
                                    valueFilter = new ValueFilter(CompareOperator.EQUAL, new BinaryComparator(Bytes.toBytes(value)));
                                }
                                else{
                                    throw new Exception("ValueFilter:运算符字段错误");
                                }
                            }
                            //字段为substring
                            else if(binOrsub.equals("substring")){
                                if(opSymbol.equals("<")){
                                    valueFilter = new ValueFilter(CompareOperator.LESS, new SubstringComparator(value));
                                }
                                else if(opSymbol.equals("<=")){
                                    valueFilter = new ValueFilter(CompareOperator.LESS_OR_EQUAL, new SubstringComparator(value));
                                }
                                else if(opSymbol.equals(">")){
                                    valueFilter = new ValueFilter(CompareOperator.GREATER, new SubstringComparator(value));
                                }
                                else if(opSymbol.equals(">=")){
                                    valueFilter = new ValueFilter(CompareOperator.GREATER_OR_EQUAL, new SubstringComparator(value));
                                }
                                else if(opSymbol.equals("=")){
                                    valueFilter = new ValueFilter(CompareOperator.EQUAL, new SubstringComparator(value));
                                }
                                else{
                                    throw new Exception("ValueFilter:运算符字段错误");
                                }
                            }
                            else{
                                throw new Exception("ValueFilter:binary/substring字段错误");
                            }
                            filters.addFilter(valueFilter);
                            System.out.println(valueFilter);
                        }
                        //按列簇查找，判断条件同上
                        else if(columnMeta.contains("FamilyFilter")){
                            columnMeta = columnMeta.replace(" ", "");
                            beginIndex = columnMeta.indexOf("FamilyFilter") + 12;
                            endIndex = getEndIndexOfBracket(beginIndex, columnMeta);
                            columnMeta = columnMeta.substring(beginIndex, endIndex);
                            columnMeta = getBetweenBrackes(columnMeta);
                            columnMeta = columnMeta.replace(",", "");
                            String[] splitedColumnMeta = columnMeta.split("\'");
                            String opSymbol = splitedColumnMeta[0];
                            String[]  splitedColumnMeta1 = splitedColumnMeta[1].split(":");
                            String binOrsub = splitedColumnMeta1[0];
                            String value = splitedColumnMeta1[1];
                            FamilyFilter familyFilter;

                            System.out.println(opSymbol);
                            System.out.println(binOrsub);
                            System.out.println(value);

                            //字段为binary
                            if(binOrsub.equals("binary")){
                                if(opSymbol.equals("<")){
                                    familyFilter = new FamilyFilter(CompareOperator.LESS, new BinaryComparator(Bytes.toBytes(value)));
                                }
                                else if(opSymbol.equals("<=")){
                                    familyFilter = new FamilyFilter(CompareOperator.LESS_OR_EQUAL, new BinaryComparator(Bytes.toBytes(value)));
                                }
                                else if(opSymbol.equals(">")){
                                    familyFilter = new FamilyFilter(CompareOperator.GREATER, new BinaryComparator(Bytes.toBytes(value)));
                                }
                                else if(opSymbol.equals(">=")){
                                    familyFilter = new FamilyFilter(CompareOperator.GREATER_OR_EQUAL, new BinaryComparator(Bytes.toBytes(value)));
                                }
                                else if(opSymbol.equals("=")){
                                    familyFilter = new FamilyFilter(CompareOperator.EQUAL, new BinaryComparator(Bytes.toBytes(value)));
                                }
                                else{
                                    throw new Exception("FamilyFilter:运算符字段错误");
                                }
                            }
                            //字段为substring
                            else if(binOrsub.equals("substring")){
                                if(opSymbol.equals("<")){
                                    familyFilter = new FamilyFilter(CompareOperator.LESS, new SubstringComparator(value));
                                }
                                else if(opSymbol.equals("<=")){
                                    familyFilter = new FamilyFilter(CompareOperator.LESS_OR_EQUAL, new SubstringComparator(value));
                                }
                                else if(opSymbol.equals(">")){
                                    familyFilter = new FamilyFilter(CompareOperator.GREATER, new SubstringComparator(value));
                                }
                                else if(opSymbol.equals(">=")){
                                    familyFilter = new FamilyFilter(CompareOperator.GREATER_OR_EQUAL, new SubstringComparator(value));
                                }
                                else if(opSymbol.equals("=")){
                                    familyFilter = new FamilyFilter(CompareOperator.EQUAL, new SubstringComparator(value));
                                }
                                else{
                                    throw new Exception("FamilyFilter:运算符字段错误");
                                }
                            }
                            else{
                                throw new Exception("FamilyFilter:binary/substring字段错误");
                            }
                            filters.addFilter(familyFilter);
                            System.out.println(familyFilter);
                        }
                        //按时间过滤
                        else if(columnMeta.contains("TimestampsFilter")){
                            columnMeta = columnMeta.replace(" ", "");
                            beginIndex = columnMeta.indexOf("TimestampsFilter") + 16;
                            endIndex = getEndIndexOfSqrBracket(beginIndex, columnMeta);
                            columnMeta = columnMeta.substring(beginIndex, endIndex);
                            columnMeta = getBetweenBrackes(columnMeta);
                            String[] timeStamps = columnMeta.split(",");
                            long minTimeStamp = (long)Integer.valueOf(timeStamps[0]);
                            long maxTimeStamp = (long)Integer.valueOf(timeStamps[1]);
                            TimestampsFilter filter = new TimestampsFilter(new ArrayList<Long>(){{
                                add(minTimeStamp);
                                add(maxTimeStamp);
                            }});
                            filters.addFilter(filter);
                            System.out.println(filter);
                        }
                        else{
                            throw new Exception("Filter缺失");
                        }
                        //FirstKeyOnlyFilter:一个rowkey可以有多个version,同一个rowkey的同一个column也会有多个的值, 只拿出key中的第一个column的第一个version
                        if(columnMeta.contains("FirstKeyOnlyFilter()")){
                            filters.addFilter(new FirstKeyOnlyFilter());
                        }
                        //KeyOnlyFilter: 只要key,不要value
                        if(columnMeta.contains("KeyOnlyFilter()")){
                            filters.addFilter(new KeyOnlyFilter());
                        }
                    }
//                    scan.setFilter(filters);
                    finalFilter.addFilter(filters);
                    System.out.println(scan);
                }
                scan.setFilter(finalFilter);
            }
            //是否反转 ****REVERSED****
            if(meta.contains("REVERSED")){
                String newMeta = meta.replace(" ", "");
                int beginIndex = newMeta.indexOf("REVERSED") + 10;
                int endIndex = getEndIndexOfBoolean(beginIndex, newMeta);
                newMeta = newMeta.substring(beginIndex, endIndex);
                if(newMeta.equals("true")){
                    scan.setReversed(true);
                }
                else if(newMeta.equals("false")){
                    scan.setReversed(false);
                }
                else{
                    throw new Exception("判断语句错误，请检查");
                }
                System.out.println(scan);
            }
            //是否返回所有单元格(包括删除标记和未收集的已删除单元格) ****RAW****
            if(meta.contains("RAW")){
                String newMeta = meta.replace(" ", "");
                int beginIndex = newMeta.indexOf("RAW") + 5;
                int endIndex = getEndIndexOfBoolean(beginIndex, newMeta);
                newMeta = newMeta.substring(beginIndex, endIndex);
                if(newMeta.equals("true")){
                    scan.setRaw(true);
                }
                else if(newMeta.equals("false")){
                    scan.setRaw(false);
                }
                else{
                    throw new Exception("判断语句错误，请检查");
                }
                System.out.println(scan);
            }
            //查询返回条数 ****LIMIT****
            if(meta.contains("LIMIT")){
                String newMeta = meta.replace(" ", "");
                int beginIndex = newMeta.indexOf("LIMIT") + 7;
                int endIndex = getEndIndexOfInt(beginIndex, newMeta);
                newMeta = newMeta.substring(beginIndex, endIndex);
                int limitPage = Integer.valueOf(newMeta);
                System.out.println(limitPage);
                PageFilter pageFilter = new PageFilter(limitPage);
                scan.setFilter(pageFilter);
                System.out.println(scan);
            }
            //是否返回全部指标 ****ALL_METRICS****
            //------------<这条不确定函数用的对不对>------------
            if(meta.contains("ALL_METRICS")){
                String newMeta = meta.replace(" ", "");
                int beginIndex = newMeta.indexOf("ALL_METRICS") + 13;
                int endIndex = getEndIndexOfBoolean(beginIndex, newMeta);
                newMeta = newMeta.substring(beginIndex, endIndex);
                if(newMeta.equals("true")){
                    scan.setScanMetricsEnabled(true);//------------<这条不确定函数用的对不对>------------
                }
                else if(newMeta.equals("false")){
                    scan.setScanMetricsEnabled(false);//------------<这条不确定函数用的对不对>------------
                }
                else{
                    throw new Exception("判断语句错误，请检查");
                }
                System.out.println(scan);
            }
            //在scan的字段中取columnFamilies和columnName值
            ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
            int begin = scan.toString().indexOf("families") + 11;
            int end = scan.toString().indexOf("caching") - 3;
            if(begin != end){
                String subScan = scan.toString().substring(begin, end);
                subScan = subScan.replace("\",\"","\";\"");
                subScan = subScan.replace("[", "").replace("]" , "");
                String[] colFamiliesMetas = subScan.split(",");
                for(String colFamiliesMeta : colFamiliesMetas){
                    String[] colMeta = colFamiliesMeta.split(":");
                    String colFamilyName = colMeta[0];
                    String[] colNames = colMeta[1].split(";");
                    for(String colName : colNames){
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(colFamilyName, colName);
                        list.add(map);
                    }
                }
            }
            columnFamilies.put(tableNameStr, list);
            //shellMeta.set
            shellMeta.setSqlType(sqlType);
            shellMeta.setScan(scan);
            shellMeta.setTableName(new HashMap<String, String>(){{
                put(tableNameStr, tableNameStr);
            }});
            shellMeta.setColumnFamily(columnFamilies);
        }
        else{
//            throw new Exception("语句出错，请仔细检查");
            //表名
            String tableNameStr = getBetweenQuot(shell);
            tableName.put(tableNameStr, tableNameStr);
            //Scan类
            Scan scan = new Scan();
            shellMeta.setTableName(new HashMap<String, String>(){{
                put(tableNameStr, tableNameStr);
            }});
            shellMeta.setSqlType(sqlType);
            shellMeta.setScan(scan);
            ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
            columnFamilies.put(tableNameStr, list);
            shellMeta.setColumnFamily(columnFamilies);
        }
        return shellMeta;
    }

    private HBaseShellMeta getTableStatement(SqlTypeEnum sqlType, String shell) throws Exception {
        HBaseShellMeta shellMeta = new HBaseShellMeta();
        HashMap<String, String> tableName = new HashMap<String, String>();
        HashMap<String, ArrayList<Map<String, String>>> columnFamilies = new HashMap<String, ArrayList<Map<String, String>>>();
        shell = subSuffix(shell);//去除操作类型
        // Get类
        Get get = null;
        if (shell.contains("{")) {
            String[] metaList = shell.split("\\{");
            if (metaList.length > 2) throw new Exception("语句出错，请仔细检查");
            String[] str = metaList[0].split(",");
            //表名
            String tableNameStr = getTableName(str[0]);
            tableName.put(tableNameStr, tableNameStr);
            //行键
            String rowKeyStr = getTableName(str[1]);
            get = new Get(Bytes.toBytes(rowKeyStr));
            //右侧{}内的字段
            String meta = filterRight(metaList[1]);
            //查找所有列

            //按列查找    ****COLUMN=>****
            //包含获取多个列           ****COLUMN => ['a', 'b', 'c']****
            if (meta.contains("COLUMN")) {
                //切割出COLUMNS=>后面的值
                String newMeta = meta.replace(" ", "");
                int beginIndex = 0;
                int endIndex = 0;
                //单列，无'[]'
                if (newMeta.charAt(newMeta.indexOf("COLUMN") + 8) != '[') {
                    beginIndex = newMeta.indexOf("COLUMN") + 8;
                    endIndex = getEndIndexOfQuot(beginIndex, newMeta);
                }
                //多列,含'[]'
                else {
                    beginIndex = newMeta.indexOf("COLUMN") + 10;
                    endIndex = getEndIndexOfSqrBracket(beginIndex, newMeta);
                }
                newMeta = newMeta.substring(beginIndex, endIndex);
                String[] columnMetaList = newMeta.split(",");
                for (String columnMetas : columnMetaList) {
                    columnMetas = getBetweenQuot(columnMetas);
                    String[] columnMeta = columnMetas.split(":");
                    get.addColumn(Bytes.toBytes(columnMeta[0]), Bytes.toBytes(columnMeta[1]));
                }
            }else{
                throw new Exception("语句出错，请仔细检查");
            }
            //查找n个版本        ****VERSIONS=>n****
            if (meta.contains("VERSIONS")) {
                //切割出VERSIONS=>后面的值
                String newMeta = meta.replace(" ", "");
                int beginIndex = 0;
                int endIndex = newMeta.length();
                beginIndex = newMeta.indexOf("VERSIONS") + 10;
                newMeta = newMeta.substring(beginIndex, endIndex);
                int n = Integer.valueOf(newMeta).intValue();
                get.setMaxVersions(n);
            }
            //按时间查找    ****TIMERANGE=>****
            if (meta.contains("TIMERANGE")) {
                //切割出TIMERANGE=>后面的值
                String newMeta = meta.replace(" ", "");
                int beginIndex = newMeta.indexOf("TIMERANGE") + 12;
                int endIndex = getEndIndexOfSqrBracket(beginIndex, newMeta);
                String[] timeStamps = newMeta.substring(beginIndex, endIndex).split(",");
                String minTimeStr = timeStamps[0];
                String maxTimeStr = timeStamps[1];
                long minTime = (long) Integer.valueOf(minTimeStr);
                long maxTime = (long) Integer.valueOf(maxTimeStr);
                get.setTimeRange(minTime, maxTime);
                System.out.println(get);
            }
            //按时间戳查找        ****TIMESTAMP=>****
            if (meta.contains("TIMESTAMP")) {
                //切割出TIMESTAMP=>后面的值
                String newMeta = meta.replace(" ", "");
                int beginIndex = 0;
                int endIndex = newMeta.length();
                beginIndex = newMeta.indexOf("TIMESTAMP") + 11;
                newMeta = newMeta.substring(beginIndex, endIndex);
                int n = Integer.parseInt(newMeta);
                get.setTimestamp(n);
            }
            //过滤查找    ****FILTER=>****
            if(meta.contains("FILTER")) {
                String newMeta = meta.replace(" ", "");
                int beginIndex = newMeta.indexOf("FILTER") + 8;
                int endIndex = getEndIndexOfDoubleQuot(beginIndex, newMeta);
                newMeta = newMeta.substring(beginIndex, endIndex);
                newMeta = getBetweenDoubleQuot(newMeta);
                //以AND切割字段
                String[] columnMetaList_AND = newMeta.split("AND");
                for (String columnMetaList : columnMetaList_AND) {
                    //多值过滤
                    FilterList filters = new FilterList(FilterList.Operator.MUST_PASS_ALL);
                    //以OR切割字段
                    String[] columnMetaList_OR = columnMetaList.split("OR");
                    for (String columnMeta : columnMetaList_OR) {
                        //指定前缀的列过滤器
                        if (columnMeta.contains("ColumnPrefixFilter")) {
                            columnMeta = columnMeta.replace(" ", "");
                            beginIndex = columnMeta.indexOf("ColumnPrefixFilter") + 18;
                            endIndex = getEndIndexOfQuot(beginIndex, columnMeta);
                            columnMeta = columnMeta.substring(beginIndex, endIndex);
                            columnMeta = getBetweenQuot(columnMeta);
                            columnMeta = getBetweenBrackes(columnMeta);
                            columnMeta = columnMeta.replace(",", "");
                            ColumnPrefixFilter filter = new ColumnPrefixFilter(Bytes.toBytes(columnMeta));
                            filters.addFilter(filter);
                            System.out.println(filter);
                        }
                        //指定前缀的rowKey过滤器
                        else if (columnMeta.contains("PrefixFilter")) {
                            columnMeta = columnMeta.replace(" ", "");
                            beginIndex = columnMeta.indexOf("PrefixFilter") + 12;
                            endIndex = getEndIndexOfQuot(beginIndex, columnMeta);
                            columnMeta = columnMeta.substring(beginIndex, endIndex);
                            columnMeta = getBetweenQuot(columnMeta);
                            columnMeta = getBetweenBrackes(columnMeta);
                            columnMeta = columnMeta.replace(",", "");
                            PrefixFilter filter = new PrefixFilter(Bytes.toBytes(columnMeta));
                            filters.addFilter(filter);
                            System.out.println(filter);
                        }
                        //按列查找，可以指定某一确定的列或列的范围。binary是确定的参数，substring是参数中含有的值。
                        else if (columnMeta.contains("QualifierFilter")) {
                            columnMeta = columnMeta.replace(" ", "");
                            beginIndex = columnMeta.indexOf("QualifierFilter") + 15;
                            endIndex = getEndIndexOfBracket(beginIndex, columnMeta);
                            columnMeta = columnMeta.substring(beginIndex, endIndex);
                            columnMeta = getBetweenBrackes(columnMeta);
                            columnMeta = columnMeta.replace(",", "");
                            String[] splitedColumnMeta = columnMeta.split("\'");
                            String opSymbol = splitedColumnMeta[0];
                            String[] splitedColumnMeta1 = splitedColumnMeta[1].split(":");
                            String binOrsub = splitedColumnMeta1[0];
                            String value = splitedColumnMeta1[1];
                            QualifierFilter qualifierFilter;

                            System.out.println(opSymbol);
                            System.out.println(binOrsub);
                            System.out.println(value);

                            //字段为binary
                            if (binOrsub.equals("binary")) {
                                if (opSymbol.equals("<")) {
                                    qualifierFilter = new QualifierFilter(CompareOperator.LESS, new BinaryComparator(Bytes.toBytes(value)));
                                } else if (opSymbol.equals("<=")) {
                                    qualifierFilter = new QualifierFilter(CompareOperator.LESS_OR_EQUAL, new BinaryComparator(Bytes.toBytes(value)));
                                } else if (opSymbol.equals(">")) {
                                    qualifierFilter = new QualifierFilter(CompareOperator.GREATER, new BinaryComparator(Bytes.toBytes(value)));
                                } else if (opSymbol.equals(">=")) {
                                    qualifierFilter = new QualifierFilter(CompareOperator.GREATER_OR_EQUAL, new BinaryComparator(Bytes.toBytes(value)));
                                } else if (opSymbol.equals("=")) {
                                    qualifierFilter = new QualifierFilter(CompareOperator.EQUAL, new BinaryComparator(Bytes.toBytes(value)));
                                } else {
                                    throw new Exception("QualifierFilter:运算符字段错误");
                                }
                            }
                            //字段为substring
                            else if (binOrsub.equals("substring")) {
                                if (opSymbol.equals("<")) {
                                    qualifierFilter = new QualifierFilter(CompareOperator.LESS, new SubstringComparator(value));
                                } else if (opSymbol.equals("<=")) {
                                    qualifierFilter = new QualifierFilter(CompareOperator.LESS_OR_EQUAL, new SubstringComparator(value));
                                } else if (opSymbol.equals(">")) {
                                    qualifierFilter = new QualifierFilter(CompareOperator.GREATER, new SubstringComparator(value));
                                } else if (opSymbol.equals(">=")) {
                                    qualifierFilter = new QualifierFilter(CompareOperator.GREATER_OR_EQUAL, new SubstringComparator(value));
                                } else if (opSymbol.equals("=")) {
                                    qualifierFilter = new QualifierFilter(CompareOperator.EQUAL, new SubstringComparator(value));
                                } else {
                                    throw new Exception("QualifierFilter:运算符字段错误");
                                }
                            } else {
                                throw new Exception("QualifierFilter:binary/substring字段错误");
                            }
                            filters.addFilter(qualifierFilter);
                            System.out.println(qualifierFilter);
                        }
                        //按值查找，判断条件同上
                        else if (columnMeta.contains("ValueFilter")) {
                            columnMeta = columnMeta.replace(" ", "");
                            beginIndex = columnMeta.indexOf("ValueFilter") + 11;
                            endIndex = getEndIndexOfBracket(beginIndex, columnMeta);
                            columnMeta = columnMeta.substring(beginIndex, endIndex);
                            columnMeta = getBetweenBrackes(columnMeta);
                            columnMeta = columnMeta.replace(",", "");
                            String[] splitedColumnMeta = columnMeta.split("\'");
                            String opSymbol = splitedColumnMeta[0];
                            String[] splitedColumnMeta1 = splitedColumnMeta[1].split(":");
                            String binOrsub = splitedColumnMeta1[0];
                            String value = splitedColumnMeta1[1];
                            ValueFilter valueFilter;

                            System.out.println(opSymbol);
                            System.out.println(binOrsub);
                            System.out.println(value);

                            //字段为binary
                            if (binOrsub.equals("binary")) {
                                if (opSymbol.equals("<")) {
                                    valueFilter = new ValueFilter(CompareOperator.LESS, new BinaryComparator(Bytes.toBytes(value)));
                                } else if (opSymbol.equals("<=")) {
                                    valueFilter = new ValueFilter(CompareOperator.LESS_OR_EQUAL, new BinaryComparator(Bytes.toBytes(value)));
                                } else if (opSymbol.equals(">")) {
                                    valueFilter = new ValueFilter(CompareOperator.GREATER, new BinaryComparator(Bytes.toBytes(value)));
                                } else if (opSymbol.equals(">=")) {
                                    valueFilter = new ValueFilter(CompareOperator.GREATER_OR_EQUAL, new BinaryComparator(Bytes.toBytes(value)));
                                } else if (opSymbol.equals("=")) {
                                    valueFilter = new ValueFilter(CompareOperator.EQUAL, new BinaryComparator(Bytes.toBytes(value)));
                                } else {
                                    throw new Exception("ValueFilter:运算符字段错误");
                                }
                            }
                            //字段为substring
                            else if (binOrsub.equals("substring")) {
                                if (opSymbol.equals("<")) {
                                    valueFilter = new ValueFilter(CompareOperator.LESS, new SubstringComparator(value));
                                } else if (opSymbol.equals("<=")) {
                                    valueFilter = new ValueFilter(CompareOperator.LESS_OR_EQUAL, new SubstringComparator(value));
                                } else if (opSymbol.equals(">")) {
                                    valueFilter = new ValueFilter(CompareOperator.GREATER, new SubstringComparator(value));
                                } else if (opSymbol.equals(">=")) {
                                    valueFilter = new ValueFilter(CompareOperator.GREATER_OR_EQUAL, new SubstringComparator(value));
                                } else if (opSymbol.equals("=")) {
                                    valueFilter = new ValueFilter(CompareOperator.EQUAL, new SubstringComparator(value));
                                } else {
                                    throw new Exception("ValueFilter:运算符字段错误");
                                }
                            } else {
                                throw new Exception("ValueFilter:binary/substring字段错误");
                            }
                            filters.addFilter(valueFilter);
                            System.out.println(valueFilter);
                        }
                        //按列簇查找，判断条件同上
                        else if (columnMeta.contains("FamilyFilter")) {
                            columnMeta = columnMeta.replace(" ", "");
                            beginIndex = columnMeta.indexOf("FamilyFilter") + 12;
                            endIndex = getEndIndexOfBracket(beginIndex, columnMeta);
                            columnMeta = columnMeta.substring(beginIndex, endIndex);
                            columnMeta = getBetweenBrackes(columnMeta);
                            columnMeta = columnMeta.replace(",", "");
                            String[] splitedColumnMeta = columnMeta.split("\'");
                            String opSymbol = splitedColumnMeta[0];
                            String[] splitedColumnMeta1 = splitedColumnMeta[1].split(":");
                            String binOrsub = splitedColumnMeta1[0];
                            String value = splitedColumnMeta1[1];
                            FamilyFilter familyFilter;

                            System.out.println(opSymbol);
                            System.out.println(binOrsub);
                            System.out.println(value);

                            //字段为binary
                            if (binOrsub.equals("binary")) {
                                if (opSymbol.equals("<")) {
                                    familyFilter = new FamilyFilter(CompareOperator.LESS, new BinaryComparator(Bytes.toBytes(value)));
                                } else if (opSymbol.equals("<=")) {
                                    familyFilter = new FamilyFilter(CompareOperator.LESS_OR_EQUAL, new BinaryComparator(Bytes.toBytes(value)));
                                } else if (opSymbol.equals(">")) {
                                    familyFilter = new FamilyFilter(CompareOperator.GREATER, new BinaryComparator(Bytes.toBytes(value)));
                                } else if (opSymbol.equals(">=")) {
                                    familyFilter = new FamilyFilter(CompareOperator.GREATER_OR_EQUAL, new BinaryComparator(Bytes.toBytes(value)));
                                } else if (opSymbol.equals("=")) {
                                    familyFilter = new FamilyFilter(CompareOperator.EQUAL, new BinaryComparator(Bytes.toBytes(value)));
                                } else {
                                    throw new Exception("FamilyFilter:运算符字段错误");
                                }
                            }
                            //字段为substring
                            else if (binOrsub.equals("substring")) {
                                if (opSymbol.equals("<")) {
                                    familyFilter = new FamilyFilter(CompareOperator.LESS, new SubstringComparator(value));
                                } else if (opSymbol.equals("<=")) {
                                    familyFilter = new FamilyFilter(CompareOperator.LESS_OR_EQUAL, new SubstringComparator(value));
                                } else if (opSymbol.equals(">")) {
                                    familyFilter = new FamilyFilter(CompareOperator.GREATER, new SubstringComparator(value));
                                } else if (opSymbol.equals(">=")) {
                                    familyFilter = new FamilyFilter(CompareOperator.GREATER_OR_EQUAL, new SubstringComparator(value));
                                } else if (opSymbol.equals("=")) {
                                    familyFilter = new FamilyFilter(CompareOperator.EQUAL, new SubstringComparator(value));
                                } else {
                                    throw new Exception("FamilyFilter:运算符字段错误");
                                }
                            } else {
                                throw new Exception("FamilyFilter:binary/substring字段错误");
                            }
                            filters.addFilter(familyFilter);
                            System.out.println(familyFilter);
                        }
                        //按时间过滤
                        else if (columnMeta.contains("TimestampsFilter")) {
                            columnMeta = columnMeta.replace(" ", "");
                            beginIndex = columnMeta.indexOf("TimestampsFilter") + 16;
                            endIndex = getEndIndexOfSqrBracket(beginIndex, columnMeta);
                            columnMeta = columnMeta.substring(beginIndex, endIndex);
                            columnMeta = getBetweenBrackes(columnMeta);
                            String[] timeStamps = columnMeta.split(",");
                            long minTimeStamp = (long) Integer.valueOf(timeStamps[0]);
                            long maxTimeStamp = (long) Integer.valueOf(timeStamps[1]);
                            TimestampsFilter filter = new TimestampsFilter(new ArrayList<Long>() {{
                                add(minTimeStamp);
                                add(maxTimeStamp);
                            }});
                            filters.addFilter(filter);
                            System.out.println(filter);
                        } else {
                            throw new Exception("Filter缺失");
                        }
                        //FirstKeyOnlyFilter:一个rowkey可以有多个version,同一个rowkey的同一个column也会有多个的值, 只拿出key中的第一个column的第一个version
                        if (columnMeta.contains("FirstKeyOnlyFilter()")) {
                            filters.addFilter(new FirstKeyOnlyFilter());
                        }
                        //KeyOnlyFilter: 只要key,不要value
                        if (columnMeta.contains("KeyOnlyFilter()")) {
                            filters.addFilter(new KeyOnlyFilter());
                        }
                    }
                    get.setFilter(filters);
                    System.out.println(get);
                }
            }

            ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
            int begin = get.toString().indexOf("families") + 11;
            int end = get.toString().indexOf("maxVersions") - 3;
            if (begin != end) {
                String subGet = get.toString().substring(begin, end);
                subGet = subGet.replace("\",\"", "\";\"");
                subGet = subGet.replace("[", "").replace("]", "");
                String[] colFamiliesMetas = subGet.split(",");
                for (String colFamiliesMeta : colFamiliesMetas) {
                    String[] colMeta = colFamiliesMeta.split(":");
                    String colFamilyName = colMeta[0].replace("\"", "");
                    String[] colNames = colMeta[1].split(";");
                    for (String colName : colNames) {
                        colName = colName.replace("\"","");
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(colFamilyName, colName);
                        list.add(map);
                    }
                }
            }
            columnFamilies.put(tableNameStr, list);
            shellMeta.setGet(get);
            shellMeta.setSqlType(sqlType);
            shellMeta.setTableName(tableName);
            shellMeta.setColumnFamily(columnFamilies);
        }else {
            String suffix = subSuffix(shell);
            String[] meta = suffix.split(",");
            //表名
            String tableNameStr = getTableName(meta[0]);
            tableName.put(tableNameStr, tableNameStr);
            //行键
            String rowKeyStr = getTableName(meta[1]);
            get = new Get(Bytes.toBytes(rowKeyStr));
            shellMeta.setGet(get);
            shellMeta.setSqlType(sqlType);
            shellMeta.setTableName(tableName);
//            shellMeta.setColumnFamily(columnFamilies);
        }
        return shellMeta;
    }

    private HBaseShellMeta truncateTableStatement(SqlTypeEnum sqlType, String shell) throws Exception{
        // 初始化
        HBaseShellMeta shellMeta = new HBaseShellMeta();
        HashMap<String, String> tableName = new HashMap<String, String>();
        // 解析
        shell = shell.replace(" ", ",");
        String[] splitedShell = shell.split(",");
        String[] splitedNameSpace = splitedShell[1].split(":");
        String tableNameStr = filterChar(splitedNameSpace[1]);
        tableName.put(tableNameStr, tableNameStr);
        // 赋值
        shellMeta.setSqlType(sqlType);
        shellMeta.setTableName(tableName);
        return shellMeta;
    }

    private HBaseShellMeta grantStatement(SqlTypeEnum sqlType, String shell) throws Exception{
        HBaseShellMeta shellMeta = new HBaseShellMeta();
        //拆分
        shell = shell.replace(" ", ",");
        String[] splitedShell = shell.split(",");
        //排错
        if(splitedShell.length < 3) throw new Exception("语句出错请检查");
        if(splitedShell.length > 4) throw new Exception("仅限授权单空间名/表名");
        //初始化，赋值
        String userName = filterChar(splitedShell[1]);;
        char[] actions = filterChar(splitedShell[2]).toCharArray();;
        String nameSpace;
        String tableNameStr;
        HashMap<String, String> tableName = new HashMap<String, String>();
        //长度<4的情况是没有指定namespace名或table名，则是给用户全局权限，如：grant 'root','RWXCA'，设grantAll为true
        //长度=4的情况，设grantAll为false
        if(splitedShell.length == 4){
            shellMeta.setChangeForALl(false);
            String nsOrTb = filterChar(splitedShell[3]);
            //最后字段含"@"，说明给用户的是namespace的权限
            if(nsOrTb.contains("@")){
                nameSpace = nsOrTb.substring(1);
                shellMeta.setNsOrTb(true);
                shellMeta.setNameSpace(nameSpace);
            }
            //不含"@"，说明给用户的是table的权限
            else{
                tableNameStr = nsOrTb;
                shellMeta.setNsOrTb(false);
                tableName.put(tableNameStr, tableNameStr);
                shellMeta.setTableName(tableName);
            }
        }
        else{
            shellMeta.setChangeForALl(true);
        }
        shellMeta.setUserName(userName);
        shellMeta.setActions(actions);
        return shellMeta;
    }

    private HBaseShellMeta revokeStatement(SqlTypeEnum sqlType, String shell) throws Exception{
        HBaseShellMeta shellMeta = new HBaseShellMeta();
        //拆分
        shell = shell.replace(" ", ",");
        String[] splitedShell = shell.split(",");
        //排错
        if(splitedShell.length < 2) throw new Exception("语句出错请检查");
        if(splitedShell.length > 3) throw new Exception("仅限撤回单空间名/表名的权限");
        //初始化，赋值
        String userName = filterChar(splitedShell[1]);
        String nameSpace;
        String tableNameStr;
        HashMap<String, String> tableName = new HashMap<String, String>();
        //同grant
        if(splitedShell.length == 3){
            shellMeta.setChangeForALl(false);
            String nsOrTb = filterChar(splitedShell[2]);
            //最后字段含"@"，说明撤回用户的是namespace的权限
            if(nsOrTb.contains("@")){
                nameSpace = nsOrTb.substring(1);
                shellMeta.setNsOrTb(true);
                shellMeta.setNameSpace(nameSpace);
            }
            //不含"@"，说明撤回用户的是table的权限
            else{
                tableNameStr = nsOrTb;
                shellMeta.setNsOrTb(false);
                tableName.put(tableNameStr, tableNameStr);
                shellMeta.setTableName(tableName);
            }
        }
        else{
            shellMeta.setChangeForALl(true);
        }
        shellMeta.setUserName(userName);
        return shellMeta;
    }

    private HBaseShellMeta createNameSpaceStatement(SqlTypeEnum sqlType, String shell) throws Exception{
        HBaseShellMeta shellMeta = new HBaseShellMeta();
        shell = shell.replace(" ", ",");
        String[] splitedShell = shell.split(",");
        String nameSpace = filterChar(splitedShell[1]);
        shellMeta.setSqlType(sqlType);
        System.out.println(nameSpace);
        shellMeta.setNameSpace(nameSpace);
        return shellMeta;
    }

    private HBaseShellMeta dropNameSpaceStatement(SqlTypeEnum sqlType, String shell) throws Exception{
        HBaseShellMeta shellMeta = new HBaseShellMeta();
        shell = shell.replace(" ", ",");
        String[] splitedShell = shell.split(",");
        String nameSpace = filterChar(splitedShell[1]);
        shellMeta.setSqlType(sqlType);
        shellMeta.setNameSpace(nameSpace);
        return shellMeta;
    }
}
