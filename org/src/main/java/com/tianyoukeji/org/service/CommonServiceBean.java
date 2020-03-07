package com.tianyoukeji.org.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.tianyoukeji.parent.service.NamespaceRedisService;
import com.tianyoukeji.parent.service.RateLimiterService;
import com.tianyoukeji.parent.service.SmsService;
import com.tianyoukeji.parent.service.TIMService;

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
//	@Bean
//	public RegionService regionService() {
//		return new RegionService();
//	}

	
	@Bean
	public TIMService timService() {
		return new TIMService();
	}
	@Bean
	public RestTemplate restTemplate() {
	    return new RestTemplate();
	}
}
