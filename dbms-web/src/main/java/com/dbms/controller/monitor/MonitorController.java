package com.dbms.controller.monitor;

import com.alibaba.fastjson.JSONObject;
import com.dbms.core.AjaxResult;
import org.springframework.web.bind.annotation.*;

import java.util.Properties;

@RestController
@RequestMapping("/agent")
public class MonitorController {



    @PostMapping("/service/getStrategy")
    public AjaxResult getStrategy(@RequestBody Properties properties)
    {
        /**
         * "serviceIdentity":"dbfh",
         *     "strategy":{
         *         "status":true,
         *         "interval":30
         *     }
         */
        JSONObject object = new JSONObject();
        object.put("serviceIdentity", properties.getProperty("serviceIdentity"));
        JSONObject js = new JSONObject();
        js.put("status", true);
        js.put("interval", 30);
        object.put("strategy", js);
        return AjaxResult.success(object);
    }

    @PostMapping("/service/reportStatus")
    public AjaxResult reportService(@RequestBody JSONObject jsonObject)
    {
//        System.out.println(JSON.toJSONString(jsonObject));
        return AjaxResult.success();
    }
}
