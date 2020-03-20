package com.tianyoukeji.parent.entity.template;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.tianyoukeji.parent.entity.State.StateType;
import com.tianyoukeji.parent.entity.base.IBaseEntity;
import com.tianyoukeji.parent.entity.base.ISortEntity;

@Entity
@Table(name = "state_template", uniqueConstraints= {@UniqueConstraint(columnNames= {"entity","code"})})
@EntityListeners(AuditingEntityListener.class)
public class StateTemplate implements IBaseEntity,ISortEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="uuid")
	private Long uuid;
	
	@CreatedDate
	@Column(name = "created_at")
	private Date createdAt;

	@LastModifiedDate
	@Column(name = "updated_at")
	private Date updatedAt;

	@Version
	@Column(name = "version")
	private Long version;
	
	@Column(name = "entity")
	private String entity;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "code")
	private String code;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "sort")
	private Integer sort;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "state_type")
	private StateType stateType = StateType.COMMON;
	
	@ManyToOne
	@JoinColumn(name = "first_target_id")
	private StateTemplate firstTarget;
	
	@Column(name = "first_guard_spel")
	private String firstGuardSpel;
	
	@ManyToOne
	@JoinColumn(name = "then_target_id")
	private StateTemplate thenTarget;
	
	
	@Column(name = "then_guard_spel")
	private String thenGuardSpel;
	
	@ManyToOne
	@JoinColumn(name = "last_target_id")
	private StateTemplate lastTarget;
	//分支事件结束

	//允许的普通事件集合
	@ManyToMany
	@JoinTable(name="state_template_event_template",joinColumns = { @JoinColumn(name = "state_template_id") }, inverseJoinColumns = { @JoinColumn(name = "event_template_id") })
	private Set<EventTemplate> eventTemplates;
	
	@OneToMany(mappedBy = "source")
	private Set<TimerTemplate> timerTemplates;
	
	@Column(name = "enter_action")
	private String enterAction;
	
	@Column(name = "exit_action")
	private String exitAction;
	

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	public Set<EventTemplate> getEventTemplates() {
		return eventTemplates;
	}

	public void setEventTemplates(Set<EventTemplate> eventTemplates) {
		this.eventTemplates = eventTemplates;
	}

	public Set<TimerTemplate> getTimerTemplates() {
		return timerTemplates;
	}

	public void setTimerTemplates(Set<TimerTemplate> timerTemplates) {
		this.timerTemplates = timerTemplates;
	}

	public Long getUuid() {
		return uuid;
	}

	public void setUuid(Long uuid) {
		this.uuid = uuid;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

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

	public StateTemplate getFirstTarget() {
		return firstTarget;
	}

	public void setFirstTarget(StateTemplate firstTarget) {
		this.firstTarget = firstTarget;
	}


	public StateTemplate getThenTarget() {
		return thenTarget;
	}

	public void setThenTarget(StateTemplate thenTarget) {
		this.thenTarget = thenTarget;
	}

	

	public String getFirstGuardSpel() {
		return firstGuardSpel;
	}

	public void setFirstGuardSpel(String firstGuardSpel) {
		this.firstGuardSpel = firstGuardSpel;
	}

	public String getThenGuardSpel() {
		return thenGuardSpel;
	}

	public void setThenGuardSpel(String thenGuardSpel) {
		this.thenGuardSpel = thenGuardSpel;
	}

	public StateTemplate getLastTarget() {
		return lastTarget;
	}

	public void setLastTarget(StateTemplate lastTarget) {
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
	
}
