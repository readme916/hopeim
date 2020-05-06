package com.tianyoukeji.oauth.service;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tianyoukeji.parent.entity.Oauth2Client;
import com.tianyoukeji.parent.entity.Oauth2ClientRepository;
import com.tianyoukeji.parent.entity.Role;
import com.tianyoukeji.parent.entity.RoleRepository;
import com.tianyoukeji.parent.entity.User;
import com.tianyoukeji.parent.entity.UserRepository;
import com.tianyoukeji.parent.entity.template.RoleTemplate;
import com.tianyoukeji.parent.entity.template.RoleTemplate.Terminal;
import com.tianyoukeji.parent.entity.template.RoleTemplateRepository;
import com.tianyoukeji.parent.service.BaseService;

@Service
public class InitService extends BaseService<User>  implements ApplicationContextAware {

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

	
	private ApplicationContext applicationContext;

	@Autowired
	private DataSource datasource;

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	
	@Override
	public void init(){

		if (oauth2ClientRepository.count() == 0) {
			
			Resource resource = applicationContext.getResource("classpath:quartz_tables_mysql_innodb.sql");
			try {
				ScriptUtils.executeSqlScript(datasource.getConnection(), resource);
			} catch (ScriptException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			Oauth2Client oauth2ClientPlatform = new Oauth2Client();
			oauth2ClientPlatform.setAccessTokenValidity(86400 * 30);
			oauth2ClientPlatform.setAutoapprove("true");
			oauth2ClientPlatform.setAuthorizedGrantTypes("refresh_token,password");
			oauth2ClientPlatform.setClientId("org");
			oauth2ClientPlatform.setClientSecret(
					PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("org@tianyoukeji"));
			oauth2ClientPlatform.setRefreshTokenValidity(86400 * 365);
			oauth2ClientPlatform.setResourceIds(RoleTemplate.Terminal.ORG.toString());
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
			oauth2ClientApp.setResourceIds(RoleTemplate.Terminal.ORG.toString());
			oauth2ClientApp.setScope("all");
			oauth2ClientRepository.save(oauth2ClientApp);

		}

		if (roleTemplateRepository.count() == 0) {
			/**
			 * 注册无组织角色
			 */
			RoleTemplate developer = registerRoleTemplate("developer", "开发者", Terminal.ORG);
			registerRole(developer);
			RoleTemplate user = registerRoleTemplate("user", "普通用户", Terminal.USER);
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
