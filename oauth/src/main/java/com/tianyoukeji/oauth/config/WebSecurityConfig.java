package com.tianyoukeji.oauth.config;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import com.utopia.tokensart.common.modules.base.repository.UserRepository;

@EnableWebSecurity
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private HttpServletRequest httpServletRequest;
    
    @Autowired
    private UserRepository userRepository;

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 覆盖了默认配置，不能删除
    	
        http.csrf().disable()
        .authorizeRequests()
        .mvcMatchers("/sendSMSCode","/site/list","/resetPassword","/logout/**","/unionLogin","/login","/oauth/token").permitAll()
        .anyRequest().authenticated()
        .and()
        .formLogin()
        .loginPage("/unionLogin")
        .loginProcessingUrl("/login").permitAll(); // 设置登录页面
        
    }


    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.jdbcAuthentication().passwordEncoder(passwordEncoder());
		super.configure(auth);

    }
    


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
               return PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(rawPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                String authType = httpServletRequest.getParameter("auth_type");
                if (!StringUtils.isEmpty(authType) && authType.equals("sms")&&"/oauth/token".equals(httpServletRequest.getRequestURI())) {
                    return true;
                }
               return PasswordEncoderFactories.createDelegatingPasswordEncoder().matches(rawPassword,encodedPassword);
            }
        };
    }
}
