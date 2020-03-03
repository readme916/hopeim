package com.tianyoukeji.parent.entity.template;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StateTemplateRepository extends JpaRepository<StateTemplate, Long> {

	StateTemplate findByEntityAndCode(String entity,String code);
	List<StateTemplate> findByEntity(String entity);
	
}
