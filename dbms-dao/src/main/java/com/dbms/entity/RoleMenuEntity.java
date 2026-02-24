package com.dbms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName("\"ROLE_MENU\"")
public class RoleMenuEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @TableField("ROLE_ID")
    private Integer roleId;

    /**
     * 菜单ID
     */
    @TableField("MENU_ID")
    private Integer menuId;


    public static final String ROLE_ID = "ROLE_ID";

    public static final String MENU_ID = "MENU_ID";

}
