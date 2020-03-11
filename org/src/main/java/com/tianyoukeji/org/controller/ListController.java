package com.tianyoukeji.org.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.liyang.jpa.smart.query.db.SmartQuery;
import com.liyang.jpa.smart.query.response.HTTPListResponse;
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
@RequestMapping("/v1/list")
@Api(tags = "列表页的接口")
public class ListController extends DefaultHandler {

	@PostMapping(path = "/{entity}")
	@ApiOperation(value = "通用列表页", notes = "默认只包括实体普通的属性，如果有对象属性要求，自己实现mapping", httpMethod = "POST")
	@ApiImplicitParams({
			@ApiImplicitParam(dataType = "String", name = "entity", value = "实体类型", required = true, paramType = "path"),
			@ApiImplicitParam(dataType = "String", name = "params", value = "查询字符串", required = false, paramType = "query" ,example = "uuid=1&sort=name,desc&page=0&size=10")
	})
	public HTTPListResponse fetchList(@PathVariable(required = true) String entity,@RequestParam(required = false) HashMap<String, String> params) {
		params.put("fields", "*");
		HTTPListResponse fetchList = SmartQuery.fetchList(entity, params);
		return fetchList;
	}

	@PostMapping(path = "/user")
	@ApiOperation(value = "用户列表页", notes = "增加了role，org，state，department等对象", httpMethod = "POST")
	@ApiImplicitParams({
		@ApiImplicitParam(dataType = "String", name = "params", value = "查询字符串", required = false, paramType = "query" ,example = "uuid=1&sort=name,desc&page=0&size=10") })
	public HTTPListResponse fetchUserList(@RequestParam(required = false) HashMap<String, String> params) {
		params.put("fields", "*,role,org,department,state");
		HTTPListResponse fetchList = SmartQuery.fetchList("user", params);
		return fetchList;
	}

}
