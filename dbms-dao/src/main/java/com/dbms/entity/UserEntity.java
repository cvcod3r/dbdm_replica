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
@TableName("\"USER\"")
public class UserEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键，用户ID
     */
    @TableId(value = "USER_ID", type = IdType.AUTO)
    private Integer userId;

    /**
     * 用户名
     */
    @TableField("USER_NAME")
    private String userName;

    /**
     * 密码
     */
    @TableField("PASSWORD")
    private String password;

    /**
     * 用户真实姓名
     */
    @TableField("REALNAME")
    private String realname;

    /**
     * 电话
     */
    @TableField("MOBILE_NUMBER")
    private String mobileNumber;

    /**
     * 邮箱
     */
    @TableField("E_MAIL")
    private String eMail;

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
     * 用户组
     */
    @TableField("GROUP_ID")
    private Integer groupId;

    /**
     * 角色
     */
    @TableField("ROLE_ID")
    private Integer roleId;

    /**
     * 角色名
     */
    @TableField("ROLE_NAME")
    private String roleName;

    /**
     * 用户组名
     */
    @TableField("GROUP_NAME")
    private String groupName;

    /**
     * 旧密码验证
     */
    @TableField(exist = false)
    private String oldPassword;

    public static final String USER_ID = "USER_ID";

    public static final String USER_NAME = "USER_NAME";

    public static final String PASSWORD = "PASSWORD";

    public static final String REALNAME = "REALNAME";

    public static final String MOBILE_NUMBER = "MOBILE_NUMBER";

    public static final String E_MAIL = "E_MAIL";

    public static final String STATUS = "STATUS";

    public static final String COMMENTS = "COMMENTS";

    public static final String CREATETIME = "CREATETIME";

    public static final String UPDATETIME = "UPDATETIME";

    public static final String IS_DELETE = "IS_DELETE";

    public static final String GROUP_ID = "GROUP_ID";

    public static final String ROLE_ID = "ROLE_ID";

    public static final String ROLE_NAME = "ROLE_NAME";

    public static final String GROUP_NAME = "GROUP_NAME";

}
