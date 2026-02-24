package com.dbms.core.dbase;

import cn.hutool.core.text.replacer.StrReplacer;
import com.dbms.enums.SqlTypeEnum;
import com.github.CCweixiao.hbase.sdk.common.model.HTableDesc;
import lombok.Getter;
import lombok.Setter;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptor;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.TableDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class HBaseShellMeta {

    private SqlTypeEnum sqlType;

    private HashMap<String, String> tableName;

    private HashMap<String, ArrayList<Map<String, String>>> columnFamily;

    private List<ColumnFamilyDescriptor> columnFamilyDescriptors;

    private HTableDesc hTableDesc;

    private TableDescriptor tableDescriptor;

    private Put put;

    private String putValue;

    private Scan scan;

    private boolean alterDelete;

    private Get get;

    private String userName;

    private String nameSpace;

    private char[] actions;

    private boolean nsOrTb;//Ns ---> T, Tb ---> F

    private boolean changeForALl;//ALL ---> T

    @Override
    public String toString() {
        return "HBaseShellMeta{" +
                "sqlType=" + sqlType +
                ", tableName='" + tableName + '\'' +
                ", columnFamily=" + columnFamily +
                '}';
    }
}
