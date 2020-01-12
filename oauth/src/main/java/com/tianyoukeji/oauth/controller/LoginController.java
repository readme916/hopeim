package com.tianyoukeji.oauth.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.common.ContextUtils;
import com.tianyoukeji.parent.controller.DefaultHandler;
import com.tianyoukeji.parent.service.SmsService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import springfox.documentation.annotations.ApiIgnore;

@Controller
@Api(tags="登录接口")
public class LoginController extends DefaultHandler {
	
	@Autowired
	private SmsService smsService;
	
    @GetMapping("/unionLogin")
    @ApiOperation(value = "用户登录界面",notes = "",httpMethod = "GET")
    public String unionLogin() {
    	return "unionLogin";
    }
    
    
    @GetMapping("/")
    @ResponseBody
    @ApiIgnore
    public Object home(Authentication authentication) {
    	return authentication;
    }
    
    /** 
     * 获取验证码
     * @param mobile
     * @return
     */
    @GetMapping("/sms")
    @ResponseBody
    @ApiOperation(value = "获取手机验证码",notes = "后台限制每手机号20s一次，前台自己限制更大的值",httpMethod = "GET")
    @ApiImplicitParam(dataType = "String",name = "username",value = "手机号",required = true,paramType = "query")
    public String sms(@RequestParam(name = "username",required = true) String mobile){
    	if(!ContextUtils.isMobile(mobile)) {
    		throw new BusinessException(1422, "手机号不合法");
    	}
    	String number = smsService.getLoginSms(mobile);
		return number;
    	
    }
}
