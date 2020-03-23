package com.tianyoukeji.oauth.config;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tianyoukeji.oauth.filter.UsernamePasswordAuthenticationProvider;
import com.tianyoukeji.parent.service.NamespaceRedisService;
import com.tianyoukeji.parent.service.NamespaceRedisService.RedisNamespace;

@EnableWebSecurity
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UsernamePasswordAuthenticationProvider usernamePasswordAuthenticationProvider;

	@Autowired
	private NamespaceRedisService redisService;

	@Autowired
	private RedisConnectionFactory redisConnectionFactory;

	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(usernamePasswordAuthenticationProvider);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// 覆盖了默认配置，不能删除
		http.csrf().disable().authorizeRequests()
		
				.antMatchers("/swagger-ui.html").permitAll()

				.antMatchers("/swagger-resources/**").permitAll()

				.antMatchers("/images/**").permitAll()

				.antMatchers("/webjars/**").permitAll()

				.antMatchers("/v2/api-docs").permitAll()

				.antMatchers("/configuration/ui").permitAll()

				.antMatchers("/configuration/security").permitAll()

				.antMatchers("/unionLogin", "/login", "/oauth/authorize", "/oauth/token", "/logout", "/sms","/structure","/structure/*").permitAll()

				.anyRequest().authenticated().and().formLogin().loginPage("/unionLogin").loginProcessingUrl("/login")
				
				.failureHandler(new SimpleAuthenticationFailureHandler()).permitAll().and().logout().permitAll();
	}
	public class SimpleAuthenticationFailureHandler implements AuthenticationFailureHandler {
		@Override
		public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
				AuthenticationException exception) throws IOException, ServletException {

			HashMap<String, String> map = new HashMap<>(3);
			map.put("status", "401");
			map.put("error", "登录失败");
			map.put("msg", exception.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setCharacterEncoding("utf-8");
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			ObjectMapper objectMapper = new ObjectMapper();
			String resBody = objectMapper.writeValueAsString(map);
			PrintWriter printWriter = response.getWriter();
			printWriter.print(resBody);
			printWriter.flush();
			printWriter.close();

		}
	}
}
