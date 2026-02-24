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
@TableName("USER_GROUP")
public class UserGroupEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键，用户组ID
     */
    @TableId(value = "GROUP_ID", type = IdType.AUTO)
    private Long groupId;

    /**
     * 用户组名
     */
    @TableField("GROUP_NAME")
    private String groupName;

    /**
     * 状态：0：警用，1：启用
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
     * 是否删除：0：未删除，1：已删除
     */
    @TableField("IS_DELETE")
    private Integer isDelete;


    public static final String GROUP_ID = "GROUP_ID";

    public static final String GROUP_NAME = "GROUP_NAME";

    public static final String STATUS = "STATUS";

    public static final String COMMENTS = "COMMENTS";

    public static final String CREATETIME = "CREATETIME";

    public static final String UPDATETIME = "UPDATETIME";

    public static final String IS_DELETE = "IS_DELETE";

}
