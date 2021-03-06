package com.tianyoukeji.org.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.tianyoukeji.parent.service.FileUploadService;
import com.tianyoukeji.parent.service.NamespaceRedisService;
import com.tianyoukeji.parent.service.RateLimiterService;
import com.tianyoukeji.parent.service.SmsService;
import com.tianyoukeji.parent.service.TIMService;
import com.tianyoukeji.parent.service.ThumbnailProperities;

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
	public TIMService timService() {
		return new TIMService();
	}
	@Bean
	public RestTemplate restTemplate() {
	    return new RestTemplate();
	}
	@Bean
	public FileUploadService fileUploadService() {
	    return new FileUploadService();
	}
	@Bean
	public ThumbnailProperities thumbnailProperities() {
		return new ThumbnailProperities();
	}
}
