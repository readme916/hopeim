package com.tianyoukeji.org.controller;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tianyoukeji.org.service.OrgService;
import com.tianyoukeji.org.service.UserService;
import com.tianyoukeji.parent.common.HttpPostReturnUuid;
import com.tianyoukeji.parent.controller.DefaultHandler;
import com.tianyoukeji.parent.entity.EventRepository;
import com.tianyoukeji.parent.service.TIMService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;

@RestController
@RequestMapping("/v1/org")
@Api(tags = "管理企业的接口")
public class OrgController extends DefaultHandler {

	@Autowired
	private OrgService orgService;

	@PostMapping(path = "/addUser")
	@ApiOperation(value = "邀请员工", notes = "直接把普通用户变成公司员工，如果已经有其他企业的不能邀请", httpMethod = "POST")
	public HttpPostReturnUuid addUser(@Valid @RequestBody(required = true) InviteBody body) {
		orgService.addUser(body.getUnionId());
		return new HttpPostReturnUuid();
	}
	
	@PostMapping(path = "/deleteUser")
	@ApiOperation(value = "删除员工", notes = "直接把公司员工踢出公司", httpMethod = "POST")
	public HttpPostReturnUuid deleteUser(@Valid @RequestBody(required = true) InviteBody body) {
		orgService.deleteUser(body.getUnionId());
		return new HttpPostReturnUuid();
	}
	
	@PostMapping(path = "/locateUserDepartment")
	@ApiOperation(value = "分配员工部门", notes = "", httpMethod = "POST")
	public HttpPostReturnUuid locateUserDepartment(@Valid @RequestBody(required = true) UserDepartmentBody body) {
		orgService.locateUserDepartment(body.getUuid() , body.getDepartmentId());
		return new HttpPostReturnUuid();
	}
	
	@PostMapping(path = "/locateDepartmentManager")
	@ApiOperation(value = "分配部门主管", notes = "", httpMethod = "POST")
	public HttpPostReturnUuid locateDepartmentManager(@Valid @RequestBody(required = true) ManagerDepartmentBody body) {
		orgService.locateManagerDepartment(body.getManagerId() , body.getDepartmentId());
		return new HttpPostReturnUuid();
	}
	
	@PostMapping(path = "/locateUserRole")
	@ApiOperation(value = "分配员工角色", notes = "", httpMethod = "POST")
	public HttpPostReturnUuid locateUserRole(@Valid @RequestBody(required = true) UserRoleBody body) {
		orgService.locateUserRole(body.getUuid() , body.getRoleId());
		return new HttpPostReturnUuid();
	}
	
	public static class InviteBody {
		@NotEmpty
		@Length(min = 32,max=32)
		private String unionId;

		public String getUnionId() {
			return unionId;
		}

		public void setUnionId(String unionId) {
			this.unionId = unionId;
		}

	}
	
	
	public static class ManagerDepartmentBody {
		@NotNull
		@Min(1)
		private Long managerId;
		@NotNull
		@Min(1)
		private Long departmentId;
		

		public Long getManagerId() {
			return managerId;
		}
		public void setManagerId(Long managerId) {
			this.managerId = managerId;
		}
		public Long getDepartmentId() {
			return departmentId;
		}
		public void setDepartmentId(Long departmentId) {
			this.departmentId = departmentId;
		}

	}
	
	public static class UserDepartmentBody {
		@NotNull
		@Min(1)
		private Long uuid;
		@NotNull
		@Min(1)
		private Long departmentId;
		
		public Long getUuid() {
			return uuid;
		}
		public void setUuid(Long uuid) {
			this.uuid = uuid;
		}
		public Long getDepartmentId() {
			return departmentId;
		}
		public void setDepartmentId(Long departmentId) {
			this.departmentId = departmentId;
		}

	}
	public static class UserRoleBody {
		@NotNull
		@Min(1)
		private Long uuid;
		@NotNull
		@Min(1)
		private Long roleId;
		public Long getUuid() {
			return uuid;
		}
		public void setUuid(Long uuid) {
			this.uuid = uuid;
		}
		public Long getRoleId() {
			return roleId;
		}
		public void setRoleId(Long roleId) {
			this.roleId = roleId;
		}

	}
	
}
