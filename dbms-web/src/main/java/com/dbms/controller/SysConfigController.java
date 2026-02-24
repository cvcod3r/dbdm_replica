package com.dbms.controller;


import com.dbms.config.GlobalConfig;
import com.dbms.core.AjaxResult;
import com.dbms.core.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys-config")
public class SysConfigController extends BaseController {

    @Autowired
    private GlobalConfig globalConfig;

    @RequestMapping("/getAgentConfig")
    public AjaxResult getAgentConfig(){
        if (globalConfig.isRiskFlag()) {
            return AjaxResult.success("拦截模式");
        } else {
            return AjaxResult.success("审计/监测模式");
        }
    }

    @RequestMapping("/getAgentFlag")
    public AjaxResult getAgentFlag(){
        return AjaxResult.success(globalConfig.isRiskFlag());
    }

    @RequestMapping("/changeAgentFlag")
    public AjaxResult changeAgent(){
        globalConfig.setRiskFlag(!globalConfig.isRiskFlag());
        return AjaxResult.success();
    }

}
