package com.tianyoukeji.org.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.liyang.jpa.smart.query.db.SmartQuery;
import com.tianyoukeji.org.service.StateService;
import com.tianyoukeji.org.service.StateTemplateService;
import com.tianyoukeji.org.service.UserService;
import com.tianyoukeji.parent.common.ContextUtils;
import com.tianyoukeji.parent.controller.DefaultHandler;
import com.tianyoukeji.parent.entity.Org;
import com.tianyoukeji.parent.entity.OrgRepository;
import com.tianyoukeji.parent.entity.RoleRepository;
import com.tianyoukeji.parent.entity.State;
import com.tianyoukeji.parent.entity.User;
import com.tianyoukeji.parent.entity.template.OrgTemplate;
import com.tianyoukeji.parent.entity.template.OrgTemplateRepository;
import com.tianyoukeji.parent.entity.template.RoleTemplate;

import springfox.documentation.annotations.ApiIgnore;

@RestController
@ApiIgnore
public class TestController extends DefaultHandler{

	@Autowired
	private UserService userService;
	
	@Autowired
	private StateService stateService;
	
	@Autowired
	private StateTemplateService stateTemplateService;

	@Autowired
	private OrgTemplateRepository orgTemplateRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private OrgRepository orgRepository;
	
	@GetMapping(path="/test/test")
	public Object test1() {
		return SmartQuery.fetchGroup("user", "group=nickname");
	}
	@GetMapping(path = "/test/enable")
	public void enable(Authentication authentication) {
		
		List<String> currentUserExecutableEvent = userService.currentUserExecutableEvent(1l,null);
		System.out.println(currentUserExecutableEvent);
		userService.dispatchEvent(1l, "enable");
		User findById = userService.findById(1l);
		State state = findById.getState();
		System.out.println(state.toString());
	}
	
	@GetMapping(path = "/test/forbid")
	public void forbid(Authentication authentication) {
		userService.dispatchEvent(1l, "forbid");
//		User findById = userService.findById(5l);
//		State state = findById.getState();
//		System.out.println(userService.stateExecutableEvent(state));
//		System.out.println(userService.currentUserStateExecutableEvent(state));
//		return null;
	}
	@GetMapping(path = "/test/status")
	public Object test(Authentication authentication) {
		return userService.currentUserExecutableEvent(1l,"enabled");
	}
	@GetMapping(path = "/test/state")
	public Object state(Authentication authentication) {
		return stateService.fetchList("fields=events,events.roles,events.target,firstTarget,thenTarget,lastTarget,timers,*&entity=user&sort=sort,asc");
	}
}
