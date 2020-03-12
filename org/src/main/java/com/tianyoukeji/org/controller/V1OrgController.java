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
@Api(tags = "用户的接口")
public class V1OrgController extends DefaultHandler {

	@Autowired
	private OrgService orgService;

	@Autowired
	private TIMService timService;

	@PostMapping(path = "/inviteUser")
	@ApiOperation(value = "邀请员工", notes = "直接把普通用户变成公司员工，如果已经有其他企业的不能邀请", httpMethod = "POST")
	public HttpPostReturnUuid addUser(@RequestBody(required = true) InviteBody body) {
		orgService.addUser(body.getUnionId());
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
	
}
