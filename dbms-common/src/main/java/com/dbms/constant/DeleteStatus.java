package com.dbms.constant;

/**
 * 用户状态
 *
 * @author
 */
public enum DeleteStatus
{
    OK("0", "正常"), DELETED("1", "删除");

    private final String code;
    private final String info;

    DeleteStatus(String code, String info)
    {
        this.code = code;
        this.info = info;
    }

    public String getCode()
    {
        return code;
    }

    public String getInfo()
    {
        return info;
    }
}
