package com.tianyoukeji.org.controller;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esotericsoftware.kryo.NotNull;
import com.liyang.jpa.smart.query.db.SmartQuery;
import com.liyang.jpa.smart.query.response.HTTPListResponse;
import com.tianyoukeji.org.controller.OrgController.InviteBody;
import com.tianyoukeji.org.service.StateMachineManagementService;
import com.tianyoukeji.org.service.UserService;
import com.tianyoukeji.parent.annotation.StateMachineAction;
import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.common.HttpPostReturnUuid;
import com.tianyoukeji.parent.controller.DefaultHandler;
import com.tianyoukeji.parent.entity.Event;
import com.tianyoukeji.parent.entity.EventRepository;
import com.tianyoukeji.parent.entity.Role;
import com.tianyoukeji.parent.entity.State;
import com.tianyoukeji.parent.entity.Timer;
import com.tianyoukeji.parent.entity.State.StateType;
import com.tianyoukeji.parent.entity.template.EventTemplate;
import com.tianyoukeji.parent.entity.template.RoleTemplate.Terminal;
import com.tianyoukeji.parent.service.StateMachineService;
import com.tianyoukeji.parent.service.TIMService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/v1/stateMachine")
@Api(tags = "管理状态机的接口")
public class StateMachineController extends DefaultHandler {
	
	@Autowired
	private StateMachineManagementService stateMachineManagementService;


	@PostMapping(path = "/addState")
	@ApiOperation(value = "添加状态", notes = "新增一个state", httpMethod = "POST")
	public HttpPostReturnUuid addState(@Valid @RequestBody(required = true) AddStateRequest body) {
		return stateMachineManagementService.addState(body);
	}
	
	@PostMapping(path = "/deleteState")
	@ApiOperation(value = "删除状态", notes = "删除一个state", httpMethod = "POST")
	public HttpPostReturnUuid deleteState(@Valid @RequestBody(required = true) DeleteStateRequest body) {
		return stateMachineManagementService.deleteState(body);
	}
	
	@PostMapping(path = "/updateState")
	@ApiOperation(value = "更新状态", notes = "更新一个state,把所有字段重新提交一次", httpMethod = "POST")
	public HttpPostReturnUuid updateState(@Valid @RequestBody(required = true) UpdateStateRequest body) {
		return stateMachineManagementService.updateState(body);
	}
	
	@PostMapping(path = "/addEvent")
	@ApiOperation(value = "添加事件", notes = "新增一个event,可执行角色也一并设置", httpMethod = "POST")
	public HttpPostReturnUuid addEvent(@Valid @RequestBody(required = true) AddEventRequest body) {
		return stateMachineManagementService.addEvent(body);
	}
	
	@PostMapping(path = "/deleteEvent")
	@ApiOperation(value = "删除事件", notes = "删除一个event", httpMethod = "POST")
	public HttpPostReturnUuid deleteEvent(@Valid @RequestBody(required = true) DeleteEventRequest body) {
		return stateMachineManagementService.deleteEvent(body);
	}
	
	@PostMapping(path = "/updateEvent")
	@ApiOperation(value = "更新事件", notes = "更新一个event，把所有字段重新提交一次", httpMethod = "POST")
	public HttpPostReturnUuid updateState(@Valid @RequestBody(required = true) UpdateEventRequest body) {
		return stateMachineManagementService.updateEvent(body);
	}
	
	@PostMapping(path = "/stateAddTimer")
	@ApiOperation(value = "添加定时器", notes = "新增一个timer，定时器附属于state，不复用", httpMethod = "POST")
	public HttpPostReturnUuid stateAddTimer(@Valid @RequestBody(required = true) StateAddTimerRequest body) {
		return stateMachineManagementService.stateAddTimer(body);
	}
	
	@PostMapping(path = "/stateDeleteTimer")
	@ApiOperation(value = "删除定时器", notes = "删除一个timer,删除定时器时候，已经生效的定时器不会删除", httpMethod = "POST")
	public HttpPostReturnUuid stateDeleteTimer(@Valid @RequestBody(required = true) StateDeleteTimerRequest body) {
		return stateMachineManagementService.stateDeleteTimer(body);
	}
	
	@PostMapping(path = "/stateLinkEvent")
	@ApiOperation(value = "状态添加事件", notes = "给状态新增一个事件处理能力", httpMethod = "POST")
	public HttpPostReturnUuid stateLinkEvent(@Valid @RequestBody(required = true) StateLinkEvent body) {
		return stateMachineManagementService.stateLinkEvent(body);
	}
	@PostMapping(path = "/stateUnlinkEvent")
	@ApiOperation(value = "状态取消事件", notes = "状态删除一个它的事件", httpMethod = "POST")
	public HttpPostReturnUuid stateUnlinkEvent(@Valid @RequestBody(required = true) StateUnlinkEvent body) {
		return stateMachineManagementService.stateUnlinkEvent(body);
	}
	

	@GetMapping(path = "/list")
	@ApiOperation(value = "状态机种类", notes = "返回一个列表", httpMethod = "GET")
	public HTTPListResponse fetchStateList() {
		return SmartQuery.fetchGroup("state","fields=*&sort=sort,asc&group=entity");
	}
	
	@GetMapping(path = "/list/{entity}")
	@ApiOperation(value = "状态机的细节展示", notes = "根据Entity参数，返回对应的状态机的状态-事件细节", httpMethod = "GET")
	public HTTPListResponse fetchStateMachine(@PathVariable(required = true) String entity) {
		return SmartQuery.fetchList("state","fields=events,events.roles,events.target,firstTarget,thenTarget,lastTarget,timers,*&entity="+entity+"&sort=sort,asc");
	}
	
	@GetMapping(path = "/list/{entity}/action")
	@ApiOperation(value = "Java中，已经编写的可执行动作", notes = "Java程序已经编写好的动作函数名称", httpMethod = "GET")
	public HTTPListResponse fetchStateMachineAction(@PathVariable(required = true) String entity) {
		HashMap<String, StateMachineService> services = StateMachineService.services;
		StateMachineService stateMachineService = services.get(entity);
		if(stateMachineService==null) {
			throw new BusinessException(2766, "实体状态机不存在");
		}
		
		HTTPListResponse httpListResponse = new HTTPListResponse();
		ArrayList<String> arrayList = new ArrayList<String>();
		Method[] declaredMethods = stateMachineService.getClass().getDeclaredMethods();
		for (Method method : declaredMethods) {
			StateMachineAction annotation = method.getDeclaredAnnotation(StateMachineAction.class);
			if(annotation!=null) {
				arrayList.add(method.getName());
			}
		}
		httpListResponse.setItems(arrayList);
		httpListResponse.setTotal(arrayList.size());
		return httpListResponse;
	
	}
	
	@GetMapping(path = "/list/{entity}/uselessEvent")
	@ApiOperation(value = "状态机还没有使用的事件列表", notes = "", httpMethod = "GET")
	public HTTPListResponse fetchStateMachineUselessEvent(@PathVariable(required = true) String entity) {
		HTTPListResponse fetchList = SmartQuery.fetchList("event","fields=target,roles,sources,*&entity="+entity+"&sort=sort,asc");
		if(fetchList.getTotal()>0) {
			List items = (List)fetchList.getItems();
			List collect = (List)items.stream().filter(e -> ((Map)e).get("sources").equals(Collections.EMPTY_MAP)).collect(Collectors.toList());
			fetchList.setItems(collect);
			fetchList.setTotal(collect.size());
		}
		return fetchList;
	}
	
	public static class AddStateRequest{
		@NotBlank
		private String entity;
		@NotBlank
		private String name;
		@NotBlank
		private String code;
		
		private String description;
		
		@Enumerated(EnumType.STRING)
		private StateType stateType = StateType.COMMON;
		
		private Long firstTarget;
		
		private String firstGuardSpel;
		
		private Long thenTarget;
		
		private String thenGuardSpel;
		
		private Long lastTarget;

		private String enterAction;
		
		private String exitAction;
		
		private Integer sort = 0;

		public String getEntity() {
			return entity;
		}

		public void setEntity(String entity) {
			this.entity = entity;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public StateType getStateType() {
			return stateType;
		}

		public void setStateType(StateType stateType) {
			this.stateType = stateType;
		}

		public Long getFirstTarget() {
			return firstTarget;
		}

		public void setFirstTarget(Long firstTarget) {
			this.firstTarget = firstTarget;
		}

		public String getFirstGuardSpel() {
			return firstGuardSpel;
		}

		public void setFirstGuardSpel(String firstGuardSpel) {
			this.firstGuardSpel = firstGuardSpel;
		}

		public Long getThenTarget() {
			return thenTarget;
		}

		public void setThenTarget(Long thenTarget) {
			this.thenTarget = thenTarget;
		}

		public String getThenGuardSpel() {
			return thenGuardSpel;
		}

		public void setThenGuardSpel(String thenGuardSpel) {
			this.thenGuardSpel = thenGuardSpel;
		}

		public Long getLastTarget() {
			return lastTarget;
		}

		public void setLastTarget(Long lastTarget) {
			this.lastTarget = lastTarget;
		}

		public String getEnterAction() {
			return enterAction;
		}

		public void setEnterAction(String enterAction) {
			this.enterAction = enterAction;
		}

		public String getExitAction() {
			return exitAction;
		}

		public void setExitAction(String exitAction) {
			this.exitAction = exitAction;
		}

		public Integer getSort() {
			return sort;
		}

		public void setSort(Integer sort) {
			this.sort = sort;
		}
		
	}
	public static class DeleteStateRequest{
		@javax.validation.constraints.NotNull
		private Long uuid;

		public Long getUuid() {
			return uuid;
		}

		public void setUuid(Long uuid) {
			this.uuid = uuid;
		}
		
	}
	public static class UpdateStateRequest extends AddStateRequest{
		@javax.validation.constraints.NotNull
		private Long uuid;

		public Long getUuid() {
			return uuid;
		}

		public void setUuid(Long uuid) {
			this.uuid = uuid;
		}
	}
	
	public static class AddEventRequest{
		@NotBlank
		private String entity;
		@NotBlank
		private String name;
		@NotBlank
		private String code;
		
		@Column(name = "description")
		private String description;
		
		private Long target;
		
		private String guardSpel;
		
		@Column(name = "action")
		private String action;
		
		@Column(name = "sort")
		private Integer sort = 0;

		private Set<Long> roles;
		
		@javax.validation.constraints.NotNull
		private Terminal terminal;

		public String getEntity() {
			return entity;
		}

		public void setEntity(String entity) {
			this.entity = entity;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Long getTarget() {
			return target;
		}

		public void setTarget(Long target) {
			this.target = target;
		}

		public String getGuardSpel() {
			return guardSpel;
		}

		public void setGuardSpel(String guardSpel) {
			this.guardSpel = guardSpel;
		}

		public String getAction() {
			return action;
		}

		public void setAction(String action) {
			this.action = action;
		}

		public Integer getSort() {
			return sort;
		}

		public void setSort(Integer sort) {
			this.sort = sort;
		}

		public Set<Long> getRoles() {
			return roles;
		}

		public void setRoles(Set<Long> roles) {
			this.roles = roles;
		}

		public Terminal getTerminal() {
			return terminal;
		}

		public void setTerminal(Terminal terminal) {
			this.terminal = terminal;
		}
		
	}
	public static class DeleteEventRequest{
		@javax.validation.constraints.NotNull
		private Long uuid;

		public Long getUuid() {
			return uuid;
		}

		public void setUuid(Long uuid) {
			this.uuid = uuid;
		}
		
	}
	public static class UpdateEventRequest extends AddEventRequest{
		@javax.validation.constraints.NotNull
		private Long uuid;

		public Long getUuid() {
			return uuid;
		}

		public void setUuid(Long uuid) {
			this.uuid = uuid;
		}
	}
	public static class StateAddTimerRequest{
		@NotBlank
		private String entity;
		@NotBlank
		private String name;
		@NotBlank
		private String code;
		
		@Column(name = "description")
		private String description;
		
		@javax.validation.constraints.NotNull
		private Long state;
		
		private String action;

		private Integer timerInterval;
		
		private Integer timerOnce;

		public String getEntity() {
			return entity;
		}

		public void setEntity(String entity) {
			this.entity = entity;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Long getState() {
			return state;
		}

		public void setState(Long state) {
			this.state = state;
		}

		public String getAction() {
			return action;
		}

		public void setAction(String action) {
			this.action = action;
		}

		public Integer getTimerInterval() {
			return timerInterval;
		}

		public void setTimerInterval(Integer timerInterval) {
			this.timerInterval = timerInterval;
		}

		public Integer getTimerOnce() {
			return timerOnce;
		}

		public void setTimerOnce(Integer timerOnce) {
			this.timerOnce = timerOnce;
		}
		
	}
	
	public static class StateDeleteTimerRequest{
		@javax.validation.constraints.NotNull
		private Long uuid;

		public Long getUuid() {
			return uuid;
		}

		public void setUuid(Long uuid) {
			this.uuid = uuid;
		}
		
	}
	
	public static class StateLinkEvent{
		@javax.validation.constraints.NotNull
		private Long state;
		@javax.validation.constraints.NotNull
		private Long event;
		public Long getState() {
			return state;
		}
		public void setState(Long state) {
			this.state = state;
		}
		public Long getEvent() {
			return event;
		}
		public void setEvent(Long event) {
			this.event = event;
		}
		
	}
	public static class StateUnlinkEvent{
		@javax.validation.constraints.NotNull
		private Long state;
		@javax.validation.constraints.NotNull
		private Long event;
		public Long getState() {
			return state;
		}
		public void setState(Long state) {
			this.state = state;
		}
		public Long getEvent() {
			return event;
		}
		public void setEvent(Long event) {
			this.event = event;
		}
		
	}
}
