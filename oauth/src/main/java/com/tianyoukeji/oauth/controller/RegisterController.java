package com.tianyoukeji.oauth.controller;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.tianyoukeji.oauth.service.OauthUserService;
import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.common.ContextUtils;
import com.tianyoukeji.parent.common.HttpPostReturnUuid;
import com.tianyoukeji.parent.controller.DefaultHandler;
import com.tianyoukeji.parent.entity.User;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags="注册接口")
public class RegisterController extends DefaultHandler {

	@Autowired
	private OauthUserService oauthUserService;
    
    /** 
     * 	直接注册
     * @param mobile
     * @return
     */
    @PostMapping("/registerUser")
    @ApiOperation(value = "手机号密码注册",notes = "username的格式为mobile",httpMethod = "POST")
    public Res registerUser(@Valid @RequestBody(required = true) RegisterBody body){
    	if(!ContextUtils.isMobile(body.getUsername())) {
    		throw new BusinessException(1422, "手机号不合法");
    	}
    	User user = oauthUserService.registerUser(body.getUsername(), body.getPassword(), "user", null);
		Res res = new Res();
		res.setUuid(user.getUuid());
		res.setUnionId(user.getUnionId());
		return res;
		
    	
    }
    public static class Res{
    	private Long uuid;
    	private String unionId;

		public Long getUuid() {
			return uuid;
		}

		public void setUuid(Long uuid) {
			this.uuid = uuid;
		}

		public String getUnionId() {
			return unionId;
		}

		public void setUnionId(String unionId) {
			this.unionId = unionId;
		}
    }
    
    
    public static class RegisterBody{
    	@NotNull
    	private String username;
    	@NotNull
    	private String password;
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
    	
    	
    }
}
