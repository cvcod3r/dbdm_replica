package com.dbms.constant;

/**
 * 缓存的key 常量
 *
 * @author
 */
public class CacheConstants
{
    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 参数管理 cache key
     */
    public static final String SYS_CONFIG_KEY = "sys_config:";

    /**
     *  授权数据源列表
     */
    public static final String DBASE_ACCESSIBLE_LIST = "dbase_access_list:";


    /**
     *  授权数据源
     */
    public static final String DBASE_ACCESS= "dbase_access:";

    /**
     *  高危操作
     */
    public static final String RISK_OPER= "risk_oper:";

    public static final String RISK_TABLE_ROW_LIMIT= "risk_table_row_limits:";
    /**
     *  脱敏配置
     */
    public static final String DESENSITIZATION= "desensitization:";
    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = "sys_dict:";

    /**
     * 防重提交 redis key
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 限流 redis key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * 登录账户密码错误次数 redis key
     */
    public static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";
}
