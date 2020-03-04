package com.tianyoukeji.oauth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tianyoukeji.parent.common.AvatarUtils;
import com.tianyoukeji.parent.common.BusinessException;
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
	private OauthUserService OauthUserService;

	@Override
	@PostConstruct
	public void init() {
//		if (OauthUserService.count() == 0) {
//			
//			/** 
//			 * 	注册无组织角色
//			 */
//			RoleTemplate developer = OauthUserService.registerRoleTemplate("developer", "开发者");
//			OauthUserService.registerRole(developer, null);
//			RoleTemplate user = OauthUserService.registerRoleTemplate("user", "普通用户");
//			OauthUserService.registerRole(user, null);
//			
//			/**
//			 * 	注册用户
//			 */
//			User admin = OauthUserService.registerUser("admin", "admin", "developer",null);
//			
//			
//			/**
//			 * 	注册有组织角色
//			 */
//			RoleTemplate orgOwner = OauthUserService.registerRoleTemplate("orgOwner", "企业主");
//			RoleTemplate orgManager = OauthUserService.registerRoleTemplate("orgManager", "企业管理员");
//			RoleTemplate orgUser = OauthUserService.registerRoleTemplate("orgUser", "企业员工");
//			HashSet<RoleTemplate> hashSet = new HashSet<RoleTemplate>();
//			hashSet.add(orgOwner);
//			hashSet.add(orgManager);
//			hashSet.add(orgUser);
//			OauthUserService.registerOrg("天邮科技有限公司", admin, hashSet);

//		}
	}
}
