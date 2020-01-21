package com.tianyoukeji.platform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.liyang.jpa.smart.query.db.SmartQuery;
import com.tianyoukeji.parent.controller.DefaultHandler;
import com.tianyoukeji.platform.service.UserService;

import springfox.documentation.annotations.ApiIgnore;

@RestController
@ApiIgnore
public class TestController extends DefaultHandler{

	
	@GetMapping(path = "/")
	public Object home(Authentication authentication) {
		return authentication;
	}
	@GetMapping(path = "/test")
	public Object test(Authentication authentication) {
		return SmartQuery.fetchList("user", "fields=*");
	}
}
