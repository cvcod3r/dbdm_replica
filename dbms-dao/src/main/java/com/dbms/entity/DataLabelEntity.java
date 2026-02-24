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
@TableName("DATA_LABEL")
public class DataLabelEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "LABEL_ID", type = IdType.AUTO)
    private Integer labelId;

    /**
     * 标签名
     */
    @TableField("LABEL_NAME")
    private String labelName;

    /**
     * 状态
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
     * 规则
     */
    @TableField("RULE_ID")
    private Integer ruleId;

    /**
     * SHIELD：遮蔽，SIMULATE：仿真，FORWARD：正向，REVERSE：逆向
     */
    @TableField("LABEL_TYPE")
    private String labelType;

    /**
     * 参数
     */
    @TableField("PARAM")
    private String param;

    /**
     * 正则表达式
     */
    @TableField("LABEL_REG")
    private String labelReg;

    /**
     * 0：不可修改，1：可修改
     */
    @TableField("MODIFY")
    private Integer modify;

    public static final String LABEL_ID = "LABEL_ID";

    public static final String LABEL_NAME = "LABEL_NAME";

    public static final String STATUS = "STATUS";

    public static final String COMMENTS = "COMMENTS";

    public static final String CREATETIME = "CREATETIME";

    public static final String UPDATETIME = "UPDATETIME";

    public static final String IS_DELETE = "IS_DELETE";

    public static final String RULE_ID = "RULE_ID";

    public static final String LABEL_TYPE = "LABEL_TYPE";

    public static final String PARAM = "PARAM";

    public static final String LABEL_REG = "LABEL_REG";

    public static final String MODIFY = "MODIFY";
}
