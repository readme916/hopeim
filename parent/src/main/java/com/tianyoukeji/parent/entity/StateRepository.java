package com.tianyoukeji.parent.entity;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;


public interface StateRepository extends JpaRepository<State, Long> {

	List<State> findByEntity(String entityName);
	Set<State> findByEntityAndOrgUuid(String entityName, Long uuid);
	Set<State> findByEntityAndOrgIsNull(String entityName);
	
	State findByOrgUuidAndEntityAndIsStart(Long uuid, String entityName, boolean isStart);
	State findByOrgIsNullAndEntityAndIsStart(String entityName, boolean isStart);
	State findByOrgUuidAndEntityAndCode(Long uuid, String entityName , String code);
	State findByOrgIsNullAndEntityAndCode(String entityName , String code);
}
