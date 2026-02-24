package com.dbms.bean;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RiskBean {

    public RiskBean(boolean riskFlag, String action, String sqlType) {
        this.riskFlag = riskFlag;
        this.actionType = action;
        this.sqlType = sqlType;
    }

    private boolean riskFlag;

    private String actionType;

    private String sqlType;

    private String schemas;

    private String tables;

    private String tableColumns;

}
