package com.tianyoukeji.oauth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tianyoukeji.parent.common.AvatarUtils;
import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.entity.Oauth2Client;
import com.tianyoukeji.parent.entity.Oauth2ClientRepository;
import com.tianyoukeji.parent.entity.Org;
import com.tianyoukeji.parent.entity.Role;
import com.tianyoukeji.parent.entity.RoleRepository;
import com.tianyoukeji.parent.entity.User;
import com.tianyoukeji.parent.entity.Userinfo;
import com.tianyoukeji.parent.entity.UserinfoRepository;
import com.tianyoukeji.parent.entity.template.RoleTemplate;
import com.tianyoukeji.parent.entity.template.RoleTemplateRepository;
import com.tianyoukeji.parent.entity.template.RoleTemplate.Terminal;
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
	private UserRepository userRepository;

	@Autowired
	private OauthUserService oauthUserService;

	@Autowired
	private Oauth2ClientRepository oauth2ClientRepository;

	@Override
	public void init() {

		if (oauth2ClientRepository.count() == 0) {
			Oauth2Client oauth2ClientPlatform = new Oauth2Client();
			oauth2ClientPlatform.setAccessTokenValidity(86400 * 30);
			oauth2ClientPlatform.setAutoapprove("true");
			oauth2ClientPlatform.setAuthorizedGrantTypes("refresh_token,password");
			oauth2ClientPlatform.setClientId("org");
			oauth2ClientPlatform.setClientSecret(
					PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("org@tianyoukeji"));
			oauth2ClientPlatform.setRefreshTokenValidity(86400 * 365);
			oauth2ClientPlatform.setResourceIds(RoleTemplate.Terminal.org.toString());
			oauth2ClientPlatform.setScope("all");
			oauth2ClientRepository.save(oauth2ClientPlatform);
			Oauth2Client oauth2ClientApp = new Oauth2Client();
			oauth2ClientApp.setAccessTokenValidity(86400 * 30);
			oauth2ClientApp.setAutoapprove("true");
			oauth2ClientApp.setAuthorizedGrantTypes("refresh_token,password");
			oauth2ClientApp.setClientId("user");
			oauth2ClientApp.setClientSecret(
					PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("user@tianyoukeji"));
			oauth2ClientApp.setRefreshTokenValidity(86400 * 365);
			oauth2ClientApp.setResourceIds(RoleTemplate.Terminal.user.toString());
			oauth2ClientApp.setScope("all");
			oauth2ClientRepository.save(oauth2ClientApp);

		}

		if (roleTemplateRepository.count() == 0) {
			/**
			 * 注册无组织角色
			 */
			RoleTemplate developer = registerRoleTemplate("developer", "开发者", Terminal.org);
			registerRole(developer);
			RoleTemplate user = registerRoleTemplate("user", "普通用户", Terminal.user);
			registerRole(user);
		}
		
		if(userRepository.count()==0) {
			/**
			 * 注册用户
			 */
			User admin = oauthUserService.registerUser("admin", "admin", "developer", null);
		}
	}

	@Transactional
	private RoleTemplate registerRoleTemplate(String code, String name, Terminal terminal) {
		RoleTemplate findByCode = roleTemplateRepository.findByCode(code);
		if (findByCode != null) {
			return findByCode;
		} else {
			RoleTemplate roleTemplate = new RoleTemplate();
			roleTemplate.setCode(code);
			roleTemplate.setName(name);
			roleTemplate.setTerminal(terminal);
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
		role.setTerminal(template.getTerminal());
		role = roleRepository.save(role);
		return role;

	}

}
