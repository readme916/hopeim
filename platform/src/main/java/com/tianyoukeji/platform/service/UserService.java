package com.tianyoukeji.platform.service;

import java.util.Date;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tianyoukeji.parent.annotation.StateMachineAction;
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
			state2.setCode("forbidden");
			state2.setDescription("禁止态");
			state2.setIsStart(false);
			state2.setEntity(getServiceEntity());
			state2.setName("禁止");
			stateRepository.save(state2);
			
			State state3 = new State();
			state3.setCode("enabled");
			state3.setDescription("正常态");
			state3.setIsStart(false);
			state3.setEntity(getServiceEntity());
			state3.setName("正常");
			stateRepository.save(state3);
		}
	}
	
	/**
	 * 	返回值表示是否可以继续往下执行后续，前置截面
	 * @param uuid
	 * @return
	 */
	@StateMachineAction
	public void enable(Long uuid , StateMachine<String,String> stateMachine) {
		System.out.println(stateMachine.getState().getId());
		System.out.println("enable  动作");
	}
	
	@StateMachineAction
	public void forbid(Long uuid , StateMachine<String,String> stateMachine) {
		System.out.println("forbid  动作");
	}
	
	@StateMachineAction
	public void speak(Long uuid , StateMachine<String,String> stateMachine) {
		System.out.println(new Date());
		System.out.println("speak  动作");
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
