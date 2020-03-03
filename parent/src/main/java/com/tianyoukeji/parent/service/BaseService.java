package com.tianyoukeji.parent.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.liyang.jpa.smart.query.db.SmartQuery;
import com.liyang.jpa.smart.query.db.structure.EntityStructure;
import com.liyang.jpa.smart.query.response.HTTPListResponse;
import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.common.ContextUtils;
import com.tianyoukeji.parent.entity.User;
import com.tianyoukeji.parent.entity.UserRepository;
import com.tianyoukeji.parent.entity.base.IBaseEntity;
import com.tianyoukeji.parent.entity.base.IOrgEntity;

/**
 * 	抽象基础服务类
 * 
 * 	封装了基本的jpa的操作和平台的其他基础功能
 * @author Administrator
 *
 * @param <T>
 */

public abstract class BaseService<T extends IBaseEntity> {

	@Autowired
	private JpaRepository<T, Long> jpaRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	/**
	 * 继承的方法,状态机初始化之前调用，可以插入一些业务的state等到states数据库，完成状态机的设置
	 */
	@PostConstruct
	abstract protected void init();
	
	/**
	 * 	返回当前用户
	 * @return
	 */
	public User getCurrentUser() {
		String username = ContextUtils.getCurrentUserName();
		return userRepository.findByUserinfoMobile(username);
	}
	
	/**
	 * 	返回当前服务的实体
	 * @return
	 */
	public String getServiceEntity() {
		ResolvableType resolvableType = ResolvableType.forClass(jpaRepository.getClass());
		Class<?> entityClass = resolvableType.as(JpaRepository.class).getGeneric(0).resolve();
		EntityStructure structure = SmartQuery.getStructure(entityClass);
		return structure.getName();
	}
	
	/**
	 * 	当前服务的实体的jpa
	 * @return
	 */
	public JpaRepository<T, Long> getJpaRepository() {
		return jpaRepository;
	}
	
	
	
	
	//smartyquery查询方法部分

	/**
	 * 	根据query，返回一个具体的map格式的对象
	 * @param queryString
	 * @return
	 */
	public Map fetchOne(String queryString) {		
		return SmartQuery.fetchOne(getServiceEntity(), queryString);
	}
	
	/**
	 * 	传入null，返回所有企业数据，否则返回orgId企业的数据
	 * @param queryString
	 * @param orgId
	 * @return
	 */
	public Map fetchOneByOrg(String queryString , Long orgId) {
		if(!(this instanceof IOrgEntity)) {
			throw new BusinessException(1864, "当前实体，非org类型");
		}
		if(orgId != null) {
			return fetchOne(queryString + "&org.uuid="+orgId);
		}else {
			return fetchOne(queryString);
		}
	}
	
	/**
	 * 	返回一个带翻页的结果的Response对象
	 * @param queryString
	 * @return
	 */
	public HTTPListResponse fetchList(String queryString) {		
		return SmartQuery.fetchList(getServiceEntity(), queryString);
	}
	
	/**
	 * 	返回一个按照树形组织的Response对象
	 * @param queryString
	 * @return
	 */
	public HTTPListResponse fetchTree(String queryString) {		
		return SmartQuery.fetchTree(getServiceEntity(), queryString);
	}
	
	
	/**
	 *	返回一个按照分组组织的Response对象
	 * @param queryString
	 * @return
	 */
	public HTTPListResponse fetchGroup(String queryString) {		
		return SmartQuery.fetchGroup(getServiceEntity(), queryString);
	}
	
	/**
	 * 	返回一个查询count
	 * @param queryString
	 * @return
	 */
	public long fetchCount(String queryString) {		
		return SmartQuery.fetchCount(getServiceEntity(), queryString);
	}
	
	
	
	
	//jpa 基础方法
	/**
	 *
	 * null 如果id不存在.
	 */
	public T findById(Long id) {
		Optional<T> findById = getJpaRepository().findById(id);
		if(findById.isPresent()) {
			return findById.get();
		}else {
			return null;
		}
	}

	/**
	 *	空的时候返回[] 
	 * @return 
	 */
	
	public List<T> findAll() {
		return getJpaRepository().findAll();
	}

	public Long count() {
		return getJpaRepository().count();
	}

	/**
	 *	如果entity是游离态，id在保存时候会被忽略
	 * @param entity
	 * @return
	 */
	public T save(T entity) {
		return getJpaRepository().save(entity);
	}
	
	/**
	 *	如果entity是游离态，id在保存时候会被忽略
	 * @param entity
	 * @return
	 */
	public T saveAndFlush(T entity) {
		return getJpaRepository().saveAndFlush(entity);
	}
	
	
	/**
	 * @param entity
	 * @throws IllegalArgumentException  如果entity不存在
	 */
	public void delete(T entity) {
		getJpaRepository().delete(entity);
	}
}
