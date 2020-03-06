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
public class OrgService extends BaseService<Org> {

	final static Logger logger = LoggerFactory.getLogger(OrgService.class);

	@Autowired
	private OrgTemplateService orgTemplateService;

	@Autowired
	private StateTemplateService stateTemplateService;

	@Autowired
	private OrgRepository orgRepository;

	@Autowired
	private UserService userService;

	/**
	 * 创建默认企业
	 */
	@Override
	public void init() {
		if (orgRepository.count() == 0) {
			platformDeploy();
		}
	}

	/**
	 * 平台部署的细节，相当于第一个企业
	 */
	public void platformDeploy() {
		//找到创建人
		User findById = userService.findById(1L);
		//根据platform模板给组织创建部门，角色，菜单
		Org org = orgTemplateService.orgTemplateDeploy("天邮平台", findById, "platform", "中华人民共和国", "浙江省", "杭州市");
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