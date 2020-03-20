package com.tianyoukeji.org.controller;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.hql.spi.id.inline.IdsClauseBuilder;
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

	@GetMapping(path = "/{entity}/{uuid}")
	@ApiOperation(value = "通用详细页", notes = "默认只包括实体普通的属性，如果有对象属性要求，自己实现mapping", httpMethod = "GET")
	public Map fetchOne(@PathVariable(required = true) String entity, @PathVariable(required = true) Long uuid) {
		if (StateMachineService.services.containsKey(entity)) {
			if (isAddOrg(entity)) {
				Long orgId = orgService.getCurrentOrg().getUuid();
				return StateMachineService.services.get(entity)
						.fetchOne("fields=*&org.uuid=" + orgId + "&uuid=" + uuid);
			} else {
				return StateMachineService.services.get(entity).fetchOne("fields=*&uuid=" + uuid);
			}
		} else {
			if (isAddOrg(entity)) {
				Long orgId = orgService.getCurrentOrg().getUuid();
				return SmartQuery.fetchOne(entity, "fields=*&org.uuid=" + orgId + "&uuid=" + uuid);
			} else {
				return SmartQuery.fetchOne(entity, "fields=*&uuid=" + uuid);
			}
		}
	}

	@GetMapping(path = "/user/{unionId}")
	@ApiOperation(value = "用户详细页", notes = "增加了role，org，state，department等对象，并且使用unionId来定位，防止遍历", httpMethod = "GET")
	public Map fetchUser(@PathVariable(required = true) String unionId) {
		Map fetchOne = StateMachineService.services.get("user")
				.fetchOne("fields=*,role,org,department,state&unionId=" + unionId);
		return fetchOne;

	}

	/**
	 * 是否添加企业筛选
	 */
	private boolean isAddOrg(String entity) {
		EntityStructure structure = SmartQuery.getStructure(entity);
		Class<?> entityClass = structure.getEntityClass();
		if (IOrgEntity.class.isAssignableFrom(entityClass)) {
			if (orgService.getCurrentOrg() != null) {
				return true;
			}
		}
		return false;
	}
}
