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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.tianyoukeji.parent.entity.base.IBaseEntity;
import com.tianyoukeji.parent.entity.base.ISortEntity;
import com.tianyoukeji.parent.entity.template.RoleTemplate.Terminal;

@Entity
@Table(name = "event_template", uniqueConstraints= {@UniqueConstraint(columnNames= {"entity","code"})})
@EntityListeners(AuditingEntityListener.class)
public class EventTemplate implements IBaseEntity,ISortEntity{
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
	
	@ManyToMany(mappedBy = "eventTemplates")
	private Set<StateTemplate> sourceStateTemplates;
	
	//内部事件，target为null
	@ManyToOne
	@JoinColumn(name = "target_id")
	private StateTemplate target;
	
	//事件的限制条件spel表达式
	@Column(name = "guard_spel")
	private String guardSpel;
	
	@Column(name = "action")
	private String action;
	
	@Column(name = "sort")
	private Integer sort = 0;

	@ManyToMany
	@JoinTable(name="event_template_role_template",joinColumns = { @JoinColumn(name = "event_template_id") }, inverseJoinColumns = { @JoinColumn(name = "role_template_id") })
	private Set<RoleTemplate> roleTemplates;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "terminal")
	private Terminal terminal;
	
	
	public Terminal getTerminal() {
		return terminal;
	}

	public void setTerminal(Terminal terminal) {
		this.terminal = terminal;
	}

	public Set<RoleTemplate> getRoleTemplates() {
		return roleTemplates;
	}

	public void setRoleTemplates(Set<RoleTemplate> roleTemplates) {
		this.roleTemplates = roleTemplates;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}


	public Set<StateTemplate> getSourceStateTemplates() {
		return sourceStateTemplates;
	}

	public void setSourceStateTemplates(Set<StateTemplate> sourceStateTemplates) {
		this.sourceStateTemplates = sourceStateTemplates;
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

	public StateTemplate getTarget() {
		return target;
	}

	public void setTarget(StateTemplate target) {
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
