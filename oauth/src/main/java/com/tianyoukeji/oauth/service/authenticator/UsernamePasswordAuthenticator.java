package com.tianyoukeji.oauth.service.authenticator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.utopia.tokensart.auth.service.IntegrationAuthentication;
import com.utopia.tokensart.common.modules.base.init.GlobalType;
import com.utopia.tokensart.common.modules.base.models.Role;
import com.utopia.tokensart.common.modules.base.models.User;
import com.utopia.tokensart.common.modules.base.repository.RoleRepository;
import com.utopia.tokensart.common.modules.base.repository.UserRepository;


/**
 * 默认登录处理
 * @author LIQIU
 * @date 2018-3-31
 **/
@Component
@Primary
public class UsernamePasswordAuthenticator extends AbstractPreparableIntegrationAuthenticator {

	@Autowired
	private UserRepository userRepository;

    @Override
    public User authenticate(IntegrationAuthentication integrationAuthentication) {
        
//    	User user = userRepository.findByUsernameAndOauth2ClientsClientId(integrationAuthentication.getUsername(),integrationAuthentication.getClientId());
    	String clientId = integrationAuthentication.getClientId();
    	String username = integrationAuthentication.getAuthParameter("username");
    	GlobalType.ClientGroup group=null;
    	User findByUsername = null;
    	if("user-web".equals(clientId) || "user-app".equals(clientId)) {
    		group = GlobalType.ClientGroup.USER;
    		findByUsername = userRepository.findFirstByUsernameAndRolesClientGroup(username,group);
    	}else if("org-web".equals(clientId) || "org-app".equals(clientId)){
    		findByUsername = userRepository.findFirstByUsernameAndOrgStaffsNotNull(username);
    	}else if("platform-web".equals(clientId) || "platform-app".equals(clientId)){
    		group = GlobalType.ClientGroup.PLATFORM;
    		findByUsername = userRepository.findFirstByUsernameAndRolesClientGroup(username,group);
    	}
    	
        return findByUsername;
    }

    @Override
    public void prepare(IntegrationAuthentication integrationAuthentication) {

    }

    @Override
    public boolean support(IntegrationAuthentication integrationAuthentication) {
        return (integrationAuthentication.getAuthType()==null || "".equals( integrationAuthentication.getAuthType())
				||"password".equals( integrationAuthentication.getAuthType())||"refresh_token".equals( integrationAuthentication.getAuthType()));
    }
}
