package com.dbms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
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
@TableName("DBASE")
public class DbaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;


    public DbaseEntity deepClone(){
        DbaseEntity newDbaseEntity = new DbaseEntity();
        newDbaseEntity.setDbId(this.getDbId());
        newDbaseEntity.setHost(this.getHost());
        newDbaseEntity.setPort(this.getPort());
        newDbaseEntity.setUsername(this.getUsername());
        newDbaseEntity.setPassword(this.getPassword());
        newDbaseEntity.setDbType(this.getDbType());
        newDbaseEntity.setVersion(this.getVersion());
        newDbaseEntity.setStatus(this.getStatus());
        newDbaseEntity.setComments(this.getComments());
        newDbaseEntity.setCreatetime(this.getCreatetime());
        newDbaseEntity.setUpdatetime(this.getUpdatetime());
        newDbaseEntity.setIsDelete(this.getIsDelete());
        newDbaseEntity.setDsType(this.getDsType());
        newDbaseEntity.setUrl(this.getUrl());
        newDbaseEntity.setDbName(this.getDbName());
        newDbaseEntity.setConnName(this.getConnName());
        newDbaseEntity.setDbDriver(this.getDbDriver());
        newDbaseEntity.setSchemaName(this.getSchemaName());
        return newDbaseEntity;
    }
    /**
     * 数据源ID
     */
    @TableId(value = "DB_ID", type = IdType.AUTO)
    private Integer dbId;

    /**
     * IP地址
     */
    @TableField("HOST")
    private String host;

    /**
     * 端口号
     */
    @TableField("PORT")
    private String port;

    /**
     * 用户名
     */
    @TableField("USERNAME")
    private String username;

    /**
     * 密码
     */
    @TableField("PASSWORD")
    private String password;

    /**
     * ssh用户名
     */
    @TableField("SSH_USERNAME")
    private String sshUsername;

    /**
     * ssh密码
     */
    @TableField("SSH_PASSWORD")
    private String sshPassword;

    /**
     * 数据库类型
     */
    @TableField("DB_TYPE")
    private String dbType;

    /**
     * 版本
     */
    @TableField("VERSION")
    @Version
    private String version;

    /**
     * 0：启用，1：禁用
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
     * 数据源类型，国产数据库：S，大数据：N
     */
    @TableField("DS_TYPE")
    private String dsType;

    /**
     * 连接串
     */
    @TableField("URL")
    private String url;

    @TableField("DB_NAME")
    private String dbName;

    @TableField("CONN_NAME")
    private String connName;

    @TableField("DB_DRIVER")
    private String dbDriver;

    @TableField("ICON_OPEN")
    private String iconOpen;
    @TableField("ICON_CLOSE")
    private String iconClose;
    @TableField("URL_PREFIX")
    private String urlPrefix;
    @TableField("URL_DIR")
    private String urlDir;

    @TableField(exist = false)
    private String schemaName;

    public static final String DB_ID = "DB_ID";

    public static final String HOST = "HOST";

    public static final String PORT = "PORT";

    public static final String USERNAME = "USERNAME";

    public static final String PASSWORD = "PASSWORD";

    public static final String SSH_USERNAME = "SSH_USERNAME";

    public static final String SSH_PASSWORD = "SSH_PASSWORD";

    public static final String DB_TYPE = "DB_TYPE";

    public static final String VERSION = "VERSION";

    public static final String STATUS = "STATUS";

    public static final String COMMENTS = "COMMENTS";

    public static final String CREATETIME = "CREATETIME";

    public static final String UPDATETIME = "UPDATETIME";

    public static final String IS_DELETE = "IS_DELETE";

    public static final String DS_TYPE = "DS_TYPE";

    public static final String URL = "URL";

    public static final String DB_NAME = "DB_NAME";

    public static final String CONN_NAME = "CONN_NAME";

    public static final String DB_DRIVER = "DB_DRIVER";
    public static final String ICON_OPEN = "ICON_OPEN";
    public static final String ICON_CLOSE = "ICON_CLOSE";
    public static final String URL_PREFIX = "URL_PREFIX";
    public static final String URL_DIR = "URL_DIR";
}
