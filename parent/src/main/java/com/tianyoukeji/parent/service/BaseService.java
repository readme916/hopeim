package com.tianyoukeji.parent.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tianyoukeji.parent.entity.IEntity;

/**
 * 	抽象基础服务类
 * 
 * 	封装了基本的jpa的操作和平台的其他基础功能
 * @author Administrator
 *
 * @param <T>
 */

public abstract class BaseService<T extends IEntity> {

	@Autowired
	private JpaRepository<T, Long> jpaRepository;

	/**
	 *
	 * null 如果id不存在.
	 */
	public T findById(Long id) {
		Optional<T> findById = jpaRepository.findById(id);
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
		return jpaRepository.findAll();
	}

	public Long count() {
		return jpaRepository.count();
	}

	/**
	 *	如果entity是游离态，id在保存时候会被忽略
	 * @param entity
	 * @return
	 */
	public T save(T entity) {
		return jpaRepository.save(entity);
	}
	
	/**
	 *	如果entity是游离态，id在保存时候会被忽略
	 * @param entity
	 * @return
	 */
	public T saveAndFlush(T entity) {
		return jpaRepository.saveAndFlush(entity);
	}
	
	
	/**
	 * @param entity
	 * @throws IllegalArgumentException  如果entity不存在
	 */
	public void delete(T entity) {
		jpaRepository.delete(entity);
	}
}
