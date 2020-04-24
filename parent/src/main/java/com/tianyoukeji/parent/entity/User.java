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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.tianyoukeji.parent.entity.base.IDepartmentEntity;
import com.tianyoukeji.parent.entity.base.IRegionEntity;
import com.tianyoukeji.parent.entity.base.IStateMachineEntity;
import com.tianyoukeji.parent.service.TIMService.Gender;

@Entity
@Table(name = "user" , uniqueConstraints= {@UniqueConstraint(columnNames= {"union_id"})})
@EntityListeners(AuditingEntityListener.class)
public class User implements IStateMachineEntity,IRegionEntity,IDepartmentEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="uuid")
	private Long uuid;
	
	@ManyToOne
	@JoinColumn(name="country_id")
	private Region country;
	
	@ManyToOne
	@JoinColumn(name="province_id")
	private Region province;
	
	@ManyToOne
	@JoinColumn(name="city_id")
	private Region city;
	
	@ManyToOne
	@JoinColumn(name= "org_id")
	private Org org;
	
	@ManyToOne
	@JoinColumn(name= "department_id")
	private Department department;
	
	@ManyToOne
	@JoinColumn(name= "state_id")
	private State state;
	
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
	
	@Column(name="gender")
	@Enumerated(EnumType.STRING)
	private Gender gender;
	
	@Column(name="headimgurl")
	private String headimgurl;
	
	@Column(name="position")
	@Enumerated(EnumType.STRING)
	private Position position ;
	
	@OneToOne
	@JoinColumn(name="userinfo_id")
	private Userinfo userinfo;
	
	@ManyToOne
	@JoinColumn(name= "role_id")
	private Role role;
	
	@OneToMany(mappedBy = "buyer")
	private Set<Order> orders;
	
	@OneToMany(mappedBy = "user")
	private Set<UserEquipmentRelationship> relativeEquipments;
	
	@OneToMany(mappedBy = "fromUser")
	private Set<Task> publishTasks;
	
	@OneToMany(mappedBy = "toUser")
	private Set<Task> acceptTasks;
	
	@ManyToMany(mappedBy = "cc")
	private Set<Task> ccTasks;
	
	
	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public Set<Order> getOrders() {
		return orders;
	}

	public void setOrders(Set<Order> orders) {
		this.orders = orders;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Set<Task> getPublishTasks() {
		return publishTasks;
	}

	public void setPublishTasks(Set<Task> publishTasks) {
		this.publishTasks = publishTasks;
	}

	public Set<Task> getAcceptTasks() {
		return acceptTasks;
	}

	public void setAcceptTasks(Set<Task> acceptTasks) {
		this.acceptTasks = acceptTasks;
	}

	public Set<Task> getCcTasks() {
		return ccTasks;
	}

	public void setCcTasks(Set<Task> ccTasks) {
		this.ccTasks = ccTasks;
	}

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


	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
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
	
	public static enum Position{
		MANAGER
	}

	

	
}
