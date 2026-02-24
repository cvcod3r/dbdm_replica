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
 * @author
 * @since 2023-03-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("SENSITIVE_SCAN_RESULT")
public class SensitiveScanResultEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据源ID
     */
    @TableId(value = "DB_ID")
    private Integer dbId;

    /**
     * 数据源连接串
     */
    @TableField("URL")
    private String url;

    /**
     * 数据库名
     */
    @TableField("SCHEMA_NAME")
    private String schemaName;

    /**
     * 表名
     */
    @TableField("TABLE_NAME")
    private String tableName;

    /**
     * 列名
     */
    @TableField("COLUMN_NAME")
    private String columnName;

    /**
     * 列名称结果
     */
    @TableField("COLUMN_NAME_RESULT")
    private String columnNameResult;

    /**
     * 列备注结果
     */
    @TableField("COLUMN_REMARK_RESULT")
    private String columnRemarkResult;

    /**
     * 列内容结果
     */
    @TableField("COLUMN_CONTENT_RESULT")
    private String columnContentResult;

    /**
     * 扫描时间
     */
    @TableField("SCAN_TIME")
    private LocalDateTime scanTime;

    @TableField(exist = false)
    private List<Integer> columnNameResultList;

    @TableField(exist = false)
    private List<Integer> columnRemarkResultList;

    @TableField(exist = false)
    private List<Integer> columnContentResultList;

    public void stringToObject(){
        this.columnNameResultList = JSON.parseArray(this.columnNameResult, Integer.class);
        this.columnRemarkResultList = JSON.parseArray(this.columnRemarkResult, Integer.class);
        this.columnContentResultList = JSON.parseArray(this.columnContentResult, Integer.class);
    }


    public static final String DB_ID = "DB_ID";

    public static final String URL = "URL";

    public static final String SCHEMA_NAME = "SCHEMA_NAME";

    public static final String TABLE_NAME = "TABLE_NAME";

    public static final String COLUMN_NAME = "COLUMN_NAME";

    public static final String COLUMN_NAME_RESULT = "COLUMN_NAME_RESULT";

    public static final String COLUMN_REMARK_RESULT = "COLUMN_REMARK_RESULT";

    public static final String COLUMN_CONTENT_RESULT = "COLUMN_CONTENT_RESULT";

    public static final String SCAN_TIME = "SCAN_TIME";

}
