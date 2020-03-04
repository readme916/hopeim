package com.tianyoukeji.parent.entity.template;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleTemplateRepository extends JpaRepository<RoleTemplate, Long> {
	RoleTemplate findByCode(String code);
	
}
