package com.tianyoukeji.platform.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.entity.DepartmentRepository;
import com.tianyoukeji.parent.entity.MenuRepository;
import com.tianyoukeji.parent.entity.Org;
import com.tianyoukeji.parent.entity.OrgRepository;
import com.tianyoukeji.parent.entity.RegionRepository;
import com.tianyoukeji.parent.entity.RoleRepository;
import com.tianyoukeji.parent.entity.User;
import com.tianyoukeji.parent.entity.template.DepartmentTemplate;
import com.tianyoukeji.parent.entity.template.DepartmentTemplateRepository;
import com.tianyoukeji.parent.entity.template.EventTemplate;
import com.tianyoukeji.parent.entity.template.EventTemplateRepository;
import com.tianyoukeji.parent.entity.template.MenuTemplate;
import com.tianyoukeji.parent.entity.template.MenuTemplateRepository;
import com.tianyoukeji.parent.entity.template.OrgTemplate;
import com.tianyoukeji.parent.entity.template.OrgTemplateRepository;
import com.tianyoukeji.parent.entity.template.RoleTemplate;
import com.tianyoukeji.parent.entity.template.RoleTemplateRepository;
import com.tianyoukeji.parent.entity.template.StateTemplate;
import com.tianyoukeji.parent.entity.template.StateTemplateRepository;
import com.tianyoukeji.parent.entity.template.TimerTemplateRepository;
import com.tianyoukeji.platform.service.OrgTemplateService.Role;
import com.tianyoukeji.platform.service.StateTemplateService.Builder;
import com.tianyoukeji.platform.service.StateTemplateService.Event;
import com.tianyoukeji.platform.service.StateTemplateService.State;
import com.tianyoukeji.platform.service.StateTemplateService.Timer;

@Service
public class OrgTemplateService {

	final static Logger logger = LoggerFactory.getLogger(OrgTemplateService.class);

	@Autowired
	private OrgTemplateRepository orgTemplateRepository;

	@Autowired
	private DepartmentTemplateRepository departmentTemplateRepository;
	
	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private MenuTemplateRepository menuTemplateRepository;
	
	@Autowired
	private MenuRepository menuRepository;

	@Autowired
	private RoleTemplateRepository roleTemplateRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private RegionRepository regionRepository;
	
	@Autowired
	private OrgRepository orgRepository;


	
	public Org orgTemplateDeploy(String name, User owner , String orgTemplateCode , String country, String province , String city) {
		OrgTemplate orgTemplate = orgTemplateRepository.findByCode(orgTemplateCode);
		if(orgTemplate == null) {
			throw new BusinessException(1742, "企业模板不存在");
		}
		Org org = new Org();
		org.setName(name);
		org.setOrgTemplate(orgTemplate);
		org.setOwner(owner);
		org.setCity(regionRepository.findByFullname(city));
		org.setProvince(regionRepository.findByFullname(province));
		org.setCountry(regionRepository.findByFullname(country));
		org = orgRepository.save(org);
		
		Set<DepartmentTemplate> departmentTemplates = orgTemplate.getDepartmentTemplates();
		for (DepartmentTemplate departmentTemplate : departmentTemplates) {
			departmentTemplateTransfer(departmentTemplate , org);
		}
		
		Set<RoleTemplate> roleTemplates = orgTemplate.getRoleTemplates();
		roleTemplateTransfer(roleTemplates , org);
		
		Set<MenuTemplate> menuTemplates = orgTemplate.getMenuTemplates();
		for (MenuTemplate menuTemplate : menuTemplates) {
			menuTemplateTransfer(menuTemplate,org);
		}
		return org;
	}
	
	private com.tianyoukeji.parent.entity.Department departmentTemplateTransfer(DepartmentTemplate departmentTemplate, Org org) {
		if(departmentTemplate==null) {
			return null;
		}
		com.tianyoukeji.parent.entity.Department department = departmentRepository.findByCodeAndOrg(departmentTemplate.getCode(), org);
		if(department!=null) {
			return department;
		}
	
		department = new com.tianyoukeji.parent.entity.Department();
		department.setCode(departmentTemplate.getCode());
		department.setDepartmentTemplate(departmentTemplate);
		department.setName(departmentTemplate.getName());
		department.setOrg(org);
		department.setParent(departmentTemplateTransfer(departmentTemplate.getParent(),org));
	
		department = departmentRepository.saveAndFlush(department);
		return department;
		
	}
	
	private void roleTemplateTransfer(Set<RoleTemplate> roleTemplates, Org org) {
		if(roleTemplates==null) {
			return;
		}
		Set<com.tianyoukeji.parent.entity.Role> roles = new HashSet<>();
		for (RoleTemplate roleTemplate : roleTemplates) {
			com.tianyoukeji.parent.entity.Role role = roleRepository.findByCode(roleTemplate.getCode());
			if(role == null) {
				role = new com.tianyoukeji.parent.entity.Role();
				role.setCode(roleTemplate.getCode());
				role.setName(roleTemplate.getName());
				role.setRoleTemplate(roleTemplate);
				role = roleRepository.saveAndFlush(role);
			}
			roles.add(role);
		}
		
		org.setRoles(roles);
		orgRepository.save(org);
	}
	
	private com.tianyoukeji.parent.entity.Menu menuTemplateTransfer(MenuTemplate menuTemplate, Org org) {
		if(menuTemplate==null) {
			return null;
		}
		com.tianyoukeji.parent.entity.Menu menu = menuRepository.findByMenuTemplateAndOrg(menuTemplate, org);
		if(menu!=null) {
			return menu;
		}
	
		menu = new com.tianyoukeji.parent.entity.Menu();
		menu.setCode(menuTemplate.getCode());
		menu.setName(menuTemplate.getName());
		menu.setMenuTemplate(menuTemplate);
		menu.setOrg(org);
		menu.setSort(menuTemplate.getSort());
		Set<RoleTemplate> roleTemplates = menuTemplate.getRoleTemplates();
		HashSet<com.tianyoukeji.parent.entity.Role> roles = new HashSet<>();
		for (RoleTemplate roleTemplate : roleTemplates) {
			roles.add(roleRepository.findByCode(roleTemplate.getCode()));
		}
		menu.setRoles(roles);
		menu.setParent(menuTemplateTransfer(menuTemplate.getParent(),org));
		menu = menuRepository.saveAndFlush(menu);
		return menu;
		
	}
	public Builder getBuilder() {
		Builder builder = new Builder();
		builder.setDepartmentTemplateRepository(departmentTemplateRepository);
		builder.setRoleTemplateRepository(roleTemplateRepository);
		builder.setMenuTemplateRepository(menuTemplateRepository);
		builder.setOrgTemplateRepository(orgTemplateRepository);
		return builder;
	}

	public static class Builder {
		private HashMap<String, Department> allDepartments = new HashMap<String, Department>();
		private HashMap<String, Role> allRoles = new HashMap<String, Role>();
		private HashMap<String, Menu> allMenus = new HashMap<String, Menu>();

		private RoleTemplateRepository roleTemplateRepository;
		private OrgTemplateRepository orgTemplateRepository;
		private DepartmentTemplateRepository departmentTemplateRepository;
		private MenuTemplateRepository menuTemplateRepository;
		private String name;
		private String code;

		public void setRoleTemplateRepository(RoleTemplateRepository roleTemplateRepository) {
			this.roleTemplateRepository = roleTemplateRepository;
		}

		public void setOrgTemplateRepository(OrgTemplateRepository orgTemplateRepository) {
			this.orgTemplateRepository = orgTemplateRepository;
		}

		public void setDepartmentTemplateRepository(DepartmentTemplateRepository departmentTemplateRepository) {
			this.departmentTemplateRepository = departmentTemplateRepository;
		}

		public void setMenuTemplateRepository(MenuTemplateRepository menuTemplateRepository) {
			this.menuTemplateRepository = menuTemplateRepository;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder code(String code) {
			this.code = code;
			return this;
		}

		public Role getRole(String code) {
			return allRoles.get(code);
		}

		public Department getDepartment(String code) {
			return allDepartments.get(code);
		}

		public Menu getMenu(String code) {
			return allMenus.get(code);
		}

		public Builder department(String name, String code, String parent) {
			Department d = new Department(name, code, parent);
			if (allDepartments.containsKey(code)) {
				throw new BusinessException(1746, "部门 ： " + code + "已经存在");
			}
			d.setBuilder(this);
			allDepartments.put(code, d);
			return this;
		}

		public Builder role(String name, String code) {
			Role r = new Role(name, code);
			if (allRoles.containsKey(code)) {
				throw new BusinessException(1796, "角色： " + code + "已经存在");
			}
			r.setBuilder(this);
			allRoles.put(code, r);
			return this;
		}

		public Builder menu(String name, String code, String url, int sort, String parent) {
			Menu m = new Menu(name, code, url, sort, parent);
			if (allMenus.containsKey(code)) {
				throw new BusinessException(1796, "菜单： " + code + "已经存在");
			}
			m.setBuilder(this);
			allMenus.put(code, m);
			return this;
		}

		@Transactional
		public Builder build() {
			/**
			 * 填入数据库
			 */
			if (name == null || code == null) {
				throw new BusinessException(1284, "资料不全");
			}
			OrgTemplate orgTemplate = orgTemplateRepository.findByCode(code);
			if (orgTemplate == null) {
				orgTemplate = new OrgTemplate();
			}
			orgTemplate.setCode(code);
			orgTemplate.setName(name);
			orgTemplate = orgTemplateRepository.save(orgTemplate);
			
			
			Set<Entry<String, Department>> departmentSet = allDepartments.entrySet();
			for (Entry<String, Department> entry : departmentSet) {
				convertToDepartmentTemplate(entry.getValue(),orgTemplate);
			}
	
			convertToRoleTemplate(allRoles.values(),orgTemplate);
			
			Set<Entry<String, Menu>> menuSet = allMenus.entrySet();
			for (Entry<String, Menu> entry : menuSet) {
				convertToMenuTemplate(entry.getValue(),orgTemplate);
			}
			return this;
		}

		private void convertToRoleTemplate(Collection<Role> values, OrgTemplate orgTemplate) {
			HashSet<RoleTemplate> hashSet = new HashSet<RoleTemplate>();
			for (Role role : values) {
				RoleTemplate roleTemplate = roleTemplateRepository.findByCode(role.code);
				if(roleTemplate == null) {
					roleTemplate = new RoleTemplate();
				}
				roleTemplate.setCode(role.code);
				roleTemplate.setName(role.name);
				roleTemplate = roleTemplateRepository.saveAndFlush(roleTemplate);
				hashSet.add(roleTemplate);
			}
			orgTemplate.setRoleTemplates(hashSet);
			orgTemplateRepository.save(orgTemplate);
			
		}

		private MenuTemplate convertToMenuTemplate(Menu menu, OrgTemplate orgTemplate) {
			if (menu == null) {
				return null;
			}
			MenuTemplate findByCodeAndOrgTemplate = menuTemplateRepository.findByCodeAndOrgTemplate(menu.code, orgTemplate);
			if (findByCodeAndOrgTemplate == null) {
				findByCodeAndOrgTemplate = new MenuTemplate();
			}
			findByCodeAndOrgTemplate.setCode(menu.code);
			findByCodeAndOrgTemplate.setName(menu.name);
			findByCodeAndOrgTemplate.setOrgTemplate(orgTemplate);
			findByCodeAndOrgTemplate.setUrl(menu.url);
			findByCodeAndOrgTemplate.setSort(menu.sort);
			
			HashSet<String> roles = menu.roles;
			HashSet<RoleTemplate> roleTemplates = new HashSet<>();
			for (String r : roles) {
				Role role = getRole(r);
				RoleTemplate roleToRoleTemplate = convertToRoleTemplate(role,orgTemplate);
				if(roleToRoleTemplate!=null) {
					roleTemplates.add(roleToRoleTemplate);					
				}
			}
			findByCodeAndOrgTemplate.setRoleTemplates(roleTemplates);
			findByCodeAndOrgTemplate = menuTemplateRepository.saveAndFlush(findByCodeAndOrgTemplate);
			findByCodeAndOrgTemplate.setParent(convertToMenuTemplate(getMenu(menu.parent), orgTemplate));
			return menuTemplateRepository.saveAndFlush(findByCodeAndOrgTemplate);

		}

		private RoleTemplate convertToRoleTemplate(Role role, OrgTemplate orgTemplate) {
			if(role == null) {
				return null;
			}
			return roleTemplateRepository.findByCode(role.code);
		}

		private DepartmentTemplate convertToDepartmentTemplate(Department department, OrgTemplate orgTemplate) {
			if (department == null) {
				return null;
			}
			DepartmentTemplate findByCodeAndOrgTemplate = departmentTemplateRepository.findByCodeAndOrgTemplate(department.code, orgTemplate);
			if (findByCodeAndOrgTemplate == null) {
				findByCodeAndOrgTemplate = new DepartmentTemplate();
			}
			findByCodeAndOrgTemplate.setCode(department.code);
			findByCodeAndOrgTemplate.setName(department.code);
			findByCodeAndOrgTemplate.setOrgTemplate(orgTemplate);
			findByCodeAndOrgTemplate.setParent(convertToDepartmentTemplate(getDepartment(department.parent), orgTemplate));
			return departmentTemplateRepository.saveAndFlush(findByCodeAndOrgTemplate);
		}
	}

	public static class Department {
		private Builder builder;
		private String name;
		private String code;
		private String parent;

		public Department(String name, String code, String parent) {
			super();
			this.name = name;
			this.code = code;
			this.parent = parent;
		}

		public void setBuilder(Builder builder) {
			this.builder = builder;
		}

	}

	public static class Role {
		private Builder builder;
		private String name;
		private String code;

		public Role(String name, String code) {
			super();
			this.name = name;
			this.code = code;
		}

		public void setBuilder(Builder builder) {
			this.builder = builder;
		}
	}

	public static class Menu {
		private Builder builder;
		private String name;
		private String code;
		private String url;
		private int sort;
		private String parent;
		private HashSet<String> roles = new HashSet<String>();

		public Menu(String name, String code, String url, int sort, String parent) {
			super();
			this.name = name;
			this.code = code;
			this.url = url;
			this.sort = sort;
			this.parent = parent;
		}

		public void setBuilder(Builder builder) {
			this.builder = builder;
		}

		public Menu addRole(String role) {
			this.roles.add(role);
			return this;
		}

	}

}