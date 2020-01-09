package com.tianyoukeji.oauth.service.authenticator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Component;

import com.utopia.tokensart.auth.service.IntegrationAuthentication;
import com.utopia.tokensart.common.modules.base.init.GlobalType;
import com.utopia.tokensart.common.modules.base.models.User;
import com.utopia.tokensart.common.modules.base.repository.UserRepository;



/**
 * 集成验证码认证
 * @author LIQIU
 * @date 2018-3-31
 **/
@Component
public class MobilePasswordIntegrationAuthenticator extends UsernamePasswordAuthenticator {

    private final static String VERIFICATION_CODE_AUTH_TYPE = "mobile";

	@Autowired
	private UserRepository userRepository;

    @Override
    public User authenticate(IntegrationAuthentication integrationAuthentication) {
    	String clientId = integrationAuthentication.getClientId();
    	GlobalType.ClientGroup group=null;
    	User findByUsername = null;
    	
    	String callingCode = integrationAuthentication.getAuthParameter("callingCode");
    	if(callingCode==null) {
    		System.out.println("需要callingCode参数");
    		return null;
    	}
    	String mobile = integrationAuthentication.getAuthParameter("mobile");
    	if(mobile == null) {
    		System.out.println("需要mobile参数");
    		return null;
    	}
    	if("user-web".equals(clientId) || "user-app".equals(clientId)) {
    		group = GlobalType.ClientGroup.USER;
    		findByUsername = userRepository.findFirstByUserMobilesCallingCodeAndUserMobilesMobileAndUserMobilesIsCertifiedAndUserMobilesStateAndRolesClientGroup(callingCode,mobile, true, true ,group);
    	}else if("org-web".equals(clientId) || "org-app".equals(clientId)){
    		findByUsername = userRepository.findFirstByUserMobilesCallingCodeAndUserMobilesMobileAndUserMobilesIsCertifiedAndUserMobilesStateAndOrgStaffsNotNull(callingCode,mobile, true, true);
    	}else if("platform-web".equals(clientId) || "platform-app".equals(clientId)){
    		group = GlobalType.ClientGroup.PLATFORM;
    		findByUsername = userRepository.findFirstByUserMobilesCallingCodeAndUserMobilesMobileAndUserMobilesIsCertifiedAndUserMobilesStateAndRolesClientGroup(callingCode,mobile, true, true ,group);
    	}
    	
        return findByUsername;
    
    }

    @Override
    public boolean support(IntegrationAuthentication integrationAuthentication) {
        return VERIFICATION_CODE_AUTH_TYPE.equals(integrationAuthentication.getAuthType())||"refresh_token".equals(integrationAuthentication.getAuthType());
    }
    
    @Override
    public void prepare(IntegrationAuthentication integrationAuthentication) {

    }

}
