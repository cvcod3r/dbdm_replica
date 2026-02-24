package com.dbms.core.dbase;

import com.dbms.enums.SqlTypeEnum;

public interface IHBaseShellParser {
    SqlTypeEnum getSqlType(String sql) throws Exception;

    HBaseShellMeta getHBaseShellMeta(String shell) throws Exception;
}
