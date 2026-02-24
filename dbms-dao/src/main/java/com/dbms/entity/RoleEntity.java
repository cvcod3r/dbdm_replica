package com.dbms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

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
@TableName("\"ROLE\"")
public class RoleEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键，用户ID
     */
    @TableId(value = "ROLE_ID", type = IdType.AUTO)
    private Integer roleId;

    /**
     * 角色名
     */
    @TableField("ROLE_NAME")
    private String roleName;

    /**
     * 权限关键字
     */
    @TableField("ROLE_KEY")
    private String roleKey;

    /**
     * 角色状态，0：启用，1：禁用
     */
    @TableField("STATUS")
    private Integer status;

    /**
     * 权限级别
     */
    @TableField("PERMISSION")
    private Integer permission;

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

//    /**
//     * 更新时间
//     */
//    @TableField("UPDATETIME")
//    private LocalDateTime updatetime;

    /**
     * 0：未删除，1：已删除
     */
    @TableField("IS_DELETE")
    private Integer isDelete;

    /**
     * 菜单组
     * */
    @TableField(exist = false)
    private List<Integer> menuIds;

    public static final String ROLE_ID = "ROLE_ID";

    public static final String ROLE_NAME = "ROLE_NAME";

    public static final String ROLE_KEY = "ROLE_KEY";

    public static final String STATUS = "STATUS";

    public static final String PERMISSION = "PERMISSION";

    public static final String COMMENTS = "COMMENTS";

    public static final String CREATETIME = "CREATETIME";

    public static final String UPDATETIME = "UPDATETIME";

    public static final String IS_DELETE = "IS_DELETE";

}
