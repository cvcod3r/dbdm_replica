package com.dbms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.io.Serializable;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author YSL
 * @since 2023-02-23
 */
@Getter
@Setter
@Data
@Accessors(chain = true)
@TableName("DATABASE_CONFIG")
public class DatabaseConfigEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "DATABASE_ID", type = IdType.AUTO)
    private Integer databaseId;

    /**
     * 数据库名
     */
    @TableField("DATABASE_NAME")
    private String databaseName;

    /**
     * SQL:关系型，NoSQL:大数据
     */
    @TableField("DS_TYPE")
    private String dsType;

    /**
     * 数据源类型
     */
    @TableField("DB_TYPE")
    private String dbType;

    /**
     * 驱动类型
     */
    @TableField("DB_DRIVER")
    private String dbDriver;

    /**
     * 版本信息
     */
    @TableField("VERSION")
    @Version
    private String version;
    /**
     * 图标名
     */
    @TableField("ICON_OPEN")
    private String iconOpen;

    @TableField("ICON_CLOSE")
    private String iconClose;

    @TableField("URL_PREFIX")
    private String urlPrefix;

    @TableField("URL_DIR")
    private String urlDir;

    public static final String DATABASE_ID = "DATABASE_ID";

    public static final String DATABASE_NAME = "DATABASE_NAME";


    public static final String DS_TYPE = "DS_TYPE";


    public static final String DB_TYPE = "DB_TYPE";

    public static final String DB_DRIVER = "DB_DRIVER";

    public static final String ICON_OPEN = "ICON_OPEN";

    public static final String ICON_CLOSE = "ICON_CLOSE";

    public static final String URL_PREFIX = "URL_PREFIX";

    public static final String URL_DIR = "URL_DIR";

    public static final String VERSION = "VERSION";

}
