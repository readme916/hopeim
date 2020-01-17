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

import com.tianyoukeji.parent.entity.template.OrgTemplate;

@Entity
@Table(name = "org")
public class Org implements IEntity{
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
	
	@OneToMany(mappedBy = "boughtOrg")
	private Set<Equipment> boughtEquipments;
	
	@ManyToOne
	@JoinColumn(name="org_template_id")
	private OrgTemplate orgTemplate;
	
	
	
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

	public Set<Equipment> getBoughtEquipments() {
		return boughtEquipments;
	}

	public void setBoughtEquipments(Set<Equipment> boughtEquipments) {
		this.boughtEquipments = boughtEquipments;
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
