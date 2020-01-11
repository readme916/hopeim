package com.tianyoukeji.platform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tianyoukeji.parent.common.HttpPostReturnUuid;
import com.tianyoukeji.parent.entity.User;
import com.tianyoukeji.parent.entity.UserRepository;
import com.tianyoukeji.platform.service.Oauth2Util;
import com.tianyoukeji.platform.service.UserService;

@RestController
public class UserController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private RedisConnectionFactory redisConnectionFactory;
	
	@Autowired
	private Oauth2Util oauth2Util;
	
	@PostMapping(path = "/v1/user/ban/{uuid}")
	public HttpPostReturnUuid banUser(@PathVariable String uuid) {
		User user = userService.findById(Long.valueOf(uuid));
		user.setEnabled(false);
		userService.save(user);
		RedisTokenStore redisTokenStore = new RedisTokenStore(redisConnectionFactory);
		User findById = userService.findById(Long.valueOf(uuid));
		oauth2Util.removeTokenAccess(findById.getUserinfo().getMobile(), "platform", "all");
		return new HttpPostReturnUuid(uuid);
	}
	
	
}
