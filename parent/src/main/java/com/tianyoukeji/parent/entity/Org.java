package com.tianyoukeji.parent.entity;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.tianyoukeji.parent.entity.base.IBaseEntity;
import com.tianyoukeji.parent.entity.base.IRegionEntity;
import com.tianyoukeji.parent.entity.template.OrgTemplate;

@Entity
@Table(name = "org" , uniqueConstraints= {@UniqueConstraint(columnNames= {"name"})})
public class Org implements IRegionEntity{
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
	
	@OneToOne
	@JoinColumn(name="owner_id")
	private User owner;
	
	@Column(name = "description")
	private String description;
	
	@OneToMany(mappedBy = "org")
	private Set<Department> departments;
	
	@OneToMany(mappedBy = "org")
	private Set<Role> roles;
	
	@ManyToOne
	@JoinColumn(name = "parent")
	private Org parent;
	
	@OneToMany(mappedBy = "parent")
	private Set<Org> children;
	
	@ManyToOne
	@JoinColumn(name="org_template_id")
	private OrgTemplate orgTemplate;
	
	@ManyToOne
	@JoinColumn(name="country_id")
	private Region country;
	
	@ManyToOne
	@JoinColumn(name="province_id")
	private Region province;
	
	@ManyToOne
	@JoinColumn(name="city_id")
	private Region city;
	
	
	public Region getCountry() {
		return country;
	}

	public void setCountry(Region country) {
		this.country = country;
	}

	public Region getProvince() {
		return province;
	}

	public void setProvince(Region province) {
		this.province = province;
	}

	public Region getCity() {
		return city;
	}

	public void setCity(Region city) {
		this.city = city;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public Set<Department> getDepartments() {
		return departments;
	}

	public void setDepartments(Set<Department> departments) {
		this.departments = departments;
	}

	public Org getParent() {
		return parent;
	}

	public void setParent(Org parent) {
		this.parent = parent;
	}

	public Set<Org> getChildren() {
		return children;
	}

	public void setChildren(Set<Org> children) {
		this.children = children;
	}

	public OrgTemplate getOrgTemplate() {
		return orgTemplate;
	}

	public void setOrgTemplate(OrgTemplate orgTemplate) {
		this.orgTemplate = orgTemplate;
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

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	
	
}
