package com.tianyoukeji.platform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.liyang.jpa.smart.query.db.SmartQuery;
import com.tianyoukeji.platform.service.UserService;

@RestController
public class TestController {

	@Autowired
	private UserService schoolService;
	
	@GetMapping(path = "/")
	public Object test() {
		return SmartQuery.fetchOne("student", "uuid=1&fields=*,school");
	}
}
