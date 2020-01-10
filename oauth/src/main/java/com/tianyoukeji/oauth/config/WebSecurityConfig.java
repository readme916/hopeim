package com.tianyoukeji.oauth.config;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;

import com.tianyoukeji.oauth.filter.MobileSmsAuthenticationProcessingFilter;
import com.tianyoukeji.oauth.filter.MobileSmsAuthenticationProvider;
import com.tianyoukeji.oauth.filter.UsernamePasswordAuthenticationProvider;


@EnableWebSecurity
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    @Qualifier("authenticationManagerBean")
    private AuthenticationManager authenticationManager;

    
    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    
    @Bean
    public MobileSmsAuthenticationProcessingFilter mobileSmsAuthenticationProcessingFilter() {
    	MobileSmsAuthenticationProcessingFilter filter = new MobileSmsAuthenticationProcessingFilter();
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }
    
    @Bean
    public UsernamePasswordAuthenticationProvider usernamePasswordAuthenticationProvider() {
        return new UsernamePasswordAuthenticationProvider();
    }
    
    @Bean
    public MobileSmsAuthenticationProvider mobileSmsAuthenticationProvider() {
        return new MobileSmsAuthenticationProvider();
    }
    
    
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .authenticationProvider(mobileSmsAuthenticationProvider())
            .authenticationProvider(usernamePasswordAuthenticationProvider());
    }
    
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 覆盖了默认配置，不能删除
        http.csrf().disable()
        .authorizeRequests()
        .mvcMatchers("/**").permitAll()
        .and()
        .formLogin()
        .loginPage("/unionLogin").loginProcessingUrl("/login").permitAll().and()
        .logout()
        .permitAll();
        http.addFilterAfter(mobileSmsAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class);; // 设置登录页面
    }
}
