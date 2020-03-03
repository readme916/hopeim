package com.tianyoukeji.parent.entity.template;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.tianyoukeji.parent.entity.base.IBaseEntity;
import com.tianyoukeji.parent.entity.template.RoleTemplate;

@Entity
@Table(name = "menu_template")
public class MenuTemplate implements IBaseEntity{
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
	
	@Column(name="name")
	private String name;

	@Column(name="sort")
    private Integer sort;

	@Column(name="icon_url")
	private String iconUrl;

	@Column(name="url")
	private String url;
	
	@ManyToMany(mappedBy = "menuTemplates")
	private Set<RoleTemplate> roleTemplates;

	@ManyToOne
	@JoinColumn(name="parent_id")
	private MenuTemplate parent;
	
	@OneToMany(mappedBy = "parent")	
	private Set<MenuTemplate> children;

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

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Set<RoleTemplate> getRoleTemplates() {
		return roleTemplates;
	}

	public void setRoleTemplates(Set<RoleTemplate> roleTemplates) {
		this.roleTemplates = roleTemplates;
	}

	public MenuTemplate getParent() {
		return parent;
	}

	public void setParent(MenuTemplate parent) {
		this.parent = parent;
	}

	public Set<MenuTemplate> getChildren() {
		return children;
	}

	public void setChildren(Set<MenuTemplate> children) {
		this.children = children;
	}


	
}
