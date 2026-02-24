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
@TableName("DATA_RULE")
public class DataRuleEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "RULE_ID", type = IdType.AUTO)
    private Integer ruleId;

    /**
     * 规则名
     */
    @TableField("RULE_NAME")
    private String ruleName;

    /**
     * 正则表达式
     */
    @TableField("RULE_REG")
    private String ruleReg;

    /**
     * SQL代码模板片段
     */
    @TableField("RULE_CODE")
    private String ruleCode;

    /**
     * 数据样例
     */
    @TableField("DATA_EXAMPLE")
    private String dataExample;

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
     * 0：已删除，1：未删除
     */
    @TableField("IS_DELETE")
    private Integer isDelete;


    public static final String RULE_ID = "RULE_ID";

    public static final String RULE_NAME = "RULE_NAME";

    public static final String RULE_REG = "RULE_REG";

    public static final String RULE_CODE = "RULE_CODE";

    public static final String DATA_EXAMPLE = "DATA_EXAMPLE";

    public static final String STATUS = "STATUS";

    public static final String COMMENTS = "COMMENTS";

    public static final String CREATETIME = "CREATETIME";

    public static final String UPDATETIME = "UPDATETIME";

    public static final String IS_DELETE = "IS_DELETE";

}
