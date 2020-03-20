package com.tianyoukeji.parent.entity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tianyoukeji.parent.entity.template.RoleTemplate.Terminal;

public interface EventRepository extends JpaRepository<Event, Long> {

	Event findBySourcesUuidAndActionAndRolesCode(Long uuid , String action , String role);
	Event findBySourcesUuidAndAction(Long uuid , String action);
	Event findByEntityAndCode(String entity,String code);
	
	List<Event> findBySourcesUuidAndRolesCode(Long uuid , String role);
	List<Event> findBySourcesUuid(Long uuid);
	
}
