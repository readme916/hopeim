package com.tianyoukeji.parent.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tianyoukeji.parent.entity.template.MenuTemplate;

public interface MenuRepository extends JpaRepository<Menu, Long> {
	Menu findByMenuTemplateAndOrg(MenuTemplate menuTemplate , Org org);
	Menu findByMenuTemplateAndOrgIsNull(MenuTemplate menuTemplate);
}
