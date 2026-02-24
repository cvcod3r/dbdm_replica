package com.dbms.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HBaseRiskBean {

    private boolean riskFlag;

    private String actionType;

    private String sqlType;

    private String tables;

    private String tableColumns;

}
