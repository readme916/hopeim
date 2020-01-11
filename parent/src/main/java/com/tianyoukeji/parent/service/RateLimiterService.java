package com.tianyoukeji.parent.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.RateLimiter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * RedisTemplate操作工具类
 *
 * @author lh
 * @version 3.0
 * @since 2016-8-29
 */
public final class RateLimiterService {

	private static HashMap<String,RateLimiter> rateLimiters = new HashMap();
	

	/**
	 * 取得限流器
	 * @param namespace 业务名
	 * @param key  每个用户名独立限流，如果对业务限流可以为空字符串
	 * @param rate   每分钟几个请求
	 * @return RateLimiter
	 */
	public  RateLimiter  get(final RateLimiterNamespace namespace, final String key ,final double rate) {
		if(rateLimiters.containsKey(namespace + ":" + key)) {
			return rateLimiters.get(namespace + ":" + key);
		}else {
			rateLimiters.put(namespace + ":" + key, RateLimiter.create(rate));
			return  rateLimiters.get(namespace + ":" + key);
		}
	}
	
	public enum RateLimiterNamespace{
		SMS,LOGIN
	}
}