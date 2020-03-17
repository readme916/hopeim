package com.tianyoukeji.parent.entity;

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
import javax.persistence.Index;
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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.tianyoukeji.parent.entity.base.IBaseEntity;
import com.tianyoukeji.parent.entity.base.IOrgEntity;
import com.tianyoukeji.parent.entity.template.RoleTemplate;
import com.tianyoukeji.parent.entity.template.RoleTemplate.Terminal;

@Entity
@Table(name = "role", uniqueConstraints= {@UniqueConstraint(columnNames= {"code"})})
@EntityListeners(AuditingEntityListener.class)
public class Role implements IBaseEntity{
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
	
	@ManyToMany(mappedBy = "roles")
	private Set<Org> orgs;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "code")
	private String code;
	
	@ManyToOne
	@JoinColumn(name= "role_template_id")
	private RoleTemplate roleTemplate;

	@ManyToMany(mappedBy = "roles")
	private Set<Menu> menus;

	@ManyToMany(mappedBy = "roles")
	private Set<Event> events;
	
	@OneToMany(mappedBy = "role")
	private Set<User> users;
	
	@Enumerated(EnumType.STRING)
	@Column(name="terminal")
	private Terminal terminal;
	
	public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}

	public Terminal getTerminal() {
		return terminal;
	}

	public void setTerminal(Terminal terminal) {
		this.terminal = terminal;
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


	public Set<Event> getEvents() {
		return events;
	}


	public void setEvents(Set<Event> events) {
		this.events = events;
	}


	public Set<Menu> getMenus() {
		return menus;
	}


	public void setMenus(Set<Menu> menus) {
		this.menus = menus;
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


	public Set<Org> getOrgs() {
		return orgs;
	}


	public void setOrgs(Set<Org> orgs) {
		this.orgs = orgs;
	}


	public RoleTemplate getRoleTemplate() {
		return roleTemplate;
	}


	public void setRoleTemplate(RoleTemplate roleTemplate) {
		this.roleTemplate = roleTemplate;
	}
	

	
}
