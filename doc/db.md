# 基于Spring data jpa 模型

模型位置
------------------------
Entity目录：parent模块的**com.tianyoukeji.parent.entity**包

标识接口
----------------------------
标识接口,位于**com.tianyoukeji.parent.entity.base**包


1. IBaseEntity 基础接口，全局实体类型必须实现
  
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