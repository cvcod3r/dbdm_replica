package com.dbms.controller;


import com.dbms.core.AjaxResult;
import com.dbms.entity.DbaseEntity;
import com.dbms.repository.DbopService;
import com.dbms.annotation.SysLogAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dbop")
public class DbopController {

    @Autowired
    public DbopService dbopService;

    @RequestMapping("/testConn")
    @SysLogAnnotation(moduleName = "数据源模块", childModule = "国产数据源", operation = "测试", procedureInfo = "测试数据源")
    public AjaxResult testConn(@RequestBody DbaseEntity dbaseEntity){
//        System.out.println("测试连接");
//        System.out.println(JSON.toJSONString(dbaseEntity));
        //Map<String, Object> mapResult = new HashMap<>();
        String mess = "";
        String status = "";
        boolean flag = dbopService.testConn(dbaseEntity);
        if (flag)
        {
            mess = "连接成功！";
            status = "success";
        }
        else
        {
            mess = "连接失败！";
            status = "fail";
        }
        AjaxResult ajaxResult = AjaxResult.success(mess);
        ajaxResult.put("mess", mess);
        ajaxResult.put("status", status);
        return ajaxResult;
    }

}
