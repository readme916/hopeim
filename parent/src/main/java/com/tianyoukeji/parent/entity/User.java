package com.tianyoukeji.parent.entity;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Table(name = "user" , uniqueConstraints= {@UniqueConstraint(columnNames= {"union_id"})})
public class User implements IEntity{
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
	
	@Column(name="enabled")
	private Boolean enabled;

	@Column(name="nickname")
	private String nickname;
	
	@Column(name="sex")
	private String sex;
	
	@Column(name="headimgurl")
	private String headimgurl;
	
	@ManyToOne
	@JoinColumn(name="country_id")
	private Region country;
	
	@ManyToOne
	@JoinColumn(name="province_id")
	private Region province;
	
	@ManyToOne
	@JoinColumn(name="city_id")
	private Region city;
	
	@OneToOne
	@JoinColumn(name="userinfo_id")
	private Userinfo userinfo;
	
	
	@ManyToOne
	@JoinColumn(name= "org_id")
	private Org org;
	
	@ManyToOne
	@JoinColumn(name= "department_id")
	private Department department;
	
	@ManyToOne
	@JoinColumn(name= "role_id")
	private Role role;
	
	
	@OneToMany(mappedBy = "boughtUser")
	private Set<Equipment> boughtEquipments;
	
	
	@OneToOne(mappedBy = "owner")
	private Org OwnedOrg;
	
	@OneToMany(mappedBy = "user")
	private Set<UserEquipmentRelationship> relativeEquipments;

	public Set<UserEquipmentRelationship> getRelativeEquipments() {
		return relativeEquipments;
	}

	public void setRelativeEquipments(Set<UserEquipmentRelationship> relativeEquipments) {
		this.relativeEquipments = relativeEquipments;
	}

	public Org getOrg() {
		return org;
	}

	public void setOrg(Org org) {
		this.org = org;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Set<Equipment> getBoughtEquipments() {
		return boughtEquipments;
	}

	public void setBoughtEquipments(Set<Equipment> boughtEquipments) {
		this.boughtEquipments = boughtEquipments;
	}

	public Org getOwnedOrg() {
		return OwnedOrg;
	}

	public void setOwnedOrg(Org ownedOrg) {
		OwnedOrg = ownedOrg;
	}

	public String getUnionId() {
		return unionId;
	}

	public void setUnionId(String unionId) {
		this.unionId = unionId;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getHeadimgurl() {
		return headimgurl;
	}

	public void setHeadimgurl(String headimgurl) {
		this.headimgurl = headimgurl;
	}

	public Userinfo getUserinfo() {
		return userinfo;
	}

	public void setUserinfo(Userinfo userinfo) {
		this.userinfo = userinfo;
	}

	public void setCountry(Region country) {
		this.country = country;
	}

	public void setProvince(Region province) {
		this.province = province;
	}

	public void setCity(Region city) {
		this.city = city;
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


	public Region getCountry() {
		return country;
	}

	public Region getProvince() {
		return province;
	}

	public Region getCity() {
		return city;
	}

	

	
}
