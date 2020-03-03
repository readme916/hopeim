package com.tianyoukeji.parent.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.util.concurrent.RateLimiter;
import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.common.ContextUtils;
import com.tianyoukeji.parent.entity.EventRepository;
import com.tianyoukeji.parent.entity.StateRepository;
import com.tianyoukeji.parent.entity.TimerRepository;
import com.tianyoukeji.parent.entity.template.EventTemplate;
import com.tianyoukeji.parent.entity.template.EventTemplateRepository;
import com.tianyoukeji.parent.entity.template.RoleTemplate;
import com.tianyoukeji.parent.entity.template.RoleTemplateRepository;
import com.tianyoukeji.parent.entity.template.StateTemplate;
import com.tianyoukeji.parent.entity.template.StateTemplateRepository;
import com.tianyoukeji.parent.entity.template.TimerTemplate;
import com.tianyoukeji.parent.entity.template.TimerTemplateRepository;
import com.tianyoukeji.parent.service.NamespaceRedisService.RedisNamespace;
import com.tianyoukeji.parent.service.RateLimiterService.RateLimiterNamespace;
import com.tianyoukeji.parent.service.StateTemplateService.State;

/**
 * RedisTemplate操作工具类
 *
 * @author lh
 * @version 3.0
 * @since 2016-8-29
 */
public class StateTemplateService {
	
	final static Logger logger = LoggerFactory.getLogger(StateTemplateService.class);


	@Autowired
	private StateRepository stateRepository;
	
	@Autowired
	private EventRepository eventRepository;
	
	@Autowired
	private TimerRepository timerRepository;
	
	@Autowired
	private RoleTemplate roleTemplate;
	
	@Autowired
	private RoleTemplateRepository roleTemplateRepository;
	
	@Autowired
	private StateTemplateRepository stateTemplateRepository;
	
	@Autowired
	private EventTemplateRepository eventTemplateRepository;
	
	@Autowired
	private TimerTemplateRepository timerTemplateRepository;
	
	public Builder getBuilder() {
			Builder builder = new Builder();
			builder.setEventTemplateRepository(eventTemplateRepository);
			builder.setRoleTemplateRepository(roleTemplateRepository);
			builder.setStateTemplateRepository(stateTemplateRepository);
			builder.setTimerTemplateRepository(timerTemplateRepository);
			return builder;
	}
	
	
	public static class Builder{
		private String entity;
		private HashMap<String,State> allStates = new HashMap<String,State>();
		private HashMap<String,Event> allEvents = new HashMap<String,Event>();
		private HashMap<String,Timer> allTimers = new HashMap<String,Timer>();
		
		private RoleTemplateRepository roleTemplateRepository;
		private StateTemplateRepository stateTemplateRepository;
		private EventTemplateRepository eventTemplateRepository;
		private TimerTemplateRepository timerTemplateRepository;
		
		public void setRoleTemplateRepository(RoleTemplateRepository roleTemplateRepository) {
			this.roleTemplateRepository = roleTemplateRepository;
		}
		public void setStateTemplateRepository(StateTemplateRepository stateTemplateRepository) {
			this.stateTemplateRepository = stateTemplateRepository;
		}
		public void setEventTemplateRepository(EventTemplateRepository eventTemplateRepository) {
			this.eventTemplateRepository = eventTemplateRepository;
		}
		public void setTimerTemplateRepository(TimerTemplateRepository timerTemplateRepository) {
			this.timerTemplateRepository = timerTemplateRepository;
		}
		public Builder entity(String entity) {
			this.entity = entity;
			return this;
		}
		public Builder state(String code, String name, Boolean isStart, Boolean isEnd, Boolean isChoice,
				String firstTarget, String firstGuardSpel, String thenTarget, String thenGuardSpel, String lastTarget, String enterAction, String exitAction) {
			State s = new State(code, name, isStart, isEnd, isChoice,firstTarget, firstGuardSpel, thenTarget, thenGuardSpel, lastTarget, enterAction, exitAction);
			if(allStates.containsKey(code)) {
				throw new BusinessException(1746, "状态 ： " + code +"已经存在");
			}
			s.setBuilder(this);
			allStates.put(code, s);
			return this;
		}
		public Builder event(String code, String name, String target, String guardSpel, String action, long sort) {
			Event e = new Event(code, name, target ,guardSpel ,action , sort);
			if(allEvents.containsKey(code)) {
				throw new BusinessException(1746, "事件： " + code +"已经存在");
			}
			e.setBuilder(this);
			allEvents.put(code, e);
			return this;
		}
		public Builder timer(String code, String name, String source, String action, Integer timerInterval, Integer timerOnce) {
			Timer t = new Timer(code, name, source ,action ,timerInterval , timerOnce);
			if(allTimers.containsKey(code)) {
				throw new BusinessException(1746, "定时器： " + code +"已经存在");
			}
			t.setBuilder(this);
			allTimers.put(code, t);
			return this;
		}
		
		public State getState(String code) {
			return allStates.get(code);
		}
		public Event getEvent(String code) {
			return allEvents.get(code);
		}
		public Timer getTimer(String code) {
			return allTimers.get(code);
		}
		
		
		public Builder build() {
			if(this.entity == null) {
				throw new BusinessException(1832, "请指定entity");
			}
			
			/**
			 * 	填入数据库
			 */
			Set<Entry<String,State>> entrySet = allStates.entrySet();
			for (Entry<String, State> entry : entrySet) {
				stateTostateTemplate(entry.getValue());
			}
			return this;
		}
		
		private StateTemplate stateToStateTemplate(State state) {
			if(state == null) {
				return null;
			}
			StateTemplate stateTemplate = stateTemplateRepository.findByEntityAndCode(entity, state.code);
			if(stateTemplate == null) {
				stateTemplate = new StateTemplate();
			}
			
			stateTemplate.setCode(state.code);
			stateTemplate.setEnterAction(state.enterAction);
			stateTemplate.setEntity(entity);
			stateTemplate.setExitAction(state.exitAction);
			stateTemplate.setFirstGuardSpel(state.firstGuardSpel);
			stateTemplate.setFirstTarget(state.firstTarget);
			stateTemplate.setIsChoice(state.isChoice);
			stateTemplate.setIsEnd(state.isEnd);
			stateTemplate.setIsStart(state.isStart);
			stateTemplate.setLastTarget(state.lastTarget);
			stateTemplate.setName(state.name);
			stateTemplate.setThenGuardSpel(state.thenGuardSpel);
			stateTemplate.setThenTarget(state.thenTarget);
		}
		
		private EventTemplate eventToEventTemplate(Event event) {
			if(event == null) {
				return null;
			}
			EventTemplate eventTemplate = eventTemplateRepository.findByEntityAndCode(entity, event.code);
			if(eventTemplate == null) {
				eventTemplate = new EventTemplate();
			}
		}
		private TimerTemplate timerToTimerTemplate(Timer timer) {
			if(timer == null) {
				return null;
			}
			TimerTemplate timerTemplate = timerTemplateRepository.findByEntityAndCode(entity, timer.code);
			if(timerTemplate == null) {
				timerTemplate = new TimerTemplate();
			}
		}
		
	}
	public static class State{
		
		//保存builder的引用
		private Builder builder;
		private String code;
		private String name;
		private Boolean isStart;
		private Boolean isEnd;
		private Boolean isChoice;
		private String firstTarget;
		private String firstGuardSpel;
		private String thenTarget;
		private String thenGuardSpel;
		private String lastTarget;
		private String enterAction;
		private String exitAction;
		private HashSet<Event> events = new HashSet<Event>();
		private HashSet<Timer> timers = new HashSet<Timer>();
		public State(String code, String name, Boolean isStart, Boolean isEnd, Boolean isChoice,
				String firstTarget, String firstGuardSpel, String thenTarget, String thenGuardSpel, String lastTarget, String enterAction, String exitAction) {
			super();
			this.code = code;
			this.name = name;
			this.isStart = isStart;
			this.isEnd = isEnd;
			this.isChoice = isChoice;
			this.firstTarget = firstTarget;
			this.firstGuardSpel = firstGuardSpel;
			this.thenTarget = thenTarget;
			this.thenGuardSpel = thenGuardSpel;
			this.lastTarget = lastTarget;
			this.enterAction = enterAction;
			this.exitAction = exitAction;
		}
		
		public void setBuilder(Builder builder) {
			this.builder = builder;
		}

		public State addEvent(String event) {
			if(builder.getEvent(event)==null) {
				throw new BusinessException(1329, "事件:" + event+ "没有设置");
			}
			this.events.add(builder.getEvent(event));
			return this;
		}
		public State addTimer(String timer) {
			if(builder.getTimer(timer)==null) {
				throw new BusinessException(1329, "定时器:" + timer+ "没有设置");
			}
			this.timers.add(builder.getTimer(timer));
			return this;
		}
	}
	public static class Event{
		//保存builder的引用
		private Builder builder;
		private String code;
		private String name;
		private String target;
		private String guardSpel;
		private String action;
		private long sort;
		private HashSet<String> roles = new HashSet<String>();
		public Event(String code, String name, String target, String guardSpel, String action, long sort) {
			super();
			this.code = code;
			this.name = name;
			this.target = target;
			this.guardSpel = guardSpel;
			this.action = action;
			this.sort = sort;
		}
		public Event addRole(String role) {
			this.roles.add(role);
			return this;
		}
		public void setBuilder(Builder builder) {
			this.builder = builder;
		}

	}
	public static class Timer{
		//保存builder的引用
		private Builder builder;
		private String code;
		private String name;
		private String source;
		private String action;
		private Integer timerInterval;
		private Integer timerOnce;
		public Timer(String code, String name, String source, String action, Integer timerInterval, Integer timerOnce) {
			super();
			this.code = code;
			this.name = name;
			this.source = source;
			this.action = action;
			this.timerInterval = timerInterval;
			this.timerOnce = timerOnce;
		}
		public void setBuilder(Builder builder) {
			this.builder = builder;
		}

	}
	

}