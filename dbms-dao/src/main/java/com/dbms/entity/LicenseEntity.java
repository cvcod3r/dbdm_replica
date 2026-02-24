package com.dbms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
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
 * @author
 * @since 2022-03-29
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("license")
public class LicenseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "license_id", type = IdType.AUTO)
    private Integer licenseId;

    /**
     * 版本
     */
    @TableField("version")
    @Version
    private String version;

    /**
     * 时间
     */
    @TableField("createtime")
    private LocalDateTime createtime;

    /**
     * 许可
     */
    @TableField("license_name")
    private String licenseName;

    /**
     * 公司
     */
    @TableField("company")
    private String company;

    /**
     * 0：未删除；1：已删除
     */
    @TableField("is_delete")
    private Integer isDelete;


}
