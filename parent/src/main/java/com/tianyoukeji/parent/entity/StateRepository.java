package com.tianyoukeji.parent.entity;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;


public interface StateRepository extends JpaRepository<State, Long> {

	Set<State> findByEntity(String entityName);
	
	State findByEntityAndIsStart(String entityName, boolean isStart);
	State findByEntityAndCode(String entityName , String code);
}
