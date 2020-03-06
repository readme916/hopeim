package com.tianyoukeji.oauth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tianyoukeji.parent.common.AvatarUtils;
import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.entity.Org;
import com.tianyoukeji.parent.entity.Role;
import com.tianyoukeji.parent.entity.RoleRepository;
import com.tianyoukeji.parent.entity.User;
import com.tianyoukeji.parent.entity.Userinfo;
import com.tianyoukeji.parent.entity.UserinfoRepository;
import com.tianyoukeji.parent.entity.template.RoleTemplate;
import com.tianyoukeji.parent.entity.template.RoleTemplateRepository;
import com.tianyoukeji.parent.entity.UserRepository;
import com.tianyoukeji.parent.service.BaseService;

import java.util.*;

import javax.annotation.PostConstruct;

@Service
public class InitService extends BaseService<User> {

	@Autowired
	private RoleTemplateRepository roleTemplateRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private OauthUserService oauthUserService;
	
	@Override
	public void init() {
		if (roleTemplateRepository.count() == 0) {
			/** 
			 * 	注册无组织角色
			 */
			RoleTemplate developer = registerRoleTemplate("developer", "开发者");
			registerRole(developer);
			RoleTemplate user = registerRoleTemplate("user", "普通用户");
			registerRole(user);
			
			/**
			 * 	注册用户
			 */
			User admin = oauthUserService.registerUser("admin", "admin", "developer",null);
		}
	}
	@Transactional
	private RoleTemplate registerRoleTemplate(String code, String name) {
		RoleTemplate findByCode = roleTemplateRepository.findByCode(code);
		if (findByCode != null) {
			return findByCode;
		} else {
			RoleTemplate roleTemplate = new RoleTemplate();
			roleTemplate.setCode(code);
			roleTemplate.setName(name);
			roleTemplate = roleTemplateRepository.save(roleTemplate);
			return roleTemplate;
		}
		
	}

	@Transactional
	private Role registerRole(RoleTemplate template) {
			Role role = new Role();
			role.setCode(template.getCode());
			role.setName(template.getName());
			role.setRoleTemplate(template);
			role = roleRepository.save(role);
			return role;
		
	}
	
}
