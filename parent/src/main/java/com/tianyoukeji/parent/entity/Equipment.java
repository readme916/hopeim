package com.tianyoukeji.parent.entity;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.tianyoukeji.parent.entity.base.IBaseEntity;
import com.tianyoukeji.parent.entity.base.IDepartmentEntity;
import com.tianyoukeji.parent.entity.base.IRegionEntity;
import com.tianyoukeji.parent.entity.base.IStateMachineEntity;


/**
 * 产品和设备的区别，产品是卖出之前，在挂卖的商品，卖出后就变成个人或者公司的设备,设备用于用户扫码绑定使用，用outerid交互
 * 设备对应  product sku
 * @author Administrator
 *
 */
@Entity
@Table(name = "equipment")
public class Equipment implements IStateMachineEntity,IRegionEntity,IDepartmentEntity{
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
	
	
	// 外部设备号用于通讯，每个生产商的设备号唯一
	@Column(name = "outer_id")
	private String outerId;
	
	@CreatedDate
	@Column(name = "created_at")
	private Date createdAt;

	@LastModifiedDate
	@Column(name = "updated_at")
	private Date updatedAt;

	@Version
	@Column(name = "version")
	private Long version;
	
	@ManyToOne
	@JoinColumn(name= "product_sku_id")
	private ProductSKU productSKU;
	
	@OneToMany(mappedBy = "equipment")
	private Set<UserEquipmentRelationship> relativeUsers;


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

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public String getOuterId() {
		return outerId;
	}

	public void setOuterId(String outerId) {
		this.outerId = outerId;
	}

	public Set<UserEquipmentRelationship> getRelativeUsers() {
		return relativeUsers;
	}

	public void setRelativeUsers(Set<UserEquipmentRelationship> relativeUsers) {
		this.relativeUsers = relativeUsers;
	}

	public ProductSKU getProductSKU() {
		return productSKU;
	}

	public void setProductSKU(ProductSKU productSKU) {
		this.productSKU = productSKU;
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

}
