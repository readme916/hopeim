package com.tianyoukeji.oauth.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tianyoukeji.parent.common.AvatarUtils;
import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.entity.Oauth2Client;
import com.tianyoukeji.parent.entity.User;
import com.tianyoukeji.parent.entity.Userinfo;
import com.tianyoukeji.parent.entity.UserinfoRepository;
import com.tianyoukeji.parent.entity.UserRepository;
import com.tianyoukeji.parent.service.BaseService;

import java.util.*;

import javax.annotation.PostConstruct;

@Service
public class OauthClientService extends BaseService<Oauth2Client>{

    @PostConstruct
    public void init() {
    	if(this.count()==0) {
    		Oauth2Client oauth2ClientPlatform = new Oauth2Client();
    		oauth2ClientPlatform.setAccessTokenValidity(86400*30);
    		oauth2ClientPlatform.setAutoapprove("true");
    		oauth2ClientPlatform.setAuthorizedGrantTypes("refresh_token,password");
    		oauth2ClientPlatform.setClientId("platform");
    		oauth2ClientPlatform.setClientSecret(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("tianyoukeji@platform"));
    		oauth2ClientPlatform.setRefreshTokenValidity(86400*365);
    		oauth2ClientPlatform.setResourceIds("platform");
    		oauth2ClientPlatform.setScope("all");
    		save(oauth2ClientPlatform);
    		Oauth2Client oauth2ClientApp = new Oauth2Client();
    		oauth2ClientApp.setAccessTokenValidity(86400*30);
    		oauth2ClientApp.setAutoapprove("true");
    		oauth2ClientApp.setAuthorizedGrantTypes("refresh_token,password");
    		oauth2ClientApp.setClientId("app");
    		oauth2ClientApp.setClientSecret(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("tianyoukeji@app"));
    		oauth2ClientApp.setRefreshTokenValidity(86400*365);
    		oauth2ClientApp.setResourceIds("app");
    		oauth2ClientApp.setScope("all");
    		save(oauth2ClientApp);
    		
    	}
    	
    }
    
}
