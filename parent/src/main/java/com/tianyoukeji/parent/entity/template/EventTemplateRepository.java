package com.tianyoukeji.parent.entity.template;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventTemplateRepository extends JpaRepository<EventTemplate, Long> {
	EventTemplate findByEntityAndCode(String entity,String code);
}
