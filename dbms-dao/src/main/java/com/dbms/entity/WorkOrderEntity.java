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
 * @author YSL
 * @since 2023-05-24
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("WORK_ORDER")
public class WorkOrderEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "WORK_ORDER_ID", type = IdType.AUTO)
    private Integer workOrderId;

    @TableField("USER_ID")
    private Integer userId;

    @TableField("USERNAME")
    private String username;

    @TableField("REAL_NAME")
    private String realName;

    @TableField("DB_ID")
    private Integer dbId;

    @TableField("DB_URL")
    private String dbUrl;

    @TableField("DB_TYPE")
    private String dbType;

    @TableField("SQL_STATEMENT")
    private String sqlStatement;

    @TableField("LIMIT_COUNT")
    private Integer limitCount;

    @TableField("PROCESS_STATUS")
    private Integer processStatus;

    @TableField("PROCESS_RESULT")
    private Integer processResult;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;

    @TableField("PROCESS_TIME")
    private LocalDateTime processTime;

    @TableField("PROCESSOR")
    private String processor;

    @TableField("PROCESSOR_ID")
    private Integer processorId;

    @TableField("PROCESS_COMMENT")
    private String processComment;

    @TableField("REMARKS")
    private String remarks;

    @TableField("IS_DELETE")
    private Integer isDelete;

    @TableField("STATUS")
    private Integer status;


    public static final String WORK_ORDER_ID = "WORK_ORDER_ID";

    public static final String USER_ID = "USER_ID";

    public static final String USERNAME = "USERNAME";

    public static final String REAL_NAME = "REAL_NAME";

    public static final String DB_ID = "DB_ID";

    public static final String DB_URL = "DB_URL";

    public static final String DB_TYPE = "DB_TYPE";

    public static final String SQL_STATEMENT = "SQL_STATEMENT";

    public static final String LIMIT_COUNT = "LIMIT_COUNT";

    public static final String PROCESS_STATUS = "PROCESS_STATUS";

    public static final String PROCESS_RESULT = "PROCESS_RESULT";

    public static final String CREATE_TIME = "CREATE_TIME";

    public static final String PROCESS_TIME = "PROCESS_TIME";

    public static final String PROCESSOR = "PROCESSOR";

    public static final String PROCESSOR_ID = "PROCESSOR_ID";

    public static final String PROCESS_COMMENT = "PROCESS_COMMENT";

    public static final String REMARKS = "REMARKS";

    public static final String IS_DELETE = "IS_DELETE";

    public static final String STATUS = "STATUS";

}
