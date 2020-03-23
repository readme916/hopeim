package com.tianyoukeji.org.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esotericsoftware.kryo.NotNull;
import com.tianyoukeji.org.service.UserService;
import com.tianyoukeji.parent.common.HttpPostReturnUuid;
import com.tianyoukeji.parent.controller.DefaultHandler;
import com.tianyoukeji.parent.entity.EventRepository;
import com.tianyoukeji.parent.service.TIMService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/v1/user")
@Api(tags = "管理用户的接口")
public class V1UserController extends DefaultHandler {

	@Autowired
	private UserService userService;

	@PostMapping(path = "/{uuid}/kick")
	@ApiOperation(value = "踢下线", notes = "如果用户在线则直接踢下线", httpMethod = "POST")
	public HttpPostReturnUuid kick(@PathVariable(required = true) Long uuid) {
		userService.dispatchEvent(uuid, "kick",null);
		return new HttpPostReturnUuid(uuid);
	}

	@PostMapping(path = "/{uuid}/enable")
	@ApiOperation(value = "启用用户", notes = "如果用户被禁用状态下，可以启用", httpMethod = "POST")
	public HttpPostReturnUuid enable(@PathVariable(required = true) Long uuid,
			@Valid @RequestBody(required = true) ReasonBody body) {
		userService.dispatchEvent(uuid, "enable",body);
		return new HttpPostReturnUuid(uuid);
	}

	@PostMapping(path = "/{uuid}/disable")
	@ApiOperation(value = "禁用用户", notes = "如果用户启用状态下，可以禁用账号", httpMethod = "POST")
	public HttpPostReturnUuid disable(@PathVariable(required = true) Long uuid,@Valid @RequestBody(required = true) ReasonBody body) {
		userService.dispatchEvent(uuid, "disable",body);
		return new HttpPostReturnUuid(uuid);
	}

	
	public static class ReasonBody {
		@javax.validation.constraints.NotNull
		private String reason;

		public String getReason() {
			return reason;
		}

		public void setReason(String reason) {
			this.reason = reason;
		}

	}
	public static class InviteBody {
		private Long orgId;

		public Long getOrgId() {
			return orgId;
		}

		public void setOrgId(Long orgId) {
			this.orgId = orgId;
		}

		

	}
	
}
