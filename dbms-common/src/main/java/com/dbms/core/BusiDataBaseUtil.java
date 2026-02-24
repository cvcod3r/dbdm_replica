package com.dbms.core;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSON;
import com.dbms.domain.dbase.MetaInfo;
import com.dbms.entity.DbaseEntity;
import com.dbms.utils.CryptoUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * 业务数据库操作工具
 *
 */
public class BusiDataBaseUtil
{
    private static final Logger logger = LoggerFactory.getLogger(BusiDataBaseUtil.class);

    private DbaseEntity dbaseEntity;

    private String schemaName;

    public DbaseEntity getDbaseEntity() {
        return dbaseEntity;
    }

    public void setDbaseEntity(DbaseEntity dbaseEntity) {
        String pw = CryptoUtil.decode(dbaseEntity.getPassword());
        if (pw.contains("`"))
        {
            pw = StringUtils.substringAfter(pw, "`");
        }
        dbaseEntity.setPassword(pw);
        this.dbaseEntity = dbaseEntity;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public static final Map<String,Connection> connMap = new ConcurrentHashMap<>();

    public void checkConnection() {
        System.out.println("Check database connection is valid or closed or alive!!!");
        for (String key:connMap.keySet()){
            Connection connection = connMap.getOrDefault(key, null);
            try {
                if (connection.isClosed()){
                    System.out.println("connection died:" + key);
                    connMap.remove(key);
                } else if (!connection.isValid(2)){
                    System.out.println("connection died:" + key);
                    connMap.remove(key);
                } else {
                    System.out.println("connection alive:" + key);
                }
            } catch (SQLException e) {

            }
        }
    }

    public BusiDataBaseUtil(DbaseEntity dbaseEntity)
    {
        String pw = CryptoUtil.decode(dbaseEntity.getPassword());
        if (pw.contains("`"))
        {
            pw = StringUtils.substringAfter(pw, "`");
        }
        this.dbaseEntity = dbaseEntity.deepClone();
        this.dbaseEntity.setPassword(pw);
    }

    public BusiDataBaseUtil(DbaseEntity dbaseEntity, String schemaName)
    {
        this.schemaName = schemaName;
        String pw = CryptoUtil.decode(dbaseEntity.getPassword());
        if (pw.contains("`"))
        {
            pw = StringUtils.substringAfter(pw, "`");
        }
        String dir = "/";
        if (dbaseEntity.getDbType().equals("DM") || dbaseEntity.getDbType().equals("Oracle")){
            dir = ":";
        } else if (dbaseEntity.getDbType().equals("MSSQL")){
            dir = ";database=";
        }
        this.dbaseEntity = dbaseEntity.deepClone();
        this.dbaseEntity.setPassword(pw);
        if (dbaseEntity.getDbType().equals("GBase")||dbaseEntity.getDbType().equals("Oracle")||dbaseEntity.getDbType().equals("ElasticSearch")){

        } else {
            this.dbaseEntity.setUrl(dbaseEntity.getUrl() + dir + schemaName);
        }
    }

    private Connection getConnection()
    {
        try
        {
            Class.forName(this.dbaseEntity.getDbDriver());
            return DriverManager.getConnection(this.dbaseEntity.getUrl(), this.dbaseEntity.getUsername(), this.dbaseEntity.getPassword());
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    private Connection getConnection(String key)
    {
        try
        {
            Connection currentConn = connMap.getOrDefault(key,null);
            System.out.println("getConn:" + currentConn);
//            currentConn.setNetworkTimeout();
            if (currentConn == null){
                Class.forName(this.dbaseEntity.getDbDriver());
                currentConn = DriverManager.getConnection(this.dbaseEntity.getUrl(), this.dbaseEntity.getUsername(), this.dbaseEntity.getPassword());
                currentConn.setAutoCommit(false);
                connMap.put(key,currentConn);
            }else if(currentConn.isClosed() ){
                connMap.remove(key);
                Class.forName(this.dbaseEntity.getDbDriver());
                currentConn = DriverManager.getConnection(this.dbaseEntity.getUrl(), this.dbaseEntity.getUsername(), this.dbaseEntity.getPassword());
                System.out.println("closed,getConn:" + currentConn);
                currentConn.setAutoCommit(false);
                connMap.put(key,currentConn);
            }else if (!currentConn.isValid(2)){
                connMap.remove(key);
                Class.forName(this.dbaseEntity.getDbDriver());
                currentConn = DriverManager.getConnection(this.dbaseEntity.getUrl(), this.dbaseEntity.getUsername(), this.dbaseEntity.getPassword());
                currentConn.setAutoCommit(false);
                System.out.println("invalid,getConn:" + currentConn);
                connMap.put(key,currentConn);
            }
            return currentConn;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public void closeConn(String key) throws Exception {
        Connection conn = getConnection(key);
        try {
            assert conn != null;
            conn.close();
            connMap.remove(key);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
    }

    public void startTransaction(String key) throws Exception {
        Connection conn = getConnection(key);
        System.out.println("currentConn:"+conn);
        try {
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            conn.rollback();
            logger.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
    }

    public void commit(String key) throws Exception {
        Connection conn = getConnection(key);
        try {
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            logger.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
    }

    public void rollback(String key) throws Exception {
        Connection conn = getConnection(key);
        try {
            conn.rollback();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
    }

    public List<Map<String, Object>> executeSQL(String sql) throws SQLException {
        // 连接
        Connection conn = null;
        // sql安全接口
        Statement statement = null;
        // 结果集
        ResultSet rs = null;
        // 数据库元数据
        ResultSetMetaData rsmd = null;
        int maxSize = -1;
        String[] fields = null;
        List<String> times = new ArrayList<>();
        List<String> clob = new ArrayList<>();
        List<String> binary = new ArrayList<>();
        List<Map<String, Object>> results = new ArrayList<>();
        conn = getConnection();
        statement = conn.createStatement();
        rs = statement.executeQuery(sql);
        rsmd = rs.getMetaData();
        maxSize = rsmd.getColumnCount();
        fields = new String[maxSize];
        for (int i = 0; i < maxSize; i++)
        {
            fields[i] = rsmd.getColumnLabel(i + 1);
            if (("java.sql.Timestamp".equals(rsmd.getColumnClassName(i + 1))) || ("oracle.sql.TIMESTAMP".equals(rsmd.getColumnClassName(i + 1))))
            {
                times.add(fields[i]);
            }
            if (("oracle.jdbc.OracleClob".equals(rsmd.getColumnClassName(i + 1))) || ("oracle.jdbc.OracleBlob".equals(rsmd.getColumnClassName(i + 1))))
            {
                clob.add(fields[i]);
            }
            if ("[B".equals(rsmd.getColumnClassName(i + 1)))
            {
                binary.add(fields[i]);
            }
        }
        Map<String, Object> result = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while (rs.next())
        {
            System.out.println(rs.getString(1));
            result = new LinkedHashMap<>();
            for (int i = 0; i < maxSize; i++)
            {
                Object value= rs.getString(1);
//                Object value = times.contains(fields[i]) ? rs.getTimestamp(fields[i]) : rs.getString(1);
//                if ((times.contains(fields[i])) && (value != null))
//                {
//                    value = sdf.format(value);
//                }
//                if ((clob.contains(fields[i])) && (value != null))
//                {
//                    value = "(Blob)";
//                }
//                if ((binary.contains(fields[i])) && (value != null))
//                {
//                    value = new String((byte[])value);
//                }
                result.put(fields[i], value);
            }
            results.add(result);
        }
        try
        {
            rs.close();
            statement.close();
            conn.close();
        }
        catch (SQLException e)
        {
            logger.error(e.getMessage(), e);
        }
        return results;
    }

    // 测试连接
    public static boolean testConnection(DbaseEntity dbaseEntity)
    {
        try {
            Class.forName(dbaseEntity.getDbDriver());
            System.out.println(JSON.toJSONString(dbaseEntity));
            Connection conn = DriverManager.getConnection(dbaseEntity.getUrl(), dbaseEntity.getUsername(), dbaseEntity.getPassword());
            if(conn != null){
                conn.close();
                return true;
            }else{
                return false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
//        try
//        {
//            String url = "";
//            if(dbaseEntity.equals("DM")) {
//
//                url = "jdbc:dm://" + ip + ":" + port + ":" + databaseName;
//            }
//            if(databaseType.equals("KingBase")) {
//                // jdbc:kingbase8://host:port/database?para1=val1&para2=val2…
//                Class.forName("com.kingbase8.Driver");
//                url = "jdbc:kingbase8://" + ip + ":" + port + "/" + databaseName;
//            }
//            if(databaseType.equals("GBase")) {
////                Class.forName("com.gbase.jdbc.Driver");
////                url = "jdbc:gbase://" + ip + ":" + port + "/" + databaseName;
//                Class.forName("com.gbasedbt.jdbc.Driver");
//                url = "jdbc:gbasedbt-sqli://" + ip + ":" + port + "/" + databaseName;
//            }
//            if (databaseType.equals("MySql"))
//            {
//                Class.forName("com.mysql.jdbc.Driver");
//                url = "jdbc:mysql://" + ip + ":" + port + "/" + databaseName + "?characterEncoding=utf8&connectTimeout=3000&socketTimeout=60000";
//            }
//            if (databaseType.equals("Oracle"))
//            {
//                Class.forName("oracle.jdbc.driver.OracleDriver");
//                url = "jdbc:oracle:thin:@" + ip + ":" + port + ":" + databaseName;
//            }
//            if (databaseType.equals("MSSQL"))
//            {
//                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
//                url = "jdbc:sqlserver://" + ip + ":" + port + ";database=" + databaseName + ";encrypt=true;trustServerCertificate=true";
//            }
//            Connection conn = DriverManager.getConnection(url, user, password);
//            if(conn != null){
//                conn.close();
//                return true;
//            }else{
//                return false;
//            }
//        }
//        catch (Exception e)
//        {
//            logger.error(e.getMessage(), e);
//        }
//        return false;
    }

    // 执行update语句,update,insert,delete等
    public int execUpdate(String sql)
            throws Exception
    {
        Connection conn = getConnection();
        Statement stmt = null;
        try
        {
            stmt = conn.createStatement();
            return stmt.executeUpdate(sql);
        }
        catch (SQLException e)
        {
            logger.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
        finally
        {
            try
            {
                stmt.close();
                conn.close();
            }
            catch (SQLException e)
            {
                logger.error(e.getMessage(), e);
            }
        }
    }

    // 执行update语句,update,insert,delete等
    public int execUpdate(String sql, String key)
            throws Exception
    {
        Connection conn = getConnection(key);
        Statement stmt = null;
        try
        {
            stmt = conn.createStatement();
            return stmt.executeUpdate(sql);
        }
        catch (SQLException e)
        {
            logger.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
        finally
        {
            try
            {
                stmt.close();
            }
            catch (SQLException e)
            {
                logger.error(e.getMessage(), e);
            }
        }
    }

    // 批量执行update
    public int updateExecuteBatch(List<String> sqlList)
            throws Exception
    {
        Connection conn = getConnection();
        Statement stmt = null;
        try
        {
            stmt = conn.createStatement();
            conn.setAutoCommit(false);
            for (String sql : sqlList)
            {
                sql = sql.replaceAll(";", "");
                stmt.addBatch(sql);
            }
            int[] updateCounts = stmt.executeBatch();
            conn.commit();
            return updateCounts.length;
        }
        catch (Exception e)
        {
            conn.rollback();
            logger.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
        finally
        {
            try
            {
                stmt.close();
                conn.close();
            }
            catch (SQLException e)
            {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public List<Map<String, Object>> queryForList(String sql,String key)
            throws Exception
    {
        // 连接
        Connection conn = null;
        // 预编译sql安全接口
        PreparedStatement pstmt = null;
        // 结果集
        ResultSet rs = null;
        // 数据库元数据
        ResultSetMetaData rsmd = null;
        int maxSize = -1;
        String[] fields = null;
        List<String> times = new ArrayList<>();
        List<String> clob = new ArrayList<>();
        List<String> binary = new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        conn = getConnection(key);
//        System.out.println("数据库获取连接");
//        System.out.println(conn);
//        System.out.println(sql);
        pstmt = conn.prepareStatement(sql);
        rs = pstmt.executeQuery();
        rsmd = rs.getMetaData();
        maxSize = rsmd.getColumnCount();
        fields = new String[maxSize];
        for (int i = 0; i < maxSize; i++)
        {
            fields[i] = rsmd.getColumnLabel(i + 1);
            if (("java.sql.Timestamp".equals(rsmd.getColumnClassName(i + 1))) || ("oracle.sql.TIMESTAMP".equals(rsmd.getColumnClassName(i + 1))))
            {
                times.add(fields[i]);
            }
            if (("oracle.jdbc.OracleClob".equals(rsmd.getColumnClassName(i + 1))) || ("oracle.jdbc.OracleBlob".equals(rsmd.getColumnClassName(i + 1))))
            {
                clob.add(fields[i]);
            }
            if ("[B".equals(rsmd.getColumnClassName(i + 1)))
            {
                binary.add(fields[i]);
            }
        }
        Map<String, Object> row = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while (rs.next())
        {
            row = new LinkedHashMap<>();
            for (int i = 0; i < maxSize; i++)
            {
                Object value = times.contains(fields[i]) ? rs.getTimestamp(fields[i]) : rs.getObject(fields[i]);
                if ((times.contains(fields[i])) && (value != null))
                {
                    value = sdf.format(value);
                }
                if ((clob.contains(fields[i])) && (value != null))
                {
                    value = "(Blob)";
                }
                if ((binary.contains(fields[i])) && (value != null))
                {
                    value = new String((byte[])value);
                }
                row.put(fields[i], value);
            }
            rows.add(row);
        }
        try
        {
            rs.close();
            pstmt.close();
//            conn.close();
        }
        catch (SQLException e)
        {
            logger.error(e.getMessage(), e);
        }
        return rows;
    }

    // 执行查询
    public List<Map<String, Object>> queryForList(String sql)
            throws Exception
    {
        // 连接
        Connection conn = null;
        // 预编译sql安全接口
        PreparedStatement pstmt = null;
        // 结果集
        ResultSet rs = null;
        // 数据库元数据
        ResultSetMetaData rsmd = null;
        int maxSize = -1;
        String[] fields = null;
        List<String> times = new ArrayList<>();
        List<String> clob = new ArrayList<>();
        List<String> binary = new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        conn = getConnection();
        pstmt = conn.prepareStatement(sql);
        rs = pstmt.executeQuery();
        rsmd = rs.getMetaData();
        maxSize = rsmd.getColumnCount();
        fields = new String[maxSize];
        for (int i = 0; i < maxSize; i++)
        {
            fields[i] = rsmd.getColumnLabel(i + 1);
            if (("java.sql.Timestamp".equals(rsmd.getColumnClassName(i + 1))) || ("oracle.sql.TIMESTAMP".equals(rsmd.getColumnClassName(i + 1))))
            {
                times.add(fields[i]);
            }
            if (("oracle.jdbc.OracleClob".equals(rsmd.getColumnClassName(i + 1))) || ("oracle.jdbc.OracleBlob".equals(rsmd.getColumnClassName(i + 1))))
            {
                clob.add(fields[i]);
            }
            if ("[B".equals(rsmd.getColumnClassName(i + 1)))
            {
                binary.add(fields[i]);
            }
        }
        Map<String, Object> row = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while (rs.next())
        {
            row = new LinkedHashMap<>();
            for (int i = 0; i < maxSize; i++)
            {
                Object value = times.contains(fields[i]) ? rs.getTimestamp(fields[i]) : rs.getObject(fields[i]);
                if ((times.contains(fields[i])) && (value != null))
                {
                    value = sdf.format(value);
                }
                if ((clob.contains(fields[i])) && (value != null))
                {
                    value = "(Blob)";
                }
                if ((binary.contains(fields[i])) && (value != null))
                {
                    value = new String((byte[])value);
                }
                row.put(fields[i], value);
            }
            rows.add(row);
        }
        try
        {
            rs.close();
            pstmt.close();
            conn.close();
        }
        catch (SQLException e)
        {
            logger.error(e.getMessage(), e);
        }
        return rows;
    }

    public List<Map<String, Object>> queryForList2(String sql)
            throws Exception
    {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ResultSetMetaData rsmd = null;
        int maxSize = -1;
        conn = getConnection();
        pstmt = conn.prepareStatement(sql);
        rs = pstmt.executeQuery();
        rsmd = rs.getMetaData();
        maxSize = rsmd.getColumnCount();
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row;
        while (rs.next())
        {
            row = new HashMap<>();
            for (int i = 0; i < maxSize; i++)
            {
                row.put(rsmd.getColumnLabel(i + 1), rs.getObject(rsmd.getColumnLabel(i + 1)));
            }
            rows.add(row);
        }
        return rows;
    }

    public List<Map<String, Object>> queryForListForMSSQL(String sql, int maxRow, int beginIndex)
            throws Exception
    {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ResultSetMetaData rsmd = null;
        int maxSize = -1;
        String[] fields;
        List<String> times = new ArrayList<>();
        conn = getConnection();
        pstmt = conn.prepareStatement(sql, 1005, 1008);
        pstmt.setMaxRows(maxRow);
        rs = pstmt.executeQuery();
        rsmd = rs.getMetaData();
        // 获取列数
        maxSize = rsmd.getColumnCount();
        // 初始化字段列表
        fields = new String[maxSize];
        for (int i = 0; i < maxSize; i++)
        {
            fields[i] = rsmd.getColumnLabel(i + 1);
            if ("java.sql.Timestamp".equals(rsmd.getColumnClassName(i + 1)))
            {
                // 如果字段的数据类型为Timestamp
                times.add(fields[i]);
            }
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row;
        //时间戳与日期的转换格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        rs.absolute(beginIndex);
        while (rs.next())
        {
            row = new HashMap<>();
            for (int i = 0; i < maxSize; i++)
            {
                Object value = times.contains(fields[i]) ? rs.getTimestamp(fields[i]) : rs.getObject(fields[i]);
                if ((times.contains(fields[i])) && (value != null))
                {
                    value = sdf.format(value);
                }
                row.put(fields[i], value);
            }
            rows.add(row);
        }
        try
        {
            rs.close();
            pstmt.close();
            conn.close();
        }
        catch (SQLException e)
        {
            logger.error(e.getMessage(), e);
        }
        return rows;
    }

    public List<Map<String, Object>> queryForListWithType(String sql)
    {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> rows2 = new ArrayList<>();
        try
        {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            ResultSetMetaData rsme = rs.getMetaData();
            int columnCount = rsme.getColumnCount();
            rs.next();
            for (int i = 1; i < columnCount + 1; i++)
            {
                Map<String, Object> map = new HashMap<>();
                map.put("column_name", rsme.getColumnName(i));
                map.put("column_value", rs.getObject(rsme.getColumnName(i)));
                map.put("data_type", rsme.getColumnTypeName(i));
                map.put("precision", Integer.valueOf(rsme.getPrecision(i)));
                map.put("isAutoIncrement", Boolean.valueOf(rsme.isAutoIncrement(i)));
                map.put("is_nullable", Integer.valueOf(rsme.isNullable(i)));
                map.put("isReadOnly", Boolean.valueOf(rsme.isReadOnly(i)));
                rows2.add(map);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        finally
        {
            try
            {
                rs.close();
                pstmt.close();
                conn.close();
            }
            catch (SQLException localSQLException1)
            {
                logger.error(localSQLException1.getMessage(), localSQLException1);
            }
        }
        return rows2;
    }

    public List<Map<String, Object>> queryForColumnOnly(String sql)
    {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> rows2 = new ArrayList<>();
        try
        {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            ResultSetMetaData rsme = rs.getMetaData();
            int columnCount = rsme.getColumnCount();
            for (int i = 1; i < columnCount + 1; i++)
            {
                Map<String, Object> map = new HashMap<>();
                map.put("column_name", rsme.getColumnName(i));
                map.put("data_type", rsme.getColumnTypeName(i));
                map.put("precision", Integer.valueOf(rsme.getPrecision(i)));
                map.put("isAutoIncrement", Boolean.valueOf(rsme.isAutoIncrement(i)));
                map.put("is_nullable", Integer.valueOf(rsme.isNullable(i)));
                map.put("isReadOnly", Boolean.valueOf(rsme.isReadOnly(i)));
                rows2.add(map);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        finally
        {
            try
            {
                rs.close();
                pstmt.close();
                conn.close();
            }
            catch (SQLException localSQLException1)
            {
                logger.error(localSQLException1.getMessage(), localSQLException1);
            }
        }
        return rows2;
    }

    public List<Map<String, Object>> executeSqlForColumns(String sql,String key)
            throws Exception
    {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ResultSetMetaData rsmd = null;
        int maxSize = -1;
        conn = getConnection(key);
        List<Map<String, Object>> rows = new ArrayList<>();
        try
        {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            rsmd = rs.getMetaData();
            maxSize = rsmd.getColumnCount();
            for (int i = 0; i < maxSize; i++)
            {
                Map<String, Object> map = new HashMap<>();
                map.put("column_name", rsmd.getColumnLabel(i + 1));
                map.put("data_type", rsmd.getColumnTypeName(i + 1));
                rows.add(map);
            }
        }
        finally
        {
            try
            {
                rs.close();
                pstmt.close();
//                conn.close();
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
            }
        }
        return rows;
    }

    public List<Map<String, Object>> executeSqlForColumns(String sql)
            throws Exception
    {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ResultSetMetaData rsmd = null;
        int maxSize = -1;
        conn = getConnection();
        List<Map<String, Object>> rows = new ArrayList<>();
        try
        {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            rsmd = rs.getMetaData();
            maxSize = rsmd.getColumnCount();
            for (int i = 0; i < maxSize; i++)
            {
                Map<String, Object> map = new HashMap<>();
                map.put("column_name", rsmd.getColumnLabel(i + 1));
                map.put("data_type", rsmd.getColumnTypeName(i + 1));
                rows.add(map);
            }
        }
        finally
        {
            try
            {
                rs.close();
                pstmt.close();
                conn.close();
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
            }
        }
        return rows;
    }

    public int executeQueryForCount(String sql)
    {
        int rowCount = 0;
        Connection conn = getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        try
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                Object count = rs.getObject("count(*)");
                rowCount = Integer.parseInt(count.toString());
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        finally
        {
            try
            {
                rs.close();
                stmt.close();
                conn.close();
            }
            catch (SQLException localSQLException1)
            {
                logger.error(localSQLException1.getMessage(), localSQLException1);
            }
        }
        return rowCount;
    }


    public int executeQueryForCount2(String sql)
    {
        int rowCount = 0;
        Connection conn = getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        try
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            rs.last();
            rowCount = rs.getRow();
            System.out.println(rowCount);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        finally
        {
            try
            {
                rs.close();
                stmt.close();
                conn.close();
            }
            catch (SQLException localSQLException1)
            {
                logger.error(localSQLException1.getMessage(), localSQLException1);
            }
        }
        return rowCount;
    }

    public boolean executeQuery(String sql)
    {
        boolean bl = false;
        Connection conn = getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        try
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next())
            {
                bl = true;
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        finally
        {
            try
            {
                rs.close();
                stmt.close();
                conn.close();
            }
            catch (SQLException localSQLException1)
            {
                logger.error(localSQLException1.getMessage(), localSQLException1);
            }
        }
        return bl;
    }

    public String getPrimaryKeys(String databaseName, String tableName)
    {
        Connection conn = null;
        try
        {
            conn = getConnection();
            DatabaseMetaData metadata = conn.getMetaData();
            ResultSet rs2 = metadata.getPrimaryKeys(databaseName, null, tableName);
            if (rs2.next())
            {
                logger.info("主键名称: {}", rs2.getString(4));
                return rs2.getString(4);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        finally
        {
            try
            {
                conn.close();
            }
            catch (SQLException localSQLException2)
            {
                logger.error(localSQLException2.getMessage(), localSQLException2);
            }
        }
        return "";
    }

    public List<String> getPrimaryKeyss(String databaseName, String tableName)
    {
        Connection conn = null;
        List<String> rows2 = new ArrayList<>();
        try
        {
            conn = getConnection();
            DatabaseMetaData metadata = conn.getMetaData();
            ResultSet rs2 = metadata.getPrimaryKeys(databaseName, null, tableName);
            while (rs2.next())
            {
                logger.info("主键名称2: {}", rs2.getString(4));
                rows2.add(rs2.getString(4));
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        finally
        {
            try
            {
                conn.close();
            }
            catch (SQLException localSQLException1)
            {
                logger.error(localSQLException1.getMessage(), localSQLException1);
            }
        }
        return rows2;
    }

    public int executeQueryForCountForOracle(String sql)
    {
        int rowCount = 0;
        Connection conn = getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        String sql3 = " select count(*) as count from  (" + sql + ")";
        try
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql3);
            rs.next();
            rowCount = rs.getInt("count");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        finally
        {
            try
            {
                rs.close();
                stmt.close();
                conn.close();
            }
            catch (SQLException localSQLException1)
            {
                logger.error(localSQLException1.getMessage(), localSQLException1);
            }
        }
        return rowCount;
    }

    public List<String> getSchemas(DbaseEntity dbaseEntity) {
        List<String> schemaList = new ArrayList<>();
        Connection conn = getConnection();
        DatabaseMetaData databaseMetaData = null;
        try {
            // 获取所有数据库
            assert conn != null;
            databaseMetaData = conn.getMetaData();
            System.out.println("===========获取所有数据库===========");
            ResultSet rs1 = null;
            if (dbaseEntity.getDbType().equals("MySql") || dbaseEntity.getDbType().equals("GBase") || dbaseEntity.getDbType().equals("ElasticSearch")){
                rs1 = databaseMetaData.getCatalogs();
                while (rs1.next()) {
                    String schema = rs1.getString("TABLE_CAT");
                    schemaList.add(schema);
                }
                rs1.close();
            } else {
                rs1 = databaseMetaData.getSchemas();
                while (rs1.next()) {
                    String schema = rs1.getString("TABLE_SCHEM");
                    schemaList.add(schema);
                }
            }
            rs1.close();
            conn.close();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
        return schemaList;
    }

    public List<String> getTables(String dbType, String schemaName, String connKey) {
        List<String> tableList = new ArrayList<>();
        Connection conn = getConnection(connKey);
        System.out.println("===========获取所有表=============");
        DatabaseMetaData databaseMetaData = null;
        try {
            assert conn != null;
            databaseMetaData = conn.getMetaData();
            ResultSet rs = null;
            if (dbType.equals("GBase")){
                rs = databaseMetaData.getTables(schemaName, null, null, new String[] {"TABLE"});
            } else{
                rs = databaseMetaData.getTables(null, schemaName, null, new String[] {"TABLE"});
            }
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                tableList.add(tableName);
            }
            rs.close();
//            conn.close();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
        return tableList;
    }

    public List<String> getViews(String dbType, String schemaName, String connKey) {
        List<String> tableList = new ArrayList<>();
        Connection conn = getConnection(connKey);
        System.out.println("===========获取所有视图=============");
        DatabaseMetaData databaseMetaData = null;
        try {
            assert conn != null;
            databaseMetaData = conn.getMetaData();
            ResultSet rs = null;
            if (dbType.equals("GBase")){
                rs = databaseMetaData.getTables(schemaName, null, null, new String[] {"VIEW"});
            } else {
                rs = databaseMetaData.getTables(null, schemaName, null, new String[] {"VIEW"});
            }
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                tableList.add(tableName);
            }
            rs.close();
            conn.close();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
        return tableList;
    }

    public List<Map<String, Object>> getFuncs(String dbType, String schemaName, String connKey) {
        List<Map<String, Object>> funcs = new ArrayList<>();
        Connection conn = getConnection(connKey);
        System.out.println("===========获取所有函数和存储过程=============");
        DatabaseMetaData databaseMetaData = null;
        try {
            assert conn != null;
            databaseMetaData = conn.getMetaData();
            if(!dbType.equals("GBase")){
                ResultSet rs1 = databaseMetaData.getFunctions(null, schemaName ,null);
                while (rs1.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("ROUTINE_TYPE", "FUNCTION");
                    map.put("ROUTINE_NAME", rs1.getString("FUNCTION_NAME"));
                    funcs.add(map);
                }
                rs1.close();
            }
            ResultSet rs2 = databaseMetaData.getProcedures(null, schemaName, null);
            while (rs2.next()){
                Map<String, Object> map = new HashMap<>();
                map.put("ROUTINE_TYPE", "PROCEDURE");
                map.put("ROUTINE_NAME", rs2.getString("PROCEDURE_NAME"));
                funcs.add(map);
            }
            rs2.close();
            conn.close();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
        return funcs;
    }

    public List<Map<String, Object>> getTableMetas(String dbType, String schemaName, String connKey) {
        List<Map<String, Object>> tableMetas = new ArrayList<>();
        Connection conn = getConnection(connKey);
        System.out.println("===========获取所有表和字段信息=============");
        DatabaseMetaData databaseMetaData = null;
        try {
            assert conn != null;
            databaseMetaData = conn.getMetaData();
            List<String> tables = getTables(dbType, schemaName, connKey);
            for(String table:tables){
                ResultSet rs = null;
                if (dbType.equals("GBase")){
                    rs = databaseMetaData.getColumns(schemaName , null, table,null);
                } else {
                    rs = databaseMetaData.getColumns(null, schemaName , table,null);
                }
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("TABLE_NAME", rs.getString("TABLE_NAME"));
                    map.put("COLUMN_NAME", rs.getString("COLUMN_NAME"));
                    tableMetas.add(map);
                }
                rs.close();
            }
            conn.close();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
        return tableMetas;
    }

    public List<String> getColumns(String dbType, String schemaName, String tableName, String connKey){
        List<String> columnList = new ArrayList<>();
        Connection conn = getConnection(connKey);
        System.out.println("===========获取相应表的字段信息=============");
        DatabaseMetaData databaseMetaData = null;
        try {
            assert conn != null;
            databaseMetaData = conn.getMetaData();
            ResultSet rs = null;
            if (dbType.equals("GBase")){
                rs = databaseMetaData.getColumns(schemaName , null, tableName,null);
            } else {
                rs = databaseMetaData.getColumns(null, schemaName , tableName,null);
            }
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                columnList.add(columnName);
            }
            rs.close();
            conn.close();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
        return columnList;
    }

    public MetaInfo getDataBaseMeta(){
        MetaInfo metaInfo = new MetaInfo();
        Connection connection = getConnection();
        DatabaseMetaData databaseMetaData = null;
        try {
            databaseMetaData = connection.getMetaData();
            metaInfo.setDatabaseName(databaseMetaData.getDatabaseProductName());
            metaInfo.setVersion(databaseMetaData.getDatabaseProductVersion());
            metaInfo.setDriverName(databaseMetaData.getDriverName());
            metaInfo.setDriverVersion(databaseMetaData.getDriverVersion());
            metaInfo.setUrl(databaseMetaData.getURL());
            metaInfo.setUserName(databaseMetaData.getUserName());
            metaInfo.setTransaction(databaseMetaData.supportsTransactions());
//            System.out.println("获取数据库产品名称：" + databaseMetaData.getDatabaseProductName());
//            System.out.println("获取数据库的版本号：" + databaseMetaData.getDatabaseProductVersion());
//            System.out.println("获取数据库驱动名称：" + databaseMetaData.getDriverName());
//            System.out.println("获取数据库驱动版本：" + databaseMetaData.getDriverVersion());
//            System.out.println("获取数据库连接URL：" + databaseMetaData.getURL());
//            System.out.println("获取数据库的用户名：" + databaseMetaData.getUserName());
//            System.out.println("数据库是否允许事务：" + databaseMetaData.supportsTransactions());
            connection.close();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
        return metaInfo;
    }


}
