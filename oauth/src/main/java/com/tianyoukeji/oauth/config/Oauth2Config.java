package com.tianyoukeji.oauth.config;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.alibaba.druid.pool.DruidDataSource;
import com.utopia.tokensart.auth.service.IntegrationAuthenticationFilter;
import com.utopia.tokensart.auth.service.IntegrationUserDetailsService;
import com.utopia.tokensart.common.modules.base.repository.UserRepository;

@Configuration
@EnableAuthorizationServer
public class Oauth2Config extends AuthorizationServerConfigurerAdapter {

	@Autowired
	private AuthenticationManager authenticationManager;



	@Resource
	private DruidDataSource dataSource;

	@Autowired
	private IntegrationUserDetailsService integrationUserDetailsService;

	@Autowired
	private IntegrationAuthenticationFilter integrationAuthenticationFilter;

	@Autowired
    RedisConnectionFactory redisConnectionFactory;
	@Autowired
	private UserRepository userRepository;


	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		/* 配置token获取合验证时的策略 */
		security.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()").allowFormAuthenticationForClients()
				.addTokenEndpointAuthenticationFilter(integrationAuthenticationFilter);
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
		// 配置tokenStore
//		endpoints.authenticationManager(authenticationManager).userDetailsService(userDetailsService)
		endpoints.authenticationManager(authenticationManager);
		endpoints.userDetailsService(integrationUserDetailsService)
				.tokenStore(new RedisTokenStore(redisConnectionFactory));
	}


}
