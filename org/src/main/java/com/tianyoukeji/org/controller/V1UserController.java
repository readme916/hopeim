package com.tianyoukeji.org.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tianyoukeji.org.service.UserService;
import com.tianyoukeji.parent.common.HttpPostReturnUuid;
import com.tianyoukeji.parent.controller.DefaultHandler;
import com.tianyoukeji.parent.entity.EventRepository;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

@RestController("/v1/user/")
@Api(tags="用户的接口")
public class V1UserController extends DefaultHandler {

	@Autowired
	private UserService userService;
	
	@Autowired
	private EventRepository eventRepository;
	
	@PostMapping(path = "{uuid}/ban")
	@ApiOperation(value = "禁用用户",notes = "如果用户在线则直接踢下线",httpMethod = "POST")
    @ApiImplicitParam(dataType = "long",name = "uuid",value = "用户id",required = true,paramType = "path")
	public HttpPostReturnUuid banUser(@PathVariable(required = true) Long uuid) {
		userService.dispatchEvent(uuid, "kick");
		userService.dispatchEvent(uuid, "enable");
		return new HttpPostReturnUuid(uuid);
	}
}
