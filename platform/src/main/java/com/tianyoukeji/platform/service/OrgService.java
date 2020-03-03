package com.tianyoukeji.platform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tianyoukeji.parent.entity.DepartmentRepository;
import com.tianyoukeji.parent.entity.MenuRepository;
import com.tianyoukeji.parent.entity.Org;
import com.tianyoukeji.parent.entity.OrgRepository;
import com.tianyoukeji.parent.entity.RoleRepository;
import com.tianyoukeji.parent.entity.template.DepartmentTemplateRepository;
import com.tianyoukeji.parent.entity.template.MenuTemplate;
import com.tianyoukeji.parent.entity.template.MenuTemplateRepository;
import com.tianyoukeji.parent.entity.template.OrgTemplate;
import com.tianyoukeji.parent.entity.template.OrgTemplateRepository;
import com.tianyoukeji.parent.entity.template.RoleTemplateRepository;

@Service
public class OrgService {

	final static Logger logger = LoggerFactory.getLogger(OrgService.class);

	@Autowired
	private OrgRepository orgRepository;
	
	@Autowired
	private DepartmentRepository departmentRepository;
	
	@Autowired
	private MenuRepository menuRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private OrgTemplateRepository orgTemplateRepository;
	
	@Autowired
	private DepartmentTemplateRepository departmentTemplateRepository;
	
	@Autowired
	private MenuTemplateRepository menuTemplateRepository;
	
	@Autowired
	private RoleTemplateRepository roleTemplateRepository;
	
	public Org createByTemplate(OrgTemplate orgTemplate) {
		return null;
	}
	

}