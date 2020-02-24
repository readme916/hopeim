package com.tianyoukeji.parent.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ResolvableType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachineException;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.config.configurers.ExternalTransitionConfigurer;
import org.springframework.statemachine.config.configurers.InternalTransitionConfigurer;
import org.springframework.statemachine.data.redis.RedisPersistingStateMachineInterceptor;
import org.springframework.statemachine.data.redis.RedisStateMachineContextRepository;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.statemachine.data.redis.RedisStateMachineRepository;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.persist.RepositoryStateMachinePersist;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.transition.Transition;
import org.springframework.util.StringUtils;

import com.alibaba.druid.stat.TableStat.Name;
import com.liyang.jpa.smart.query.db.SmartQuery;
import com.liyang.jpa.smart.query.db.structure.EntityStructure;
import com.liyang.jpa.smart.query.service.EntityRegister;
import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.entity.Event;
import com.tianyoukeji.parent.entity.IStateMachineEntity;
import com.tianyoukeji.parent.entity.State;
import com.tianyoukeji.parent.entity.StateRepository;
import com.tianyoukeji.parent.entity.Timer;

/**
 * 状态机服务基础服务类
 * 
 * 封装了stateMachine的创建，和变换和持久化功能
 * 
 * @author Administrator
 *
 * @param <T>
 */

public abstract class StateMachineService<T extends IStateMachineEntity> extends BaseService<T> {

	@Autowired
	private JpaRepository<T, Long> jpaRepository;

	@Autowired
	private StateRepository stateRepository;

	private String serviceEntity;

	@Autowired
	private RedisStateMachineRepository redisStateMachineRepository;

	private StateMachinePersist<String, String, String> stateMachinePersist;

	// 没有实际使用，只是用来确定注入的顺序
	@Autowired
	private EntityRegister entityRegister;

	protected Builder<String, String> builder;

//	// 包含timer的状态列表，如果包含timer这种状态的实例，重启服务器时候必须restore这个状态机
//	private Set<String> timerStates = new HashSet<String>();

	public String getServiceEntity() {
		return serviceEntity;
	}


	/**
	 * 	返回事件是否成功执行
	 * @param id
	 * @param eventCode
	 * @return
	 */
	
	public boolean dispatchEvent(Long id, String eventCode) {
		StateMachine<String, String> acquireStateMachine = this.acquireStateMachine(id);
		Collection<Transition<String,String>> transitions = acquireStateMachine.getTransitions();
		
		boolean success = acquireStateMachine.sendEvent(eventCode);
		if(success) {
			T findById = findById(id);
			State findByEntityAndCode = stateRepository.findByEntityAndCode(serviceEntity,acquireStateMachine.getState().getId());
			if(findByEntityAndCode == null) {
				throw new BusinessException(1274, "不存在的状态"+acquireStateMachine.getState().getId());
			}
			findById.setState(findByEntityAndCode);
			save(findById);
		}else {
			throw new BusinessException(2111, "禁止动作");
		}
		return success;
		
	}

	/**
	 * 	当前状态下的可执行事件
	 * 
	 * @param id
	 * @return
	 */
	public List<String> executableEvent(Long id) {

		T findById = this.findById(id);
		if (findById != null) {
			State state = findById.getState();
			if (state != null) {
				List<String> events = state.getEvents().stream().sorted(new Comparator<Event>() {
					@Override
					public int compare(Event o1, Event o2) {
						return o1.getSort() - o2.getSort();
					}
				}).map(e -> e.getCode()).collect(Collectors.toList());
				return events;
			} else {
				State findByEntityAndIsStart = stateRepository.findByEntityAndIsStart(serviceEntity, true);
				if (findByEntityAndIsStart != null) {
					List<String> events = findByEntityAndIsStart.getEvents().stream().sorted(new Comparator<Event>() {
						@Override
						public int compare(Event o1, Event o2) {
							return o1.getSort() - o2.getSort();
						}
					}).map(e -> e.getCode()).collect(Collectors.toList());
					return events;
				}
			}
		} else {
			throw new BusinessException(1816, "id不存在");
		}

		return new ArrayList<String>();
	}

	/**
	 * 继承的方法,状态机初始化之前调用，可以插入一些业务的state等到states数据库，完成状态机的设置
	 */
	abstract protected void init();

	// 内部方法 ----------------------------------------------------------------------

	@PostConstruct
	private void base_init() throws Exception {
		this.init();
		ResolvableType resolvableType = ResolvableType.forClass(jpaRepository.getClass());
		Class<?> entityClass = resolvableType.as(JpaRepository.class).getGeneric(0).resolve();
		EntityStructure structure = SmartQuery.getStructure(entityClass);

		// 设置服务对应的实例名字
		this.serviceEntity = structure.getName();

		builder = StateMachineBuilder.<String, String>builder();
		List<State> allStates = stateRepository.findByEntity(structure.getName());
		if (allStates.isEmpty()) {
			throw new BusinessException(1321, structure.getName() + "的states，没有配置");
		}
		builder.configureConfiguration().withVerifier().enabled(false);

		builder.configureStates().withStates()
				.states(new HashSet<String>(allStates.stream().map(e -> e.getCode()).collect(Collectors.toSet())));
		for (State state : allStates) {
			if (state.getIsStart() != null && state.getIsStart()) {
				builder.configureStates().withStates().initial(state.getCode());
			}
			if (state.getIsEnd() != null && state.getIsEnd()) {
				builder.configureStates().withStates().end(state.getCode());
			}
			if (state.getIsChoice() != null && state.getIsChoice()) {
				builder.configureStates().withStates().choice(state.getCode());
				builder.configureTransitions().withChoice().first(state.getFirstTarget().getCode(),
						guardFactory(state.getFirstGuardSpel()));
				if (state.getThenTarget() != null) {
					builder.configureTransitions().withChoice().then(state.getThenTarget().getCode(),
							guardFactory(state.getThenGuardSpel()));
				}
				builder.configureTransitions().withChoice().last(state.getLastTarget().getCode());
			}

			if (StringUtils.hasText(state.getEnterAction()) && StringUtils.hasText(state.getExitAction())) {
				builder.configureStates().withStates().state(state.getCode(), ActionFactory(state.getEnterAction()),
						ActionFactory(state.getExitAction()));
			} else if (StringUtils.hasText(state.getEnterAction())) {
				builder.configureStates().withStates().state(state.getCode(), ActionFactory(state.getEnterAction()),
						null);
			} else if (StringUtils.hasText(state.getExitAction())) {
				builder.configureStates().withStates().state(state.getCode(), null,
						ActionFactory(state.getExitAction()));
			}

			Set<Timer> timers = state.getTimers();
			for (Timer timer : timers) {

				// 如果这种状态涉及到timer，把他加入集合中
//				timerStates.add(state.getCode());
				if (timer.getTarget() == null) {
					InternalTransitionConfigurer<String, String> eventTemp1 = builder.configureTransitions()
							.withInternal().source(state.getCode()).event(timer.getCode());

					if (timer.getTimerInterval() != null && !timer.getTimerInterval().equals(0)) {
						eventTemp1.timer(timer.getTimerInterval());
					}else if (timer.getTimerOnce() != null && !timer.getTimerOnce().equals(0)) {
						eventTemp1.timerOnce(timer.getTimerOnce());
					}
					if (StringUtils.hasText(timer.getAction())) {
						eventTemp1.action(TimerActionFactory(timer.getAction(),state.getCode()));
					}
				} else {
					ExternalTransitionConfigurer<String, String> eventTemp2 = builder.configureTransitions()
							.withExternal().source(state.getCode()).target(timer.getTarget().getCode());
					if (timer.getTimerInterval() != null && !timer.getTimerInterval().equals(0)) {
						eventTemp2.timer(timer.getTimerInterval());
					}else if (timer.getTimerOnce() != null && !timer.getTimerOnce().equals(0)) {
						eventTemp2.timerOnce(timer.getTimerOnce());
					}
					if (StringUtils.hasText(timer.getAction())) {
						eventTemp2.action(TimerActionFactory(timer.getAction(),state.getCode()));
					}
				}
			}

			Set<Event> events = state.getEvents();
			for (Event event : events) {
				if (event.getTarget() == null) {
					InternalTransitionConfigurer<String, String> eventTemp1 = builder.configureTransitions()
							.withInternal().source(state.getCode()).event(event.getCode());
					if (StringUtils.hasText(event.getAction())) {
						eventTemp1.action(ActionFactory(event.getAction()));
					}
					if (StringUtils.hasText(event.getGuardSpel())) {
						eventTemp1.guard(guardFactory(event.getGuardSpel()));
					}

				} else {
					ExternalTransitionConfigurer<String, String> eventTemp2 = builder.configureTransitions()
							.withExternal().source(state.getCode()).target(event.getTarget().getCode()).event(event.getCode());
					if (StringUtils.hasText(event.getAction())) {
						eventTemp2.action(ActionFactory(event.getAction()));
					}
					if (StringUtils.hasText(event.getGuardSpel())) {
						eventTemp2.guard(guardFactory(event.getGuardSpel()));
					}
				}
			}
		}
		System.out.println(structure.getName() + "状态机初始化完成，开始创建所有带timer的状态的状态机的实例");

	}
	

	/**
	 * 	根据当前实体的id，得到他的状态机
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private StateMachine<String, String> acquireStateMachine(Long id) {

		String machineId = this.serviceEntity + String.valueOf(id);

		StateMachine<String, String> stateMachine = builder.build();
		T findById = this.findById(id);
		if (findById != null) {
			if (findById.getState() != null) {
				DefaultExtendedState defaultExtendedState = new DefaultExtendedState();
				defaultExtendedState.getVariables().put("id", id);
				defaultExtendedState.getVariables().put("entity",  this.serviceEntity);
				DefaultStateMachineContext<String, String> stateMachineContext = new DefaultStateMachineContext<String, String>(
						findById.getState().getCode(), null, null, defaultExtendedState, null, null);
				stateMachine.getStateMachineAccessor()
						.doWithRegion(new StateMachineFunction<StateMachineAccess<String, String>>() {
							@Override
							public void apply(StateMachineAccess<String, String> function) {
								function.resetStateMachine(stateMachineContext);
							}
						});
			}
			stateMachine.getExtendedState().getVariables().put("id", id);
			stateMachine.getExtendedState().getVariables().put("entity",  this.serviceEntity);
			stateMachine.start();
			return stateMachine;
		} else {
			throw new BusinessException(1273, "id不存在");
		}
	}

	private Action<String, String> ActionFactory(String actionStr) {
		return new Action<String, String>() {
			@Override
			public void execute(StateContext<String, String> context) {
				System.out.println(actionStr);
			}
		};
	}

	private Action<String, String> TimerActionFactory(String actionStr, String state) {
		return new Action<String, String>() {
			@Override
			public void execute(StateContext<String, String> context) {
				System.out.println(actionStr);
				Long id = context.getExtendedState().get("id", Long.class);
				String entity = context.getExtendedState().get("entity", String.class);
				//timer执行时候，查看是否有时间锁，有则取消
				//然后查看id的state是否和设置的时候一致，不一致则取消
				
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
