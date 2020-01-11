package com.tianyoukeji.parent.service;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.util.concurrent.RateLimiter;
import com.tianyoukeji.parent.common.ContextUtils;
import com.tianyoukeji.parent.service.RateLimiterService.RateLimiterNamespace;
import com.tianyoukeji.parent.service.RedisService.RedisNamespace;

/**
 * RedisTemplate操作工具类
 *
 * @author lh
 * @version 3.0
 * @since 2016-8-29
 */
public final class SmsService {
	
	final static Logger logger = LoggerFactory.getLogger(SmsService.class);

	private String templates[] = {"您的注册码：{number}" , "您的申请已通过，请查看！" , "不知道都是什么{number},{username}你知道么"};
	
	/**
	 * 短信提供商的driver
	 */
	private Object driver;
	
	
	@Autowired 
	private RateLimiterService rateLimiterService;
	
	@Autowired 
	private RedisService redisService;
	
	public SmsService(Object driver) {
		this.driver = driver;
	}
	
	
	/**
	 * 验证码登录时候发送的验证码
	 * @param mobile
	 * @return
	 */
	
	public String getLoginSms(final String mobile) {
		HashMap<String,String> hashMap = new HashMap<String,String>();
		String number = ContextUtils.randomInt(6);
		hashMap.put("number", number);
		_send(mobile,0, hashMap);
		logger.info(mobile +" 验证码登录," + "验证码是：" + number);
		/**
		 * 发送过后存到redis中10分钟，等待校验
		 */
		redisService.set(RedisNamespace.LOGIN_SMS, mobile, number, 600);
		return number;
	}
	
	
	
	/**
	 * 内部方法根据模板和参数，给手机发送短信
	 * @param mobile
	 * @param template
	 * @param params
	 * @return
	 */
	private  boolean  _send(final String mobile, final int template , HashMap<String,String> params) {
		
		
		//每个手机号码，发送短信的频率限制10秒一次
		RateLimiter rateLimiter = rateLimiterService.get(RateLimiterNamespace.SMS, mobile, 0.05);
		rateLimiter.acquire();
		
		//driver.send(mobile,template,params);
		
		return true;
	}
	

}