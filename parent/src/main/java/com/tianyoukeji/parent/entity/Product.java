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

/**
 * parent product 是 child product的组合，每个child product可以有不同的供应商，组合成一个产品，每个parent产品的sku 对应于一个设备 
 * @author Administrator
 *
 */

@Entity
@Table(name = "product")
public class Product implements IEntity{
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
	
	
	//每个产品支持的支付渠道
	@ManyToMany
	@JoinTable(name="product_pay_channel",joinColumns = { @JoinColumn(name = "product_id") }, inverseJoinColumns = { @JoinColumn(name = "pay_channel_id") })
	private Set<PayChannel> payChannels;
	

	//保险渠道
	@ManyToMany
	@JoinTable(name="product_insure_channel",joinColumns = { @JoinColumn(name = "product_id") }, inverseJoinColumns = { @JoinColumn(name = "insure_channel_id") })
	private Set<InsureChannel> insureChannels;
	
	@ManyToOne
	@JoinColumn(name="parent")
	private Product parent;
	
	@OneToMany(mappedBy = "parent")	
	private Set<Product> children;
	
	
	public Product getParent() {
		return parent;
	}

	public void setParent(Product parent) {
		this.parent = parent;
	}

	public Set<Product> getChildren() {
		return children;
	}

	public void setChildren(Set<Product> children) {
		this.children = children;
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
