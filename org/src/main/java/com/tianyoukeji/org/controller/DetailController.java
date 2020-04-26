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
import com.tianyoukeji.org.service.OrgService;
import com.tianyoukeji.org.service.StateTemplateService;
import com.tianyoukeji.org.service.UserService;
import com.tianyoukeji.parent.common.ContextUtils;
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
			if (isOrg(entity)) {
				Long orgId = orgService.getCurrentOrg().getUuid();
				return StateMachineService.services.get(entity)
						.fetchOne("fields=*&org.uuid=" + orgId + "&uuid=" + uuid);
			} else {
				return StateMachineService.services.get(entity).fetchOne("fields=*&uuid=" + uuid);
			}
		} else {
			if (isOrg(entity)) {
				Long orgId = orgService.getCurrentOrg().getUuid();
				return SmartQuery.fetchOne(entity, "fields=*&org.uuid=" + orgId + "&uuid=" + uuid);
			} else {
				return SmartQuery.fetchOne(entity, "fields=*&uuid=" + uuid);
			}
		}
	}
	
	@GetMapping(path = "/user/{uuid}")
	@ApiOperation(value = "通过id查询的用户详细页", notes = "增加了role，org，state，department等对象，主要给管理后台的详细页使用,带状态机的事件管理和log记录", httpMethod = "GET")
	public Map fetchUserById(@PathVariable(required = true) String uuid) {
		Map fetchOne = StateMachineService.services.get("user")
				.fetchOne("fields=*,country,province,city,role,org,department,state&uuid=" + uuid);
		return fetchOne;

	}
	

	@GetMapping(path = "/user/unionId/{unionId}")
	@ApiOperation(value = "通过unionId查询的用户详细页", notes = "增加了role，org，state，department等对象，并且使用unionId来定位，防止遍历，一般暴露给用户端查询使用，或者用二维码格式扫码查询", httpMethod = "GET")
	public Map fetchUserByUnionId(@PathVariable(required = true) String unionId) {
		Map fetchOne = SmartQuery.fetchOne("user","fields=*,country,province,city,role,org,department,state&unionId=" + unionId);
		return fetchOne;

	}
	
	@GetMapping(path = "/user/mobile/{username}")
	@ApiOperation(value = "通过手机号查询的用户详细页", notes = "通过手机号查询，增加了role，org，state，department等对象，并且使用mobile来定位，后台使用", httpMethod = "GET")
	public Map fetchUserMobile(@PathVariable(required = true) String username) {
		Map fetchOne = SmartQuery.fetchOne("user","fields=*,country,province,city,role,org,department,state&userinfo.mobile=" + username);
		return fetchOne;

	}
	
	
	@GetMapping(path = "/role/{uuid}")
	@ApiOperation(value = "角色详细页", notes = "增加了orgs,menus", httpMethod = "GET")
	public Map fetchRole(@PathVariable(required = true) String uuid) {
		Map fetchOne = SmartQuery.fetchOne("role","fields=*,orgs,menus&uuid=" + uuid);
		return fetchOne;

	}
	
	@GetMapping(path = "/department/{uuid}")
	@ApiOperation(value = "部门详细页", notes = "增加了manager,users，parent，children", httpMethod = "GET")
	public Map fetch(@PathVariable(required = true) String uuid) {
		Map fetchOne = SmartQuery.fetchOne("department","fields=*,manager,users,parent,children&uuid=" + uuid);
		return fetchOne;

	}
	
	@GetMapping(path = "/myInfo")
	@ApiOperation(value = "登录后自己的信息", notes = "增加了role，org，state，department等", httpMethod = "GET")
	public Map myInfo() {
		String currentUserName = ContextUtils.getCurrentUserName();
		Map fetchOne = SmartQuery.fetchOne("user","fields=*,country,province,city,role,org,department,state,userinfo.mobile&userinfo.mobile=" + currentUserName);
		return fetchOne;

	}
	

	/**
	 * 是否添加企业筛选
	 */
	private boolean isOrg(String entity) {
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
