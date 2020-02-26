package com.tianyoukeji.platform.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

	@Autowired
	RedisConnectionFactory redisConnectionFactory;

	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
		resources.resourceId("platform"); // 重点，设置资源id
		resources.tokenStore(new RedisTokenStore(redisConnectionFactory));
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests()
		
				.antMatchers("/swagger-ui.html").permitAll()
				
				.antMatchers("/test/**").permitAll()

				.antMatchers("/swagger-resources/**").permitAll()

				.antMatchers("/images/**").permitAll()

				.antMatchers("/webjars/**").permitAll()

				.antMatchers("/v2/api-docs").permitAll()

				.antMatchers("/configuration/ui").permitAll()

				.antMatchers("/configuration/security").permitAll()

				.anyRequest().authenticated();

	}

}