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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.tianyoukeji.parent.entity.base.IBaseEntity;


/**
 * 全局角色对象，不同的企业同名的角色，应该使用不同的code以区分
 * @author Administrator
 *
 */
@Entity
@Table(name = "role_template" , uniqueConstraints= {@UniqueConstraint(columnNames= {"code"})})
@EntityListeners(AuditingEntityListener.class)
public class RoleTemplate implements IBaseEntity{
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
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "code")
	private String code;
	
	@ManyToMany(mappedBy = "roleTemplates")
	private Set<MenuTemplate> menuTemplates;
	
	@ManyToMany(mappedBy = "roleTemplates")
	private Set<EventTemplate> eventTemplates;
	
	@ManyToMany(mappedBy = "roleTemplates")
	private Set<OrgTemplate> orgTemplates;

	@Enumerated(EnumType.STRING)
	@Column(name="terminal")
	private Terminal terminal;
	
	
	public Terminal getTerminal() {
		return terminal;
	}

	public void setTerminal(Terminal terminal) {
		this.terminal = terminal;
	}

	public Set<OrgTemplate> getOrgTemplates() {
		return orgTemplates;
	}

	public void setOrgTemplates(Set<OrgTemplate> orgTemplates) {
		this.orgTemplates = orgTemplates;
	}

	public Set<EventTemplate> getEventTemplates() {
		return eventTemplates;
	}

	public void setEventTemplates(Set<EventTemplate> eventTemplates) {
		this.eventTemplates = eventTemplates;
	}

	public Set<MenuTemplate> getMenuTemplates() {
		return menuTemplates;
	}

	public void setMenuTemplates(Set<MenuTemplate> menuTemplates) {
		this.menuTemplates = menuTemplates;
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

	public enum Terminal{
		ORG,USER
	}
	
}
