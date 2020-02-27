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
import com.tianyoukeji.parent.entity.base.IBaseEntity;

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
	
	/**
	 * 继承的方法,状态机初始化之前调用，可以插入一些业务的state等到states数据库，完成状态机的设置
	 */
	@PostConstruct
	abstract protected void init();
	
	public String getServiceEntity() {
		ResolvableType resolvableType = ResolvableType.forClass(jpaRepository.getClass());
		Class<?> entityClass = resolvableType.as(JpaRepository.class).getGeneric(0).resolve();
		EntityStructure structure = SmartQuery.getStructure(entityClass);
		return structure.getName();
	}
	
	public JpaRepository<T, Long> getJpaRepository() {
		return jpaRepository;
	}
	
	public Map fetchOne(String queryString) {		
		return SmartQuery.fetchOne(getServiceEntity(), queryString);
	}
	
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
