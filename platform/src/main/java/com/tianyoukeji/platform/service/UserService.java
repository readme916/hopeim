package com.tianyoukeji.platform.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tianyoukeji.parent.entity.User;
import com.tianyoukeji.parent.service.BaseService;
import com.tianyoukeji.parent.service.NamespaceRedisService;
import com.tianyoukeji.parent.service.NamespaceRedisService.RedisNamespace;

@Service

public class UserService extends BaseService<User> {

	@Autowired
    private RedisConnectionFactory redisConnectionFactory;
	
	@Autowired
	private NamespaceRedisService namespaceRedisService;
	
	@Transactional
	public void banUser(Long uuid) {
		User user = findById(uuid);
		user.setEnabled(false);
		save(user);
		RedisTokenStore redisTokenStore = new RedisTokenStore(redisConnectionFactory);
		Set<String> sMembers = namespaceRedisService.sMembers(RedisNamespace.USER_TOKEN, user.getUserinfo().getMobile());
		for (String str : sMembers) {
			redisTokenStore.removeAccessToken(str);
		}
		namespaceRedisService.delete(RedisNamespace.USER_TOKEN, user.getUserinfo().getMobile());
	}
}
