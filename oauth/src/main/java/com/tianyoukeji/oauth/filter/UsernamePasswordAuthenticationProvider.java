package com.tianyoukeji.oauth.filter;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.RateLimiter;
import com.tianyoukeji.oauth.service.OauthUserService;
import com.tianyoukeji.parent.entity.User;
import com.tianyoukeji.parent.entity.UserRepository;
import com.tianyoukeji.parent.service.NamespaceRedisService;
import com.tianyoukeji.parent.service.NamespaceRedisService.RedisNamespace;
import com.tianyoukeji.parent.service.RateLimiterService;
import com.tianyoukeji.parent.service.RateLimiterService.RateLimiterNamespace;

@Component
public class UsernamePasswordAuthenticationProvider implements AuthenticationProvider,UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Resource
	private HttpServletRequest request;

	@Autowired
	private NamespaceRedisService redisService;

	@Autowired
	private OauthUserService oauthUserService;

	@Autowired
	private RateLimiterService rateLimiterService;
	
	@Autowired
	private RedisConnectionFactory redisConnectionFactory;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = (authentication.getPrincipal() == null) ? "" : authentication.getName();
		String password = (String) authentication.getCredentials();
		String type = request.getParameter("type");

		if (type == null || "".equals(username)) {
			throw new BadCredentialsException("类型不能为空");
		}
		if (username == null || "".equals(username)) {
			throw new BadCredentialsException("手机号不能为空");
		}

		if (password == null || "".equals(password)) {
			throw new BadCredentialsException("密码不能为空");
		}

		// 每个手机号码，登录的频率限制5秒一次
		RateLimiter rateLimiter = rateLimiterService.get(RateLimiterNamespace.LOGIN, username, 0.2);
		rateLimiter.acquire();

		User user = null;
		if (type.equals("pwd")) {
			user = userRepository.findByUserinfoMobile(username);
			if (null == user) {
				throw new BadCredentialsException("用户不存在");
			}
			if (user.getEnabled() == false) {
				throw new BadCredentialsException("用户被禁用");
			}
			if (!PasswordEncoderFactories.createDelegatingPasswordEncoder().matches(password,
					user.getUserinfo().getPassword())) {
				throw new BadCredentialsException("用户名或密码不正确");
			}

		} else if (type.equals("sms")) {
			if (!redisService.exists(RedisNamespace.LOGIN_SMS, username)) {
				throw new BadCredentialsException("验证码不正确");
			}
			String number = redisService.get(RedisNamespace.LOGIN_SMS, username);
			if (!number.equals(password)) {
				throw new BadCredentialsException("验证码不正确");
			}
			user = userRepository.findByUserinfoMobile(username);
			if (null == user) {
				// 这里自动注册
				user = oauthUserService.registerUser(username, password, "user");
			} else if (user.getEnabled() == false) {
				throw new BadCredentialsException("用户被禁用");
			}

		} else {
			throw new BadCredentialsException("不支持的type");
		}

		UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(username, password,
				listUserGrantedAuthorities(user));
		result.setDetails(authentication.getDetails());
		return result;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
	}

	private Set<GrantedAuthority> listUserGrantedAuthorities(User user) {
		Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
		if (user == null) {
			return authorities;
		}
		authorities.add(new SimpleGrantedAuthority(user.getRole().getCode()));
		return authorities;
	}

	/**
	 * 刷新token的时候才会调用这里
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		RedisTokenStore redisTokenStore = new RedisTokenStore(redisConnectionFactory);
		OAuth2Authentication readAuthenticationForRefreshToken = redisTokenStore
				.readAuthenticationForRefreshToken(request.getParameter("refresh_token"));
		if (readAuthenticationForRefreshToken == null) {
			throw new UsernameNotFoundException("");
		} else {
			String un = readAuthenticationForRefreshToken.getName();
			User user = userRepository.findByUserinfoMobile(un);
			// 每个手机号码，刷新token的频率限制100秒一次
			RateLimiter rateLimiter = rateLimiterService.get(RateLimiterNamespace.FRESH_TOKEN, un, 0.01);
			rateLimiter.acquire();
			return new org.springframework.security.core.userdetails.User(un, "", user.getEnabled(), true, true, true, listUserGrantedAuthorities(user));
		}
	}

}
