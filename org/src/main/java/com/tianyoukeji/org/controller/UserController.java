package com.tianyoukeji.org.controller;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tianyoukeji.org.service.UserService;
import com.tianyoukeji.parent.common.HttpPostReturnUuid;
import com.tianyoukeji.parent.controller.DefaultHandler;
import com.tianyoukeji.parent.entity.EventRepository;
import com.tianyoukeji.parent.service.TIMService;
import com.tianyoukeji.parent.service.TIMService.Gender;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/v1/user")
@Api(tags = "管理用户的接口")
public class UserController extends DefaultHandler {

	@Autowired
	private UserService userService;

	@PostMapping(path = "/kick")
	@ApiOperation(value = "踢下线", notes = "管理员使用，如果用户在线则直接踢下线", httpMethod = "POST")
	public HttpPostReturnUuid kick(@Valid @RequestBody(required = true) ReasonBody body) {
		userService.dispatchEvent(body.getUuid(), "kick",body);
		return new HttpPostReturnUuid(body.getUuid());
	}

	@PostMapping(path = "/enable")
	@ApiOperation(value = "启用用户", notes = "管理员使用，如果用户被禁用状态下，可以启用", httpMethod = "POST")
	public HttpPostReturnUuid enable(@Valid @RequestBody(required = true) ReasonBody body) {
		userService.dispatchEvent(body.getUuid(), "enable",body);
		return new HttpPostReturnUuid(body.getUuid());
	}

	@PostMapping(path = "/disable")
	@ApiOperation(value = "禁用用户", notes = "管理员使用，如果用户启用状态下，可以禁用账号", httpMethod = "POST")
	public HttpPostReturnUuid disable(@Valid @RequestBody(required = true) ReasonBody body) {
		userService.dispatchEvent(body.getUuid(), "disable",body);
		return new HttpPostReturnUuid(body.getUuid());
	}
	
	@PostMapping(path = "/update")
	@ApiOperation(value = "更新自己的用户资料", notes = "更新用户的基本资料，不包括角色和部门", httpMethod = "POST")
	public HttpPostReturnUuid update(@Valid @RequestBody(required = true) UserBody body) {
		return userService.update(body.getUuid(), body.getNick(), body.getGender(), body.getFaceUrl(), body.getSelfSignature());
	}
	
	
	public static class UserBody{
		@NotNull
		private Long uuid;
		private String nick;
		private Gender gender;
		private String faceUrl;
		private String selfSignature;
		
		public Long getUuid() {
			return uuid;
		}
		public void setUuid(Long uuid) {
			this.uuid = uuid;
		}
		public String getNick() {
			return nick;
		}
		public void setNick(String nick) {
			this.nick = nick;
		}
		public Gender getGender() {
			return gender;
		}
		public void setGender(Gender gender) {
			this.gender = gender;
		}
		public String getFaceUrl() {
			return faceUrl;
		}
		public void setFaceUrl(String faceUrl) {
			this.faceUrl = faceUrl;
		}
		public String getSelfSignature() {
			return selfSignature;
		}
		public void setSelfSignature(String selfSignature) {
			this.selfSignature = selfSignature;
		}
	}
	
	public static class ReasonBody {
		@NotNull
		private Long uuid;
		@NotNull
		private String reason;

		public Long getUuid() {
			return uuid;
		}

		public void setUuid(Long uuid) {
			this.uuid = uuid;
		}

		public String getReason() {
			return reason;
		}

		public void setReason(String reason) {
			this.reason = reason;
		}

	}

	
}
