package com.tianyoukeji.platform.controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.liyang.jpa.smart.query.db.SmartQuery;
import com.tianyoukeji.parent.controller.DefaultHandler;
import com.tianyoukeji.platform.service.StateTemplateService;
import com.tianyoukeji.platform.service.UserService;
import com.tianyoukeji.platform.service.StateTemplateService.Builder;

import springfox.documentation.annotations.ApiIgnore;

@RestController
@ApiIgnore
public class TestController extends DefaultHandler{

	@Autowired
	private UserService userService;
	
	@Autowired
	private StateTemplateService stateTemplateService;

	
	@GetMapping(path="/test/test")
	public void test1() {
//		stateTemplateService.deploy("user", null);
	}
	@GetMapping(path = "/test/enable")
	public Object enable(Authentication authentication) {
		return userService.dispatchEvent(1l, "enable");
//		User findById = userService.findById(5l);
//		State state = findById.getState();
//		System.out.println(userService.stateExecutableEvent(state));
//		System.out.println(userService.currentUserStateExecutableEvent(state));
//		return null;
	}
	
	@GetMapping(path = "/test/forbid")
	public Object forbid(Authentication authentication) {
		return userService.dispatchEvent(1l, "forbid");
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
