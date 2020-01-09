package com.tianyoukeji.oauth.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.utopia.tokensart.auth.service.authenticator.IntegrationAuthenticator;
import com.utopia.tokensart.common.modules.base.init.GlobalType;
import com.utopia.tokensart.common.modules.base.models.Org;
import com.utopia.tokensart.common.modules.base.models.OrgUser;
import com.utopia.tokensart.common.modules.base.models.OrgUser.State;
import com.utopia.tokensart.common.modules.base.models.Role;
import com.utopia.tokensart.common.modules.base.models.User;
import com.utopia.tokensart.common.modules.base.repository.OrgUserRepository;
import com.utopia.tokensart.common.modules.base.repository.RoleRepository;
import com.utopia.tokensart.common.modules.base.repository.UserRepository;
import com.utopia.tokensart.common.utils.CustomUserDetails;

@Service
public class IntegrationUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private OrgUserRepository orgUserRepository;

	private List<IntegrationAuthenticator> authenticators;

	@Autowired
	RedisConnectionFactory redisConnectionFactory;

	@Autowired(required = false)
	public void setIntegrationAuthenticators(List<IntegrationAuthenticator> authenticators) {
		this.authenticators = authenticators;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		// 入口如果是/oauth/token 则integrationAuthentication 不为空
		IntegrationAuthentication integrationAuthentication = IntegrationAuthenticationContext.get();

		// 判断是否是token点登录,如果不是从token点进入这里的，就是正常的从/login页面进入的 ，第三方登录都是从/login页面登录
		// ，则设置integrationAuthentication
		if (integrationAuthentication == null) {
			integrationAuthentication = new IntegrationAuthentication();
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
					.getRequest();
			if (request != null) {
				integrationAuthentication.setAuthType(request.getParameter("auth_type"));
				integrationAuthentication.setClientId(request.getParameter("client_id"));
				integrationAuthentication.setAuthParameters(request.getParameterMap());
				IntegrationAuthenticationContext.set(integrationAuthentication);
			}

		}

		// 如果是刷新token，则不需要认证
		if (!StringUtils.isEmpty(integrationAuthentication.getAuthParameter("refresh_token"))
				&& "refresh_token".equals(integrationAuthentication.getAuthParameter("grant_type"))) {

			RedisTokenStore redisTokenStore = new RedisTokenStore(redisConnectionFactory);
			OAuth2Authentication readAuthenticationForRefreshToken = redisTokenStore
					.readAuthenticationForRefreshToken(integrationAuthentication.getAuthParameter("refresh_token"));
			if (readAuthenticationForRefreshToken == null) {
				throw new UsernameNotFoundException("");
			} else {
				String un = readAuthenticationForRefreshToken.getName();
				User user = userRepository.findByUsername(un);
				return createUserDetails(integrationAuthentication.getClientId(),user.getUsername(), user, loadUserAuthorities(user));
			}
		//正常请求token
		}else {
			User user = this.authenticate(integrationAuthentication);
			if (user != null) {
				return createUserDetails(integrationAuthentication.getClientId(),user.getUsername(), user, loadUserAuthorities(user));
			} else {
				throw new UsernameNotFoundException("");
			}
		}

	}

	public UserDetails createUserDetails(String  clientId,String username, User user, List<GrantedAuthority> userAuthorities) {
		return new CustomUserDetails(clientId,user.getUuid(), username, user.getUserInfo().getPassword(), user.getEnabled(),
				userAuthorities);
	}

	protected List<GrantedAuthority> loadUserAuthorities(User user) {

		IntegrationAuthentication integrationAuthentication = IntegrationAuthenticationContext.get();
		String clientId = integrationAuthentication.getClientId();
		Set<GrantedAuthority> dbAuthsSet = new HashSet<>();

		if ("user-web".equals(clientId) || "user-app".equals(clientId)) {
			Role role = roleRepository.findFirstByClientGroupAndUsersUsername(GlobalType.ClientGroup.USER,
					user.getUsername());
			dbAuthsSet.add(new SimpleGrantedAuthority(role.getRoleCode().toString()));

		} else if ("org-web".equals(clientId) || "org-app".equals(clientId)) {
			Org currentOrg = user.getCurrentOrg();
			if (currentOrg == null) {
			} else {
				OrgUser findFirstByUserAndOrgAndState = orgUserRepository.findFirstByUserAndOrgAndState(user,
						currentOrg, State.ENABLED);
				String str = findFirstByUserAndOrgAndState.getRole().getRoleCode().toString();
				dbAuthsSet.add(new SimpleGrantedAuthority(str));
			}

		} else if ("platform-web".equals(clientId) || "platform-app".equals(clientId)) {
			Role role = roleRepository.findFirstByClientGroupAndUsersUsername(GlobalType.ClientGroup.PLATFORM,
					user.getUsername());
			dbAuthsSet.add(new SimpleGrantedAuthority(role.getRoleCode().toString()));
		}
		return new ArrayList<>(dbAuthsSet);
	}

	private User authenticate(IntegrationAuthentication integrationAuthentication) {
		if (this.authenticators != null) {
			for (IntegrationAuthenticator authenticator : authenticators) {
				if (authenticator.support(integrationAuthentication)) {
					try {
						return authenticator.authenticate(integrationAuthentication);
					} catch (Exception ex) {
						ex.printStackTrace();
						throw ex;
					}

				}
			}
		}
		return null;
	}

}