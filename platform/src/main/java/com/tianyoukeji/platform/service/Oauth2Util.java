package com.tianyoukeji.platform.service;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.token.store.redis.JdkSerializationStrategy;
import org.springframework.stereotype.Component;

@Component
public class Oauth2Util {
	@Autowired
	private RedisConnectionFactory connectionFactory;

	public static Oauth2Util autowiredStatic;

	@PostConstruct
	public void init() {
		autowiredStatic = this;
		autowiredStatic.connectionFactory = this.connectionFactory;
	}

	private static JdkSerializationStrategy serializationStrategy = new JdkSerializationStrategy();

	/**
	 * 根据username，client_id和scope获取该用户的auth_to_access的key值
	 *
	 * @param name
	 * @param client_id
	 * @param scope
	 * @return
	 */
	public static String getAuthToAccess(String name, String client_id, String scope) {
		String tempdd = null;
		Map<String, String> values = new LinkedHashMap();
		values.put("username", name.toUpperCase());
		values.put("client_id", client_id);
		values.put("scope", OAuth2Utils.formatParameterList(new TreeSet(Collections.singleton(scope))));
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] bytes = digest.digest(values.toString().getBytes("UTF-8"));
			tempdd = String.format("%032x", new BigInteger(1, bytes));
		} catch (NoSuchAlgorithmException var4) {
			throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).", var4);
		} catch (UnsupportedEncodingException var5) {
			throw new IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).", var5);
		}
		return tempdd;
	}

	/**
	 * 清理指定账号的token，让其token强行删除，无法访问。
	 *
	 * @param name
	 * @param client_id
	 * @param scope
	 */
	public static void removeTokenAccess(String name, String client_id, String scope) {

		// 首先获取auth_to_access的key
		String key = getAuthToAccess(name, client_id, scope);
		// 再通过key获取到当前的令牌信息
		byte[] serializedKey7 = serializationStrategy.serialize("auth_to_access:" + key);

		RedisConnection conn = autowiredStatic.connectionFactory.getConnection();
		byte[] bytes = null;
		try {
			bytes = conn.get(serializedKey7);
			conn.del(serializedKey7);

		} finally {
			conn.close();
		}
		OAuth2AccessToken accessToken = serializationStrategy.deserialize(bytes, OAuth2AccessToken.class);
		if (accessToken == null) {
			return;
		}
		// 再删除令牌信息
		byte[] serializedKey = serializationStrategy.serialize("access:" + accessToken.getValue());
		byte[] serializedKey2 = serializationStrategy.serialize("access_to_refresh:" + accessToken.getValue());
		byte[] serializedKey3 = serializationStrategy.serialize("auth:" + accessToken.getValue());
		byte[] serializedKey4 = serializationStrategy.serialize("refresh:" + accessToken.getRefreshToken());
		byte[] serializedKey5 = serializationStrategy.serialize("refresh_auth:" + accessToken.getRefreshToken());
		byte[] serializedKey6 = serializationStrategy.serialize("refresh_to_access:" + accessToken.getRefreshToken());
		conn = autowiredStatic.connectionFactory.getConnection();
		try {
			conn.del(serializedKey7);
			conn.del(serializedKey);
			conn.del(serializedKey2);
			conn.del(serializedKey3);
			conn.del(serializedKey4);
			conn.del(serializedKey5);
			conn.del(serializedKey6);
		} finally {
			conn.close();
		}
	}
}