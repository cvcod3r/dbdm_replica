package com.dbms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
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
@TableName("SQL_GENERAL")
public class SqlGeneralEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    /**
     * 图标
     */
    @TableField("ICON")
    private String icon;

    /**
     * 顺序
     */
    @TableField("SORT")
    private String sort;

    /**
     * 标题
     */
    @TableField("TITLE")
    private String title;

    /**
     * 内容
     */
    @TableField("CONTENT")
    private String content;

    @TableField("PID")
    private Integer pid;

    @TableField("NOTE")
    private String note;

    @TableField(exist = false)
    private List<SqlGeneralEntity> children;

    public static final String ID = "ID";

    public static final String ICON = "ICON";

    public static final String SORT = "SORT";

    public static final String TITLE = "TITLE";

    public static final String CONTENT = "CONTENT";

    public static final String PID = "PID";

    public static final String NOTE = "NOTE";

}
