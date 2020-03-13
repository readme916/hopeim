package com.tianyoukeji.org.controller;

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
@Api(tags = "企业的接口")
public class V1OrgController extends DefaultHandler {

	@Autowired
	private OrgService orgService;

	@Autowired
	private TIMService timService;

	@PostMapping(path = "/addUser")
	@ApiOperation(value = "邀请员工", notes = "直接把普通用户变成公司员工，如果已经有其他企业的不能邀请", httpMethod = "POST")
	public HttpPostReturnUuid addUser(@RequestBody(required = true) InviteBody body) {
		orgService.addUser(body.getUnionId());
		return new HttpPostReturnUuid(0l);
	}
	
	@PostMapping(path = "/deleteUser")
	@ApiOperation(value = "删除员工", notes = "直接把公司员工踢出公司", httpMethod = "POST")
	public HttpPostReturnUuid deleteUser(@RequestBody(required = true) InviteBody body) {
		orgService.deleteUser(body.getUnionId());
		return new HttpPostReturnUuid(0l);
	}
	
	@PostMapping(path = "/locateUserDepartment")
	@ApiOperation(value = "分配员工部门", notes = "", httpMethod = "POST")
	public HttpPostReturnUuid locateUserDepartment(@RequestBody(required = true) UserDepartmentBody body) {
		orgService.locateUserDepartment(body.getUuid() , body.getDepartmentId());
		return new HttpPostReturnUuid(0l);
	}
	@PostMapping(path = "/locateUserRole")
	@ApiOperation(value = "分配员工角色", notes = "", httpMethod = "POST")
	public HttpPostReturnUuid locateUserRole(@RequestBody(required = true) UserRoleBody body) {
		orgService.locateUserRole(body.getUuid() , body.getRoleId());
		return new HttpPostReturnUuid(0l);
	}
	
	public static class InviteBody {
		private String unionId;

		public String getUnionId() {
			return unionId;
		}

		public void setUnionId(String unionId) {
			this.unionId = unionId;
		}

	}
	public static class UserDepartmentBody {
		private Long uuid;
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
		private Long uuid;
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
