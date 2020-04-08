package com.tianyoukeji.org.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.terracotta.statistics.derived.EventRateSimpleMovingAverage;

import com.liyang.jpa.smart.query.db.SmartQuery;
import com.tianyoukeji.org.controller.StateMachineController.AddEventRequest;
import com.tianyoukeji.org.controller.StateMachineController.AddStateRequest;
import com.tianyoukeji.org.controller.StateMachineController.DeleteEventRequest;
import com.tianyoukeji.org.controller.StateMachineController.DeleteStateRequest;
import com.tianyoukeji.org.controller.StateMachineController.StateAddTimerRequest;
import com.tianyoukeji.org.controller.StateMachineController.StateDeleteTimerRequest;
import com.tianyoukeji.org.controller.StateMachineController.StateLinkEvent;
import com.tianyoukeji.org.controller.StateMachineController.StateUnlinkEvent;
import com.tianyoukeji.org.controller.StateMachineController.UpdateEventRequest;
import com.tianyoukeji.org.controller.StateMachineController.UpdateStateRequest;
import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.common.HttpPostReturnUuid;
import com.tianyoukeji.parent.entity.Event;
import com.tianyoukeji.parent.entity.EventRepository;
import com.tianyoukeji.parent.entity.Org;
import com.tianyoukeji.parent.entity.OrgRepository;
import com.tianyoukeji.parent.entity.Role;
import com.tianyoukeji.parent.entity.RoleRepository;
import com.tianyoukeji.parent.entity.State;
import com.tianyoukeji.parent.entity.State.StateType;
import com.tianyoukeji.parent.entity.StateRepository;
import com.tianyoukeji.parent.entity.Timer;
import com.tianyoukeji.parent.entity.TimerRepository;
import com.tianyoukeji.parent.service.BaseService;
import com.tianyoukeji.parent.service.StateMachineService;

@Service
public class StateMachineManagementService {

	final static Logger logger = LoggerFactory.getLogger(StateMachineManagementService.class);

	@Autowired
	private StateRepository stateRepository;
	
	@Autowired
	private EventRepository eventRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private TimerRepository timerRepository;

	@Transactional
	public HttpPostReturnUuid addState(AddStateRequest body) {

		State state = new State();
		if (!SmartQuery.getNametostructure().containsKey(body.getEntity())) {
			throw new BusinessException(2711, "输入的entity不合法");
		}
		state.setEntity(body.getEntity());
		state.setCode(body.getCode());
		state.setDescription(body.getDescription());
		state.setName(body.getName());
		StateType stateType = body.getStateType();
		state.setStateType(body.getStateType());
		state.setSort(body.getSort());
		Long firstTarget = body.getFirstTarget();
		if (firstTarget != null) {
			Optional<State> findById = stateRepository.findById(firstTarget);
			if (!findById.isPresent()) {
				throw new BusinessException(2312, "state id不存在");
			}
			state.setFirstTarget(findById.get());
			String firstGuardSpel = body.getFirstGuardSpel();
			if (StringUtils.isEmpty(firstGuardSpel)) {
				throw new BusinessException(2352, "first条件判断不存在");
			}
			state.setFirstGuardSpel(body.getFirstGuardSpel());

			if (!body.getStateType().equals(StateType.CHOICE)) {
				throw new BusinessException(2864, "状态类型选择错误");
			}

			Long lastTarget = body.getLastTarget();
			if (lastTarget == null) {
				throw new BusinessException(2361, "last Target必须设置");
			}
			Optional<State> findById2 = stateRepository.findById(lastTarget);
			if (!findById2.isPresent()) {
				throw new BusinessException(2313, "state id不存在");
			}
			state.setLastTarget(findById2.get());

			Long thenTarget = body.getThenTarget();
			if (thenTarget != null) {
				Optional<State> findById3 = stateRepository.findById(thenTarget);
				if (!findById3.isPresent()) {
					throw new BusinessException(2319, "state id不存在");
				}
				state.setThenTarget(findById3.get());
				String thenGuardSpel = body.getThenGuardSpel();
				if (StringUtils.isEmpty(thenGuardSpel)) {
					throw new BusinessException(2351, "then条件判断不存在");
				}
				state.setThenGuardSpel(body.getThenGuardSpel());
			}
			if (!StringUtils.isEmpty(body.getEnterAction())) {
				state.setEnterAction(body.getEnterAction());
			}
			if (!StringUtils.isEmpty(body.getExitAction())) {
				state.setExitAction(body.getExitAction());
			}
		
		}
		state = stateRepository.saveAndFlush(state);
		return new HttpPostReturnUuid(state.getUuid());
	}

	@Transactional
	public HttpPostReturnUuid deleteState(DeleteStateRequest body) {
		try {
			stateRepository.deleteById(body.getUuid());
		} catch (RuntimeException ex) {
			throw new BusinessException(1371, "请先删除目标是自己的事件关联和定时器");
		}
		return new HttpPostReturnUuid(body.getUuid());
	}
	
	@Transactional
	public HttpPostReturnUuid updateCache(String entity) {
		StateMachineService stateMachineService = StateMachineService.services.get(entity);
		if(stateMachineService!=null) {
			stateMachineService.refreshBuilder();
			return new HttpPostReturnUuid();
		}else {
			throw new BusinessException(1741, "实体不存在");
		}
	}
	
	@Transactional
	public HttpPostReturnUuid updateState(UpdateStateRequest body) {
		Optional<State> findById4 = stateRepository.findById(body.getUuid());
		if(!findById4.isPresent()) {
			throw new BusinessException(2467, "状态id不存在 ");
		}
		State state = findById4.get();
		if (!SmartQuery.getNametostructure().containsKey(body.getEntity())) {
			throw new BusinessException(2711, "输入的entity不合法");
		}
		state.setEntity(body.getEntity());
		state.setCode(body.getCode());
		state.setDescription(body.getDescription());
		state.setName(body.getName());
		StateType stateType = body.getStateType();
		state.setStateType(body.getStateType());
		state.setSort(body.getSort());
		Long firstTarget = body.getFirstTarget();
		if (firstTarget != null) {
			Optional<State> findById = stateRepository.findById(firstTarget);
			if (!findById.isPresent()) {
				throw new BusinessException(2312, "state id不存在");
			}
			state.setFirstTarget(findById.get());
			String firstGuardSpel = body.getFirstGuardSpel();
			if (StringUtils.isEmpty(firstGuardSpel)) {
				throw new BusinessException(2352, "first条件判断不存在");
			}
			state.setFirstGuardSpel(body.getFirstGuardSpel());

			if (!body.getStateType().equals(StateType.CHOICE)) {
				throw new BusinessException(2864, "状态类型选择错误");
			}

			Long lastTarget = body.getLastTarget();
			if (lastTarget == null) {
				throw new BusinessException(2361, "last Target必须设置");
			}
			Optional<State> findById2 = stateRepository.findById(lastTarget);
			if (!findById2.isPresent()) {
				throw new BusinessException(2313, "state id不存在");
			}
			state.setLastTarget(findById2.get());

			Long thenTarget = body.getThenTarget();
			if (thenTarget != null) {
				Optional<State> findById3 = stateRepository.findById(thenTarget);
				if (!findById3.isPresent()) {
					throw new BusinessException(2319, "state id不存在");
				}
				state.setThenTarget(findById3.get());
				String thenGuardSpel = body.getThenGuardSpel();
				if (StringUtils.isEmpty(thenGuardSpel)) {
					throw new BusinessException(2351, "then条件判断不存在");
				}
				state.setThenGuardSpel(body.getThenGuardSpel());
			}
			if (!StringUtils.isEmpty(body.getEnterAction())) {
				state.setEnterAction(body.getEnterAction());
			}
			if (!StringUtils.isEmpty(body.getExitAction())) {
				state.setExitAction(body.getExitAction());
			}
		}
		state = stateRepository.saveAndFlush(state);
		return new HttpPostReturnUuid(state.getUuid());
	}
	
	@Transactional
	public HttpPostReturnUuid addEvent(AddEventRequest body) {

		Event event = new Event();
		if (!SmartQuery.getNametostructure().containsKey(body.getEntity())) {
			throw new BusinessException(2711, "输入的entity不合法");
		}
		event.setEntity(body.getEntity());
		event.setCode(body.getCode());
		event.setDescription(body.getDescription());
		event.setName(body.getName());
		event.setTerminal(body.getTerminal());
		event.setSort(body.getSort());
		if (!StringUtils.isEmpty(body.getAction())) {
			event.setAction(body.getAction());
		}
		Long target = body.getTarget();
		if (target != null) {
			Optional<State> findById = stateRepository.findById(target);
			if (!findById.isPresent()) {
				throw new BusinessException(2312, "state id不存在");
			}
			event.setTarget(findById.get());
			if (!StringUtils.isEmpty( body.getGuardSpel())) {
				event.setGuardSpel(body.getGuardSpel());
			}
		}
		Set<Long> roles = body.getRoles();
		if(roles!=null) {
			HashSet<Role> hashSet = new HashSet<Role>();
			for (Long role : roles) {
				Optional<Role> findById = roleRepository.findById(role);
				if(findById.isPresent()) {
					hashSet.add(findById.get());
				}
			}
			event.setRoles(hashSet);
		}
		
		event = eventRepository.saveAndFlush(event);
		return new HttpPostReturnUuid(event.getUuid());
	}
	@Transactional
	public HttpPostReturnUuid deleteEvent(DeleteEventRequest body) {
		try {
			eventRepository.deleteById(body.getUuid());
		} catch (RuntimeException ex) {
			throw new BusinessException(1371, "请先删除状态下的事件关联和事件的角色关联");
		}
		return new HttpPostReturnUuid(body.getUuid());
	}
	
	@Transactional
	public HttpPostReturnUuid updateEvent(UpdateEventRequest body) {
		Optional<Event> findById4 = eventRepository.findById(body.getUuid());
		if(!findById4.isPresent()) {
			throw new BusinessException(2467, "事件id不存在 ");
		}
		Event event = findById4.get();
		if (!SmartQuery.getNametostructure().containsKey(body.getEntity())) {
			throw new BusinessException(2711, "输入的entity不合法");
		}
		event.setEntity(body.getEntity());
		event.setCode(body.getCode());
		event.setDescription(body.getDescription());
		event.setName(body.getName());
		event.setTerminal(body.getTerminal());
		event.setSort(body.getSort());
		Long target = body.getTarget();
		if (!StringUtils.isEmpty(body.getAction())) {
			event.setAction(body.getAction());
		}
		if (target != null) {
			Optional<State> findById = stateRepository.findById(target);
			if (!findById.isPresent()) {
				throw new BusinessException(2312, "state id不存在");
			}
			event.setTarget(findById.get());
			if (!StringUtils.isEmpty( body.getGuardSpel())) {
				event.setGuardSpel(body.getGuardSpel());
			}
		}
		Set<Long> roles = body.getRoles();
		if(roles!=null) {
			HashSet<Role> hashSet = new HashSet<Role>();
			for (Long role : roles) {
				Optional<Role> findById = roleRepository.findById(role);
				if(findById.isPresent()) {
					hashSet.add(findById.get());
				}
			}
			event.setRoles(hashSet);
		}
		
		event = eventRepository.saveAndFlush(event);
		return new HttpPostReturnUuid(event.getUuid());
	}
	
	@Transactional
	public HttpPostReturnUuid stateAddTimer(StateAddTimerRequest body) {
		Timer timer = new Timer();
		if (!SmartQuery.getNametostructure().containsKey(body.getEntity())) {
			throw new BusinessException(2711, "输入的entity不合法");
		}
		timer.setEntity(body.getEntity());
		timer.setCode(body.getCode());
		timer.setDescription(body.getDescription());
		timer.setName(body.getName());

		Long target = body.getState();
		if (target != null) {
			Optional<State> findById = stateRepository.findById(target);
			if (!findById.isPresent()) {
				throw new BusinessException(2312, "state id不存在");
			}
			timer.setSource(findById.get());
		}
		if(body.getTimerInterval()==null && body.getTimerOnce()==null) {
			throw new BusinessException(2641, "必须指定时间间隔");
		}
		
		if (body.getTimerInterval() != null) {
			if (body.getTimerInterval() < 10) {
				throw new BusinessException(1765, "timer间隔不低于10s");
			}
		} else if (body.getTimerOnce() != null) {
			if (body.getTimerOnce() < 10) {
				throw new BusinessException(1765, "timer间隔不低于10s");
			}
		}
		if (!StringUtils.hasText(body.getAction())) {
			throw new BusinessException(1521, "Timer的动作不能为空");
		}
		

		timer.setAction(body.getAction());
		timer.setTimerInterval(body.getTimerInterval());
		timer.setTimerOnce(body.getTimerOnce());
		timer = timerRepository.saveAndFlush(timer);
		return new HttpPostReturnUuid(timer.getUuid());
	}
	
	@Transactional
	public HttpPostReturnUuid stateDeleteTimer(StateDeleteTimerRequest body) {
		try {
			timerRepository.deleteById(body.getUuid());
		} catch (RuntimeException ex) {
			throw new BusinessException(1371, "删除定时器失败");
		}
		return new HttpPostReturnUuid(body.getUuid());
	}
	
	@Transactional
	public HttpPostReturnUuid stateLinkEvent(StateLinkEvent body) {
		State state=null;
		Event event=null;
		Optional<State> findById = stateRepository.findById(body.getState());
		if(!findById.isPresent()) {
			throw new BusinessException(2811, "状态不存在");
		}
		state = findById.get();
		Optional<Event> findById2 = eventRepository.findById(body.getEvent());
		if(!findById2.isPresent()) {
			throw new BusinessException(2821, "event不存在");
		}
		event = findById2.get();
		Set<Event> events = state.getEvents();
		if(events == null) {
			events = new HashSet<Event>();
		}
		events.add(event);
		stateRepository.save(state);
		return new HttpPostReturnUuid(state.getUuid());
	}
	
	@Transactional
	public HttpPostReturnUuid stateUnlinkEvent(StateUnlinkEvent body) {
		State state=null;
		Event event=null;
		Optional<State> findById = stateRepository.findById(body.getState());
		if(!findById.isPresent()) {
			throw new BusinessException(2811, "状态不存在");
		}
		state = findById.get();
		Optional<Event> findById2 = eventRepository.findById(body.getEvent());
		if(!findById2.isPresent()) {
			throw new BusinessException(2821, "event不存在");
		}
		event = findById2.get();
		Set<Event> events = state.getEvents();
		if(events == null) {
			return new HttpPostReturnUuid();
		}
		events.remove(event);
		stateRepository.save(state);
		return new HttpPostReturnUuid(state.getUuid());
	}
}