package com.dbms.domain.dbase;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MetaInfo {

    /**
     * 数据库产品名称
     */
    private String databaseName;
    /**
     * 数据库版本号
     */
    private String version;
    /**
     * 数据库驱动名称
     */
    private String driverName;
    /**
     * 数据库驱动版本
     */
    private String driverVersion;
    /**
     * 数据库连接URL
     */
    private String url;
    /**
     * 数据库用户名
     */
    private String userName;
    /**
     * 数据库是否允许事务
     */
    private boolean transaction;

}
