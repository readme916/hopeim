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
import com.tianyoukeji.parent.entity.Order;
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
public class OrderService extends StateMachineService<Order> {
	
	@Override
	public void init() {
	}
	
	
	@StateMachineAction
	public void doTest2(Long uuid, StateMachine<String,String> stateMachine) {
		System.out.println("doTest2");
	}
}