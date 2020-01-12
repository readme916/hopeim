package com.tianyoukeji.oauth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.alibaba.druid.pool.DruidDataSource;
import com.tianyoukeji.oauth.filter.UsernamePasswordAuthenticationProvider;
import com.tianyoukeji.parent.service.NamespaceRedisService;
import com.tianyoukeji.parent.service.NamespaceRedisService.RedisNamespace;
import com.tianyoukeji.parent.service.RateLimiterService;


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
				return accessToken;
			}
		});
	}

}
