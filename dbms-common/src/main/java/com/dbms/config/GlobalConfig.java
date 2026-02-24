package com.dbms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class GlobalConfig {

    /**
     * 是否开启风险拦截, 值为true时开启
     */
    private boolean riskFlag = true;

}
