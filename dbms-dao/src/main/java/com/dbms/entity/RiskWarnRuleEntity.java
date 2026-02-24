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
 * @author YSL
 * @since 2023-05-12
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("RISK_WARN_RULE")
public class RiskWarnRuleEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("RULE_ID")
    private Integer ruleId;

    @TableField("RULE_NAME")
    private String ruleName;

    @TableField("RULE_DESCRIPTION")
    private String ruleDescription;

    @TableField("SCOPE_APPLY")
    private String scopeApply;

    @TableField("RULE_TYPE")
    private String ruleType;

    @TableField("RISK_LEVEL")
    private String riskLevel;

    @TableField("TEST_CASE")
    private String testCase;


    public static final String RULE_ID = "RULE_ID";

    public static final String RULE_NAME = "RULE_NAME";

    public static final String RULE_DESCRIPTION = "RULE_DESCRIPTION";

    public static final String SCOPE_APPLY = "SCOPE_APPLY";

    public static final String RULE_TYPE = "RULE_TYPE";

    public static final String RISK_LEVEL = "RISK_LEVEL";

    public static final String TEST_CASE = "TEST_CASE";

}
