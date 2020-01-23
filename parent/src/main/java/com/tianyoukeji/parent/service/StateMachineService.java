package com.tianyoukeji.parent.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.config.configurers.ExternalTransitionConfigurer;
import org.springframework.statemachine.config.configurers.InternalTransitionConfigurer;
import org.springframework.statemachine.data.redis.RedisStateMachineContextRepository;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.persist.RepositoryStateMachinePersist;
import org.springframework.util.StringUtils;

import com.liyang.jpa.smart.query.db.SmartQuery;
import com.liyang.jpa.smart.query.db.structure.EntityStructure;
import com.liyang.jpa.smart.query.service.EntityRegister;
import com.tianyoukeji.parent.entity.Event;
import com.tianyoukeji.parent.entity.IStateMachineEntity;
import com.tianyoukeji.parent.entity.State;
import com.tianyoukeji.parent.entity.StateRepository;
import com.tianyoukeji.parent.entity.Timer;

/**
 * 	状态机服务基础服务类
 * 
 * 	封装了stateMachine的创建，和变换和持久化功能
 * @author Administrator
 *
 * @param <T>
 */

public abstract class StateMachineService<T extends IStateMachineEntity> extends BaseService<T> {

	@Autowired
	private JpaRepository<T, Long> jpaRepository;
	
	@Autowired
	private StateRepository stateRepository;
	
	
	//没有实际使用，只是用来确定注入的顺序
	@Autowired
	private EntityRegister entityRegister;
	
	protected Builder<String, String> builder;
	
	@PostConstruct
	private void init() throws Exception {
		ResolvableType resolvableType = ResolvableType.forClass(jpaRepository.getClass());
		Class<?> entityClass = resolvableType.as(JpaRepository.class).getGeneric(0).resolve();
		EntityStructure structure = SmartQuery.getStructure(entityClass);
		builder = StateMachineBuilder.<String,String>builder();
		List<State> allStates = stateRepository.findByEntity(structure.getName());
		builder.configureStates().withStates().states(new HashSet<String>(allStates.stream().map(e -> e.getCode()).collect(Collectors.toSet())));
		
		for (State state : allStates) {
			if(state.getIsStart()) {
				builder.configureStates().withStates().initial(state.getCode());
			}
			if(state.getIsEnd()) {
				builder.configureStates().withStates().end(state.getCode());
			}
			if(state.getIsChoice()) {
				builder.configureStates().withStates().choice(state.getCode());
				builder.configureTransitions().withChoice().first(state.getFirstTarget().getCode(), guardFactory(state.getFirstGuardSpel()));
				if(state.getThenTarget()!=null) {
					builder.configureTransitions().withChoice().then(state.getThenTarget().getCode(), guardFactory(state.getThenGuardSpel()));
				}
				builder.configureTransitions().withChoice().last(state.getLastTarget().getCode());
			}
			
			if(StringUtils.hasText(state.getEnterAction()) && StringUtils.hasText(state.getExitAction())) {
				builder.configureStates().withStates().state(state.getCode(), ActionFactory(state.getEnterAction()), ActionFactory(state.getExitAction()));
			}else if(StringUtils.hasText(state.getEnterAction())) {
				builder.configureStates().withStates().state(state.getCode(), ActionFactory(state.getEnterAction()), null);
			}else if(StringUtils.hasText(state.getExitAction())) {
				builder.configureStates().withStates().state(state.getCode(), null, ActionFactory(state.getExitAction()));
			}
			
			Set<Timer> timers = state.getTimers();
			for (Timer timer : timers) {
				if(timer.getTarget()==null) {
					 InternalTransitionConfigurer<String, String> eventTemp1 = builder.configureTransitions().withInternal().source(state.getCode()).event(timer.getCode());
					 
					 if(timer.getTimerInterval()!=null) {
						 eventTemp1.timer(timer.getTimerInterval());
					 }
					 if(timer.getTimerOnce()!=null) {
						 eventTemp1.timerOnce(timer.getTimerOnce());
					 }
					 if(StringUtils.hasText(timer.getAction())) {
						 eventTemp1.action(ActionFactory(timer.getAction()));
					 }
					 
				}else {
					ExternalTransitionConfigurer<String, String> eventTemp2 = builder.configureTransitions().withExternal().source(state.getCode()).target(timer.getTarget().getCode());
					 if(timer.getTimerInterval()!=null) {
						 eventTemp2.timer(timer.getTimerInterval());
					 }
					 if(timer.getTimerOnce()!=null) {
						 eventTemp2.timerOnce(timer.getTimerOnce());
					 }
					if(StringUtils.hasText(timer.getAction())) {
						 eventTemp2.action(ActionFactory(timer.getAction()));
					 }
				}
			}
			
			Set<Event> events = state.getEvents();
			for (Event event : events) {
				if(event.getTarget()==null) {
					 InternalTransitionConfigurer<String, String> eventTemp1 = builder.configureTransitions().withInternal().source(state.getCode()).event(event.getCode());
					 if(StringUtils.hasText(event.getAction())) {
						 eventTemp1.action(ActionFactory(event.getAction()));
					 }
					 if(StringUtils.hasText(event.getGuardSpel())) {
						 eventTemp1.guard(guardFactory(event.getGuardSpel()));
					 }
					 
				}else {
					ExternalTransitionConfigurer<String, String> eventTemp2 = builder.configureTransitions().withExternal().source(state.getCode()).target(event.getTarget().getCode());
					 if(StringUtils.hasText(event.getAction())) {
						 eventTemp2.action(ActionFactory(event.getAction()));
					 }
					 if(StringUtils.hasText(event.getGuardSpel())) {
						 eventTemp2.guard(guardFactory(event.getGuardSpel()));
					 }
				}
			}
		}
		
		System.out.println(structure.getName()+"状态机初始化完成");
	}
	
	@Bean
	public StateMachinePersist<String, String, String> stateMachinePersist(RedisConnectionFactory connectionFactory) {
		RedisStateMachineContextRepository<String, String> repository =
				new RedisStateMachineContextRepository<String, String>(connectionFactory);
		return new RepositoryStateMachinePersist<String, String>(repository);
	}

	@Bean
	public RedisStateMachinePersister<States, Events> redisStateMachinePersister(
			StateMachinePersist<States, Events, String> stateMachinePersist) {
		return new RedisStateMachinePersister<States, Events>(stateMachinePersist);
	}
	
	private Action<String,String> ActionFactory(String actionStr){
		return new Action<String, String>() {
			@Override
			public void execute(StateContext<String, String> context) {
				System.out.println(actionStr);
			}
		};
	}
	
	private Guard<String, String> guardFactory(String spel) {
		return new Guard<String, String>() {
			@Override
			public boolean evaluate(StateContext<String, String> context) {
				System.out.println(spel);
				return true;
			}
		};
	}
	
}
