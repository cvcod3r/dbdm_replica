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
 * @author <a href="https://www.fengwenyi.com?code">YSL</a>
 * @since 2023-02-05
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("DOCKER_MONITOR")
public class DockerMonitorEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    /**
     * CPU信息
     */
    @TableField("CPU_INFO")
    private String cpuInfo;

    /**
     * 内存信息
     */
    @TableField("MEMORY_INFO")
    private String memoryInfo;

    /**
     * 网络带宽
     */
    @TableField("NETWORK_INFO")
    private String networkInfo;


    public static final String ID = "ID";

    public static final String CPU_INFO = "CPU_INFO";

    public static final String MEMORY_INFO = "MEMORY_INFO";

    public static final String NETWORK_INFO = "NETWORK_INFO";

}
