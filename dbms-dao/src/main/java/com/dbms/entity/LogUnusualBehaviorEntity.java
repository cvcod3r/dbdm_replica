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
@TableName("LOG_UNUSUAL_BEHAVIOR")
public class LogUnusualBehaviorEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 行为ID
     */
    @TableId(value = "BEHAVIOR_ID", type = IdType.AUTO)
    private Integer behaviorId;

    /**
     * 用户名称
     */
    @TableField("USERNAME")
    private String username;

    /**
     * 异常行为告警类型
     */
    @TableField("ALTER_TYPE")
    private String alterType;

    /**
     * 时间
     */
    @TableField("CREATETIME")
    private LocalDateTime createtime;

    /**
     * 用户ID
     */
    @TableField("USER_ID")
    private Integer userId;

    /**
     * 连接串
     */
    @TableField("URL")
    private String url;


    public static final String BEHAVIOR_ID = "BEHAVIOR_ID";

    public static final String USERNAME = "USERNAME";

    public static final String ALTER_TYPE = "ALTER_TYPE";

    public static final String CREATETIME = "CREATETIME";

    public static final String USER_ID = "USER_ID";

    public static final String URL = "url";
}
