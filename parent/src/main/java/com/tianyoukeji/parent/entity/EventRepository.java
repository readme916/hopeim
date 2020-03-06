package com.tianyoukeji.parent.entity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

	Event findByEntityAndActionAndRolesCode(String entity, String action , String role);
	Event findByEntityAndCode(String entity,String code);
	List<Event> findBySourcesUuidAndRolesCode(Long uuid , String role);
}
