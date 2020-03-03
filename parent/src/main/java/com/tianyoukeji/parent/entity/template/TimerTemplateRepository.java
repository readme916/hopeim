package com.tianyoukeji.parent.entity.template;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TimerTemplateRepository extends JpaRepository<TimerTemplate, Long> {
	TimerTemplate findByEntityAndCode(String entity,String code);
	List<TimerTemplate> findByEntity(String entity);
}
