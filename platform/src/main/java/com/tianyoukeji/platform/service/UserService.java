package com.tianyoukeji.platform.service;

import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tianyoukeji.parent.entity.State;
import com.tianyoukeji.parent.entity.StateRepository;
import com.tianyoukeji.parent.entity.User;
import com.tianyoukeji.parent.service.NamespaceRedisService;
import com.tianyoukeji.parent.service.NamespaceRedisService.RedisNamespace;
import com.tianyoukeji.parent.service.StateMachineService;

@Service
public class UserService extends StateMachineService<User> {

	@Autowired
    private RedisConnectionFactory redisConnectionFactory;
	
	@Autowired
	private NamespaceRedisService namespaceRedisService;
	
	@Autowired
	private StateRepository stateRepository;
	
	
	@Override
	public void init() {
		if(stateRepository.findByEntity("user").isEmpty()) {
			State state = new State();
			state.setCode("created");
			state.setDescription("刚创建的时候");
			state.setIsStart(true);
			state.setEntity(getServiceEntity());
			state.setName("创建");
			stateRepository.save(state);
			
			State state2 = new State();
			state.setCode("forbidden");
			state.setDescription("禁止");
			state.setIsStart(false);
			state.setEntity(getServiceEntity());
			state.setName("禁止");
			stateRepository.save(state);
		}
	}
	
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
