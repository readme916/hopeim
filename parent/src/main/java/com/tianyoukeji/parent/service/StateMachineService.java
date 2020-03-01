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

import com.liyang.jpa.smart.query.db.SmartQuery;
import com.liyang.jpa.smart.query.db.structure.EntityStructure;
import com.liyang.jpa.smart.query.service.EntityRegister;
import com.tianyoukeji.parent.annotation.StateMachineAction;
import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.common.ContextUtils;
import com.tianyoukeji.parent.entity.Event;
import com.tianyoukeji.parent.entity.EventRepository;
import com.tianyoukeji.parent.entity.Org;
import com.tianyoukeji.parent.entity.State;
import com.tianyoukeji.parent.entity.StateRepository;
import com.tianyoukeji.parent.entity.Timer;
import com.tianyoukeji.parent.entity.TimerRepository;
import com.tianyoukeji.parent.entity.User;
import com.tianyoukeji.parent.entity.UserRepository;
import com.tianyoukeji.parent.entity.base.IOrgEntity;
import com.tianyoukeji.parent.entity.base.IStateMachineEntity;
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

public abstract class StateMachineService<T extends IStateMachineEntity> extends BaseService<T>  {

	@Autowired
	private JpaRepository<T, Long> jpaRepository;

	@Autowired
	private StateRepository stateRepository;

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private TimerRepository timerRepository;

	// 没有实际使用，只是用来确定注入的顺序
	@Autowired
	private EntityRegister entityRegister;

	@Autowired
	private Scheduler scheduler;


	@Value("${server.name}")
	private String serverName;

	//状态机池子
	private static HashMap<String,HashMap<Long,Builder<String, String>>> pools = new HashMap<String,HashMap<Long,Builder<String,String>>>();
	
	//所有服务的静态引用
	public static HashMap<String,StateMachineService> services = new HashMap<String,StateMachineService>();
	
	/**
	 * 返回事件是否成功执行
	 * 
	 * @param id
	 * @param eventCode
	 * @return 失败返回false
	 */

	@Transactional
	public boolean dispatchEvent(Long id, String eventCode) {
		StateMachine<String, String> acquireStateMachine = this.acquireStateMachine(id);
		boolean success = acquireStateMachine.sendEvent(eventCode);
		Object error = acquireStateMachine.getExtendedState().getVariables().get("error");
		if(error!=null) {
			return false;
		}
		return success;

	}


	/**
	 * 当前状态下的当前登录用户可执行事件，如果是null，则默认为start的状态
	 * 
	 * @param
	 * @return
	 */
	public List<String> currentUserStateExecutableEvent(String state) {
		State findByEntity=null;
		User currentUser = getCurrentUser();
		
		//当前用户米有组织
		if(currentUser.getOrg() == null) {
			//状态是null，可能由于是一个新建的状态机，默认为start的状态
			if(state == null) {
				findByEntity = stateRepository.findByOrgIsNullAndEntityAndIsStart(getServiceEntity(), true);			
			}else {
				findByEntity = stateRepository.findByOrgIsNullAndEntityAndCode(getServiceEntity(), state);
			}
		}else {
			if(state == null) {
				findByEntity = stateRepository.findByOrgUuidAndEntityAndIsStart(currentUser.getOrg().getUuid(), getServiceEntity(), true);
			}else {
				findByEntity = stateRepository.findByOrgUuidAndEntityAndCode(currentUser.getOrg().getUuid(), getServiceEntity(), state);
			}
		}
		if(findByEntity == null) {
			return new ArrayList<String>();
		}
		
		
		List<Event> findBySourcesUuidAndRolesCode = eventRepository.findBySourcesUuidAndRolesCode(findByEntity.getUuid(),
				ContextUtils.getRole());
		List<String> collect = findBySourcesUuidAndRolesCode.stream().sorted(new Comparator<Event>() {
			@Override
			public int compare(Event o1, Event o2) {
				return o1.getSort() - o2.getSort();
			}
		}).map(e -> e.getCode()).collect(Collectors.toList());

		return collect;
	}

//	/**
//	 * 	返回企业的当前实体的状态机builder 
//	 * @param orgId 可以为null
//	 * @return
//	 */
//	
//	public Builder<String,String> acquireBuilder(Long orgId){
//		if(orgId == null) {
//			orgId = 0l;
//		}
//		return pools.get(getServiceEntity()).get(orgId);
//	}
	
	/**
	 * 	刷新企业的当前实体的状态机builder 
	 * @param orgId 可以为null
	 * @return
	 */
	
	public Builder<String,String> refreshBuilder(Long orgId){
		Set<State> states = new HashSet<State>();
		if(orgId == null) {
			orgId = 0l;
			states = stateRepository.findByEntityAndOrgIsNull(getServiceEntity());
		}else {
			states = stateRepository.findByEntityAndOrgUuid(getServiceEntity(), orgId);
		}
		try {
			_init_org_statemachine_builder(orgId, states);
		}catch(Exception e) {
			e.printStackTrace();
			throw new BusinessException(2612, getServiceEntity() + "状态机 ，企业id："+orgId+",更新失败");
		}
		return pools.get(getServiceEntity()).get(orgId);
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

			// 如果是企业类型的数据，则设置企业的id，否则一律为0
			Long orgId = 0l;
			if (findById instanceof IOrgEntity) {
				Org org = ((IOrgEntity) findById).getOrg();
				if (org != null) {
					orgId = org.getUuid();
				}
			}
			
			HashMap<Long, Builder<String, String>> hashMap = pools.get(getServiceEntity());
			if(hashMap == null) {
				throw new BusinessException(1856, "状态机获取失败");
			}
			Builder<String, String> builder = hashMap.get(orgId);
			if(builder == null) {
				throw new BusinessException(1857, "状态机获取失败");
			}
			
			StateMachine<String, String> stateMachine = builder.build();
			// findById.getState()为null，可能是刚创建的数据，没有为其设置state，这时状态机的state为start状态
			if (findById.getState() != null) {
				DefaultExtendedState defaultExtendedState = new DefaultExtendedState();
				defaultExtendedState.getVariables().put("id", id);
				defaultExtendedState.getVariables().put("entity", getServiceEntity());
				DefaultStateMachineContext<String, String> stateMachineContext = new DefaultStateMachineContext<String, String>(
						findById.getState().getCode(), null, null, defaultExtendedState, null, null);
				stateMachine.getStateMachineAccessor()
						.doWithRegion(new StateMachineFunction<StateMachineAccess<String, String>>() {
							@Override
							public void apply(StateMachineAccess<String, String> function) {
								// 这里把状态初始化进去
								function.resetStateMachine(stateMachineContext);
								// 这里处理状态机状态变化的持久化和定时器方法
								function.addStateMachineInterceptor(
										new StateMachineInterceptorAdapter<String, String>() {
											@Override
											public void postStateChange(
													org.springframework.statemachine.state.State<String, String> state,
													Message<String> message, Transition<String, String> transition,
													StateMachine<String, String> stateMachine) {
												User user = getCurrentUser();
												State findByEntityAndCode = null;
												if (user.getOrg() == null) {
													findByEntityAndCode = stateRepository
															.findByOrgIsNullAndEntityAndCode(getServiceEntity(),
																	state.getId());
												} else {
													findByEntityAndCode = stateRepository.findByOrgUuidAndEntityAndCode(
															user.getOrg().getUuid(), getServiceEntity(), state.getId());
												}

												if (findByEntityAndCode == null) {
													throw new BusinessException(1274,
															"不存在的状态" + stateMachine.getState().getId());
												}
												findById.setState(findByEntityAndCode);
												save(findById);

												stopJobs(getServiceEntity(), id);
												Set<Timer> timers=null;
												if(user.getOrg() == null) {
													timers = timerRepository.findByOrgIsNullAndEntityAndSource(getServiceEntity(), findByEntityAndCode);
												}else {
													timers = timerRepository.findByOrgUuidAndEntityAndSource(user.getOrg().getUuid(), getServiceEntity(),findByEntityAndCode);
												}
												if (timers != null && !timers.isEmpty()) {
													startJobs(getServiceEntity(), id, timers);
												}
											}
											@Override
											public StateContext<String, String> preTransition(StateContext<String, String> stateContext) {
												if(stateContext.getTransition().getGuard()!=null) {
													boolean evaluate = stateContext.getTransition().getGuard().evaluate(stateContext);
													if(!evaluate) {
														stateContext.getExtendedState().getVariables().put("error", 1);
													}
												}
												return stateContext;
											}

										});
							}
						});
			}
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
	private void base_init() {
		this.init();
		try {
			this._init_statemachine();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BusinessException(1284, getServiceEntity() + "状态机初始化失败");
		}

		services.put(getServiceEntity(), this);
	}

	/**
	 * 把当前实体的各个企业独立的状态机的builder，存储到redis里面，方便以后调用
	 * 
	 * @throws Exception
	 */
	private void _init_statemachine() {

		List<State> allStates = stateRepository.findByEntity(getServiceEntity());
		if (allStates.isEmpty()) {
			throw new BusinessException(1321, getServiceEntity() + "的states，没有配置");
		}
		// 先把state按照org分组
		Map<Long, Set<State>> result = new HashMap<>();
		for (State s : allStates) {
			Long orgId;
			Org org = s.getOrg();
			if (org == null) {
				orgId = 0l;
			} else {
				orgId = org.getUuid();
			}
			Set<State> orgStates = result.get(orgId);
			if (orgStates == null) {
				orgStates = new HashSet<State>();
				result.put(orgId, orgStates);
			}
			orgStates.add(s);
		}
		//因为创建builder非常耗时间，有很多数据库查询，所以把创建好的builder保存
		pools.put(getServiceEntity(), new HashMap<Long,Builder<String,String>>());
		Set<Entry<Long, Set<State>>> entrySet = result.entrySet();
		for (Entry<Long, Set<State>> entry : entrySet) {
			try {
				_init_org_statemachine_builder(entry.getKey(), entry.getValue());
			} catch (Exception e) {
				e.printStackTrace();
				throw new BusinessException(1357, getServiceEntity() + " - 企业id: " + entry.getKey() + "的状态机初始化错误");
			}
		}
		System.out.println(getServiceEntity() + "状态机初始化完成");

	}

	
	
	/**
	 * 	如果没有org，则orgid输入0
	 * @param orgId
	 * @param states
	 * @throws Exception
	 */
	private void _init_org_statemachine_builder(Long orgId, Set<State> states) throws Exception {
		Builder<String, String> builder = StateMachineBuilder.<String, String>builder();
		// builder可以为空状态，空事件
		builder.configureConfiguration().withVerifier().enabled(false);
		builder.configureStates().withStates()
				.states(new HashSet<String>(states.stream().map(e -> e.getCode()).collect(Collectors.toSet())));
		for (State state : states) {
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
		pools.get(getServiceEntity()).put(orgId, builder);
	}

	private Action<String, String> AuthorizeActionFactory(String actionStr) {

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
					if(currentUser.getOrg() == null) {
						findByEntityAndCode = eventRepository.findByOrgIsNullAndEntityAndCodeAndRolesCode(getServiceEntity(), actionStr,ContextUtils.getRole());
					}else {
						findByEntityAndCode = eventRepository.findByOrgUuidAndEntityAndCodeAndRolesCode(currentUser.getOrg().getUuid(), getServiceEntity(), actionStr , ContextUtils.getRole());
					}
					if (findByEntityAndCode == null) {
						throw new BusinessException(1231, "角色" + ContextUtils.getRole() + "无操作权限");
					}
					try {
						method.invoke(_this, context.getExtendedState().get("id", Long.class),
								context.getStateMachine());
					} catch (IllegalAccessException e) {
						e.printStackTrace();
						throw new BusinessException(1862, getServiceEntity() + "服务 ," + actionStr + "的方法，非法访问");
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
						throw new BusinessException(1862, getServiceEntity() + "服务 ," + actionStr + "的方法，非法参数");
					} catch (InvocationTargetException e) {
						e.printStackTrace();
						throw new BusinessException(2000, e.getCause().getMessage());
					}
				}
			};

		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new BusinessException(1861, getServiceEntity() + "服务，没有" + actionStr + "的方法");
		} catch (SecurityException e) {
			e.printStackTrace();
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
						throw new BusinessException(1862, getServiceEntity() + "服务 ," + actionStr + "的方法，非法访问");
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
						throw new BusinessException(1862, getServiceEntity() + "服务 ," + actionStr + "的方法，非法参数");
					} catch (InvocationTargetException e) {
						e.printStackTrace();
						throw new BusinessException(2000, e.getCause().getMessage());
					}
				}
			};

		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new BusinessException(1861, getServiceEntity() + "服务，没有" + actionStr + "的方法");
		} catch (SecurityException e) {
			e.printStackTrace();
			throw new BusinessException(1861, getServiceEntity() + "服务，禁止访问" + actionStr + "方法");
		}
	}
	
	/**
	 * 事件可执行角色
	 * 
	 * @param
	 * @return
	 */
	private Set<String> eventExecutableRole(Event event) {
		if (event != null) {
			Set<String> roles = event.getRoles().stream().map(e -> e.getCode()).collect(Collectors.toSet());
			return roles;
		} else {
			throw new BusinessException(1561, "event不能为空");
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
			map.put("server", serverName);
			
			JobDetail jobDetail = JobBuilder.newJob(QuartzTimerJob.class).withIdentity(timer.getCode(), serviceEntity + id)
					.usingJobData(map).build();
			SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
			Calendar calendar= Calendar.getInstance();
			if (timer.getTimerOnce() != null && timer.getTimerOnce() > 0) {
				simpleScheduleBuilder.withIntervalInSeconds(timer.getTimerOnce());
				simpleScheduleBuilder.withRepeatCount(0);
				calendar.add(Calendar.SECOND, timer.getTimerOnce()); 
			} else {
				simpleScheduleBuilder.withIntervalInSeconds(timer.getTimerInterval()).repeatForever();
				calendar.add(Calendar.SECOND, timer.getTimerInterval()); 
			}
			Date d=calendar.getTime();
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
