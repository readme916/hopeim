package com.tianyoukeji.parent.entity.template;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuTemplateRepository extends JpaRepository<MenuTemplate, Long> {
	MenuTemplate findByCodeAndOrgTemplate(String code , OrgTemplate orgTemplate);
}
