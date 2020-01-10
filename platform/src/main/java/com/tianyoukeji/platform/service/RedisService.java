package com.tianyoukeji.platform.service;

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
@Component
public final class RedisService {

	private final RedisTemplate<String, Object> redisTemplate;

	@Autowired
	public RedisService(RedisConnectionFactory connectionFactory) {
		this.redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);
		redisTemplate.afterPropertiesSet();
	}

	/**
	 * 写入缓存
	 *
	 * @param key
	 * @param value
	 * @param expire
	 */
	public void set(final String namespace, final String key, final Object value, final long expire) {
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
	public <T> T get(final String namespace, final String key, Class<T> clazz) {
		Object result = redisTemplate.boundValueOps(namespace + ":" + key).get();
		if (result == null) {
			return null;
		}
		return (T) result;
	}

	/**
	 * 读取缓存
	 *
	 * @param key
	 * @return
	 */
	public Object getObj(final String namespace, final String key) {
		return redisTemplate.boundValueOps(namespace + ":" + key).get();
	}

	/**
	 * 删除，根据key精确匹配
	 *
	 * @param key
	 */
	public void del(final String namespace, final String... key) {
		redisTemplate.delete(Arrays.stream(key).map(k -> namespace + ":" + k).collect(Collectors.toList()));
	}

	/**
	 * 批量删除，根据key模糊匹配
	 *
	 * @param pattern
	 */
	public void delpn(final String namespace, final String... pattern) {
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
	public boolean exists(final String namespace, final String key) {
		return redisTemplate.hasKey(namespace + ":" + key);
	}

}