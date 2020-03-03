package com.tianyoukeji.parent.entity.template;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TimerTemplateRepository extends JpaRepository<TimerTemplate, Long> {
	TimerTemplate findByEntityAndCode(String entity,String code);
}
