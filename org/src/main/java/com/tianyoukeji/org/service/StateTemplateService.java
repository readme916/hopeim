package com.tianyoukeji.org.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.util.concurrent.Service.State;
import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.entity.EventRepository;
import com.tianyoukeji.parent.entity.Role;
import com.tianyoukeji.parent.entity.RoleRepository;
import com.tianyoukeji.parent.entity.StateRepository;
import com.tianyoukeji.parent.entity.TimerRepository;
import com.tianyoukeji.parent.entity.State.StateType;
import com.tianyoukeji.parent.entity.template.EventTemplate;
import com.tianyoukeji.parent.entity.template.EventTemplateRepository;
import com.tianyoukeji.parent.entity.template.RoleTemplate;
import com.tianyoukeji.parent.entity.template.RoleTemplate.Terminal;
import com.tianyoukeji.parent.entity.template.RoleTemplateRepository;
import com.tianyoukeji.parent.entity.template.StateTemplate;
import com.tianyoukeji.parent.entity.template.StateTemplateRepository;
import com.tianyoukeji.parent.entity.template.TimerTemplate;
import com.tianyoukeji.parent.entity.template.TimerTemplateRepository;

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

	/**
	 * 企业专属的state系列 ，每个企业一套专属的state记录
	 * 
	 * @param entity
	 * @param org
	 */

	@Transactional
	public void entityStateDeploy(String entity) {
		List<StateTemplate> findByEntity = stateTemplateRepository.findByEntity(entity);
		
		
		for (StateTemplate stateTemplate : findByEntity) {
			stateTemplateTransfer(stateTemplate);
		}
	}

	// -------------------------------------private----------------------------

	private com.tianyoukeji.parent.entity.State stateTemplateTransfer(StateTemplate stateTemplate) {
		if (stateTemplate == null) {
			return null;
		}

		com.tianyoukeji.parent.entity.State findByOrgIsNullAndEntityAndCode = stateRepository
				.findByEntityAndCode(stateTemplate.getEntity(), stateTemplate.getCode());
		if (findByOrgIsNullAndEntityAndCode != null) {
			return findByOrgIsNullAndEntityAndCode;
		}

		com.tianyoukeji.parent.entity.State state = new com.tianyoukeji.parent.entity.State();
		state.setCode(stateTemplate.getCode());
		state.setSort(stateTemplate.getSort());
		state.setEnterAction(stateTemplate.getEnterAction());
		state.setEntity(stateTemplate.getEntity());
		state.setExitAction(stateTemplate.getExitAction());
		state.setFirstGuardSpel(stateTemplate.getFirstGuardSpel());
		state.setFirstTarget(stateTemplateTransfer(stateTemplate.getFirstTarget()));
		state.setStateType(stateTemplate.getStateType());
		state.setLastTarget(stateTemplateTransfer(stateTemplate.getLastTarget()));
		state.setName(stateTemplate.getName());
		state.setThenGuardSpel(stateTemplate.getThenGuardSpel());
		state.setThenTarget(stateTemplateTransfer(stateTemplate.getThenTarget()));
		state.setStateTemplate(stateTemplate);
		state = stateRepository.saveAndFlush(state);
		timerTemplateTransfer(stateTemplate.getTimerTemplates(), state);
		Set<com.tianyoukeji.parent.entity.Event> collect = new HashSet<>();

		Set<EventTemplate> eventTemplates = stateTemplate.getEventTemplates();
		for (EventTemplate eventTemplate : eventTemplates) {
			collect.add(eventTemplateTransfer(eventTemplate));
		}
		state.setEvents(collect);
		state = stateRepository.saveAndFlush(state);
		return state;

	}

	private Set<com.tianyoukeji.parent.entity.Timer> timerTemplateTransfer(Set<TimerTemplate> timerTemplates,
			com.tianyoukeji.parent.entity.State state) {
		if (timerTemplates == null || timerTemplates.isEmpty()) {
			return null;
		}
		HashSet<com.tianyoukeji.parent.entity.Timer> timers = new HashSet<com.tianyoukeji.parent.entity.Timer>();
		for (TimerTemplate timerTemplate : timerTemplates) {
			com.tianyoukeji.parent.entity.Timer timer = new com.tianyoukeji.parent.entity.Timer();
			timer.setAction(timerTemplate.getAction());
			timer.setCode(timerTemplate.getCode());
			timer.setEntity(timerTemplate.getEntity());
			timer.setName(timerTemplate.getName());
			timer.setSource(state);
			timer.setTimerInterval(timerTemplate.getTimerInterval());
			timer.setTimerOnce(timerTemplate.getTimerOnce());
			timer.setTimerTemplate(timerTemplate);
			timer = timerRepository.saveAndFlush(timer);
			timers.add(timer);
		}
		return timers;
	}

	private com.tianyoukeji.parent.entity.Event eventTemplateTransfer(EventTemplate eventTemplate) {
		if (eventTemplate == null) {
			return null;
		}

		com.tianyoukeji.parent.entity.Event findByOrgIsNullAndEntityAndCode = eventRepository
				.findByEntityAndCode(eventTemplate.getEntity(), eventTemplate.getCode());
		if (findByOrgIsNullAndEntityAndCode != null) {
			return findByOrgIsNullAndEntityAndCode;
		}

		com.tianyoukeji.parent.entity.Event event = new com.tianyoukeji.parent.entity.Event();
		event.setAction(eventTemplate.getAction());
		event.setCode(eventTemplate.getCode());
		event.setEntity(eventTemplate.getEntity());
		event.setGuardSpel(eventTemplate.getGuardSpel());
		event.setName(eventTemplate.getName());
		event.setRoles(getRoles(eventTemplate.getRoleTemplates()));
		event.setSort(eventTemplate.getSort());
		event.setTerminal(eventTemplate.getTerminal());
		event.setEventTemplate(eventTemplate);
		event = eventRepository.saveAndFlush(event);
		event.setTarget(stateTemplateTransfer(eventTemplate.getTarget()));
		return eventRepository.saveAndFlush(event);

	}

	private Set<com.tianyoukeji.parent.entity.Role> getRoles(Set<RoleTemplate> roleTemplates) {
		if (roleTemplates == null || roleTemplates.isEmpty()) {
			return null;
		}
		HashSet<com.tianyoukeji.parent.entity.Role> roles = new HashSet<com.tianyoukeji.parent.entity.Role>();
		for (RoleTemplate roleTemplate : roleTemplates) {
			com.tianyoukeji.parent.entity.Role role = roleRepository.findByCode(roleTemplate.getCode());
			if (role != null) {
				roles.add(role);
			}else {
				role = new Role();
				role.setCode(roleTemplate.getCode());
				role.setTerminal(roleTemplate.getTerminal());
				role.setName(roleTemplate.getName());
				role.setRoleTemplate(roleTemplate);
				role = roleRepository.saveAndFlush(role);
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

		public Builder state(Integer sort , String code, String name, StateType stateType,
				String firstTarget, String firstGuardSpel, String thenTarget, String thenGuardSpel, String lastTarget,
				String enterAction, String exitAction) {
			State s = new State(sort ,code, name, stateType, firstTarget, firstGuardSpel, thenTarget,
					thenGuardSpel, lastTarget, enterAction, exitAction);
			if (allStates.containsKey(code)) {
				throw new BusinessException(1746, "状态 ： " + code + "已经存在");
			}
			s.setBuilder(this);
			allStates.put(code, s);
			return this;
		}

		public Builder event( int sort, String code, String name, String target, String guardSpel, String action,Terminal terminal) {
			Event e = new Event(sort ,code, name, target, guardSpel, action, terminal);
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
				convertToStateTemplate(entry.getValue());
			}
			Set<Entry<String, Event>> eventSet = allEvents.entrySet();
			for (Entry<String, Event> entry : eventSet) {
				convertToEventTemplate(entry.getValue());
			}
			Set<Entry<String, Timer>> timerSet = allTimers.entrySet();
			for (Entry<String, Timer> entry : timerSet) {
				convertToTimerTemplate(entry.getValue());
			}
			return this;
		}

		private StateTemplate convertToStateTemplate(State state) {
			if (state == null) {
				return null;
			}
			StateTemplate stateTemplate = stateTemplateRepository.findByEntityAndCode(entity, state.code);
			if (stateTemplate == null) {
				stateTemplate = new StateTemplate();
			}
			stateTemplate.setSort(state.sort);
			stateTemplate.setCode(state.code);
			stateTemplate.setEnterAction(state.enterAction);
			stateTemplate.setEntity(entity);
			stateTemplate.setExitAction(state.exitAction);
			stateTemplate.setFirstGuardSpel(state.firstGuardSpel);
			stateTemplate.setFirstTarget(convertToStateTemplate(getState(state.firstTarget)));
			stateTemplate.setStateType(state.stateType);
			stateTemplate.setLastTarget(convertToStateTemplate(getState(state.lastTarget)));
			stateTemplate.setName(state.name);
			stateTemplate.setThenGuardSpel(state.thenGuardSpel);
			stateTemplate.setThenTarget(convertToStateTemplate(getState(state.thenTarget)));
			stateTemplate = stateTemplateRepository.saveAndFlush(stateTemplate);
			return stateTemplate;
		}

		private EventTemplate convertToEventTemplate(Event event) {
			if (event == null) {
				return null;
			}
			EventTemplate eventTemplate = eventTemplateRepository.findByEntityAndCode(entity, event.code);
			if (eventTemplate == null) {
				eventTemplate = new EventTemplate();
			}
			eventTemplate.setAction(event.action);
			eventTemplate.setCode(event.code);
			eventTemplate.setEntity(entity);
			eventTemplate.setGuardSpel(event.guardSpel);
			eventTemplate.setName(event.name);
			eventTemplate.setSort(event.sort);
			eventTemplate.setTerminal(event.terminal);
			eventTemplate.setTarget(convertToStateTemplate(getState(event.target)));
			HashSet<String> roles = event.roles;
			HashSet<RoleTemplate> roleTemplates = new HashSet<>();
			for (String role : roles) {
				RoleTemplate roleToRoleTemplate = convertToRoleTemplate(role);
				roleTemplates.add(roleToRoleTemplate);
			}
			eventTemplate.setRoleTemplates(roleTemplates);
			eventTemplate = eventTemplateRepository.saveAndFlush(eventTemplate);

			Set<State> collect = allStates.values().stream().filter(s -> s.events.contains(event))
					.collect(Collectors.toSet());
			for (State state : collect) {
				StateTemplate stateToStateTemplate = convertToStateTemplate(getState(state.code));
				stateToStateTemplate.getEventTemplates().add(eventTemplate);
				stateTemplateRepository.saveAndFlush(stateToStateTemplate);
			}
			return eventTemplate;

		}

		private TimerTemplate convertToTimerTemplate(Timer timer) {
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
			timerTemplate.setSource(convertToStateTemplate(getState(timer.source)));
			timerTemplate.setTimerInterval(timer.timerInterval);
			timerTemplate.setTimerOnce(timer.timerOnce);
			return timerTemplateRepository.saveAndFlush(timerTemplate);
		}

		private RoleTemplate convertToRoleTemplate(String role) {
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
		private Integer sort;
		private String code;
		private String name;
		private StateType stateType;
		private String firstTarget;
		private String firstGuardSpel;
		private String thenTarget;
		private String thenGuardSpel;
		private String lastTarget;
		private String enterAction;
		private String exitAction;
		private HashSet<Event> events = new HashSet<Event>();
		private HashSet<Timer> timers = new HashSet<Timer>();

		public State(Integer sort, String code, String name, StateType stateType, String firstTarget,
				String firstGuardSpel, String thenTarget, String thenGuardSpel, String lastTarget, String enterAction,
				String exitAction) {
			super();
			this.sort = sort;
			this.code = code;
			this.name = name;
			this.stateType = stateType;
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
		private Terminal terminal;
		private HashSet<String> roles = new HashSet<String>();

		public Event(int sort ,String code, String name, String target, String guardSpel, String action,Terminal terminal) {
			super();
			this.code = code;
			this.name = name;
			this.target = target;
			this.guardSpel = guardSpel;
			this.action = action;
			this.sort = sort;
			this.terminal = terminal;
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