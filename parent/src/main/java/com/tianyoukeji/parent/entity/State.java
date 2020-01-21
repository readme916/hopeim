package com.tianyoukeji.parent.entity;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.tianyoukeji.parent.entity.template.DepartmentTemplate;

@Entity
@Table(name = "state", uniqueConstraints= {@UniqueConstraint(columnNames= {"code"})})
public class State implements IEntity{
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
	
	@Column(name = "is_start")
	private Boolean isStart;
	
	@Column(name = "is_end")
	private Boolean isEnd;
	
	
	//这一部分处理分支事件
	@Column(name = "is_choice")
	private Boolean isChoice;
	
	@ManyToOne
	@JoinColumn(name = "first_target")
	private State firstTarget;
	
	@Column(name = "first_guard_spel")
	private String firstGuardSpel;
	
	@ManyToOne
	@JoinColumn(name = "then_target")
	private State thenTarget;
	
	
	@Column(name = "then_guard_spel")
	private String thenGuardSpel;
	
	@ManyToOne
	@JoinColumn(name = "last_target")
	private State lastTarget;
	//分支事件结束

	//允许的普通事件集合
	@ManyToMany
	@JoinTable(name="state_event",joinColumns = { @JoinColumn(name = "state_id") }, inverseJoinColumns = { @JoinColumn(name = "event_id") })
	private Set<Event> events;
	
	@OneToMany(mappedBy = "source")
	private Set<Timer> timers;
	
	@Column(name = "enter_action")
	private String enterAction;
	
	@Column(name = "exit_action")
	private String exitAction;
	

	public Set<Timer> getTimers() {
		return timers;
	}

	public void setTimers(Set<Timer> timers) {
		this.timers = timers;
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


	public Boolean getIsStart() {
		return isStart;
	}

	public void setIsStart(Boolean isStart) {
		this.isStart = isStart;
	}

	public Set<Event> getEvents() {
		return events;
	}

	public void setEvents(Set<Event> events) {
		this.events = events;
	}

	public Boolean getIsEnd() {
		return isEnd;
	}

	public void setIsEnd(Boolean isEnd) {
		this.isEnd = isEnd;
	}

	public Boolean getIsChoice() {
		return isChoice;
	}

	public void setIsChoice(Boolean isChoice) {
		this.isChoice = isChoice;
	}

	public State getFirstTarget() {
		return firstTarget;
	}

	public void setFirstTarget(State firstTarget) {
		this.firstTarget = firstTarget;
	}


	public State getThenTarget() {
		return thenTarget;
	}

	public void setThenTarget(State thenTarget) {
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

	public State getLastTarget() {
		return lastTarget;
	}

	public void setLastTarget(State lastTarget) {
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
