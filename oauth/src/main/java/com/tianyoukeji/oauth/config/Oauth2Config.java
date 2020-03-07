package com.tianyoukeji.oauth.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import com.alibaba.druid.pool.DruidDataSource;
import com.tencentyun.TLSSigAPIv2;
import com.tianyoukeji.oauth.filter.UsernamePasswordAuthenticationProvider;
import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.entity.User;
import com.tianyoukeji.parent.entity.UserRepository;
import com.tianyoukeji.parent.service.NamespaceRedisService;
import com.tianyoukeji.parent.service.NamespaceRedisService.RedisNamespace;


@Configuration
@EnableAuthorizationServer
public class Oauth2Config extends AuthorizationServerConfigurerAdapter {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private DruidDataSource dataSource;

	@Autowired
    private RedisConnectionFactory redisConnectionFactory;
	
	@Autowired
	private NamespaceRedisService redisService;
	
	@Autowired
	private UsernamePasswordAuthenticationProvider usernamePasswordAuthenticationProvider;
	
	@Autowired
	private UserRepository userRepository;
	
	@Value("${SDKAppID}")
	private String SDKAppID;
	
	@Value("${SDKAPPSecret}")
	private String SDKAPPSecret;
	

	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		security.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()");
	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.withClientDetails(clientDetails());
	}

	@Bean
	public ClientDetailsService clientDetails() {
		return new JdbcClientDetailsService(dataSource);
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		/** 
		 * 	 配置tokenStore
		 * 	userDetailsService 当freshtoken时候会调用usernamePasswordAuthenticationProvider。loadUserByUsername
		 * 	其他都走authenticate分支
		 */
		endpoints.authenticationManager(authenticationManager).userDetailsService(usernamePasswordAuthenticationProvider).tokenStore(new RedisTokenStore(redisConnectionFactory));
		endpoints.tokenEnhancer(new TokenEnhancer() {
			
			@Override
			public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
				String value = accessToken.getValue();
				String username = authentication.getName();
				redisService.sAdd(RedisNamespace.USER_TOKEN, username, value);
				redisService.expire(RedisNamespace.USER_TOKEN, username, 2*30*86400);
				
				String user = authentication.getName();
				User u = userRepository.findByUserinfoMobile(user);
				
				String terminal = u.getRole().getTerminal().toString();
				if(authentication.getOAuth2Request().getClientId().equals("org")) {
					if(!terminal.equals("org")) {
						throw new BadCredentialsException("角色不允许登录");
					}
				}
				
		        final Map<String, Object> additionalInfo = new HashMap<>();
		        additionalInfo.put("unionID", u.getUnionId());
		        additionalInfo.put("userID", user);
		        TLSSigAPIv2 api = new TLSSigAPIv2(Long.valueOf(SDKAppID), SDKAPPSecret);
		        additionalInfo.put("userSig", api.genSig(user, 365*86400));
		        
		        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
				return accessToken;
			}
		});
	}

}
