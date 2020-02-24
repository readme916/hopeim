package com.tianyoukeji.platform.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.liyang.jpa.smart.query.db.SmartQuery;
import com.tianyoukeji.parent.controller.DefaultHandler;
import com.tianyoukeji.platform.service.UserService;

import springfox.documentation.annotations.ApiIgnore;

@RestController
@ApiIgnore
public class TestController extends DefaultHandler{

	@Autowired
	private UserService userService;
	
	@GetMapping(path="/test1")
	public void test1() {
		List<String> executableEvent = userService.executableEvent(2l);
		System.out.println(executableEvent);
		userService.dispatchEvent(2l, "enable");
		List<String> executableEvent2 = userService.executableEvent(2l);
		System.out.println(executableEvent2);
	}
	
	@GetMapping(path = "/")
	public Object home(Authentication authentication) {
		return authentication;
	}
	@GetMapping(path = "/test")
	public Object test(Authentication authentication) {
		return SmartQuery.fetchList("user", "fields=*");
	}
}
