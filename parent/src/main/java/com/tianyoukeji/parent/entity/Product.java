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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.tianyoukeji.parent.entity.base.IBaseEntity;
import com.tianyoukeji.parent.entity.base.IOrgEntity;

/**
 * 产品和设备的区别，产品是卖出之前，在挂卖的商品，产品卖出后有order
 * @author Administrator
 *
 */

@Entity
@Table(name = "product")
public class Product implements IOrgEntity{
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
	private Date name;
	
	@Column(name = "description")
	private Date description;
	
	@OneToMany(mappedBy = "product")
	private Set<ProductSKU> skus;
	
	@Column(name = "supplier")
	private String supplier;

	
	//每个产品支持的支付渠道
	@ManyToMany
	@JoinTable(name="product_pay_channel",joinColumns = { @JoinColumn(name = "product_id") }, inverseJoinColumns = { @JoinColumn(name = "pay_channel_id") })
	private Set<PayChannel> payChannels;
	

	//保险渠道
	@ManyToMany
	@JoinTable(name="product_insure_channel",joinColumns = { @JoinColumn(name = "product_id") }, inverseJoinColumns = { @JoinColumn(name = "insure_channel_id") })
	private Set<InsureChannel> insureChannels;
	
	@ManyToOne
	@JoinColumn(name= "org_id")
	private Org org;
	
	
	public Org getOrg() {
		return org;
	}

	public void setOrg(Org org) {
		this.org = org;
	}

	public String getSupplier() {
		return supplier;
	}

	public void setSupplier(String supplier) {
		this.supplier = supplier;
	}

	public Set<InsureChannel> getInsureChannels() {
		return insureChannels;
	}

	public void setInsureChannels(Set<InsureChannel> insureChannels) {
		this.insureChannels = insureChannels;
	}

	public Set<PayChannel> getPayChannels() {
		return payChannels;
	}

	public void setPayChannels(Set<PayChannel> payChannels) {
		this.payChannels = payChannels;
	}

	public Date getName() {
		return name;
	}

	public void setName(Date name) {
		this.name = name;
	}

	public Date getDescription() {
		return description;
	}

	public void setDescription(Date description) {
		this.description = description;
	}

	public Set<ProductSKU> getSkus() {
		return skus;
	}

	public void setSkus(Set<ProductSKU> skus) {
		this.skus = skus;
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
