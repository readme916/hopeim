package com.tianyoukeji.oauth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tianyoukeji.parent.controller.DefaultHandler;
import com.tianyoukeji.parent.service.SmsService;

@Controller
public class LoginController extends DefaultHandler {
	
	@Autowired
	private SmsService smsService;
	
    @GetMapping("/unionLogin")
    public String unionLogin() {
    	return "unionLogin";
    }
    @GetMapping("/")
    public String home() {
    	return "home";
    }
    
    /** 
     * 获取验证码
     * @param mobile
     * @return
     */
    @GetMapping("/sms")
    @ResponseBody
    public String sms(@RequestParam(name = "username",required = true) String mobile){
    	String number = smsService.getLoginSms(mobile);
		return number;
    	
    }
}
