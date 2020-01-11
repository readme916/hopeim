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
    private void init() {
    	if(this.count()==0) {
    		Oauth2Client oauth2Client = new Oauth2Client();
    		oauth2Client.setAccessTokenValidity(86400*30);
    		oauth2Client.setAutoapprove("true");
    		oauth2Client.setAuthorizedGrantTypes("authorization_code,refresh_token,password");
    		oauth2Client.setClientId("platform");
    		oauth2Client.setClientSecret(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("tianyoukeji@platform"));
    		oauth2Client.setRefreshTokenValidity(86400*365);
    		oauth2Client.setResourceIds("platform");
    		oauth2Client.setScope("all");
    		save(oauth2Client);
    	}
    	
    }
    
}
