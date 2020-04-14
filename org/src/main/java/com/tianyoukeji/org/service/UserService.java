package com.tianyoukeji.org.service;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tianyoukeji.org.controller.MenuController.UpdateMenuRequest;
import com.tianyoukeji.parent.annotation.StateMachineAction;
import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.common.HttpPostReturnUuid;
import com.tianyoukeji.parent.entity.Department;
import com.tianyoukeji.parent.entity.DepartmentRepository;
import com.tianyoukeji.parent.entity.Menu;
import com.tianyoukeji.parent.entity.Org;
import com.tianyoukeji.parent.entity.OrgRepository;
import com.tianyoukeji.parent.entity.Role;
import com.tianyoukeji.parent.entity.RoleRepository;
import com.tianyoukeji.parent.entity.StateRepository;
import com.tianyoukeji.parent.entity.User;
import com.tianyoukeji.parent.service.NamespaceRedisService;
import com.tianyoukeji.parent.service.NamespaceRedisService.RedisNamespace;
import com.tianyoukeji.parent.service.TIMService.ActionStatus;
import com.tianyoukeji.parent.service.TIMService.Gender;
import com.tianyoukeji.parent.service.TIMService.TIMResponse;
import com.tianyoukeji.parent.service.StateMachineService;
import com.tianyoukeji.parent.service.TIMService;

@Service
public class UserService extends StateMachineService<User> {

	@Autowired
    private RedisConnectionFactory redisConnectionFactory;
	
	@Autowired
	private NamespaceRedisService namespaceRedisService;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private DepartmentRepository departmentRepository;
	
	@Autowired
	private TIMService timService;
	
	@Override
	public void init() {
	}
	/**
	 * 	返回值表示是否可以继续往下执行后续，前置截面
	 * @param uuid
	 * @return
	 */
	@StateMachineAction
	public void doEnable(Long uuid , StateMachine<String,String> stateMachine) {
		User user = findById(uuid);
		user.setEnabled(true);
		save(user);
	}
	
	@StateMachineAction
	public void doDisable(Long uuid , StateMachine<String,String> stateMachine) {
		User user = findById(uuid);
		user.setEnabled(false);
		save(user);
		this.doKick(uuid, stateMachine);
		timService.kickUser(uuid);
	}
	
	@StateMachineAction
	public void doSpeak(Long uuid , StateMachine<String,String> stateMachine) {
		System.out.println(new Date());
	}
	
	@StateMachineAction
	public void doTest(Long uuid , StateMachine<String,String> stateMachine) {
	}
	
	
	@StateMachineAction
	public void doKick(Long uuid, StateMachine<String,String> stateMachine) {
		User user = findById(uuid);
		RedisTokenStore redisTokenStore = new RedisTokenStore(redisConnectionFactory);
		Set<String> sMembers = namespaceRedisService.sMembers(RedisNamespace.USER_TOKEN, user.getUserinfo().getMobile());
		for (String str : sMembers) {
			redisTokenStore.removeAccessToken(str);
		}
		namespaceRedisService.delete(RedisNamespace.USER_TOKEN, user.getUserinfo().getMobile());
	}
	
	
	/**
	 *	 更新用户基本信息
	 * @param body
	 * @return
	 */
	@Transactional
	public HttpPostReturnUuid update(Long uuid, String nick, Gender gender, String faceUrl, String selfSignature) {
		
		User user = findById(uuid);
		if(user==null) {
			throw new BusinessException(1391, "用户不存在");
		}
		TIMResponse updateUser = timService.updateUser(user.getUserinfo().getMobile(), nick, gender, faceUrl, selfSignature, null, null);
		if(updateUser.getActionStatus().equals(ActionStatus.OK)) {
			
			if (nick != null) {
				user.setNickname(nick);
			}
			if(gender!=null) {
				user.setGender(gender);
			}
			if(faceUrl!=null) {
				user.setHeadimgurl(faceUrl);
			}
			user = save(user);
			return new HttpPostReturnUuid(user.getUuid());
		}else {
			throw new BusinessException(1642, "更新tim用户信息失败");
		}
		
	}
}
