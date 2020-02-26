package com.tianyoukeji.platform.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tianyoukeji.parent.service.NamespaceRedisService;
import com.tianyoukeji.parent.service.RateLimiterService;
import com.tianyoukeji.parent.service.RegionService;
import com.tianyoukeji.parent.service.SmsService;

@Configuration
public class CommonServiceBean {
	
	@Bean
	public NamespaceRedisService redisSerivce() {
		return new NamespaceRedisService();
	}
	
	@Bean
	public RateLimiterService rateLimiterService() {
		return new RateLimiterService();
	}

	@Bean
	public SmsService smsService() {
		return new SmsService(new Object());
	}
	@Bean
	public RegionService regionService() {
		return new RegionService();
	}
}
