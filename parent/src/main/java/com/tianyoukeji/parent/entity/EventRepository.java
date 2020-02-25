package com.tianyoukeji.parent.entity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

	List<Event> findBySourcesCodeAndRolesCode(String state , String role);
	Event findByEntityAndCode(String entity,String code);
}
