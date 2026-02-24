package com.dbms.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author <a href="https://www.fengwenyi.com?code">YSL</a>
 * @since 2023-02-05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DbaseEntityVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据源ID
     */
    private Integer dbId;

    /**
     * IP地址
     */

    private String host;

    /**
     * 端口号
     */
    private String port;

    /**
     * 用户名
     */

    private String username;

    /**
     * 密码
     */

    private String password;

    /**
     * 数据库类型
     */

    private String dbType;

    /**
     * 版本
     */

    private String version;

    /**
     * 0：启用，1：禁用
     */
    private Integer status;

    /**
     * 备注
     */
    private String comments;

    /**
     * 创建时间
     */
    private LocalDateTime createtime;

    /**
     * 更新时间
     */
    private LocalDateTime updatetime;

    /**
     * 0：未删除，1：已删除
     */
    private Integer isDelete;

    /**
     * 数据源类型，国产数据库：S，大数据：N
     */
    private String dsType;

    /**
     * 连接串
     */
    private String url;

    private String dbName;

    private String connName;

    private String schemaName;

    private Integer accessId;

    private Integer userId;

    private Integer groupId;

    private Integer accountId;


    private String iconOpen;

    private String iconClose;

    private String urlPrefix;

    private String urlDir;


    public static final String DB_ID = "DB_ID";

    public static final String HOST = "HOST";

    public static final String PORT = "PORT";

    public static final String USERNAME = "USERNAME";

    public static final String PASSWORD = "PASSWORD";

    public static final String DB_TYPE = "DB_TYPE";

    public static final String VERSION = "VERSION";

    public static final String STATUS = "STATUS";

    public static final String COMMENTS = "COMMENTS";

    public static final String CREATETIME = "CREATETIME";

    public static final String UPDATETIME = "UPDATETIME";

    public static final String IS_DELETE = "IS_DELETE";

    public static final String DS_TYPE = "DS_TYPE";

    public static final String URL = "URL";

    public static final String DB_NAME = "DB_NAME";

    public static final String CONN_NAME = "CONN_NAME";

    public DbaseEntityVo(Integer dbId, String dbType, String dsType, String username, String password, Integer groupId, Integer userId, Integer accessId, Integer accountId, String url) {
        this.dbId = dbId;
        this.username = username;
        this.password = password;
        this.dbType = dbType;
        this.dsType = dsType;
        this.url = url;
        this.accessId = accessId;
        this.userId = userId;
        this.groupId = groupId;
        this.accountId = accountId;
    }
}
