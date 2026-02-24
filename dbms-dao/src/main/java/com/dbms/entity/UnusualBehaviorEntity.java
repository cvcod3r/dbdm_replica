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
 * @author HLH
 * @since 2023-06-15
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("UNUSUAL_BEHAVIOR")
public class UnusualBehaviorEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 异常行为ID
     */
    @TableId(value = "BEHAVIOR_ID", type = IdType.AUTO)
    private Integer behaviorId;

    /**
     * 用户名
     */
    @TableField("USERNAME")
    private String username;

    /**
     * 用户ID
     */
    @TableField("USER_ID")
    private Integer userId;

    /**
     * 异常行为类型（超出次数限制，非限定时间操作）
     */
    @TableField("ALTER_TYPE")
    private String alterType;

    /**
     * 时间限制
     */
    @TableField("TIME_LIMIT")
    private String timeLimit;

    /**
     * 次数限制
     */
    @TableField("NUM_LIMIT")
    private Integer numLimit;

    /**
     * 0：启用，1：禁用
     */
    @TableField("STATUS")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("CREATETIME")
    private LocalDateTime createtime;

    /**
     * 修改时间
     */
    @TableField("UPDATETIME")
    private LocalDateTime updatetime;


    public static final String BEHAVIOR_ID = "BEHAVIOR_ID";

    public static final String USERNAME = "USERNAME";

    public static final String USER_ID = "USER_ID";

    public static final String ALTER_TYPE = "ALTER_TYPE";

    public static final String TIME_LIMIT = "TIME_LIMIT";

    public static final String NUM_LIMIT = "NUM_LIMIT";

    public static final String STATUS = "STATUS";

    public static final String CREATETIME = "CREATETIME";

    public static final String UPDATETIME = "UPDATETIME";

}
