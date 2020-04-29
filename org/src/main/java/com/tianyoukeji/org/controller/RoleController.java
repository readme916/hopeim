package com.tianyoukeji.org.controller;

import java.util.ArrayList;
import java.util.List;
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
import com.tianyoukeji.org.service.RoleService;
import com.tianyoukeji.org.service.UserService;
import com.tianyoukeji.parent.common.HttpPostReturnUuid;
import com.tianyoukeji.parent.controller.DefaultHandler;
import com.tianyoukeji.parent.entity.EventRepository;
import com.tianyoukeji.parent.entity.Menu;
import com.tianyoukeji.parent.entity.Org;
import com.tianyoukeji.parent.entity.Role;
import com.tianyoukeji.parent.entity.template.MenuTemplate;
import com.tianyoukeji.parent.entity.template.RoleTemplate.Terminal;
import com.tianyoukeji.parent.service.TIMService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/v1/role")
@Api(tags = "角色的管理接口")
public class RoleController extends DefaultHandler {

	@Autowired
	private RoleService roleService;
	
	

	@PostMapping(path = "/add")
	@ApiOperation(value = "添加角色", notes = "新增一个企业角色", httpMethod = "POST")
	public HttpPostReturnUuid addRole(@Valid @RequestBody(required = true) AddRoleRequest body) {
		return roleService.add(body);
	}

	@PostMapping(path = "/delete")
	@ApiOperation(value = "删除角色", notes = "删除一个企业角色，逻辑没有写完，暂时能用，后续需要补充，不建议使用删除功能", httpMethod = "POST")
	public HttpPostReturnUuid deleteRole(@Valid @RequestBody(required = true) DeleteRoleRequest body) {
		return roleService.delete(body);
	}

	@PostMapping(path = "/update")
	@ApiOperation(value = "更新角色", notes = "更新一个企业角色,把所有字段重新提交一次，角色和菜单的关系在菜单里维护", httpMethod = "POST")
	public HttpPostReturnUuid updateRole(@Valid @RequestBody(required = true) UpdateRoleRequest body) {
		return roleService.update(body);
	}
	
	@PostMapping(path = "/linkMenu")
	@ApiOperation(value = "链接到菜单", notes = "一次提交所有的menu ids", httpMethod = "POST")
	public HttpPostReturnUuid linkMenu(@Valid @RequestBody(required = true) LinkMenuRequest body) {
		return roleService.linkMenu(body);
	}
	
	public static class LinkMenuRequest {
		@NotNull
		private Long uuid;
		private List<Long> menuIds = new ArrayList<Long>();

		public Long getUuid() {
			return uuid;
		}
		public void setUuid(Long uuid) {
			this.uuid = uuid;
		}
		public List<Long> getMenuIds() {
			return menuIds;
		}
		public void setMenuIds(List<Long> menuIds) {
			this.menuIds = menuIds;
		}
		
	}

	public static class AddRoleRequest {
		@NotBlank
		private String name;
		@NotBlank
		private String code;
		@NotNull
		private Long org;
		@NotNull
		private Terminal terminal;
		
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

		public Long getOrg() {
			return org;
		}

		public void setOrg(Long org) {
			this.org = org;
		}

		public Terminal getTerminal() {
			return terminal;
		}

		public void setTerminal(Terminal terminal) {
			this.terminal = terminal;
		}


	}

	public static class DeleteRoleRequest {
		@NotNull
		private Long uuid;
		
		@NotNull
		private Long org;

		public Long getOrg() {
			return org;
		}

		public void setOrg(Long org) {
			this.org = org;
		}

		public Long getUuid() {
			return uuid;
		}

		public void setUuid(Long uuid) {
			this.uuid = uuid;
		}

	}

	public static class UpdateRoleRequest extends AddRoleRequest {
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
