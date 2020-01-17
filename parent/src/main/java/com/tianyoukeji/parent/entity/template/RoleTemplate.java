package com.tianyoukeji.parent.entity.template;

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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.tianyoukeji.parent.entity.IEntity;
import com.tianyoukeji.parent.entity.Menu;

@Entity
@Table(name = "role_template" , uniqueConstraints= {@UniqueConstraint(columnNames= {"code"})})
public class RoleTemplate implements IEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="uuid")
	private Long uuid;
	
	@Column(name="union_id")
	private String unionId;
	
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
	
	@ManyToOne
	@JoinColumn(name= "org_template_id")
	private OrgTemplate orgTemplate;
	
	@ManyToMany
	@JoinTable(name="role_template_menu_template",joinColumns = { @JoinColumn(name = "role_template_id") }, inverseJoinColumns = { @JoinColumn(name = "menu_template_id") })
	private Set<MenuTemplate> menuTemplates;
	

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

	public String getUnionId() {
		return unionId;
	}

	public void setUnionId(String unionId) {
		this.unionId = unionId;
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

	public OrgTemplate getOrgTemplate() {
		return orgTemplate;
	}

	public void setOrgTemplate(OrgTemplate orgTemplate) {
		this.orgTemplate = orgTemplate;
	}
	
	
	

	
}
