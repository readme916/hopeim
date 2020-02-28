package com.tianyoukeji.parent.entity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

	Event findByOrgUuidAndEntityAndCodeAndRolesCode(Long uuid ,String entity, String event , String role);
	Event findByOrgIsNullAndEntityAndCodeAndRolesCode(String entity, String event , String role);
//	Event findByOrgUuidAndEntityAndCode(Long uuid, String entity,String code);
//	Event findByOrgIsNullAndEntityAndCode(String entity,String code);
	
	List<Event> findBySourcesUuidAndRolesCode(Long uuid , String role);
}
