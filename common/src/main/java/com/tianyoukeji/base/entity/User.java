package com.tianyoukeji.base.entity;

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
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Table(name = "users")
public class User implements IEntity{
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
	
	@Column(name="wx_bind")
	private Boolean wxBind;
	
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
	private UserInfo userInfo;

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

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
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

	public Boolean getWxBind() {
		return wxBind;
	}

	public void setWxBind(Boolean wxBind) {
		this.wxBind = wxBind;
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
