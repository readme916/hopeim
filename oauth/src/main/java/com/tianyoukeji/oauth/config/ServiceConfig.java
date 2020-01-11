package com.tianyoukeji.oauth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.tianyoukeji.parent.service.RateLimiterService;
import com.tianyoukeji.parent.service.RedisService;
import com.tianyoukeji.parent.service.SmsService;

@Configuration
public class ServiceConfig {
	
	@Bean
	public RedisService redisSerivce() {
		return new RedisService();
	}
	
	@Bean
	public RateLimiterService rateLimiterService() {
		return new RateLimiterService();
	}

	@Bean
	public SmsService smsService() {
		return new SmsService(new Object());
	}
}
