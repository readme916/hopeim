package com.tianyoukeji.oauth.controller;

import com.liyang.jpa.smart.query.db.SmartQuery;

import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class UnionLoginController {
    @GetMapping("/unionLogin")
    public String unionLogin(String domain) {
    	return "unionLogin";
    }
}
