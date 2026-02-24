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
@TableName("SQL_NOTE")
public class SqlNoteEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "NOTE_ID", type = IdType.AUTO)
    private Integer noteId;

    /**
     * 笔记名称
     */
    @TableField("NOTE_NAME")
    private String noteName;

    /**
     * 笔记内容
     */
    @TableField("SQLS")
    private String sqls;

    /**
     * 外键，用户ID
     */
    @TableField("USER_ID")
    private Integer userId;

    /**
     * 外键，数据源ID
     */
    @TableField("DB_ID")
    private Integer dbId;

    /**
     * 创建时间
     */
    @TableField("CREATETIME")
    private LocalDateTime createtime;

    /**
     * 0：未删除，1：已删除
     */
    @TableField("IS_DELETE")
    private Integer isDelete;


    public static final String NOTE_ID = "NOTE_ID";

    public static final String NOTE_NAME = "NOTE_NAME";

    public static final String SQLS = "SQLS";

    public static final String USER_ID = "USER_ID";

    public static final String DB_ID = "DB_ID";

    public static final String CREATETIME = "CREATETIME";

    public static final String IS_DELETE = "IS_DELETE";

}
