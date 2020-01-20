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
@Table(name = "event", uniqueConstraints= {@UniqueConstraint(columnNames= {"code"})})
public class Event implements IEntity{
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
	
	@ManyToMany(mappedBy = "events")
	private Set<State> sources;
	
	//内部事件，target为null
	@ManyToOne
	@JoinColumn(name = "target")
	private State target;
	
	//事件的限制条件spel表达式
	@Column(name = "guard_spel")
	private String guardSpel;
	
	@Column(name = "action")
	private String action;
	
	@ManyToMany
	@JoinTable(name="event_notify",joinColumns = { @JoinColumn(name = "event_id") }, inverseJoinColumns = { @JoinColumn(name = "user_id") })
	private Set<User> eventNotifyUsers;
	
	@Column(name = "notify_template")
	private String notifyTemplate;
	
	@Column(name = "time")
	private Integer timer;
	
	@Column(name = "time_once")
	private Integer timerOnce;
	

	public Integer getTimer() {
		return timer;
	}

	public void setTimer(Integer timer) {
		this.timer = timer;
	}

	public Integer getTimerOnce() {
		return timerOnce;
	}

	public void setTimerOnce(Integer timerOnce) {
		this.timerOnce = timerOnce;
	}

	public Set<State> getSources() {
		return sources;
	}

	public void setSources(Set<State> sources) {
		this.sources = sources;
	}

	public Set<User> getEventNotifyUsers() {
		return eventNotifyUsers;
	}

	public void setEventNotifyUsers(Set<User> eventNotifyUsers) {
		this.eventNotifyUsers = eventNotifyUsers;
	}

	public String getNotifyTemplate() {
		return notifyTemplate;
	}

	public void setNotifyTemplate(String notifyTemplate) {
		this.notifyTemplate = notifyTemplate;
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

	public State getTarget() {
		return target;
	}

	public void setTarget(State target) {
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
	
}
