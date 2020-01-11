package com.tianyoukeji.parent.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * RedisTemplate操作工具类
 *
 * @author lh
 * @version 3.0
 * @since 2016-8-29
 */
public final class RedisService {
	
	@Autowired
	private  RedisTemplate<String, String> redisTemplate;

//	@Autowired
//	public RedisService(RedisConnectionFactory connectionFactory) {
//		this.redisTemplate = new RedisTemplate<String,String>();
//		redisTemplate.setConnectionFactory(connectionFactory);
//		redisTemplate.afterPropertiesSet();
//	}

	/**
	 * 写入缓存
	 *
	 * @param key
	 * @param value
	 * @param expire
	 */
	public void set(final RedisNamespace namespace, final String key, final String value, final long expire) {
		redisTemplate.opsForValue().set(namespace + ":" + key, value, expire, TimeUnit.SECONDS);
	}

	/**
	 * 读取缓存
	 *
	 * @param key
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String get(final RedisNamespace namespace, final String key) {
		String result = redisTemplate.boundValueOps(namespace + ":" + key).get();
		return result;
	}


	/**
	 * 删除，根据key精确匹配
	 *
	 * @param key
	 */
	public void del(final RedisNamespace namespace, final String... key) {
		redisTemplate.delete(Arrays.stream(key).map(k -> namespace + ":" + k).collect(Collectors.toList()));
	}

	/**
	 * 批量删除，根据key模糊匹配
	 *
	 * @param pattern
	 */
	public void delpn(final RedisNamespace namespace, final String... pattern) {
		for (String kp : pattern) {
			redisTemplate.delete(redisTemplate.keys(namespace + ":" + kp + "*"));
		}
	}

	/**
	 * key是否存在
	 *
	 * @param namespace
	 * @param key
	 */
	public boolean exists(final RedisNamespace namespace, final String key) {
		return redisTemplate.hasKey(namespace + ":" + key);
	}
	
	public enum  RedisNamespace{
		LOGIN_SMS
	}

}