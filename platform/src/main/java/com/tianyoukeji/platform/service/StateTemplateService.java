package com.tianyoukeji.platform.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.util.concurrent.RateLimiter;
import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.common.ContextUtils;
import com.tianyoukeji.parent.entity.Event;
import com.tianyoukeji.parent.entity.EventRepository;
import com.tianyoukeji.parent.entity.Org;
import com.tianyoukeji.parent.entity.RoleRepository;
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
import com.tianyoukeji.platform.service.StateTemplateService.State;

@Service
public class StateTemplateService {

	final static Logger logger = LoggerFactory.getLogger(StateTemplateService.class);

	@Autowired
	private StateRepository stateRepository;

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private TimerRepository timerRepository;
	
	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private RoleTemplateRepository roleTemplateRepository;

	@Autowired
	private StateTemplateRepository stateTemplateRepository;

	@Autowired
	private EventTemplateRepository eventTemplateRepository;

	@Autowired
	private TimerTemplateRepository timerTemplateRepository;

	
	@Transactional
	public void entityStateDeploy(String entity, Org org) {
		List<StateTemplate> findByEntity = stateTemplateRepository.findByEntity(entity);
		for (StateTemplate stateTemplate : findByEntity) {
			stateTemplateTransfer(stateTemplate, org);
		}
	}

	// -------------------------------------private----------------------------

	private com.tianyoukeji.parent.entity.State stateTemplateTransfer(StateTemplate stateTemplate, Org org) {
		if(stateTemplate==null) {
			return null;
		}
		if(org == null) {
			com.tianyoukeji.parent.entity.State findByOrgIsNullAndEntityAndCode = stateRepository.findByOrgIsNullAndEntityAndCode(stateTemplate.getEntity(), stateTemplate.getCode());
			if(findByOrgIsNullAndEntityAndCode!=null) {
				return findByOrgIsNullAndEntityAndCode;
			}
		}else {
			com.tianyoukeji.parent.entity.State findByOrgIsNullAndEntityAndCode = stateRepository.findByOrgUuidAndEntityAndCode(org.getUuid(),stateTemplate.getEntity(), stateTemplate.getCode());
			if(findByOrgIsNullAndEntityAndCode!=null) {
				return findByOrgIsNullAndEntityAndCode;
			}
		}
		com.tianyoukeji.parent.entity.State state = new com.tianyoukeji.parent.entity.State();
		state.setCode(stateTemplate.getCode());
		state.setEnterAction(stateTemplate.getEnterAction());
		state.setEntity(stateTemplate.getEntity());
		state.setExitAction(stateTemplate.getExitAction());
		state.setFirstGuardSpel(stateTemplate.getFirstGuardSpel());
		state.setFirstTarget(stateTemplateTransfer(stateTemplate.getFirstTarget(),org));
		state.setIsChoice(stateTemplate.getIsChoice());
		state.setIsEnd(stateTemplate.getIsEnd());
		state.setIsStart(stateTemplate.getIsStart());
		state.setLastTarget(stateTemplateTransfer(stateTemplate.getLastTarget(), org));
		state.setName(stateTemplate.getName());
		state.setOrg(org);
		state.setThenGuardSpel(stateTemplate.getThenGuardSpel());
		state.setThenTarget(stateTemplateTransfer(stateTemplate.getThenTarget(),org));
		state = stateRepository.saveAndFlush(state);
		timerTemplateTransfer(stateTemplate.getTimerTemplates(),org,state);
		Set<com.tianyoukeji.parent.entity.Event> collect = new HashSet<>();
		
		Set<EventTemplate> eventTemplates = stateTemplate.getEventTemplates();
		for (EventTemplate eventTemplate : eventTemplates) {
			collect.add(eventTemplateTransfer(eventTemplate,org));
		}
		state.setEvents(collect);
		state  = stateRepository.saveAndFlush(state);
		return state;
		
	}
	
	private Set<com.tianyoukeji.parent.entity.Timer> timerTemplateTransfer(Set<TimerTemplate> timerTemplates ,Org org , com.tianyoukeji.parent.entity.State state){
		if(timerTemplates==null || timerTemplates.isEmpty()) {
			return null;
		}
		HashSet<com.tianyoukeji.parent.entity.Timer> timers = new HashSet<com.tianyoukeji.parent.entity.Timer>();
		for (TimerTemplate timerTemplate : timerTemplates) {
			com.tianyoukeji.parent.entity.Timer timer = new com.tianyoukeji.parent.entity.Timer();
			timer.setAction(timerTemplate.getAction());
			timer.setCode(timerTemplate.getCode());
			timer.setEntity(timerTemplate.getEntity());
			timer.setName(timerTemplate.getName());
			timer.setOrg(org);
			timer.setSource(state);
			timer.setTimerInterval(timerTemplate.getTimerInterval());
			timer.setTimerOnce(timerTemplate.getTimerOnce());
			timer= timerRepository.saveAndFlush(timer);
			timers.add(timer);
		}
		return timers;
	}
	
	private com.tianyoukeji.parent.entity.Event eventTemplateTransfer(EventTemplate eventTemplate, Org org) {
		if(eventTemplate==null) {
			return null;
		}
		if(org == null) {
			com.tianyoukeji.parent.entity.Event findByOrgIsNullAndEntityAndCode = eventRepository.findByOrgIsNullAndEntityAndCode(eventTemplate.getEntity(), eventTemplate.getCode());
			if(findByOrgIsNullAndEntityAndCode!=null) {
				return findByOrgIsNullAndEntityAndCode;
			}
			
		}else {
			com.tianyoukeji.parent.entity.Event findByOrgIsNullAndEntityAndCode = eventRepository.findByOrgUuidAndEntityAndCode(org.getUuid(),eventTemplate.getEntity(), eventTemplate.getCode());
			if(findByOrgIsNullAndEntityAndCode!=null) {
				return findByOrgIsNullAndEntityAndCode;
			}
		}
		
		com.tianyoukeji.parent.entity.Event event = new com.tianyoukeji.parent.entity.Event();
		event.setAction(eventTemplate.getAction());
		event.setCode(eventTemplate.getCode());
		event.setEntity(eventTemplate.getEntity());
		event.setGuardSpel(eventTemplate.getGuardSpel());
		event.setName(eventTemplate.getName());
		event.setOrg(org);
		event.setRoles(getOrgRoles(eventTemplate.getRoleTemplates(), org));
		event.setSort(eventTemplate.getSort());
		event = eventRepository.saveAndFlush(event);
		event.setTarget(stateTemplateTransfer(eventTemplate.getTarget(),org));
		return eventRepository.saveAndFlush(event);
		
	}
	
	private Set<com.tianyoukeji.parent.entity.Role> getOrgRoles(Set<RoleTemplate> roleTemplates, Org org) {
		if(roleTemplates==null || roleTemplates.isEmpty()) {
			return null;
		}
		
		HashSet<com.tianyoukeji.parent.entity.Role> roles = new HashSet<com.tianyoukeji.parent.entity.Role>();
		if(org == null) {
			for (RoleTemplate roleTemplate : roleTemplates) {
				com.tianyoukeji.parent.entity.Role role = roleRepository.findByCodeAndOrgIsNull(roleTemplate.getCode());
				if(role == null) {
					throw new BusinessException(1741, "企业角色"+role+"没有正确设置");
				}
				roles.add(role);
			}
		}else {
			for (RoleTemplate roleTemplate : roleTemplates) {
				com.tianyoukeji.parent.entity.Role role = roleRepository.findByCodeAndOrg(roleTemplate.getCode(), org);
				if(role == null) {
					throw new BusinessException(1741, "企业角色"+role+"没有正确设置");
				}
				roles.add(role);
			}
		}
		return roles;
	}
	
	// ----------------------------------------------builder
	// ------------------------

	public Builder getBuilder() {
		Builder builder = new Builder();
		builder.setEventTemplateRepository(eventTemplateRepository);
		builder.setRoleTemplateRepository(roleTemplateRepository);
		builder.setStateTemplateRepository(stateTemplateRepository);
		builder.setTimerTemplateRepository(timerTemplateRepository);
		return builder;
	}

	public static class Builder {
		private String entity;
		private HashMap<String, State> allStates = new HashMap<String, State>();
		private HashMap<String, Event> allEvents = new HashMap<String, Event>();
		private HashMap<String, Timer> allTimers = new HashMap<String, Timer>();

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
				String firstTarget, String firstGuardSpel, String thenTarget, String thenGuardSpel, String lastTarget,
				String enterAction, String exitAction) {
			State s = new State(code, name, isStart, isEnd, isChoice, firstTarget, firstGuardSpel, thenTarget,
					thenGuardSpel, lastTarget, enterAction, exitAction);
			if (allStates.containsKey(code)) {
				throw new BusinessException(1746, "状态 ： " + code + "已经存在");
			}
			s.setBuilder(this);
			allStates.put(code, s);
			return this;
		}

		public Builder event(String code, String name, String target, String guardSpel, String action, int sort) {
			Event e = new Event(code, name, target, guardSpel, action, sort);
			if (allEvents.containsKey(code)) {
				throw new BusinessException(1746, "事件： " + code + "已经存在");
			}
			e.setBuilder(this);
			allEvents.put(code, e);
			return this;
		}

		public Builder timer(String code, String name, String source, String action, Integer timerInterval,
				Integer timerOnce) {
			Timer t = new Timer(code, name, source, action, timerInterval, timerOnce);
			if (allTimers.containsKey(code)) {
				throw new BusinessException(1746, "定时器： " + code + "已经存在");
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

		@Transactional
		public Builder build() {
			if (this.entity == null) {
				throw new BusinessException(1832, "请指定entity");
			}
			/**
			 * 填入数据库
			 */
			Set<Entry<String, State>> stateSet = allStates.entrySet();
			for (Entry<String, State> entry : stateSet) {
				stateToStateTemplate(entry.getValue());
			}
			Set<Entry<String, Event>> eventSet = allEvents.entrySet();
			for (Entry<String, Event> entry : eventSet) {
				eventToEventTemplate(entry.getValue());
			}
			Set<Entry<String, Timer>> timerSet = allTimers.entrySet();
			for (Entry<String, Timer> entry : timerSet) {
				timerToTimerTemplate(entry.getValue());
			}
			return this;
		}

		private StateTemplate stateToStateTemplate(State state) {
			if (state == null) {
				return null;
			}
			StateTemplate stateTemplate = stateTemplateRepository.findByEntityAndCode(entity, state.code);
			if (stateTemplate == null) {
				stateTemplate = new StateTemplate();
			}

			stateTemplate.setCode(state.code);
			stateTemplate.setEnterAction(state.enterAction);
			stateTemplate.setEntity(entity);
			stateTemplate.setExitAction(state.exitAction);
			stateTemplate.setFirstGuardSpel(state.firstGuardSpel);
			stateTemplate.setFirstTarget(stateToStateTemplate(getState(state.firstTarget)));
			stateTemplate.setIsChoice(state.isChoice);
			stateTemplate.setIsEnd(state.isEnd);
			stateTemplate.setIsStart(state.isStart);
			stateTemplate.setLastTarget(stateToStateTemplate(getState(state.lastTarget)));
			stateTemplate.setName(state.name);
			stateTemplate.setThenGuardSpel(state.thenGuardSpel);
			stateTemplate.setThenTarget(stateToStateTemplate(getState(state.thenTarget)));
			stateTemplate = stateTemplateRepository.saveAndFlush(stateTemplate);
			return stateTemplate;
		}

		private EventTemplate eventToEventTemplate(Event event) {
			if (event == null) {
				return null;
			}
			EventTemplate eventTemplate = eventTemplateRepository.findByEntityAndCode(entity, event.code);
			if (eventTemplate == null) {
				eventTemplate = new EventTemplate();
			}
			eventTemplate.setAction(event.code);
			eventTemplate.setCode(event.code);
			eventTemplate.setEntity(entity);
			eventTemplate.setGuardSpel(event.guardSpel);
			eventTemplate.setName(event.name);
			eventTemplate.setSort(event.sort);
			eventTemplate.setTarget(stateToStateTemplate(getState(event.target)));
			eventTemplate = eventTemplateRepository.saveAndFlush(eventTemplate);

			Set<State> collect = allStates.values().stream().filter(s -> s.events.contains(event))
					.collect(Collectors.toSet());
			for (State state : collect) {
				StateTemplate stateToStateTemplate = stateToStateTemplate(getState(state.code));
				stateToStateTemplate.getEventTemplates().add(eventTemplate);
				stateTemplateRepository.saveAndFlush(stateToStateTemplate);
			}

			HashSet<String> roles = event.roles;
			for (String role : roles) {
				RoleTemplate roleToRoleTemplate = roleToRoleTemplate(role);
				roleToRoleTemplate.getEventTemplates().add(eventTemplate);
				roleTemplateRepository.saveAndFlush(roleToRoleTemplate);
			}
			return eventTemplate;

		}

		private TimerTemplate timerToTimerTemplate(Timer timer) {
			if (timer == null) {
				return null;
			}
			TimerTemplate timerTemplate = timerTemplateRepository.findByEntityAndCode(entity, timer.code);
			if (timerTemplate == null) {
				timerTemplate = new TimerTemplate();
			}

			timerTemplate.setAction(timer.action);
			timerTemplate.setCode(timer.code);
			timerTemplate.setEntity(entity);
			timerTemplate.setName(timer.name);
			timerTemplate.setSource(stateToStateTemplate(getState(timer.source)));
			timerTemplate.setTimerInterval(timer.timerInterval);
			timerTemplate.setTimerOnce(timer.timerOnce);
			return timerTemplateRepository.saveAndFlush(timerTemplate);
		}

		private RoleTemplate roleToRoleTemplate(String role) {
			if (role == null) {
				return null;
			}
			RoleTemplate findByCode = roleTemplateRepository.findByCode(role);
			if (findByCode == null) {
				throw new BusinessException(1311, "角色" + role + "不存在");
			}
			return findByCode;
		}

	}

	public static class State {

		// 保存builder的引用
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

		public State(String code, String name, Boolean isStart, Boolean isEnd, Boolean isChoice, String firstTarget,
				String firstGuardSpel, String thenTarget, String thenGuardSpel, String lastTarget, String enterAction,
				String exitAction) {
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
			if (builder.getEvent(event) == null) {
				throw new BusinessException(1329, "事件:" + event + "没有设置");
			}
			this.events.add(builder.getEvent(event));
			return this;
		}

		public State addTimer(String timer) {
			if (builder.getTimer(timer) == null) {
				throw new BusinessException(1329, "定时器:" + timer + "没有设置");
			}
			this.timers.add(builder.getTimer(timer));
			return this;
		}
	}

	public static class Event {
		// 保存builder的引用
		private Builder builder;
		private String code;
		private String name;
		private String target;
		private String guardSpel;
		private String action;
		private int sort;
		private HashSet<String> roles = new HashSet<String>();

		public Event(String code, String name, String target, String guardSpel, String action, int sort) {
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

	public static class Timer {
		// 保存builder的引用
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