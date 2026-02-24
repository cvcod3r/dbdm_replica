package com.dbms.bean;

import com.dbms.entity.DataLabelEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
public class SensitiveBean {
    private Map<String,String> tableAliastoTrue;

    private Map<String,String> columnAliastoTrue;

    private List<Map<String,Map<String, DataLabelEntity>>> columnSensitivelist;

    private String actionType;

    private List<String> tableNames;

    private List<String> ruleNames;

    public SensitiveBean(List<Map<String,Map<String, DataLabelEntity>>> columnSensitivelist,Map<String,String> tableAliastoTrue,Map<String,String> columnAliastoTrue,String actionType,List<String> tableNames,List<String> ruleNames) {
        this.columnSensitivelist = columnSensitivelist;
        this.actionType = actionType;
        this.tableAliastoTrue = tableAliastoTrue;
        this.columnAliastoTrue = columnAliastoTrue;
        this.tableNames = tableNames;
        this.ruleNames = ruleNames;
    }
    public SensitiveBean(){
    }
}
