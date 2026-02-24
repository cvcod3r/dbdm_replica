package com.dbms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SysController {

    @RequestMapping("/index")
    public String index(){
        System.out.println("index.html");
        return "redirect:/index.html";
    }

}
