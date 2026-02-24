package com.dbms.core.dbase.impl;

import com.alibaba.fastjson.JSON;
import com.dbms.core.dbase.DataBaseUtilService;
import com.dbms.domain.dbase.MetaInfo;
import com.dbms.entity.DbaseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DataBaseUtilServiceImpl implements DataBaseUtilService {

    private static final Logger logger = LoggerFactory.getLogger(DataBaseUtilServiceImpl.class);

    public static final Map<String, Connection> connMap = new ConcurrentHashMap<>();


    @Override
    public Connection getConnection(DbaseEntity dbaseEntity) {
        try {
            Class.forName(dbaseEntity.getDbDriver());
            return DriverManager.getConnection(dbaseEntity.getUrl(), dbaseEntity.getUsername(), dbaseEntity.getPassword());
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Connection getConnectionByKey(String key){
        try {
            Connection currentConn = connMap.getOrDefault(key,null);
//            System.out.println("getConn:" + currentConn);
            return currentConn;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Connection getConnection(DbaseEntity dbaseEntity, String key) {
        try {
            Connection currentConn = connMap.getOrDefault(key,null);
//            System.out.println("getConn:" + currentConn);
//            currentConn.setNetworkTimeout();
            if (currentConn == null){
                currentConn = getConnection(dbaseEntity);
                try{
                    currentConn.setAutoCommit(false);
                } catch (Exception e){

                }
                connMap.put(key,currentConn);
            } else if (currentConn.isClosed() ){
                connMap.remove(key);
                currentConn = getConnection(dbaseEntity);
//                System.out.println("closed,getConn:" + currentConn);
                try{
                    currentConn.setAutoCommit(false);
                } catch (Exception e){

                }
                connMap.put(key,currentConn);
            } else if (!currentConn.isValid(2)){
                connMap.remove(key);
                currentConn = getConnection(dbaseEntity);
                try{
                    currentConn.setAutoCommit(false);
                } catch (Exception e){

                }
//                System.out.println("invalid,getConn:" + currentConn);
                connMap.put(key,currentConn);
            }
            return currentConn;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void checkConnectionSession(){
        for (Map.Entry<String, Connection> entry : connMap.entrySet()) {
            String key = entry.getKey();
            System.out.println("key:"+key);
            Connection conn = entry.getValue();
            try {
                if (conn.isClosed()){
                    connMap.remove(key);
                    System.out.println("removeClosed:"+key);
                } else if (!conn.isValid(2)){
                    connMap.remove(key);
                    System.out.println("removeInvalid:"+key);
                }
            } catch (Exception e) {
                connMap.remove(key);
            }
        }
    }
    @Override
    public void closeConn(String key) throws Exception {
        Connection conn = getConnectionByKey(key);
        try {
            if (conn != null){
                conn.close();
                connMap.remove(key);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void startTransaction(DbaseEntity dbaseEntity, String key) throws Exception {
        Connection conn = getConnection(dbaseEntity, key);
//        System.out.println("currentConn:"+conn);
        try {
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            conn.rollback();
            logger.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void commit(String key) throws Exception {
        Connection conn = getConnectionByKey(key);
        try {
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            logger.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void rollback(String key) throws Exception {
        Connection conn = getConnectionByKey(key);
        try {
            conn.rollback();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> executeSQL(DbaseEntity dbaseEntity, String sql) throws SQLException {
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
        conn = getConnection(dbaseEntity);
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
//            System.out.println(rs.getString(1));
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


    @Override
    public boolean testConnection(DbaseEntity dbaseEntity) {
        try {
            Class.forName(dbaseEntity.getDbDriver());
//            System.out.println(JSON.toJSONString(dbaseEntity));
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
    }

    @Override
    public int execUpdate(DbaseEntity dbaseEntity, String sql) throws Exception {
        Connection conn = getConnection(dbaseEntity);
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            return stmt.executeUpdate(sql);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        } finally {
            try {
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public Map<String, Object> execUpdateList(DbaseEntity dbaseEntity, List<String> sqlList) throws Exception {
        Map<String, Object> result = new HashMap<>();
        int success = 0;
        List<String> failureList = new ArrayList<>();
        int failure = 0;
        Connection conn = getConnection(dbaseEntity);
        Statement stmt = null;
        stmt = conn.createStatement();
        for(int i = 0; i < sqlList.size(); i++){
            try {
                System.out.println(sqlList.get(i));
                stmt.executeUpdate(sqlList.get(i));
                success += 1;
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
                failureList.add(sqlList.get(i));
                failure += 1;
            }
        }
        result.put("success", success);
        result.put("failure", failure);
        result.put("failureList", failureList);
        try {
            stmt.close();
//            conn.commit();
            conn.close();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public int execUpdate(DbaseEntity dbaseEntity, String sql, String key) throws Exception {
        Connection conn = getConnectionByKey(key);
        System.out.println(conn);
        if (conn == null){
            throw new Exception("数据库连接会话已关闭，请尝试重新连接！");
        }
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            return stmt.executeUpdate(sql);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
        finally {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public int executeUpdate(DbaseEntity dbaseEntity, String sql, String key) throws Exception {
        Connection conn = getConnectionByKey(key);
        System.out.println(conn);
        if (conn == null){
            throw new Exception("数据库连接会话已关闭，请尝试重新连接！");
        }
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            return stmt.executeUpdate(sql);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
        finally {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    // 批量执行update
    public int updateExecuteBatch(DbaseEntity dbaseEntity, List<String> sqlList)
            throws Exception
    {
        Connection conn = getConnection(dbaseEntity);
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

    @Override
    public List<Map<String, Object>> queryForList(DbaseEntity dbaseEntity, String sql,String key)
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
        conn = getConnectionByKey(key);
        if (conn == null){
            throw new Exception("数据库连接会话已关闭，请尝试重新连接！");
        }
        pstmt = conn.prepareStatement(sql);
        rs = pstmt.executeQuery();
        rsmd = rs.getMetaData();
        maxSize = rsmd.getColumnCount();
        fields = new String[maxSize];
        for (int i = 0; i < maxSize; i++) {
            fields[i] = rsmd.getColumnLabel(i + 1);
            if (("java.sql.Timestamp".equals(rsmd.getColumnClassName(i + 1))) || ("oracle.sql.TIMESTAMP".equals(rsmd.getColumnClassName(i + 1)))) {
                times.add(fields[i]);
            }
            if (("oracle.jdbc.OracleClob".equals(rsmd.getColumnClassName(i + 1))) || ("oracle.jdbc.OracleBlob".equals(rsmd.getColumnClassName(i + 1)))) {
                clob.add(fields[i]);
            }
            if ("[B".equals(rsmd.getColumnClassName(i + 1))) {
                binary.add(fields[i]);
            }
        }
        Map<String, Object> row = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while (rs.next()) {
            row = new LinkedHashMap<>();
            for (int i = 0; i < maxSize; i++) {
                Object value = times.contains(fields[i]) ? rs.getTimestamp(fields[i]) : rs.getObject(fields[i]);
                if ((times.contains(fields[i])) && (value != null)) {
                    value = sdf.format(value);
                }
                if ((clob.contains(fields[i])) && (value != null)) {
                    value = "(Blob)";
                }
                if ((binary.contains(fields[i])) && (value != null)) {
                    value = new String((byte[])value);
                }
                row.put(fields[i], value);
            }
            rows.add(row);
        }
        try {
            rs.close();
            pstmt.close();
//            conn.close();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return rows;
    }

    @Override
    // 执行查询
    public List<Map<String, Object>> queryForList(DbaseEntity dbaseEntity, String sql) throws Exception {
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
        conn = getConnection(dbaseEntity);
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

    @Override
    public List<Map<String, Object>> executeSqlForColumns(DbaseEntity dbaseEntity, String sql,String key)
            throws Exception
    {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ResultSetMetaData rsmd = null;
        int maxSize = -1;
        conn = getConnectionByKey(key);
        if (conn == null){
            throw new Exception("数据库连接会话已关闭，请尝试重新连接");
        }
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

    @Override
    public List<Map<String, Object>> executeSqlForColumns(DbaseEntity dbaseEntity, String sql)
            throws Exception
    {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ResultSetMetaData rsmd = null;
        int maxSize = -1;
        conn = getConnection(dbaseEntity);
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

    @Override
    public int executeQueryForCount(DbaseEntity dbaseEntity, String sql)
    {
        int rowCount = 0;
        Connection conn = getConnection(dbaseEntity);
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


    @Override
    public int executeQueryForCount2(DbaseEntity dbaseEntity, String sql)
    {
        int rowCount = 0;
        Connection conn = getConnection(dbaseEntity);
        Statement stmt = null;
        ResultSet rs = null;
        try
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            rs.last();
            rowCount = rs.getRow();
//            System.out.println(rowCount);
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

    @Override
    public boolean executeQuery(DbaseEntity dbaseEntity, String sql)
    {
        boolean bl = false;
        Connection conn = getConnection(dbaseEntity);
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

    @Override
    public String getPrimaryKeys(DbaseEntity dbaseEntity, String databaseName, String tableName)
    {
        Connection conn = null;
        try
        {
            conn = getConnection(dbaseEntity);
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

    @Override
    public List<String> getPrimaryKeyss(DbaseEntity dbaseEntity, String databaseName, String tableName)
    {
        Connection conn = null;
        List<String> rows2 = new ArrayList<>();
        try
        {
            conn = getConnection(dbaseEntity);
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

    @Override
    public int executeQueryForCountForOracle(DbaseEntity dbaseEntity, String sql)
    {
        int rowCount = 0;
        Connection conn = getConnection(dbaseEntity);
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

    @Override
    public List<String> getSchemas(DbaseEntity dbaseEntity) {
        List<String> schemaList = new ArrayList<>();
        Connection conn = getConnection(dbaseEntity);
        DatabaseMetaData databaseMetaData = null;
        try {
            // 获取所有数据库
            databaseMetaData = conn.getMetaData();
//            System.out.println("===========获取所有数据库===========");
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

    @Override
    public List<String> getTables(DbaseEntity dbaseEntity, String connKey) {
        List<String> tableList = new ArrayList<>();
        System.out.println(JSON.toJSONString(dbaseEntity));
        Connection conn = getConnection(dbaseEntity, connKey);
//        System.out.println("===========获取所有表=============");
        DatabaseMetaData databaseMetaData = null;
        try {
            assert conn != null;
            databaseMetaData = conn.getMetaData();
            ResultSet rs = null;
            if (dbaseEntity.getDbType().equals("GBase")){
                rs = databaseMetaData.getTables(dbaseEntity.getSchemaName(), null, null, new String[] {"TABLE"});
            } else if (dbaseEntity.getDbType().equals("HBasePhoenix")){
                rs = databaseMetaData.getTables(null, null, null, new String[] {"TABLE"});
            } else{
                rs = databaseMetaData.getTables(null, dbaseEntity.getSchemaName(), null, new String[] {"TABLE"});
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

    @Override
    public List<String> getViews(DbaseEntity dbaseEntity, String connKey) {
        List<String> tableList = new ArrayList<>();
        Connection conn = getConnection(dbaseEntity, connKey);
//        System.out.println("===========获取所有视图=============");
        DatabaseMetaData databaseMetaData = null;
        try {
            assert conn != null;
            databaseMetaData = conn.getMetaData();
            ResultSet rs = null;
            if (dbaseEntity.getDbType().equals("GBase")){
                rs = databaseMetaData.getTables(dbaseEntity.getSchemaName(), null, null, new String[] {"VIEW"});
            } else if (dbaseEntity.getDbType().equals("HBasePhoenix")){
                rs = databaseMetaData.getTables(null, null, null, new String[] {"VIEW"});
            } else {
                rs = databaseMetaData.getTables(null, dbaseEntity.getSchemaName(), null, new String[] {"VIEW"});
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

    @Override
    public List<Map<String, Object>> getFuncs(DbaseEntity dbaseEntity, String connKey) {
        List<Map<String, Object>> funcs = new ArrayList<>();
        Connection conn = getConnection(dbaseEntity, connKey);
//        System.out.println("===========获取所有函数和存储过程=============");
        DatabaseMetaData databaseMetaData = null;
        try {
            assert conn != null;
            databaseMetaData = conn.getMetaData();
            if(!dbaseEntity.getDbType().equals("GBase")){
                ResultSet rs1 = databaseMetaData.getFunctions(null, dbaseEntity.getSchemaName(),null);
                while (rs1.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("ROUTINE_TYPE", "FUNCTION");
                    map.put("ROUTINE_NAME", rs1.getString("FUNCTION_NAME"));
                    funcs.add(map);
                }
                rs1.close();
            }
            ResultSet rs2 = databaseMetaData.getProcedures(null, dbaseEntity.getSchemaName(), null);
            while (rs2.next()){
                Map<String, Object> map = new HashMap<>();
                map.put("ROUTINE_TYPE", "PROCEDURE");
                map.put("ROUTINE_NAME", rs2.getString("PROCEDURE_NAME"));
                funcs.add(map);
            }
            rs2.close();
//            conn.close();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
        return funcs;
    }

    @Override
    public List<Map<String, Object>> getTableMetas(DbaseEntity dbaseEntity, String connKey) {
        List<Map<String, Object>> tableMetas = new ArrayList<>();
        Connection conn = getConnection(dbaseEntity, connKey);
//        System.out.println("===========获取所有表和字段信息=============");
        DatabaseMetaData databaseMetaData = null;
        try {
            assert conn != null;
            databaseMetaData = conn.getMetaData();
            List<String> tables = getTables(dbaseEntity, connKey);
            for(String table:tables){
                ResultSet rs = null;
                if (dbaseEntity.getDbType().equals("GBase")){
                    rs = databaseMetaData.getColumns(dbaseEntity.getSchemaName() , null, table,null);
                } else {
                    rs = databaseMetaData.getColumns(null, dbaseEntity.getSchemaName(), table,null);
                }
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("TABLE_NAME", rs.getString("TABLE_NAME"));
                    map.put("COLUMN_NAME", rs.getString("COLUMN_NAME"));
                    tableMetas.add(map);
                }
                rs.close();
            }
//            conn.close();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
        return tableMetas;
    }

    @Override
    public List<String> getColumns(DbaseEntity dbaseEntity, String tableName, String connKey){
        List<String> columnList = new ArrayList<>();
        Connection conn = getConnection(dbaseEntity);
//        System.out.println("===========获取相应表的字段信息=============");
        DatabaseMetaData databaseMetaData = null;
        try {
            assert conn != null;
            databaseMetaData = conn.getMetaData();
            ResultSet rs = null;
            if (dbaseEntity.getDbType().equals("GBase")){
                rs = databaseMetaData.getColumns(dbaseEntity.getSchemaName() , null, tableName,null);
            } else {
                rs = databaseMetaData.getColumns(null, dbaseEntity.getSchemaName(), tableName,null);
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

    @Override
    public MetaInfo getDataBaseMeta(DbaseEntity dbaseEntity){
        MetaInfo metaInfo = new MetaInfo();
        try {
            Connection connection = getConnection(dbaseEntity);
            DatabaseMetaData databaseMetaData = null;
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
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
//            return metaInfo;
        }
        return metaInfo;
    }

    @Override
    public Integer countConnectionSession(Integer dbId) {
        String childKey = "dbId-" + dbId;
        int count = 0;
        for (String key : connMap.keySet()){
            if (key.contains(childKey)){
                count++;
            }
        }
        return count;
    }

    @Override
    public List<Map<String, Object>> getColumnsFromResultList(List<Map<String, Object>> list) {
        if (list.size() == 0){
            return Collections.emptyList();
        } else {
            List<Map<String, Object>> columns = new ArrayList<>();
            for (String key : list.get(0).keySet()){
                Map<String, Object> map = new HashMap<>();
                map.put("column_name", key);
                columns.add(map);
            }
            return columns;
        }
    }
}
