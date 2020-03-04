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
import com.tianyoukeji.parent.entity.User;
import com.tianyoukeji.parent.entity.template.DepartmentTemplateRepository;
import com.tianyoukeji.parent.entity.template.MenuTemplateRepository;
import com.tianyoukeji.parent.entity.template.OrgTemplate;
import com.tianyoukeji.parent.entity.template.OrgTemplateRepository;
import com.tianyoukeji.parent.entity.template.RoleTemplateRepository;
import com.tianyoukeji.parent.service.BaseService;
import com.tianyoukeji.platform.service.OrgTemplateService.Builder;

@Service
public class OrgService extends BaseService<Org>{

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
	private DepartmentTemplateRepository departmentTemplateRepository;
	
	@Autowired
	private MenuTemplateRepository menuTemplateRepository;
	
	@Autowired
	private RoleTemplateRepository roleTemplateRepository;
	
	@Autowired
	private OrgTemplateService orgTemplateService;
	
	@Autowired
	private OrgTemplateRepository orgTemplateRepository;
	
	@Override
	public void init() {
		if(orgTemplateRepository.count()>0) {
			return;
		}
		Builder builder = orgTemplateService.getBuilder().code("platform").name("天邮科技")
				.department("部门1", "department1", null)
				.department("部门2", "department2", null)
				.role("管理员", "platform_manager")
				.role("超管", "platform_super")
				.menu("主页", "home",  "/", 0, null)
				.menu("第一页", "one", "/one", 1, "home");
		builder.getMenu("home").addRole("platform_manager").addRole("platform_super");
		builder.build();
	}

	
	/**
	 * 平台部署的细节
	 */
	public void platformDeploy() {
		
	}
	
	/**
	 * a类企业类型部署的细节
	 */
	public void aDeploy() {
		
	}
	/**
	 * b类企业类型部署的细节
	 */
	public void bDeploy() {
		
	}
}