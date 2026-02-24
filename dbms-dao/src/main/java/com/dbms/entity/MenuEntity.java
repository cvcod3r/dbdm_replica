package com.dbms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.ArrayList;
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
@TableName("\"MENU\"")
public class MenuEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "MENU_ID", type = IdType.AUTO)
    private Integer menuId;

    /**
     * 菜单名称
     */
    @TableField("MENU_NAME")
    private String menuName;

    /**
     * 父菜单ID
     */
    @TableField("PARENT_ID")
    private Integer parentId;

    /**
     * 菜单类型（M目录C菜单F按钮）
     */
    @TableField("MENU_TYPE")
    private String menuType;

    /**
     * 权限字符
     */
    @TableField("PERMISSION")
    private String permission;

    /**
     * 备注
     */
    @TableField("COMMENTS")
    private String comments;

    /**
     * 路由
     */
    @TableField("PATH")
    private String path;

    /**
     * 组件路径
     */
    @TableField("COMPONENT")
    private String component;

    /**
     * 组件参数
     */
    @TableField("QUERY")
    private String query;

    /** 子菜单 */
    @TableField(exist = false)
    private List<MenuEntity> children = new ArrayList<MenuEntity>();

    public static final String MENU_ID = "MENU_ID";

    public static final String MENU_NAME = "MENU_NAME";

    public static final String PARENT_ID = "PARENT_ID";

    public static final String MENU_TYPE = "MENU_TYPE";

    public static final String PERMISSION = "PERMISSION";

    public static final String COMMENTS = "COMMENTS";

    public static final String PATH = "PATH";

    public static final String COMPONENT = "COMPONENT";

    public static final String QUERY = "QUERY";

}
