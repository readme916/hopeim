package com.tianyoukeji.org.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.liyang.jpa.smart.query.db.SmartQuery;
import com.tianyoukeji.org.service.StateService;
import com.tianyoukeji.org.service.StateTemplateService;
import com.tianyoukeji.org.service.UserService;
import com.tianyoukeji.parent.common.HttpPostReturnUuid;
import com.tianyoukeji.parent.controller.DefaultHandler;
import com.tianyoukeji.parent.service.StateMachineService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/v1/detail")
@Api(tags = "详细页的接口")
public class DetailController extends DefaultHandler {

	@PostMapping(path = "/{entity}/{uuid}")
	@ApiOperation(value = "通用详细页", notes = "默认只包括实体普通的属性，如果有对象属性要求，自己实现mapping", httpMethod = "POST")
	@ApiImplicitParams({
			@ApiImplicitParam(dataType = "String", name = "entity", value = "实体类型", required = true, paramType = "path"),
			@ApiImplicitParam(dataType = "Long", name = "uuid", value = "uuid", required = true, paramType = "path") })
	public Map fetchOne(@PathVariable(required = true) String entity, @PathVariable(required = true) Long uuid) {
		if (StateMachineService.services.containsKey(entity)) {
			Map fetchOne = SmartQuery.fetchOne(entity, "fields=*&uuid=" + uuid);
			fetchOne.put("events", StateMachineService.services.get(entity)
					.currentUserExecutableEvent(fetchOne.get("state").toString()));
			return fetchOne;
		}
		return SmartQuery.fetchOne(entity, "uuid=" + uuid);
	}

	@PostMapping(path = "/user/{uuid}")
	@ApiOperation(value = "用户详细页", notes = "增加了role，org，state，department等对象", httpMethod = "POST")
	@ApiImplicitParams({
			@ApiImplicitParam(dataType = "Long", name = "uuid", value = "uuid", required = true, paramType = "path") })
	public Map fetchUser(@PathVariable(required = true) Long uuid) {
		Map fetchOne = SmartQuery.fetchOne("user", "fields=*,role,org,department,state&uuid=" + uuid);
		fetchOne.put("events",
				StateMachineService.services.get("user").currentUserExecutableEvent(fetchOne.get("state").toString()));
		return fetchOne;

	}

}
