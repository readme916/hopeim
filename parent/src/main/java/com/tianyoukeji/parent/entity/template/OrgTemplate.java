package com.tianyoukeji.parent.entity.template;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.tianyoukeji.parent.entity.base.IBaseEntity;

@Entity
@Table(name = "org_template" , uniqueConstraints= {@UniqueConstraint(columnNames= {"code"})})
@EntityListeners(AuditingEntityListener.class)
public class OrgTemplate implements IBaseEntity{
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
	
	@OneToMany(mappedBy = "orgTemplate")
	private Set<DepartmentTemplate> departmentTemplates;
	
	@ManyToMany
	@JoinTable(name="org_template_role_template",joinColumns = { @JoinColumn(name = "org_template_id") }, inverseJoinColumns = { @JoinColumn(name = "role_template_id") })
	private Set<RoleTemplate> roleTemplates;
	
	@OneToMany(mappedBy = "orgTemplate")
	private Set<MenuTemplate> menuTemplates;
	
	
	public Set<MenuTemplate> getMenuTemplates() {
		return menuTemplates;
	}

	public void setMenuTemplates(Set<MenuTemplate> menuTemplates) {
		this.menuTemplates = menuTemplates;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Set<DepartmentTemplate> getDepartmentTemplates() {
		return departmentTemplates;
	}

	public void setDepartmentTemplates(Set<DepartmentTemplate> departmentTemplates) {
		this.departmentTemplates = departmentTemplates;
	}

	public Set<RoleTemplate> getRoleTemplates() {
		return roleTemplates;
	}

	public void setRoleTemplates(Set<RoleTemplate> roleTemplates) {
		this.roleTemplates = roleTemplates;
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

	
}
