package com.dbms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author YSL
 * @since 2023-04-27
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("LOG_RISK_ALERT")
public class LogRiskAlertEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "ALERT_ID", type = IdType.AUTO)
    private Integer alertId;

    /**
     * 数据源连接串
     */
    @TableField("DB_URL")
    private String dbUrl;

    /**
     * 模式名
     */
    @TableField("SCHEMA_NAME")
    private String schemaName;

    /**
     * 用户名
     */
    @TableField("USER_NAME")
    private String userName;

    /**
     * IP地址
     */
    @TableField("IP_ADDRESS")
    private String ipAddress;

    /**
     * 创建时间
     */
    @TableField("CREATETIME")
    private LocalDateTime createTime;

    /**
     * 规则类型
     */
    @TableField("RULE_TYPE")
    private String ruleType;

//    /**
//     * 表，字段
//     */
//    @TableField("TABLE_COLUMN")
//    private String tableColumn;

    /**
     * 操作类型
     */
    @TableField("SQL_OPERATION")
    private String sqlOperation;

    /**
     * 风险级别
     */
    @TableField("RISK_LEVEL")
    private String riskLevel;

//    /**
//     * 表名
//     */
//    @TableField("TABLE_NAME")
//    private String tableName;

    /**
     * sql语句
     */
    @TableField("SQL")
    private String sql;


    public static final String ALERT_ID = "ALERT_ID";

    public static final String DB_URL = "DB_URL";

    public static final String SCHEMA_NAME = "SCHEMA_NAME";

    public static final String USER_NAME = "USER_NAME";

    public static final String SQL = "SQL";

    public static final String IP_ADDRESS = "IP_ADDRESS";

    public static final String SQL_OPERATION = "SQL_OPERATION";

    public static final String CREATETIME = "CREATETIME";

    public static final String RULE_TYPE = "RULE_TYPE";

//    public static final String TABLE_COLUMN = "TABLE_COLUMN";

    public static final String RISK_LEVEL = "RISK_LEVEL";

//    public static final String TABLE_NAME = "TABLE_NAME";

}
