
package com.dbms.core;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Data

public class ConfigurationCheck {
    /**
     * 端口号
     */
    private String portId;

    /**
     * 数据库版本
     */
    private String databaseVersion;

    /**
     * 特殊字符
     */
    private String specialCharacters;

    /**
     * 确保缺省示例以及示例数据库删除
     */
    private String ensureDatabase;

    /**
     * 启用客体重用功能
     */
//    private String enableFunction;
    private List<Map<String, Object>> enableFunction;

    private String enableFunctionFilevalue;
    /**
     * 确保PUBLIC对程序包执行权限最小化(建议删除以下包PBULIC权限)
     */
    private List<Map<String, Object>> ensurePublic;

    /**
     * 限制PUBLIC对动态视图的查询权限
     */
    private List<Map<String, Object>> restrictPublic;
    //private String restrictPublic;

    private boolean justrestrictPublic;
    /**
     * 确保WITH　ADMIN　OPTION权限正确授予
     */
    private String makeSure;

    /**
     * 确保DBA角色分配合理
     */
    private boolean ensureDba;
//    private String ensureDba;

    /**
     * 确保管理员特权分配合理
     */
//    private String appropriatelyDistribute;
    private List<Map<String, Object>> appropriatelyDistribute;
    /**
     * 确保数据库系统表和其他的配置信息只限于DBA用户或角色
     */
    private String ensureLimited;

    /**
     * 确保设置数据库对象审计策略
     */
    private String ensureAuditPolicy;

    /**
     * 确保开启审计开关
     */
    private String mksureAuditSwitch;
//    private List<Map<String, Object>> mksureAuditSwitch;
    /**
     * 确保审计实时侵害检测功能开启
     */
    private String ensureAuditIntrusion;
//    private List<Map<String, Object>> ensureAuditIntrusion;
    /**
     * 确保设置密码重用策略
     */
    //private String ensurePSWReuse;
    private List<Map<String, Object>> ensurePSWReuse;

    private String reuseMax;
    /**
     * 确保检查密码是否过于简单
     */
    private String mksurePSWSimple;

    /**
     * 确保设置DBMS账户锁定时间
     */
    private String mksureLockoutTime;
//    private List<Map<String, Object>> mksureLockoutTime;
    /**
     * 确保设置账户尝试登陆次数
     */
    //private String mksureLoginAttempts;
    private List<Map<String, Object>> mksureLoginAttempts;
    /**
     * 确保设置DBMS账户密码的有效期
     */
    private String mksureValidityPeriod;

    private boolean password_time;
    /**
     * 确保系统默认口令策略设置合理
     */
    private String ensureDefaultPSW;
//    private List<Map<String, Object>> ensureDefaultPSW;
    /**
     * 限制用户IP地址
     */
    private String userIp;
//    private List<Map<String, Object>> userIp;
    /**
     * 限制用户访问时间段
     */
    private String limitAccessPeriods;
//    private List<Map<String, Object>> limitAccessPeriods;
    /**
     * 确保数据库通讯加密
     */
    private String ensureCommunicationEncrypted;

//    private String ensureCommunicationEncryptedFilevalue;
    /**
     * 确保数据文件最小权限
     */
    private List<Map<String, Object>> ensureDataLeastPrivilege;

    /**
     * 确保日志文件最小权限
     */
    private List<Map<String, Object>> ensureLogfileLeastPrivilege;

    /**
     * 确保文件最小权限
     */
    private List<Map<String, Object>> ensureLeastPrivilege;

    /**
     * 合理开启restricted_dba安全开关
     */
    private String ensureRestrictedDBA;

    /**
     * 确保开启SSL通讯保护
     */
    private String mksureSSLProtection;

    /**
     * 确保配置了非缺省的Unix socket权限
     */
    private String ensureUnixSocket;

    /**
     * 确保sql_mode中含有NO_AUTO_CREATE_USER，是STRICT_ALL_TABLES模式
     */
    private Boolean mksureSQLMode;

    /**
     * 确保某些功能正常启用
     */
    private Boolean mksureCertainFeatures;

    /**
     * 禁用非 DBA 用户使用 C 或 JAVA 程序语言的权限
     */
    private List<Map<String, Object>> mksureNoDbaUser;

    /**
     * 使用使用账号锁定
     */
    private String useAccountLockout;

    /**
     * 禁用历史口令
     */
    private String isDisableHistoricalPsw;

}
