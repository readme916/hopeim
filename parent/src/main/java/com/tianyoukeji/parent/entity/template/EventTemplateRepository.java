package com.tianyoukeji.parent.entity.template;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventTemplateRepository extends JpaRepository<EventTemplate, Long> {
	EventTemplate findByEntityAndCode(String entity,String code);
	List<EventTemplate> findByEntity(String entity);
}
