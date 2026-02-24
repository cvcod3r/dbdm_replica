package com.dbms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * @since 2023-02-26
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("REPORT_MONITOR_INFO")
public class ReportMonitorInfoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "INFO_ID", type = IdType.AUTO)
    private Integer infoId;

    /**
     * cpu当前使用率百分比,取值：0-100
     */
    @TableField("CPU_USAGE")
    private String cpuUsage;

    /**
     * 内存当前使用率百分比,取值：0-100
     */
    @TableField("MEM_USAGE")
    private String memUsage;

    /**
     * 磁盘当前使用率百分比,取值：0-100
     */
    @TableField("DISK_USAGE")
    private String diskUsage;

    /**
     * webConsole服务，1开启，0关闭
     */
    @TableField("WEB_CONSOLE_SERVICE")
    private Integer webConsoleService;

    /**
     * 日志接收服务，1开启，0关闭
     */
    @TableField("LOG_ACCEP_SERVICE")
    private Integer logAccepService;

    /**
     * 软件名称
     */
    @TableField("NAME")
    private String name;


    public static final String INFO_ID = "INFO_ID";

    public static final String CPU_USAGE = "CPU_USAGE";

    public static final String MEM_USAGE = "MEM_USAGE";

    public static final String DISK_USAGE = "DISK_USAGE";

    public static final String WEB_CONSOLE_SERVICE = "WEB_CONSOLE_SERVICE";

    public static final String LOG_ACCEP_SERVICE = "LOG_ACCEP_SERVICE";

    public static final String NAME = "NAME";

}
