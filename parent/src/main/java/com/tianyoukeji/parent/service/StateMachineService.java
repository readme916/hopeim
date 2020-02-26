package com.tianyoukeji.parent.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.config.configurers.ExternalTransitionConfigurer;
import org.springframework.statemachine.config.configurers.InternalTransitionConfigurer;
import org.springframework.statemachine.data.redis.RedisStateMachineContextRepository;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.persist.RepositoryStateMachinePersist;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.liyang.jpa.smart.query.db.SmartQuery;
import com.liyang.jpa.smart.query.db.structure.EntityStructure;
import com.liyang.jpa.smart.query.service.EntityRegister;
import com.tianyoukeji.parent.annotation.StateMachineAction;
import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.common.ContextUtils;
import com.tianyoukeji.parent.entity.Event;
import com.tianyoukeji.parent.entity.EventRepository;
import com.tianyoukeji.parent.entity.IStateMachineEntity;
import com.tianyoukeji.parent.entity.State;
import com.tianyoukeji.parent.entity.StateRepository;
import com.tianyoukeji.parent.entity.Timer;
import com.tianyoukeji.parent.entity.User;
import com.tianyoukeji.parent.entity.UserRepository;
import com.tianyoukeji.parent.service.NamespaceRedisService.RedisNamespace;

/**
 * 状态机服务基础服务类
 * 
 * 封装了stateMachine的创建，和变换和持久化功能
 * 
 * @author Administrator
 *
 * @param <T>
 */

public abstract class StateMachineService<T extends IStateMachineEntity> extends BaseService<T> implements Job {

	@Autowired
	private JpaRepository<T, Long> jpaRepository;

	@Autowired
	private StateRepository stateRepository;
	
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EventRepository eventRepository;

	// 没有实际使用，只是用来确定注入的顺序
	@Autowired
	private EntityRegister entityRegister;

	@Autowired
	private Scheduler scheduler;

	private static String serviceEntity;
	
	protected static Builder<String, String> builder;

	// 包含timer的状态列表，如果包含timer这种状态的实例
	private static HashMap<String, Set<Timer>> timerStates = new HashMap<>();

	public String getServiceEntity() {
		return serviceEntity;
	}
	public JpaRepository<T, Long> getJpaRepository() {
		if(jpaRepository!=null) {
			return jpaRepository;
		}else {
			return SmartQuery.getStructure(serviceEntity).getJpaRepository();
		}
	}


	/**
	 * 返回事件是否成功执行
	 * 
	 * @param id
	 * @param eventCode
	 * @return
	 */

	@Transactional
	public boolean dispatchEvent(Long id, String eventCode) {
		StateMachine<String, String> acquireStateMachine = this.acquireStateMachine(id);
		Collection<Transition<String, String>> transitions = acquireStateMachine.getTransitions();

		boolean success = acquireStateMachine.sendEvent(eventCode);
		if (success) {
			T findById = findById(id);
			State findByEntityAndCode = stateRepository.findByEntityAndCode(serviceEntity,
					acquireStateMachine.getState().getId());
			if (findByEntityAndCode == null) {
				throw new BusinessException(1274, "不存在的状态" + acquireStateMachine.getState().getId());
			}
			findById.setState(findByEntityAndCode);
			save(findById);
		} else {
			throw new BusinessException(2111, "禁止动作");
		}
		return success;

	}

	/**
	 * 当前状态下的所有的可执行事件，如果是null，则默认为start的状态
	 * 
	 * @param
	 * @return
	 */
	public List<String> stateExecutableEvent(State state) {
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
		return new ArrayList<String>();
	}

	/**
	 * 当前状态下的当前登录用户可执行事件，如果是null，则默认为start的状态
	 * 
	 * @param
	 * @return
	 */
	public List<String> currentUserStateExecutableEvent(State state) {

		if (state == null) {
			State findByEntityAndIsStart = stateRepository.findByEntityAndIsStart(serviceEntity, true);
			if (findByEntityAndIsStart != null) {
				state = findByEntityAndIsStart;
			}
		}
		if (state == null) {
			return new ArrayList<String>();
		}
		List<Event> findBySourcesCodeAndRolesCode = eventRepository.findBySourcesCodeAndRolesCode(state.getCode(),
				ContextUtils.getRole());
		List<String> collect = findBySourcesCodeAndRolesCode.stream().sorted(new Comparator<Event>() {
			@Override
			public int compare(Event o1, Event o2) {
				return o1.getSort() - o2.getSort();
			}
		}).map(e -> e.getCode()).collect(Collectors.toList());

		return collect;
	}

	/**
	 * 事件可执行角色
	 * 
	 * @param
	 * @return
	 */
	public Set<String> eventExecutableRole(Event event) {
		if (event != null) {
			Set<String> roles = event.getRoles().stream().map(e -> e.getCode()).collect(Collectors.toSet());
			return roles;
		} else {
			throw new BusinessException(1561, "event不能为空");
		}
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
		serviceEntity = structure.getName();

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

			if (StringUtils.hasText(state.getEnterAction())) {
				builder.configureStates().withStates().stateEntry(state.getCode(),
						UnauthorizeActionFactory(state.getEnterAction()), errorAction());
			} else if (StringUtils.hasText(state.getExitAction())) {
				builder.configureStates().withStates().stateExit(state.getCode(),
						UnauthorizeActionFactory(state.getExitAction()), errorAction());
			}

			Set<Timer> timers = state.getTimers();
			for (Timer timer : timers) {
				if (timer.getTimerInterval() != null && !timer.getTimerInterval().equals(0)) {
					if (timer.getTimerInterval() < 10) {
						throw new BusinessException(1765, "timer间隔不低于10s");
					}
				} else if (timer.getTimerOnce() != null && !timer.getTimerOnce().equals(0)) {
					if (timer.getTimerInterval() < 10) {
						throw new BusinessException(1765, "timer间隔不低于10s");
					}
				}
				if (!StringUtils.hasText(timer.getAction())) {
					throw new BusinessException(1521, "Timer的动作不能为空");
				}
				try {
					Method method = this.getClass().getDeclaredMethod(timer.getAction(), Long.class,
							StateMachine.class);
					StateMachineAction annotation = method.getDeclaredAnnotation(StateMachineAction.class);
					if (annotation == null) {
						throw new BusinessException(1861,
								serviceEntity + "服务 ," + timer.getAction() + "的方法，必须使用StateMachineAction注解，才能生效");
					}
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
					throw new BusinessException(1861, serviceEntity + "服务，没有" + timer.getAction() + "的方法");
				} catch (SecurityException e) {
					e.printStackTrace();
					throw new BusinessException(1861, serviceEntity + "服务，禁止访问" + timer.getAction() + "方法");
				}

				// 如果这种状态涉及到timer，把他加入集合中
				Set<Timer> sets = timerStates.getOrDefault(state.getCode(), new HashSet<Timer>());
				sets.add(timer);
				timerStates.put(state.getCode(), sets);
			}

			Set<Event> events = state.getEvents();
			for (Event event : events) {
				if (event.getTarget() == null) {
					InternalTransitionConfigurer<String, String> eventTemp1 = builder.configureTransitions()
							.withInternal().source(state.getCode()).event(event.getCode());
					if (StringUtils.hasText(event.getAction())) {
						eventTemp1.action(AuthorizeActionFactory(event.getAction()), errorAction());
					}
					if (StringUtils.hasText(event.getGuardSpel())) {
						eventTemp1.guard(guardFactory(event.getGuardSpel()));
					}

				} else {
					ExternalTransitionConfigurer<String, String> eventTemp2 = builder.configureTransitions()
							.withExternal().source(state.getCode()).target(event.getTarget().getCode())
							.event(event.getCode());
					if (StringUtils.hasText(event.getAction())) {
						eventTemp2.action(AuthorizeActionFactory(event.getAction()), errorAction());
					}
					if (StringUtils.hasText(event.getGuardSpel())) {
						eventTemp2.guard(guardFactory(event.getGuardSpel()));
					}
				}
			}
		}
		System.out.println(structure.getName() + "状态机初始化完成");

	}

	/**
	 * 根据当前实体的id，得到他的状态机
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private StateMachine<String, String> acquireStateMachine(Long id) {

		StateMachine<String, String> stateMachine = builder.build();
		T findById = findById(id);
		if (findById != null) {
			if (findById.getState() != null) {
				DefaultExtendedState defaultExtendedState = new DefaultExtendedState();
				defaultExtendedState.getVariables().put("id", id);
				defaultExtendedState.getVariables().put("entity", serviceEntity);
				DefaultStateMachineContext<String, String> stateMachineContext = new DefaultStateMachineContext<String, String>(
						findById.getState().getCode(), null, null, defaultExtendedState, null, null);
				stateMachine.getStateMachineAccessor()
						.doWithRegion(new StateMachineFunction<StateMachineAccess<String, String>>() {
							@Override
							public void apply(StateMachineAccess<String, String> function) {
								function.resetStateMachine(stateMachineContext);
								function.addStateMachineInterceptor(
										new StateMachineInterceptorAdapter<String, String>() {
											@Override
											public void postStateChange(
													org.springframework.statemachine.state.State<String, String> state,
													Message<String> message, Transition<String, String> transition,
													StateMachine<String, String> stateMachine) {
												stopJobs(serviceEntity, id);
												if (timerStates.keySet().contains(state.getId())) {
													startJobs(serviceEntity, id, timerStates.get(state.getId()));
												}
											}

										});
							}
						});
			}
			stateMachine.getExtendedState().getVariables().put("id", id);
			stateMachine.getExtendedState().getVariables().put("entity", serviceEntity);
			stateMachine.start();
			return stateMachine;
		} else {
			throw new BusinessException(1273, "id不存在");
		}
	}

//	private void restoreTimerFromRedis() {
//
//		Set<String> sMembers = namespaceRedisService.sMembers(RedisNamespace.TIMER, serviceEntity);
//		StateMachine<String, String> build;
//		for (String id : sMembers) {
//			String machineId = this.serviceEntity + id;
//			build = builder.build();
//			try {
//				build = redisStateMachinePersister.restore(build, machineId);
//				timerMachines.put(Long.valueOf(id), build);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	}

	private Action<String, String> AuthorizeActionFactory(String actionStr) {

		StateMachineService<T> _this = this;
		try {
			Method method = _this.getClass().getDeclaredMethod(actionStr, Long.class, StateMachine.class);
			StateMachineAction annotation = method.getDeclaredAnnotation(StateMachineAction.class);
			if (annotation == null) {
				throw new BusinessException(1861,
						serviceEntity + "服务 ," + actionStr + "的方法，必须使用StateMachineAction注解，才能生效");
			}

			return new Action<String, String>() {
				@Override
				public void execute(StateContext<String, String> context) {

					Event findByEntityAndCode = eventRepository.findByEntityAndCode(serviceEntity, actionStr);
					if (!eventExecutableRole(findByEntityAndCode).contains(ContextUtils.getRole())) {
						throw new BusinessException(1231, "角色" + ContextUtils.getRole() + "无操作权限");
					}
					try {
						method.invoke(_this, context.getExtendedState().get("id", Long.class),
								context.getStateMachine());
					} catch (IllegalAccessException e) {
						e.printStackTrace();
						throw new BusinessException(1862, serviceEntity + "服务 ," + actionStr + "的方法，非法访问");
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
						throw new BusinessException(1862, serviceEntity + "服务 ," + actionStr + "的方法，非法参数");
					} catch (InvocationTargetException e) {
						e.printStackTrace();
						throw new BusinessException(2000, e.getCause().getMessage());
					}
				}
			};

		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new BusinessException(1861, serviceEntity + "服务，没有" + actionStr + "的方法");
		} catch (SecurityException e) {
			e.printStackTrace();
			throw new BusinessException(1861, serviceEntity + "服务，禁止访问" + actionStr + "方法");
		}

	}

	private Action<String, String> UnauthorizeActionFactory(String actionStr) {

		StateMachineService<T> _this = this;
		try {
			Method method = _this.getClass().getDeclaredMethod(actionStr, Long.class, StateMachine.class);
			StateMachineAction annotation = method.getDeclaredAnnotation(StateMachineAction.class);
			if (annotation == null) {
				throw new BusinessException(1861,
						serviceEntity + "服务 ," + actionStr + "的方法，必须使用StateMachineAction注解，才能生效");
			}

			return new Action<String, String>() {
				@Override
				public void execute(StateContext<String, String> context) {
					try {
						method.invoke(_this, context.getExtendedState().get("id", Long.class),
								context.getStateMachine());
					} catch (IllegalAccessException e) {
						e.printStackTrace();
						throw new BusinessException(1862, serviceEntity + "服务 ," + actionStr + "的方法，非法访问");
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
						throw new BusinessException(1862, serviceEntity + "服务 ," + actionStr + "的方法，非法参数");
					} catch (InvocationTargetException e) {
						e.printStackTrace();
						throw new BusinessException(2000, e.getCause().getMessage());
					}
				}
			};

		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new BusinessException(1861, serviceEntity + "服务，没有" + actionStr + "的方法");
		} catch (SecurityException e) {
			e.printStackTrace();
			throw new BusinessException(1861, serviceEntity + "服务，禁止访问" + actionStr + "方法");
		}
	}

	private Action<String, String> errorAction() {
		return new Action<String, String>() {
			@Override
			public void execute(StateContext<String, String> context) {
				Exception exception = context.getException();
				if (exception instanceof BusinessException) {
					throw (BusinessException) exception;
				} else {
					exception.printStackTrace();
					throw new BusinessException(3000, exception.getMessage());
				}
			}
		};
	}

	private void stopJobs(String serviceEntity, Long id) {
		GroupMatcher<JobKey> jobKeyGroupMatcher = GroupMatcher.jobGroupEquals(serviceEntity + id);
		Set<JobKey> jobKeySet;
		try {
			jobKeySet = scheduler.getJobKeys(jobKeyGroupMatcher);
			for (JobKey jobKey : jobKeySet) {
//				JobDetail jobDetail = scheduler.getJobDetail(jobKey);
//				if (jobDetail == null)
//					return;
				scheduler.deleteJob(jobKey);
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		}

	}

	private void startJobs(String serviceEntity, Long id, Set<Timer> timers) {
		for (Timer timer : timers) {
			JobDataMap map = new JobDataMap();
			map.put("entity", serviceEntity);
			map.put("id", id);
			map.put("action", timer.getAction());
			JobDetail jobDetail = JobBuilder.newJob(this.getClass()).withIdentity(timer.getCode(), serviceEntity + id)
					.usingJobData(map).build();
			SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
			if (timer.getTimerOnce() != null && timer.getTimerOnce() > 0) {
				simpleScheduleBuilder.withIntervalInSeconds(timer.getTimerOnce());
				simpleScheduleBuilder.withRepeatCount(1);
			} else {
				simpleScheduleBuilder.withIntervalInSeconds(timer.getTimerInterval()).repeatForever();
			}
			SimpleTrigger simpleTrigger = TriggerBuilder.newTrigger().withIdentity(timer.getCode(), serviceEntity + id)
					.withSchedule(simpleScheduleBuilder).build();
			try {
				scheduler.scheduleJob(jobDetail, simpleTrigger);
			} catch (SchedulerException e) {
				e.printStackTrace();
			}

		}
	}
	// spel 内的对象 user entity
	private Guard<String, String> guardFactory(String spel) {
		return new Guard<String, String>() {
			@Override
			public boolean evaluate(StateContext<String, String> context) {
				Long id = context.getExtendedState().get("id", Long.class);
				String username = ContextUtils.getCurrentUserName();
				User user = userRepository.findByUserinfoMobile(username);
				T findById = findById(id);
				ExpressionParser parser = new SpelExpressionParser();
				EvaluationContext spelContext = new StandardEvaluationContext(); 
				spelContext.setVariable("user", user);
				spelContext.setVariable("entity", findById);
				return parser.parseExpression(spel,new TemplateParserContext()).getValue(spelContext, Boolean.class);
			}
		};
	}
	// quartz job 的执行函数
	@Override
	public void execute(JobExecutionContext context) {
		String entity = context.getJobDetail().getJobDataMap().get("entity").toString();
		String id = context.getJobDetail().getJobDataMap().get("id").toString();
		String action = context.getJobDetail().getJobDataMap().getString("action");
		try {
			Method method = this.getClass().getDeclaredMethod(action, Long.class, StateMachine.class);
			StateMachineAction annotation = method.getDeclaredAnnotation(StateMachineAction.class);
			if (annotation == null) {
				throw new BusinessException(1861,
						serviceEntity + "服务 ," + action + "的方法，必须使用StateMachineAction注解，才能生效");
			}
			try {
				method.invoke(this, Long.valueOf(id),acquireStateMachine(Long.valueOf(id)));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new BusinessException(1862, serviceEntity + "服务 ," + action + "的方法，非法访问");
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				throw new BusinessException(1862, serviceEntity + "服务 ," + action + "的方法，非法参数");
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				throw new BusinessException(2000, e.getCause().getMessage());
			}

		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new BusinessException(1861, serviceEntity + "服务，没有" + action + "的方法");
		} catch (SecurityException e) {
			e.printStackTrace();
			throw new BusinessException(1861, serviceEntity + "服务，禁止访问" + action + "方法");
		}
	}

}
