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
import com.tianyoukeji.org.service.DepartmentService;
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
@RequestMapping("/v1/department")
@Api(tags = "部门的管理接口")
public class DepartmentController extends DefaultHandler {

	@Autowired
	private DepartmentService departmentService;

	@PostMapping(path = "/add")
	@ApiOperation(value = "添加部门", notes = "新增一个部门", httpMethod = "POST")
	public HttpPostReturnUuid addDepartment(@Valid @RequestBody(required = true) AddDepartmentRequest body) {
		return departmentService.add(body);
	}

	@PostMapping(path = "/delete")
	@ApiOperation(value = "删除部门", notes = "删除一个部门，必须先把所有的用户和下级部门删除才能成功", httpMethod = "POST")
	public HttpPostReturnUuid deleteDepartment(@Valid @RequestBody(required = true) DeleteDepartmentRequest body) {
		return departmentService.delete(body);
	}

	@PostMapping(path = "/update")
	@ApiOperation(value = "更新部门", notes = "更新一个部门,把所有字段重新提交一次", httpMethod = "POST")
	public HttpPostReturnUuid updateDepartment(@Valid @RequestBody(required = true) UpdateDepartmentRequest body) {
		return departmentService.update(body);
	}

	public static class AddDepartmentRequest {
		@NotBlank
		private String name;
		@NotBlank
		private String code;

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

	public static class DeleteDepartmentRequest {
		@NotNull
		private Long uuid;

		public Long getUuid() {
			return uuid;
		}

		public void setUuid(Long uuid) {
			this.uuid = uuid;
		}

	}

	public static class UpdateDepartmentRequest extends AddDepartmentRequest {
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
