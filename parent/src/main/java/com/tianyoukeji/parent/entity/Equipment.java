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

@Entity
@Table(name = "equipment")
public class Equipment implements IEntity{
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
	
	@ManyToOne
	@JoinColumn(name= "product_sku_id")
	private ProductSKU productSKU;
	
	@ManyToOne
	@JoinColumn(name= "buy_user_id")
	private User boughtUser;
	
	@ManyToOne
	@JoinColumn(name= "buy_org_id")
	private Org boughtOrg;
	
	
	@Column(name= "bought_at")
	private Date boughtAt;
	
	@Column(name="buy_type")
	@Enumerated(EnumType.STRING)
	private BuyType buyType;
	
	@Column(name="pay_type")
	@Enumerated(EnumType.STRING)
	private PayType payType;
	
	@Column(name="pay_period")
	@Enumerated(EnumType.STRING)
	private PayPeriod payPeriod;
	
	@Column(name="pay_state")
	@Enumerated(EnumType.STRING)
	private PayState payState = PayState.UNPAID;
	
	@ManyToOne
	@JoinColumn(name= "pay_channel_id")
	private PayChannel payChannel;

	@OneToMany(mappedBy = "equipment")
	private Set<UserEquipmentRelationship> relativeUsers;

	
	public User getBoughtUser() {
		return boughtUser;
	}

	public void setBoughtUser(User boughtUser) {
		this.boughtUser = boughtUser;
	}

	public Org getBoughtOrg() {
		return boughtOrg;
	}

	public void setBoughtOrg(Org boughtOrg) {
		this.boughtOrg = boughtOrg;
	}

	public Date getBoughtAt() {
		return boughtAt;
	}

	public void setBoughtAt(Date boughtAt) {
		this.boughtAt = boughtAt;
	}

	public Set<UserEquipmentRelationship> getRelativeUsers() {
		return relativeUsers;
	}

	public void setRelativeUsers(Set<UserEquipmentRelationship> relativeUsers) {
		this.relativeUsers = relativeUsers;
	}

	public PayPeriod getPayPeriod() {
		return payPeriod;
	}

	public void setPayPeriod(PayPeriod payPeriod) {
		this.payPeriod = payPeriod;
	}

	public ProductSKU getProductSKU() {
		return productSKU;
	}

	public void setProductSKU(ProductSKU productSKU) {
		this.productSKU = productSKU;
	}

	public BuyType getBuyType() {
		return buyType;
	}

	public void setBuyType(BuyType buyType) {
		this.buyType = buyType;
	}

	public PayType getPayType() {
		return payType;
	}

	public void setPayType(PayType payType) {
		this.payType = payType;
	}


	public PayState getPayState() {
		return payState;
	}

	public void setPayState(PayState payState) {
		this.payState = payState;
	}

	public PayChannel getPayChannel() {
		return payChannel;
	}

	public void setPayChannel(PayChannel payChannel) {
		this.payChannel = payChannel;
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

	public enum BuyType{
		BUY,RENT
	}
	public enum PayType{
		ONCE,PERIOD
	}
	public enum PayPeriod{
		DAY_1,DAY_7,DAY_30,DAY_365
	}
	public enum PayState{
		UNPAID, PAID, PEROID_PAYING
	}
	
}
