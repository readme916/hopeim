package com.tianyoukeji.platform.controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.liyang.jpa.smart.query.db.SmartQuery;
import com.tianyoukeji.parent.controller.DefaultHandler;
import com.tianyoukeji.parent.service.StateTemplateService;
import com.tianyoukeji.parent.service.StateTemplateService.Builder;
import com.tianyoukeji.platform.service.UserService;

import springfox.documentation.annotations.ApiIgnore;

@RestController
@ApiIgnore
public class TestController extends DefaultHandler{

	@Autowired
	private UserService userService;
	
	@GetMapping(path="/test/test")
	public void test1() {
		Builder builder = new StateTemplateService.Builder();
		builder.entity("user").state("test", "测试", true, false, false, null, null, null, null, null, null, null).event("enable", "启动",  null, null, null, 0);
		builder.getState("test").addEvent("enable").addEvent("forbid").addTimer("speak");
		
		
	}
	@GetMapping(path = "/test/enable")
	public Object enable(Authentication authentication) {
		return userService.dispatchEvent(5l, "enable");
//		User findById = userService.findById(5l);
//		State state = findById.getState();
//		System.out.println(userService.stateExecutableEvent(state));
//		System.out.println(userService.currentUserStateExecutableEvent(state));
//		return null;
	}
	@GetMapping(path = "/test/forbid")
	public Object forbid(Authentication authentication) {
		return userService.dispatchEvent(5l, "forbid");
//		User findById = userService.findById(5l);
//		State state = findById.getState();
//		System.out.println(userService.stateExecutableEvent(state));
//		System.out.println(userService.currentUserStateExecutableEvent(state));
//		return null;
	}
	@GetMapping(path = "/test/status")
	public Object test(Authentication authentication) {
		return userService.currentUserExecutableEvent("enabled");
	}
}
