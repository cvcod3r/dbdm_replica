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
 * @author <a href="https://www.fengwenyi.com?code">YSL</a>
 * @since 2023-02-05
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("LOG_DBASE")
public class LogDbaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "DB_LOG_ID", type = IdType.AUTO)
    private Long dbLogId;

    /**
     * 连接名
     */
    @TableField("DB_CONN_NAME")
    private String dbConnName;

    /**
     * 连接串
     */
    @TableField("DB_URL")
    private String dbUrl;

    /**
     * 账户名
     */
    @TableField("DB_USERNAME")
    private String dbUsername;

    /**
     * 用户名
     */
    @TableField("USER_NAME")
    private String userName;

    /**
     * 真实姓名
     */
    @TableField("REAL_NAME")
    private String realName;

    /**
     * IP地址
     */
    @TableField("IP_ADDRESS")
    private String ipAddress;

    /**
     * 模式（库）
     */
    @TableField("SCHEMA_NAME")
    private String schemaName;

    /**
     * 表名
     */
    @TableField("TABLE_NAME")
    private String tableName;
    /**
     * 表，字段
     */
    @TableField("TABLE_COLUMN")
    private String tableColumn;

    /**
     * SQL字段
     */
    @TableField("SQL_COMMAND")
    private String sqlCommand;

    /**
     * 操作类型
     */
    @TableField("SQL_OPERATION")
    private String sqlOperation;

    /**
     * 结果信息
     */
    @TableField("RESULT_DETAIL")
    private String resultDetail;

    /**
     * 备注
     */
    @TableField("COMMENTS")
    private String comments;

    /**
     * 创建时间
     */
    @TableField("CREATETIME")
    private LocalDateTime createtime;

    /**
     * 外键，关联数据源
     */
    @TableField("DB_ID")
    private Integer dbId;

    /**
     * 外键，关联用户
     */
    @TableField("USER_ID")
    private Integer userId;

    /**
     * 0：未删除，1：已删除
     */
    @TableField("IS_DELETE")
    private Integer isDelete;

    @TableField("ROW_COUNT")
    private Integer rowCount;

    @TableField("EXEC_TIME")
    private Long execTime;

    @TableField("DATA_LABELS")
    private String dataLabels;

    @TableField("ACTION_TYPE")
    private String actionType;

    public static final String DB_LOG_ID = "DB_LOG_ID";

    public static final String DB_CONN_NAME = "DB_CONN_NAME";

    public static final String DB_URL = "DB_URL";

    public static final String DB_USERNAME = "DB_USERNAME";

    public static final String USER_NAME = "USER_NAME";

    public static final String REAL_NAME = "REAL_NAME";

    public static final String IP_ADDRESS = "IP_ADDRESS";

    public static final String SCHEMA_NAME = "SCHEMA_NAME";

    public static final String TABLE_NAME = "TABLE_NAME";

    public static final String TABLE_COLUMN = "TABLE_COLUMN";

    public static final String SQL_COMMAND = "SQL_COMMAND";

    public static final String SQL_OPERATION = "SQL_OPERATION";

    public static final String RESULT_DETAIL = "RESULT_DETAIL";

    public static final String COMMENTS = "COMMENTS";

    public static final String CREATETIME = "CREATETIME";

    public static final String DB_ID = "DB_ID";

    public static final String USER_ID = "USER_ID";

    public static final String IS_DELETE = "IS_DELETE";

    public static final String ROW_COUNT = "ROW_COUNT";

    public static final String EXEC_TIME = "EXEC_TIME";

    public static final String DATA_LABELS = "DATA_LABELS";

    public static final String ACTION_TYPE = "ACTION_TYPE";
}
