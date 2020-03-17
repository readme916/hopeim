# 基于Spring data jpa 模型

模型位置
------------------------
Entity目录：parent模块的**com.tianyoukeji.parent.entity**包

标识接口
----------------------------
标识接口,位于**com.tianyoukeji.parent.entity.base**包


1. IBaseEntity基础接口，全局实体类型必须实现
  
```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public interface IBaseEntity extends Serializable{
	public Long getUuid();
	public void setUuid(Long uuid);
	public Date getCreatedAt();
	public void setCreatedAt(Date createdAt);
	public Date getUpdatedAt();
	public void setUpdatedAt(Date updatedAt);
	public Long getVersion();
	public void setVersion(Long version);
}
```


2. IOrgEntity接口，说明数据具有企业的属性，可以使用企业来筛选，例如department,user表

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public interface IOrgEntity extends IBaseEntity{
	public Org getOrg();
	public void setOrg(Org org);
}

```

3. IDepartmentEntity接口，说明数据具有部门的属性，可以使用部门来筛选，例如user

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public interface IDepartmentEntity extends IOrgEntity{
	public Department getDepartment();
	public void setDepartment(Department department);

}


```
4. IQunEntity接口，说明数据会被作为IM群的主题，例如部门群，返修单群

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public interface IQunEntity extends IBaseEntity{
	public String getGroupId();
	public void setGroupId(String groupId);
}
```

5. IRegionEntity接口，说明数据具有地域性，可以使用地区筛选，例如订单，用户

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public interface IRegionEntity extends IBaseEntity{
	public Region getCountry();
	public Region getProvince();
	public Region getCity();
	public void setCountry(Region country);
	public void setProvince(Region province);
	public void setCity(Region city);
}
```
6. ISortEntity接口，说明数据可以排序，例如menu

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public interface ISortEntity extends IBaseEntity{
	public Integer getSort();
	public void setSort(Integer sort);
}

```
7. IStateMachineEntity接口，说明数据具备生命周期性质，不同状态，不同用户可以操作它,例如订单，用户

```java

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public interface IStateMachineEntity extends IBaseEntity{
	public State getState();
	public void setState(State state);

}
```
