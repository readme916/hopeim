package com.tianyoukeji.oauth.filter;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class MobileSmsAuthenticationProvider implements AuthenticationProvider{
	
	@Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String mobile = (authentication.getPrincipal() == null) ? "NONE_PROVIDED" : authentication.getName();
        String sms = (String) authentication.getCredentials();
//        if (sms == null || sms.equals("")) {
//            throw new BadCredentialsException("验证码不能为空");
//        }
//        if (!"13999990000".equals(mobile)) {
//            throw new BadCredentialsException("用户不存在");
//        }
//        // 手机号验证码业务还没有开发，先用4个0验证
//        if (!sms.equals("0000")) {
//            throw new BadCredentialsException("验证码不正确");
//        }
        MobileSmsAuthenticationToken result = new MobileSmsAuthenticationToken(mobile,
                listUserGrantedAuthorities(mobile));
        result.setDetails(authentication.getDetails());
        return result;
    }

    @Override
    public boolean supports(Class<?> authentication) {
    	 System.out.println(this.getClass().getName() + "---supports");
        return (MobileSmsAuthenticationToken.class.isAssignableFrom(authentication));
    }

    private Set<GrantedAuthority> listUserGrantedAuthorities(String username) {
        Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        if (username == null) {
            return authorities;
        }
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        return authorities;
    }
}
