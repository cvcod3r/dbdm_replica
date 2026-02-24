package com.dbms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
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
@TableName("DBASE_ACCOUNT")
public class DbaseAccountEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "ACCOUNT_ID", type = IdType.AUTO)
    private Integer accountId;

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
     * 状态：0：启用，1：禁用
     */
    @TableField("STATUS")
    private Integer status;

    /**
     * 备注
     */
    @TableField("COMMENTS")
    private String comments;

    /**
     * 0：未删除，1：已删除
     */
    @TableField("IS_DELETE")
    private Integer isDelete;

    /**
     * 外键，数据源ID
     */
    @TableField("DB_ID")
    private Integer dbId;

    /**
     * 数据源URl
     */
    @TableField("URL")
    private String url;

    /**
     * 数据源连接名
     */
    @TableField("CONN_NAME")
    private String connName;

    public static final String ACCOUNT_ID = "ACCOUNT_ID";

    public static final String USERNAME = "USERNAME";

    public static final String PASSWORD = "PASSWORD";

    public static final String STATUS = "STATUS";

    public static final String COMMENTS = "COMMENTS";

    public static final String IS_DELETE = "IS_DELETE";

    public static final String DB_ID = "DB_ID";

    public static final String URL = "URL";

    public static final String CONN_NAME = "CONN_NAME";

}
