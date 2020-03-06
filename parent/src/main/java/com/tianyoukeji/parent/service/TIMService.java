package com.tianyoukeji.parent.service;

import java.util.HashMap;
import java.util.Random;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tencentyun.TLSSigAPIv2;

public class TIMService {
	final static Logger logger = LoggerFactory.getLogger(TIMService.class);
	
	@Value("${SDKAppID}")
	private String SDKAppID;
	
	@Value("${SDKAPPSecret}")
	private String SDKAPPSecret;
	
	private TLSSigAPIv2 api;
	
	private String adminSig;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@PostConstruct
	private void init() {
		api = new TLSSigAPIv2(Long.valueOf(SDKAppID), SDKAPPSecret);
		adminSig = api.genSig("administrator", 500*86400);
	}
	
	/**
	 * 	生成用户签名
	 * @param username
	 * @return
	 */
	public String genSig(String username) {
		return api.genSig(username, 500*86400);
	}
	
	/**
	 * 注册app用户到tim
	 * @param username
	 * @return
	 */
	public TIMResponse registerUser(String username , String nick , String faceUrl) {
		Random random = new Random();
		String nextInt = String.valueOf(random.nextInt());
		String url = "https://console.tim.qq.com/v4/im_open_login_svc/account_import?sdkappid="+SDKAppID+"&identifier=administrator&usersig="+adminSig+"&random="+nextInt+"&contenttype=json";
		HashMap<String,String> hashMap = new HashMap<String,String>();
		hashMap.put("Identifier", username);
		hashMap.put("Nick", nick);
		hashMap.put("FaceUrl", faceUrl);
		TIMResponse postForObject = restTemplate.postForObject(url, hashMap, TIMResponse.class);
		return postForObject;
	}
	
	public static class TIMResponse{
		@JsonProperty("ActionStatus")
		private ActionStatus actionStatus;
		@JsonProperty("ErrorInfo")
		private String errorInfo;
		@JsonProperty("ErrorCode")
		private String errorCode;
		
		public ActionStatus getActionStatus() {
			return actionStatus;
		}
		public void setActionStatus(ActionStatus actionStatus) {
			this.actionStatus = actionStatus;
		}
		public String getErrorInfo() {
			return errorInfo;
		}
		public void setErrorInfo(String errorInfo) {
			this.errorInfo = errorInfo;
		}
		public String getErrorCode() {
			return errorCode;
		}
		public void setErrorCode(String errorCode) {
			this.errorCode = errorCode;
		}
		
	}
	public enum ActionStatus{
		OK,FAIL
	}
}
