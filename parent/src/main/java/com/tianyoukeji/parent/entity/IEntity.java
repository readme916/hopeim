package com.tianyoukeji.parent.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public interface IEntity extends Serializable{
	public Long getUuid();
	public void setUuid(Long uuid);
	public Date getCreatedAt();
	public void setCreatedAt(Date createdAt);
	public Date getUpdatedAt();
	public void setUpdatedAt(Date updatedAt);
	public Long getVersion();
	public void setVersion(Long version);
}
