package com.tianyoukeji.parent.entity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tianyoukeji.parent.entity.template.RoleTemplate.Terminal;

public interface EventRepository extends JpaRepository<Event, Long> {

	Event findByEntityAndActionAndRolesCode(String entity, String action , String role);
	Event findByEntityAndCode(String entity,String code);
	List<Event> findBySourcesUuidAndRolesCode(Long uuid , String role);
	
	int countByUuidAndRolesTerminal(Long uuid , Terminal terminal);
	
}
