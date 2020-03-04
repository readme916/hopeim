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
import com.tianyoukeji.parent.entity.template.StateTemplateRepository;
import com.tianyoukeji.parent.service.NamespaceRedisService;
import com.tianyoukeji.parent.service.NamespaceRedisService.RedisNamespace;
import com.tianyoukeji.platform.service.StateTemplateService.Builder;
import com.tianyoukeji.parent.service.StateMachineService;

@Service
public class UserService extends StateMachineService<User> {

	@Autowired
    private RedisConnectionFactory redisConnectionFactory;
	
	@Autowired
	private NamespaceRedisService namespaceRedisService;
	
	@Autowired
	private StateTemplateService stateTemplateService;
	
	@Autowired
	private StateTemplateRepository stateTemplateRepository;
	
	@Override
	public void init() {
		if(stateTemplateRepository.count()>0) {
			return;
		}
		Builder builder = stateTemplateService.getBuilder();
		builder.entity("user")
		.state("created", "创建", true, false, false, null, null, null, null, null, null, null)
		.state("enabled", "有效用户", false, false, false, null, null, null, null, null, null, null)
		.state("disabled", "禁止用户",  false, false, false, null, null, null, null, null, null, null)
		.event("enable", "有效", "enabled", null, "enable", 0)
		.event("disable" , "禁止" , "disabled" ,null , "disable",1)
		.timer("speak", "说话定时器", "enabled", "speak", null, 20);
		
		builder.getState("created").addEvent("enable").addEvent("disable");
		builder.getState("enabled").addEvent("disable");
		builder.getState("disabled").addEvent("enable");
		builder.getEvent("enable").addRole("developer");
		builder.getEvent("disable").addRole("developer");
		builder.build();
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
	public void disable(Long uuid , StateMachine<String,String> stateMachine) {
		System.out.println("disable  动作");
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
