package com.tianyoukeji.oauth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import com.tianyoukeji.parent.common.AvatarUtils;
import com.tianyoukeji.parent.common.BusinessException;
import com.tianyoukeji.parent.entity.Menu;
import com.tianyoukeji.parent.entity.MenuRepository;
import com.tianyoukeji.parent.entity.Org;
import com.tianyoukeji.parent.entity.OrgRepository;
import com.tianyoukeji.parent.entity.Role;
import com.tianyoukeji.parent.entity.RoleRepository;
import com.tianyoukeji.parent.entity.User;
import com.tianyoukeji.parent.entity.Userinfo;
import com.tianyoukeji.parent.entity.UserinfoRepository;
import com.tianyoukeji.parent.entity.template.MenuTemplate;
import com.tianyoukeji.parent.entity.template.RoleTemplate;
import com.tianyoukeji.parent.entity.template.RoleTemplateRepository;
import com.tianyoukeji.parent.entity.UserRepository;
import com.tianyoukeji.parent.service.BaseService;
import com.tianyoukeji.parent.service.TIMService;
import com.tianyoukeji.parent.service.TIMService.ActionStatus;
import com.tianyoukeji.parent.service.TIMService.TIMResponse;

import java.util.*;

import javax.annotation.PostConstruct;

@Service
public class OauthUserService extends BaseService<User> {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserinfoRepository userInfoRepository;
	
	@Autowired
	private TIMService timService;

	@Override
	public void init() {
	}

	/**
	 * 简单注册
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	@Transactional
	public User registerUser(String username, String password, String role, Org org) {
		if (userRepository.findByUserinfoMobile(username) != null) {
			throw new BusinessException(1000, "用户名已存在");
		}
		
		User user = new User();
		user.setEnabled(true);
		Role r = roleRepository.findByCode(role);
		if (r == null) {
			throw new BusinessException(1973, "角色 " + role + " 不存在");
		}

		user.setRole(r);
		user.setOrg(org);
		save(user);

		// 根据id生成用户名
		String nick  = "用户" + gen(user.getUuid());
		user.setNickname(nick);
		Userinfo userInfo = new Userinfo();
		userInfo.setMobile(username);
		userInfo.setPassword(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(password));
		String md5 = DigestUtils.md5DigestAsHex(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(password).getBytes());
		user.setUnionId(md5);
		userInfoRepository.save(userInfo);

		user.setUserinfo(userInfo);
		user.setHeadimgurl(AvatarUtils.generatorUserAvatar(username));
		userRepository.save(user);
		
		TIMResponse registerUser = timService.registerUser(username, nick, AvatarUtils.generatorUserAvatar(username));
		if(registerUser.getActionStatus().equals(ActionStatus.FAIL)) {
			throw new BusinessException(4000, registerUser.getErrorInfo());
		}
		return user;
	}

	/**
	 * 随机字符串
	 */
	private static final char[] CHARS = new char[] { '6', 'j', 'a', 'y', '3', 'q', 'b', 'c', 'x', '5', 'e', 'f', 's',
			'7', 'g', 'h', 'v', 'i', '9', 'w', 'k', '4', 'm', 'n', 'd', 'p', 'r', 't', 'u', '2', '8', 'z' };

	private final static int CHARS_LENGTH = 32;
	/**
	 * 邀请码长度
	 */
	private final static int CODE_LENGTH = 6;

	/**
	 * 随机数据
	 */
	private final static long SLAT = 3396552L;

	/**
	 * PRIME1 与 CHARS 的长度 L互质，可保证 ( id * PRIME1) % L 在 [0,L)上均匀分布
	 */
	private final static int PRIME1 = 3;

	/**
	 * PRIME2 与 CODE_LENGTH 互质，可保证 ( index * PRIME2) % CODE_LENGTH 在
	 * [0，CODE_LENGTH）上均匀分布
	 */
	private final static int PRIME2 = 11;

	/**
	 * 生成邀请码
	 *
	 * @param id 唯一的id主键
	 * @return code
	 */
	private String gen(Long id) {
		// 补位
		id = id * PRIME1 + SLAT;
		// 将 id 转换成32进制的值
		long[] b = new long[CODE_LENGTH];
		// 32进制数
		b[0] = id;
		for (int i = 0; i < CODE_LENGTH - 1; i++) {
			b[i + 1] = b[i] / CHARS_LENGTH;
			// 按位扩散
			b[i] = (b[i] + i * b[0]) % CHARS_LENGTH;
		}
		b[5] = (b[0] + b[1] + b[2] + b[3] + b[4]) * PRIME1 % CHARS_LENGTH;

		// 进行混淆
		long[] codeIndexArray = new long[CODE_LENGTH];
		for (int i = 0; i < CODE_LENGTH; i++) {
			codeIndexArray[i] = b[i * PRIME2 % CODE_LENGTH];
		}

		StringBuilder buffer = new StringBuilder();
		Arrays.stream(codeIndexArray).boxed().map(Long::intValue).map(t -> CHARS[t]).forEach(buffer::append);
		return buffer.toString();
	}
}
