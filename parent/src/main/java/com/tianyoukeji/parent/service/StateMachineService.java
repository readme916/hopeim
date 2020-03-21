package com.tianyoukeji.parent.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ResolvableType;
import org.springframework.data.jpa.repository.JpaRepository;
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
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import com.liyang.jpa.smart.query.db.SmartQuery;
import com.liyang.jpa.smart.query.db.structure.EntityStructure;
import com.liyang.jpa.smart.query.response.HTTPListResponse;
import com.liyang.jpa.smart.query.service.EntityRegister;
import com.tianyoukeji.parent.annotation.StateMachineAction;
import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.common.ContextUtils;
import com.tianyoukeji.parent.entity.Event;
import com.tianyoukeji.parent.entity.EventRepository;
import com.tianyoukeji.parent.entity.Log;
import com.tianyoukeji.parent.entity.LogRepository;
import com.tianyoukeji.parent.entity.Org;
import com.tianyoukeji.parent.entity.State;
import com.tianyoukeji.parent.entity.State.StateType;
import com.tianyoukeji.parent.entity.StateRepository;
import com.tianyoukeji.parent.entity.Timer;
import com.tianyoukeji.parent.entity.TimerRepository;
import com.tianyoukeji.parent.entity.User;
import com.tianyoukeji.parent.entity.UserRepository;
import com.tianyoukeji.parent.entity.base.IOrgEntity;
import com.tianyoukeji.parent.entity.base.IStateMachineEntity;
import com.tianyoukeji.parent.entity.template.RoleTemplate.Terminal;
import com.tianyoukeji.parent.service.NamespaceRedisService.RedisNamespace;
import com.tianyoukeji.parent.service.RateLimiterService.RateLimiterNamespace;

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
	private HttpServletRequest request;

	@Autowired
	private JpaRepository<T, Long> jpaRepository;

	@Autowired
	private StateRepository stateRepository;

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private TimerRepository timerRepository;

	@Autowired
	private RateLimiterService rateLimiterService;

	@Autowired
	private LogRepository logRepository;

	// 没有实际使用，只是用来确定注入的顺序
	@Autowired
	private EntityRegister entityRegister;

	@Autowired
	private Scheduler scheduler;

	@Value("${server.terminal}")
	private String terminal;

	// 状态机池子
	private static HashMap<String, Builder<String, String>> pools = new HashMap<String, Builder<String, String>>();

	// 所有服务的静态引用
	public static HashMap<String, StateMachineService> services = new HashMap<String, StateMachineService>();

	/**
	 * 带log的事件触发器
	 * 
	 * @param id
	 * @param eventCode
	 * @param params    这个是用于log的对象，一般可以设置为控制器接收的body
	 */
	@Transactional
	public void dispatchEvent(Long id, String eventCode, Object params) {
		// 事件的频率限制一次/秒
		RateLimiter rateLimiter = rateLimiterService.get(RateLimiterNamespace.STATEMACHINE_EVENT,
				getServiceEntity() + id + eventCode, 1);
		rateLimiter.acquire();

		StateMachine<String, String> acquireStateMachine = this.acquireStateMachine(id);
		boolean success = acquireStateMachine.sendEvent(eventCode);
		Object error = acquireStateMachine.getExtendedState().getVariables().get("error");
		if (success == false || error != null) {
			throw new BusinessException(3000, "不能执行");
		}
		Log log = new Log();
		log.setEvent(eventCode);
		log.setDepartment(getCurrentDepartment());
		log.setOrg(getCurrentOrg());
		log.setEntity(getServiceEntity());
		log.setEntityId(id);
		log.setOperator(getCurrentUser());
		if (params != null) {
			ObjectMapper objectMapper = new ObjectMapper();
			String writeValueAsString;
			try {
				writeValueAsString = objectMapper.writeValueAsString(params);
				log.setParams(writeValueAsString);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		logRepository.save(log);
	}

	/**
	 * 	当前对象，登录用户角色可执行事件，如果id是null，则默认为start的状态
	 * 
	 * @param
	 * @return
	 */
	public List<String> currentUserExecutableEvent(Long uuid) {
		State stateEntity = null;
		T findById=null;
		if (uuid == null) {
			stateEntity = stateRepository.findByEntityAndStateType(getServiceEntity(), StateType.BEGIN);
		}else {
			findById = findById(uuid);
			if(findById==null) {
				stateEntity = stateRepository.findByEntityAndStateType(getServiceEntity(), StateType.BEGIN);
			}else {
				State state = findById.getState();
				if(state==null) {
					stateEntity = stateRepository.findByEntityAndStateType(getServiceEntity(), StateType.BEGIN);
				}else {
					stateEntity = state;
				}
			}
		
		}
		// 如果状态不存在，返回空
		if (stateEntity == null) {
			return new ArrayList<String>();
		}
		List<Event> findBySourcesUuidAndRolesCode = null;

		if (ContextUtils.getRole().equals("developer")) {
			findBySourcesUuidAndRolesCode = eventRepository.findBySourcesUuid(stateEntity.getUuid());
		} else {
			findBySourcesUuidAndRolesCode = eventRepository.findBySourcesUuidAndRolesCode(stateEntity.getUuid(),
					ContextUtils.getRole());
		}

		// 如果角色不允许，返回空
		if (findBySourcesUuidAndRolesCode == null || findBySourcesUuidAndRolesCode.isEmpty()) {
			return new ArrayList<String>();
		}

		User user = getCurrentUser();
		ExpressionParser parser = new SpelExpressionParser();
		EvaluationContext spelContext = new StandardEvaluationContext();
		spelContext.setVariable("user", user);
		spelContext.setVariable("entity", findById);
		List<String> collect = findBySourcesUuidAndRolesCode.stream().filter(e -> StringUtils.hasText(e.getGuardSpel())
				? parser.parseExpression(e.getGuardSpel(), new TemplateParserContext()).getValue(spelContext,
						Boolean.class)
				: true).sorted(new Comparator<Event>() {
					@Override
					public int compare(Event o1, Event o2) {
						return o1.getSort() - o2.getSort();
					}
				}).map(e -> e.getCode()).collect(Collectors.toList());

		return collect;
	}

	/**
	 * 根据query，返回一个具体的map格式的对象detail,包含了当前用户角色在当前状态机可执行事件events
	 * 
	 * @param queryString
	 * @return
	 */
	@Override
	public Map fetchOne(String queryString) {
		if (!entityInstanceOf(IStateMachineEntity.class)) {
			throw new BusinessException(1864, "当前实体，非状态机类型");
		}
		Map fetchOne = SmartQuery.fetchOne(getServiceEntity(), queryString);
		if (!fetchOne.isEmpty()) {
			fetchOne.put("events", currentUserExecutableEvent(Long.valueOf(fetchOne.get("uuid").toString())));
			HTTPListResponse fetchList = SmartQuery.fetchList("log",
					"fields=*,operator,org,department&page=0&size=100&entity=" + getServiceEntity() + "&entityId="
							+ fetchOne.get("uuid").toString());
			fetchOne.put("logs", fetchList.getItems());
		}
		return fetchOne;
	}

	/**
	 * 刷新企业的当前实体的状态机builder
	 * 
	 * @param orgId 可以为null
	 * @return
	 */

	public Builder<String, String> refreshBuilder() {
		Set<State> states = new HashSet<State>();
		states = stateRepository.findByEntity(getServiceEntity());
		try {
			_init_org_statemachine_builder(states);
		} catch (Exception e) {
			e.printStackTrace();
			throw new BusinessException(2612, getServiceEntity() + "状态机 ,更新失败");
		}
		return pools.get(getServiceEntity());
	}

	/**
	 * 根据当前实体的id，得到他的状态机
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public StateMachine<String, String> acquireStateMachine(Long id) {
		T findById = findById(id);
		if (findById != null) {
			Builder<String, String> builder = pools.get(getServiceEntity());
			if (builder == null) {
				throw new BusinessException(1857, "状态机获取失败");
			}

			StateMachine<String, String> stateMachine = builder.build();
			// findById.getState()为null，可能是刚创建的数据，没有为其设置state，这时状态机的state为start状态
			String stateCode = null;
			if (findById.getState() != null) {
				stateCode = findById.getState().getCode();
			}
			DefaultExtendedState defaultExtendedState = new DefaultExtendedState();
			defaultExtendedState.getVariables().put("id", id);
			defaultExtendedState.getVariables().put("entity", getServiceEntity());
			DefaultStateMachineContext<String, String> stateMachineContext = new DefaultStateMachineContext<String, String>(
					stateCode, null, null, defaultExtendedState, null, null);
			stateMachine.getStateMachineAccessor()
					.doWithRegion(new StateMachineFunction<StateMachineAccess<String, String>>() {
						@Override
						public void apply(StateMachineAccess<String, String> function) {
							// 这里把状态初始化进去
							function.resetStateMachine(stateMachineContext);
							// 这里处理状态机状态变化的持久化和定时器方法
							function.addStateMachineInterceptor(new StateMachineInterceptorAdapter<String, String>() {
								@Override
								public void postStateChange(
										org.springframework.statemachine.state.State<String, String> state,
										Message<String> message, Transition<String, String> transition,
										StateMachine<String, String> stateMachine) {
									User user = getCurrentUser();
									State findByEntityAndCode = stateRepository.findByEntityAndCode(getServiceEntity(),
											state.getId());

									if (findByEntityAndCode == null) {
										throw new BusinessException(1274, "不存在的状态" + stateMachine.getState().getId());
									}
									findById.setState(findByEntityAndCode);
									save(findById);

									stopJobs(getServiceEntity(), id);
									Set<Timer> timers = null;

									timers = timerRepository.findByEntityAndSource(getServiceEntity(),
											findByEntityAndCode);

									if (timers != null && !timers.isEmpty()) {
										startJobs(getServiceEntity(), id, timers);
									}
								}

								@Override
								public StateContext<String, String> preTransition(
										StateContext<String, String> stateContext) {
									if (stateContext.getTransition().getGuard() != null) {
										boolean evaluate = stateContext.getTransition().getGuard()
												.evaluate(stateContext);
										if (!evaluate) {
											stateContext.getExtendedState().getVariables().put("error", 1);
										}
									}
									return stateContext;
								}

							});
						}
					});
			stateMachine.getExtendedState().getVariables().put("id", id);
			stateMachine.getExtendedState().getVariables().put("entity", getServiceEntity());
			stateMachine.start();
			return stateMachine;
		} else {
			throw new BusinessException(1273, "id不存在");
		}
	}

	// 内部方法 ----------------------------------------------------------------------

	@PostConstruct
	@Override
	public void base_init() {
		this.init();
		try {
			this._init_statemachine();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BusinessException(1284, getServiceEntity() + "状态机初始化失败");
		}

		services.put(getServiceEntity(), this);
	}

	private void _init_statemachine() {

		Set<State> allStates = stateRepository.findByEntity(getServiceEntity());
		if (allStates.isEmpty()) {
			return;
		}

		// 因为创建builder非常耗时间，有很多数据库查询，所以把创建好的builder保存
		try {
			_init_org_statemachine_builder(allStates);
		} catch (Exception e) {
			e.printStackTrace();
			throw new BusinessException(1357, getServiceEntity() + "的状态机初始化错误");
		}
		System.out.println(getServiceEntity() + "状态机初始化完成");

	}

	/**
	 * @param
	 * @param states
	 * @throws Exception
	 */
	private void _init_org_statemachine_builder(Set<State> states) throws Exception {
		Builder<String, String> builder = StateMachineBuilder.<String, String>builder();
		// builder可以为空状态，空事件
		builder.configureConfiguration().withVerifier().enabled(true);
		builder.configureStates().withStates()
				.states(new HashSet<String>(states.stream().map(e -> e.getCode()).collect(Collectors.toSet())));
		for (State state : states) {
			if (state.getStateType().equals(StateType.BEGIN)) {
				builder.configureStates().withStates().initial(state.getCode());
			} else if (state.getStateType().equals(StateType.END)) {
				builder.configureStates().withStates().end(state.getCode());
			} else if (state.getStateType().equals(StateType.CHOICE)) {
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
					if (timer.getTimerOnce() < 10) {
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
								getServiceEntity() + "服务 ," + timer.getAction() + "的方法，必须使用StateMachineAction注解，才能生效");
					}
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
					throw new BusinessException(1861, getServiceEntity() + "服务，没有" + timer.getAction() + "的方法");
				} catch (SecurityException e) {
					e.printStackTrace();
					throw new BusinessException(1861, getServiceEntity() + "服务，禁止访问" + timer.getAction() + "方法");
				}
			}
			Set<Event> events = state.getEvents();
			for (Event event : events) {
				if (!event.getTerminal().equals(Terminal.valueOf(terminal))) {
					continue;
				}
				if (event.getTarget() == null) {
					InternalTransitionConfigurer<String, String> eventTemp1 = builder.configureTransitions()
							.withInternal().source(state.getCode()).event(event.getCode());
					if (StringUtils.hasText(event.getAction())) {
						eventTemp1.action(AuthorizeActionFactory(state, event.getAction()), errorAction());
					}
					if (StringUtils.hasText(event.getGuardSpel())) {
						eventTemp1.guard(guardFactory(event.getGuardSpel()));
					}

				} else {
					ExternalTransitionConfigurer<String, String> eventTemp2 = builder.configureTransitions()
							.withExternal().source(state.getCode()).target(event.getTarget().getCode())
							.event(event.getCode());
					if (StringUtils.hasText(event.getAction())) {
						eventTemp2.action(AuthorizeActionFactory(state, event.getAction()), errorAction());
					}
					if (StringUtils.hasText(event.getGuardSpel())) {
						eventTemp2.guard(guardFactory(event.getGuardSpel()));
					}
				}
			}
		}
		pools.put(getServiceEntity(), builder);
	}

	private Action<String, String> AuthorizeActionFactory(State state, String actionStr) {

		StateMachineService<T> _this = this;
		try {
			Method method = _this.getClass().getDeclaredMethod(actionStr, Long.class, StateMachine.class);
			StateMachineAction annotation = method.getDeclaredAnnotation(StateMachineAction.class);
			if (annotation == null) {
				throw new BusinessException(1861,
						getServiceEntity() + "服务 ," + actionStr + "的方法，必须使用StateMachineAction注解，才能生效");
			}

			return new Action<String, String>() {
				@Override
				public void execute(StateContext<String, String> context) {
					Event findByEntityAndCode = null;
					User currentUser = getCurrentUser();
					if (currentUser == null) {
						findByEntityAndCode = eventRepository.findBySourcesUuidAndActionAndRolesCode(state.getUuid(),
								actionStr, "user");
					} else if (request.getRequestURI().contains("/integration/")
							|| ContextUtils.getRole().equals("developer")) {
						findByEntityAndCode = eventRepository.findBySourcesUuidAndAction(state.getUuid(), actionStr);
					} else {
						findByEntityAndCode = eventRepository.findBySourcesUuidAndActionAndRolesCode(state.getUuid(),
								actionStr, ContextUtils.getRole());
					}
					if (findByEntityAndCode == null) {
						context.getExtendedState().getVariables().put("error", 1);
						throw new BusinessException(1231, "角色" + ContextUtils.getRole() + "无操作" + actionStr + "权限");
					}

					try {
						method.invoke(_this, context.getExtendedState().get("id", Long.class),
								context.getStateMachine());
					} catch (IllegalAccessException e) {
						e.printStackTrace();
						context.getExtendedState().getVariables().put("error", 1);
						throw new BusinessException(1862, getServiceEntity() + "服务 ," + actionStr + "的方法，非法访问");
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
						context.getExtendedState().getVariables().put("error", 1);
						throw new BusinessException(1862, getServiceEntity() + "服务 ," + actionStr + "的方法，非法参数");
					} catch (InvocationTargetException e) {
						e.printStackTrace();
						context.getExtendedState().getVariables().put("error", 1);
						throw new BusinessException(2000, e.getCause().getMessage());
					}

				}
			};
		} catch (NoSuchMethodException e) {
			throw new BusinessException(1861, getServiceEntity() + "服务，没有" + actionStr + "的方法");
		} catch (SecurityException e) {
			throw new BusinessException(1861, getServiceEntity() + "服务，禁止访问" + actionStr + "方法");
		}

	}

	private Action<String, String> UnauthorizeActionFactory(String actionStr) {

		StateMachineService<T> _this = this;
		try {
			Method method = _this.getClass().getDeclaredMethod(actionStr, Long.class, StateMachine.class);
			StateMachineAction annotation = method.getDeclaredAnnotation(StateMachineAction.class);
			if (annotation == null) {
				throw new BusinessException(1861,
						getServiceEntity() + "服务 ," + actionStr + "的方法，必须使用StateMachineAction注解，才能生效");
			}

			return new Action<String, String>() {
				@Override
				public void execute(StateContext<String, String> context) {
					try {
						method.invoke(_this, context.getExtendedState().get("id", Long.class),
								context.getStateMachine());
					} catch (IllegalAccessException e) {
						e.printStackTrace();
						context.getExtendedState().getVariables().put("error", 1);
						throw new BusinessException(1862, getServiceEntity() + "服务 ," + actionStr + "的方法，非法访问");
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
						context.getExtendedState().getVariables().put("error", 1);
						throw new BusinessException(1862, getServiceEntity() + "服务 ," + actionStr + "的方法，非法参数");
					} catch (InvocationTargetException e) {
						e.printStackTrace();
						context.getExtendedState().getVariables().put("error", 1);
						throw new BusinessException(2000, e.getCause().getMessage());
					}
				}
			};

		} catch (NoSuchMethodException e) {
			System.out.println(getServiceEntity() + "服务，没有" + actionStr + "的方法");
		} catch (SecurityException e) {
			System.out.println(getServiceEntity() + "服务，禁止访问" + actionStr + "方法");
		}
		return new Action<String, String>() {
			@Override
			public void execute(StateContext<String, String> context) {
			}
		};
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

			JobDetail jobDetail = JobBuilder.newJob(QuartzTimerJob.class)
					.withIdentity(timer.getCode(), serviceEntity + id).usingJobData(map).build();
			SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
			Calendar calendar = Calendar.getInstance();
			if (timer.getTimerOnce() != null && timer.getTimerOnce() > 0) {
				simpleScheduleBuilder.withIntervalInSeconds(timer.getTimerOnce());
				simpleScheduleBuilder.withRepeatCount(0);
				calendar.add(Calendar.SECOND, timer.getTimerOnce());
			} else {
				simpleScheduleBuilder.withIntervalInSeconds(timer.getTimerInterval()).repeatForever();
				calendar.add(Calendar.SECOND, timer.getTimerInterval());
			}
			Date d = calendar.getTime();
			SimpleTrigger simpleTrigger = TriggerBuilder.newTrigger().withIdentity(timer.getCode(), serviceEntity + id)
					.withSchedule(simpleScheduleBuilder).startAt(d).build();
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
				User user = getCurrentUser();
				T findById = findById(id);
				ExpressionParser parser = new SpelExpressionParser();
				EvaluationContext spelContext = new StandardEvaluationContext();
				spelContext.setVariable("user", user);
				spelContext.setVariable("entity", findById);
				return parser.parseExpression(spel, new TemplateParserContext()).getValue(spelContext, Boolean.class);
			}
		};
	}

}
