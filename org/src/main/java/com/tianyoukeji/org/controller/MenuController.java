package com.tianyoukeji.org.controller;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tianyoukeji.org.controller.OrgController.InviteBody;
import com.tianyoukeji.org.controller.StateMachineController.AddStateRequest;
import com.tianyoukeji.org.controller.StateMachineController.DeleteStateRequest;
import com.tianyoukeji.org.controller.StateMachineController.UpdateStateRequest;
import com.tianyoukeji.org.service.MenuService;
import com.tianyoukeji.org.service.UserService;
import com.tianyoukeji.parent.common.HttpPostReturnUuid;
import com.tianyoukeji.parent.controller.DefaultHandler;
import com.tianyoukeji.parent.entity.EventRepository;
import com.tianyoukeji.parent.entity.Menu;
import com.tianyoukeji.parent.entity.Org;
import com.tianyoukeji.parent.entity.Role;
import com.tianyoukeji.parent.entity.template.MenuTemplate;
import com.tianyoukeji.parent.service.TIMService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/v1/menu")
@Api(tags = "菜单的管理接口")
public class MenuController extends DefaultHandler {

	@Autowired
	private MenuService menuService;

	@PostMapping(path = "/add")
	@ApiOperation(value = "添加菜单", notes = "新增一个菜单", httpMethod = "POST")
	public HttpPostReturnUuid addMenu(@Valid @RequestBody(required = true) AddMenuRequest body) {
		return menuService.add(body);
	}

	@PostMapping(path = "/delete")
	@ApiOperation(value = "删除菜单", notes = "删除一个菜单", httpMethod = "POST")
	public HttpPostReturnUuid deleteMenu(@Valid @RequestBody(required = true) DeleteMenuRequest body) {
		return menuService.delete(body);
	}

	@PostMapping(path = "/update")
	@ApiOperation(value = "更新菜单", notes = "更新一个菜单,把所有字段重新提交一次", httpMethod = "POST")
	public HttpPostReturnUuid updateMenu(@Valid @RequestBody(required = true) UpdateMenuRequest body) {
		return menuService.update(body);
	}

	public static class AddMenuRequest {
		@NotBlank
		private String name;
		@NotBlank
		private String code;

		private Integer sort = 0;

		private String iconUrl;
		@NotBlank
		private String url;

		private Set<Long> roles;

		private Long parent;
		@NotNull
		private Long org;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public Integer getSort() {
			return sort;
		}

		public void setSort(Integer sort) {
			this.sort = sort;
		}

		public String getIconUrl() {
			return iconUrl;
		}

		public void setIconUrl(String iconUrl) {
			this.iconUrl = iconUrl;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public Set<Long> getRoles() {
			return roles;
		}

		public void setRoles(Set<Long> roles) {
			this.roles = roles;
		}

		public Long getParent() {
			return parent;
		}

		public void setParent(Long parent) {
			this.parent = parent;
		}

		public Long getOrg() {
			return org;
		}

		public void setOrg(Long org) {
			this.org = org;
		}

	}

	public static class DeleteMenuRequest {
		@NotNull
		private Long uuid;

		public Long getUuid() {
			return uuid;
		}

		public void setUuid(Long uuid) {
			this.uuid = uuid;
		}

	}

	public static class UpdateMenuRequest extends AddMenuRequest {
		@NotNull
		private Long uuid;

		public Long getUuid() {
			return uuid;
		}

		public void setUuid(Long uuid) {
			this.uuid = uuid;
		}
	}
}
