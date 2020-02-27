package com.tianyoukeji.parent.entity.base;

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

import com.tianyoukeji.parent.entity.Department;
import com.tianyoukeji.parent.entity.Org;
import com.tianyoukeji.parent.entity.Region;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public interface IOrgEntity extends IBaseEntity{
	public Org getOrg();
	public void setOrg(Org org);
}
