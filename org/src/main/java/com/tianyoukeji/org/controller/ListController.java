package com.tianyoukeji.org.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.liyang.jpa.smart.query.db.SmartQuery;
import com.liyang.jpa.smart.query.db.structure.EntityStructure;
import com.liyang.jpa.smart.query.response.HTTPListResponse;
import com.tianyoukeji.org.service.OrgService;
import com.tianyoukeji.parent.common.ContextUtils;
import com.tianyoukeji.parent.controller.DefaultHandler;
import com.tianyoukeji.parent.entity.base.IOrgEntity;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/v1/list")
@Api(tags = "列表页的接口")
public class ListController extends DefaultHandler {
	
	@Autowired
	private OrgService orgService;

	@GetMapping(path = "/{entity}")
	@ApiOperation(value = "通用列表页", notes = "默认只包括实体普通的属性，如果有对象属性要求，自己实现mapping", httpMethod = "GET")
	@ApiImplicitParam(dataType = "String", name = "params", value = "查询字符串", required = false, paramType = "query" ,example = "uuid=1&sort=name,desc&page=0&size=10")
	public HTTPListResponse fetchList(@PathVariable(required = true) String entity,@RequestParam(required = false) HashMap<String, String> params) {
		params.put("fields", "*");
		return getOrgList(entity,params);
	}

	@GetMapping(path = "/user")
	@ApiOperation(value = "公司员工列表页", notes = "增加了role，org，state，department等对象", httpMethod = "GET")
	public HTTPListResponse fetchUserList(@RequestParam(name = "page" , defaultValue = "0") Integer page
										, @RequestParam(name = "size" , defaultValue = "20") Integer size 
										, @RequestParam(name = "sort" , required = false) String sort) {
		HashMap<String,String> params = new HashMap<String,String>();
		params.put("fields", "*,role,org,department,state,userinfo.mobile");
		params.put("page", String.valueOf(page));
		params.put("size", String.valueOf(size));
		if(sort!=null) {
			params.put("sort", sort);
		}
		return getOrgList("user",params);
	}
	
	@GetMapping(path = "/user/department/{id}")
	@ApiOperation(value = "部门员工列表页", notes = "根据部门id，返回员工列表", httpMethod = "GET")
	public HTTPListResponse fetchDepartmentUserList(@PathVariable(required = true) Long id 
			, @RequestParam(name = "page" , defaultValue = "0") Integer page
			, @RequestParam(name = "size" , defaultValue = "20") Integer size 
			, @RequestParam(name = "sort" , required = false) String sort) {
		HashMap<String,String> params = new HashMap<String,String>();
		params.put("fields", "*,role,org,department,state,userinfo.mobile");
		params.put("department.uuid", id.toString());
		params.put("page", String.valueOf(page));
		params.put("size", String.valueOf(size));
		if(sort!=null) {
			params.put("sort", sort);
		}
		return getOrgList("user",params);
	}
	
	
	@GetMapping(path = "/menuTree")
	@ApiOperation(value = "菜单列表页", notes = "根据不同角色返回不同菜单,并且自动排序", httpMethod = "GET")
	public HTTPListResponse fetchMenuList() {
		Long orgId = orgService.getCurrentOrg().getUuid();
		String role = ContextUtils.getRole();
		return SmartQuery.fetchTree("menu", "org.uuid="+orgId + "&roles.code="+role);
	}
	
	@GetMapping(path = "/departmentTree")
	@ApiOperation(value = "部门列表页", notes = "返回公司所有的部门列表", httpMethod = "GET")
	public HTTPListResponse fetchDepartmentList() {
		Long orgId = orgService.getCurrentOrg().getUuid();
		return SmartQuery.fetchTree("department", "fields=*,manager,parent&org.uuid="+orgId);
	}
	
	@GetMapping(path = "/department")
	@ApiOperation(value = "部门列表页", notes = "返回公司所有的部门列表", httpMethod = "GET")
	public HTTPListResponse fetchDepartmentList(@RequestParam(name = "page" , defaultValue = "0") Integer page
			, @RequestParam(name = "size" , defaultValue = "20") Integer size 
			, @RequestParam(name = "sort" , required = false) String sort) {
		Long orgId = orgService.getCurrentOrg().getUuid();
		HashMap<String,String> params = new HashMap<String,String>();
		params.put("fields", "*,manager,parent");
		params.put("page", String.valueOf(page));
		params.put("size", String.valueOf(size));
		if(sort!=null) {
			params.put("sort", sort);
		}
		return getOrgList("department",params);
	}
	@GetMapping(path = "/role")
	@ApiOperation(value = "公司可用角色列表页", notes = "返回公司所有的相关角色列表", httpMethod = "GET")
	public HTTPListResponse fetchRoleList(@RequestParam(name = "page" , defaultValue = "0") Integer page
			, @RequestParam(name = "size" , defaultValue = "20") Integer size 
			, @RequestParam(name = "sort" , required = false) String sort) {
		HashMap<String,String> params = new HashMap<String,String>();
		params.put("page", String.valueOf(page));
		params.put("size", String.valueOf(size));
		if(sort!=null) {
			params.put("sort", sort);
		}
		return getOrgList("role",params);
	}
	
	
	/**
	 * 	如果是企业类型的实体，则自动加入org筛选条件，让企业只能查看自己的数据
	 */
	private  HTTPListResponse getOrgList(String entity,HashMap<String, String> params) {
		EntityStructure structure = SmartQuery.getStructure(entity);
		Class<?> entityClass = structure.getEntityClass();
		if(IOrgEntity.class.isAssignableFrom(entityClass)) {
			if(orgService.getCurrentOrg()!=null) {
				Long orgId = orgService.getCurrentOrg().getUuid();
				params.put("org.uuid", String.valueOf(orgId));
			}
		}
		return SmartQuery.fetchList(entity, params);
	}

}
