package com.dbms.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Metric {

    /**
     * 指标名称
     */
    private String metricName;

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 采集值
     */
    private String value;

    /**
     * 标签，多组标签以‘,’隔开,选填
     */
    private String tags;

    /**
     * ip地址
     */
    private String endpoint;

    /**
     * 描述
     */
    private String desc;

}
