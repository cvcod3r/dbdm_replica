package com.dbms.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
@TableName("RISK_OPERATION")
public class RiskOperationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "RISK_ID", type = IdType.AUTO)
    private Integer riskId;

    /**
     * 高危操作关键字
     */
    @TableField("DS_TYPE")
    private String dsType;

    /**
     * 数据源类型，关系型数据库：S，大数据：N
     */
    @TableField("OPERATION_KEY")
    private String operationKey;

    /**
     * 动作类型：A：会话阻断，B：指令拦截，C：数据脱敏
     */
    @TableField("ACTION_TYPE")
    private String actionType;

    /**
     * 模式名
     */
    @TableField("SCHEMA_NAME")
    private String schemaName;

    /**
     * 模式权限
     */
    @TableField("SCHEMA_PRIVILEGE")
    private String schemaPrivilege;

    /**
     * 表名
     */
    @TableField("TABLE_NAME")
    private String tableName;

    /**
     * 表权限
     */
    @TableField("TABLE_PRIVILEGE")
    private String tablePrivilege;

    /**
     * 列权限
     */
    @TableField("COLUMN_PRIVILEGE")
    private String columnPrivilege;

    /**
     * 是否关联标签，0：未关联，1：已关联
     */
    @TableField("LABEL_STATUS")
    private Integer labelStatus;

    /**
     * 状态，0：启用，1：禁用
     */
    @TableField("STATUS")
    private Integer status;

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
     * 更新时间
     */
    @TableField("UPDATETIME")
    private LocalDateTime updatetime;

    /**
     * 0：未删除，1：已删除
     */
    @TableField("IS_DELETE")
    private Integer isDelete;

    /**
     * 外键，关联敏感数据标签
     */
    @TableField("LABEL_ID")
    private Integer labelId;

    /**
     * 外键，关联数据源
     */
    @TableField("DB_ID")
    private Integer dbId;

    @TableField("URL")
    private String url;

    /**
     * 列敏感数据标签
     */
    @TableField("COLUMN_SENSITIVE")
    private String columnSensitive;

    @TableField("TABLE_ROW_LIMIT")
    private String tableRowLimit;

    @TableField(exist = false)
    private List<String> tableNameList;

    @TableField(exist = false)
    private List<String> schemaPrivilegeList;

    @TableField(exist = false)
    private Map<String, List<String>> tablePrivilegeMap;

    @TableField(exist = false)
    private Map<String, Map<String, List<String>>> columnPrivilegeMap;

    @TableField(exist = false)
    private Map<String, Map<String, Integer>> columnSensitiveMap;

    @TableField(exist = false)
    private Map<String, Integer> tableRowLimitMap;

    public void copyToString(){
        this.tableName = JSON.toJSONString(this.tableNameList);
        this.columnPrivilege = JSON.toJSONString(this.columnPrivilegeMap);
        this.schemaPrivilege = JSON.toJSONString(this.schemaPrivilegeList);
        this.tablePrivilege = JSON.toJSONString(this.tablePrivilegeMap);
        this.columnSensitive = JSON.toJSONString(this.columnSensitiveMap);
        this.tableRowLimit = JSON.toJSONString(this.tableRowLimitMap);
    }

    public void stringToObject(){
        this.tableNameList = JSON.parseArray(this.tableName, String.class);
        this.columnPrivilegeMap = JSONObject.parseObject(this.columnPrivilege, new TypeReference<Map<String, Map<String, List<String>>>>() {});
        this.schemaPrivilegeList = JSON.parseArray(this.schemaPrivilege, String.class);
        this.tablePrivilegeMap = JSON.parseObject(this.tablePrivilege, new TypeReference<Map<String, List<String>>>() {});
        this.columnSensitiveMap = JSON.parseObject(this.columnSensitive, new TypeReference<Map<String, Map<String, Integer>>>() {});
        this.tableRowLimitMap = JSON.parseObject(this.tableRowLimit, new TypeReference<Map<String, Integer>>() {});
    }

    public static final String RISK_ID = "RISK_ID";

    public static final String DS_TYPE = "DS_TYPE";

    public static final String URL = "URL";

    public static final String OPERATION_KEY = "OPERATION_KEY";

    public static final String ACTION_TYPE = "ACTION_TYPE";

    public static final String SCHEMA_NAME = "SCHEMA_NAME";

    public static final String SCHEMA_PRIVILEGE = "SCHEMA_PRIVILEGE";

    public static final String TABLE_NAME = "TABLE_NAME";

    public static final String TABLE_PRIVILEGE = "TABLE_PRIVILEGE";

    public static final String COLUMN_PRIVILEGE = "COLUMN_PRIVILEGE";

    public static final String LABEL_STATUS = "LABEL_STATUS";

    public static final String STATUS = "STATUS";

    public static final String COMMENTS = "COMMENTS";

    public static final String CREATETIME = "CREATETIME";

    public static final String UPDATETIME = "UPDATETIME";

    public static final String IS_DELETE = "IS_DELETE";

    public static final String LABEL_ID = "LABEL_ID";

    public static final String DB_ID = "DB_ID";
    public static final String COLUMN_SENSITIVE = "COLUMN_SENSITIVE";

    public static final String TABLE_ROW_LIMIT = "TABLE_ROW_LIMIT";

}
