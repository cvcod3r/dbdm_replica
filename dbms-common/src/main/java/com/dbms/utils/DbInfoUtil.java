package com.dbms.utils;

import com.dbms.entity.DbaseEntity;

public class DbInfoUtil {

    public static String getURL(DbaseEntity dbaseEntity){
        String url = null;
        String ip = dbaseEntity.getHost();
        String port = dbaseEntity.getPort();
        String dbType = dbaseEntity.getDbType();
        String dbName = dbaseEntity.getDbName();
        String userName = dbaseEntity.getUsername();
        if (dbType.equals("Oracle") || dbType.equals("GBase") || dbType.equals("KingBase")){
            url = dbaseEntity.getUrlPrefix() + ip + ":" + port + dbaseEntity.getUrlDir() + dbName;
        }
//        else if (dbType.equals("KingBase")){
//            url = dbaseEntity.getUrlPrefix() + ip + ":" + port + dbaseEntity.getUrlDir() + dbName;
//        }
        else {
            url = dbaseEntity.getUrlPrefix() + ip + ":" + port;
        }
//        switch (dbType)
//        {
//            case "MySql":
//                url = "jdbc:mysql://" + ip + ":" + port;
//                break;
//            case "Oracle":
//                url = "jdbc:oracle:thin:@" + ip + ":" + port + ":" + dbName;
//                break;
//            case "MSSQL":
//                url = "jdbc:sqlserver://" + ip + ":" + port;
//                break;
//            case "DM":
//                url = "jdbc:dm://" + ip + ":" + port;
//                break;
//            case "KingBase":
//                url = "jdbc:kingbase8://" + ip + ":" + port;
//                break;
//            case "GBase":
//                // url = "jdbc:gbase://" + ip + ":" + port + "/" + dbName;
//                url = "jdbc:gbasedbt-sqli://" + ip + ":" + port + "/" +dbName;
//                break;
//            case "Hive":
//                url = "jdbc:hive2://" + ip + ":" + port;
//                break;
//            case "ElasticSearch":
//                url = "jdbc:es://" + ip + ":" + port;
//                break;
//            default:
//                break;
//        }
        return url;
    }

    public static String getConnKey(DbaseEntity dbaseEntity, Integer userId, String schemaName){
        String connKey = "connKey:" + userId + ":dbId-" +dbaseEntity.getDbId() + ":" + schemaName;
//        System.out.println("connKey:"+connKey);
        return connKey;
    }

    public static String getConnKey(DbaseEntity dbaseEntity, Integer userId){
        String connKey = "connKey:" + userId + ":dbId-" +dbaseEntity.getDbId();
        System.out.println("connKey:"+connKey);
        return connKey;
    }

}
