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
 * @author YSL
 * @since 2023-02-07
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("DBASE_ACCESS")
public class DbaseAccessEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "ACCESS_ID", type = IdType.AUTO)
    private Integer accessId;

    /**
     * 连接串
     */
    @TableField("URL")
    private String url;

    /**
     * 连接类型
     */
    @TableField("DB_TYPE")
    private String dbType;

    /**
     * 数据源类型
     */
    @TableField("DS_TYPE")
    private String dsType;

    /**
     * 用户组名
     */
    @TableField("GROUP_NAME")
    private String groupName;

    /**
     * 用户名
     */
    @TableField("UNAME")
    private String uname;

    /**
     * 账户名
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
     * 外键，用户组ID
     */
    @TableField("GROUP_ID")
    private Integer groupId;

    /**
     * 外键，用户ID
     */
    @TableField("USER_ID")
    private Integer userId;

    /**
     * 外键，用户ID
     */
    @TableField("ACCOUNT_ID")
    private Integer accountId;

    public static final String ACCESS_ID = "ACCESS_ID";

    public static final String URL = "URL";

    public static final String DB_TYPE = "DB_TYPE";

    public static final String DS_TYPE = "DS_TYPE";

    public static final String GROUP_NAME = "GROUP_NAME";

    public static final String USER_NAME = "USER_NAME";

    public static final String USERNAME = "USERNAME";

    public static final String PASSWORD = "PASSWORD";

    public static final String STATUS = "STATUS";

    public static final String COMMENTS = "COMMENTS";

    public static final String IS_DELETE = "IS_DELETE";

    public static final String DB_ID = "DB_ID";

    public static final String GROUP_ID = "GROUP_ID";

    public static final String USER_ID = "USER_ID";

    public static final String ACCOUNT_ID = "ACCOUNT_ID";
}
