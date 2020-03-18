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
import org.springframework.web.bind.annotation.RestController;

import com.liyang.jpa.smart.query.db.SmartQuery;
import com.liyang.jpa.smart.query.db.structure.EntityStructure;
import com.liyang.jpa.smart.query.response.HTTPListResponse;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;
import com.tianyoukeji.org.service.OrgService;
import com.tianyoukeji.org.service.StateService;
import com.tianyoukeji.org.service.StateTemplateService;
import com.tianyoukeji.org.service.UserService;
import com.tianyoukeji.parent.common.HttpPostReturnUuid;
import com.tianyoukeji.parent.controller.DefaultHandler;
import com.tianyoukeji.parent.entity.base.IOrgEntity;
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

	@Autowired
	private OrgService orgService;

	@PostMapping(path = "/{entity}/{uuid}")
	@ApiOperation(value = "通用详细页", notes = "默认只包括实体普通的属性，如果有对象属性要求，自己实现mapping", httpMethod = "POST")
	public Map fetchOne(@PathVariable(required = true) String entity, @PathVariable(required = true) Long uuid) {
		if (StateMachineService.services.containsKey(entity)) {
			Map fetchOne = getOrgDetail(entity, "fields=*&uuid=" + uuid);
			fetchOne.put("events", StateMachineService.services.get(entity)
					.currentUserExecutableEvent(((Map) fetchOne.get("state")).get("code").toString()));
			return fetchOne;
		}
		return getOrgDetail(entity, "fields=*&uuid=" + uuid);
	}

	@PostMapping(path = "/user/{uuid}")
	@ApiOperation(value = "用户详细页", notes = "增加了role，org，state，department等对象", httpMethod = "POST")
	public Map fetchUser(@PathVariable(required = true) Long uuid) {
		Map fetchOne = getOrgDetail("user", "fields=*,role,org,department,state&uuid=" + uuid);

		if (fetchOne.get("state") == null || ((Map) fetchOne.get("state")).isEmpty()) {
			fetchOne.put("events", StateMachineService.services.get("user").currentUserExecutableEvent(null));
		} else {
			fetchOne.put("events", StateMachineService.services.get("user")
					.currentUserExecutableEvent((((Map) fetchOne.get("state")).get("code").toString())));
		}
		return fetchOne;

	}

	private Map getOrgDetail(String entity, String query) {
		/**
		 * 如果是企业类型的实体，则自动加入org筛选条件，让企业只能查看自己的数据
		 */
		EntityStructure structure = SmartQuery.getStructure(entity);
		Class<?> entityClass = structure.getEntityClass();
		if (IOrgEntity.class.isAssignableFrom(entityClass)) {
			if (orgService.getCurrentOrg() != null) {
				Long orgId = orgService.getCurrentOrg().getUuid();
				return SmartQuery.fetchOne(entity, query + "&org.uuid=" + orgId);
			}
		}
		return SmartQuery.fetchOne(entity, query);
	}
}
