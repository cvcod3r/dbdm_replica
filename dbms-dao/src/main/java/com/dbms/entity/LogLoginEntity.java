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
@TableName("LOG_LOGIN")
public class LogLoginEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "LOGIN_ID", type = IdType.AUTO)
    private Long loginId;

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
     * 0：登录失败，1：登录成功
     */
    @TableField("LOGIN_STATUS")
    private String loginStatus;

    /**
     * 登录时间
     */
    @TableField("CREATETIME")
    private LocalDateTime createtime;

    /**
     * 外键，关联用户
     */
    @TableField("USER_ID")
    private Integer userId;

    /**
     * 说明
     */
    @TableField("COMMENTS")
    private String comments;

    public static final String LOGIN_ID = "LOGIN_ID";

    public static final String USER_NAME = "USER_NAME";

    public static final String REAL_NAME = "REAL_NAME";

    public static final String IP_ADDRESS = "IP_ADDRESS";

    public static final String LOGIN_STATUS = "LOGIN_STATUS";

    public static final String CREATETIME = "CREATETIME";

    public static final String USER_ID = "USER_ID";

    public static final String COMMENTS = "COMMENTS";
}
