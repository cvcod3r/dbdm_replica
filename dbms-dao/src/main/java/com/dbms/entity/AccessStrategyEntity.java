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
@TableName("ACCESS_STRATEGY")
public class AccessStrategyEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "STRATEGY_ID", type = IdType.AUTO)
    private Integer strategyId;


    /**
     * 0：启用，1：禁用
     */
    @TableField("STATUS")
    private Integer status;

    /**
     * 注释
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
     * 外键，关联数据源
     */
    @TableField("DB_ID")
    private Integer dbId;

    /**
     * 外键，关联用户组
     */
    @TableField("GROUP_ID")
    private Integer groupId;

    /**
     * 外键，关联高危操作
     */
    @TableField("RISK_ID")
    private Integer riskId;

    /**
     * 外键，关联，用户
     */
    @TableField("USER_ID")
    private Integer userId;

    @TableField("USERNAME")
    private String username;

    @TableField("GROUP_NAME")
    private String groupName;

    @TableField("URL")
    private String url;

    @TableField("ACTION_TYPE")
    private String actionType;

    @TableField(exist = false)
    private String riskActionTypeMap;

    public static final String STRATEGY_ID = "STRATEGY_ID";

//    public static final String RISK_STATUS = "RISK_STATUS";

    public static final String STATUS = "STATUS";

    public static final String COMMENTS = "COMMENTS";

    public static final String CREATETIME = "CREATETIME";

    public static final String UPDATETIME = "UPDATETIME";

    public static final String IS_DELETE = "IS_DELETE";

    public static final String DB_ID = "DB_ID";

//    public static final String ACCOUNT_ID = "ACCOUNT_ID";

    public static final String GROUP_ID = "GROUP_ID";

    public static final String RISK_ID = "RISK_ID";

    public static final String USER_ID = "USER_ID";

    public static final String USERNAME = "USERNAME";

    public static final String GROUP_NAME = "GROUP_NAME";

    public static final String URL = "URL";

    public static final String ACTION_TYPE = "ACTION_TYPE";

}
