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
@TableName("LOG_SYSTEM")
public class LogSystemEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "SYS_LOG_ID", type = IdType.AUTO)
    private Long sysLogId;

    /**
     * 用户名
     */
    @TableField("USER_NAME")
    private String userName;

    /**
     * 真实姓名
     */
    @TableField("REAL_NAME")
    private String realName;

    /**
     * 模块名称
     */
    @TableField("MODULE_NAME")
    private String moduleName;

    /**
     * 模块名称
     */
    @TableField("CHILD_MODULE")
    private String childModule;

    /**
     * 操作
     */
    @TableField("OPERATION")
    private String operation;

    /**
     * 操作过程信息
     */
    @TableField("PROCEDURE_INFO")
    private String procedureInfo;

    /**
     * IP地址
     */
    @TableField("IP_ADDRESS")
    private String ipAddress;

    /**
     * 操作结果
     */
    @TableField("RESULT_DETAIL")
    private String resultDetail;

    /**
     * 创建时间
     */
    @TableField("CREATETIME")
    private LocalDateTime createtime;

    /**
     * 外键，用户ID
     */
    @TableField("USER_ID")
    private Integer userId;


    public static final String SYS_LOG_ID = "SYS_LOG_ID";

    public static final String USER_NAME = "USER_NAME";

    public static final String REAL_NAME = "REAL_NAME";

    public static final String MODULE_NAME = "MODULE_NAME";

    public static final String CHILD_MODULE = "CHILD_MODULE";

    public static final String OPERATION = "OPERATION";

    public static final String PROCEDURE_INFO = "PROCEDURE_INFO";

    public static final String IP_ADDRESS = "IP_ADDRESS";

    public static final String RESULT_DETAIL = "RESULT_DETAIL";

    public static final String CREATETIME = "CREATETIME";

    public static final String USER_ID = "USER_ID";

}
