package com.tianyoukeji.parent.common;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ContextUtils {

	public static String getClientId() {

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		if (request != null) {
			Principal principal = request.getUserPrincipal();
			if (principal == null) {
				return "";
			} else {
				Map<String, Object> objectToMap = objectToMap(principal);
				Map oauth2Request = (Map) objectToMap.get("oauth2Request");
				if (oauth2Request != null) {
					Object clientId = oauth2Request.get("clientId");
					return clientId.toString();
				} else {
					return "";
				}
			}
		} else {
			return "";
		}
	}
	

//	public static String getCurrentUserName() {
//
//		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
//				.getRequest();
//		if (request != null) {
//			Principal principal = request.getUserPrincipal();
//			if (principal == null) {
//				return "";
//			} else {
//				return principal.getName();
//			}
//		} else {
//			return "";
//		}
//	}

	public static String getIPAddress() {

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();

		String ip = null;

		// X-Forwarded-For：Squid 服务代理
		String ipAddresses = request.getHeader("X-Forwarded-For");
		if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
			// Proxy-Client-IP：apache 服务代理
			ipAddresses = request.getHeader("Proxy-Client-IP");
		}
		if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
			// WL-Proxy-Client-IP：weblogic 服务代理
			ipAddresses = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
			// HTTP_CLIENT_IP：有些代理服务器
			ipAddresses = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
			// X-Real-IP：nginx服务代理
			ipAddresses = request.getHeader("X-Real-IP");
		}
		// 有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
		if (ipAddresses != null && ipAddresses.length() != 0) {
			ip = ipAddresses.split(",")[0];
		}
		// 还是不能获取到，最后再通过request.getRemoteAddr();获取
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
			ip = request.getRemoteAddr();
		}
		if("0:0:0:0:0:0:0:1".equals(ip) || "localhost".equals(ip)||"127.0.0.1".equals(ip)
				|| ip.startsWith("192.168.")||ip.startsWith("172.16.")||ip.startsWith("10.")) {
			return null;
		}
		return ip;
	}
	
	public static String objectToString(Object object) throws JsonProcessingException {

		ObjectMapper objectMapper = new ObjectMapper();
		String writeValueAsString = null;

		writeValueAsString = objectMapper.writeValueAsString(object);

		return writeValueAsString;
	}

	public static <T> T stringToObject(String jsonString, Class<T> clazz)
			throws JsonParseException, JsonMappingException, IOException {

		ObjectMapper objectMapper = new ObjectMapper();

		return objectMapper.readValue(jsonString, clazz);

	}

	public static <T> T mapToObject(Map<String, Object> map, Class<T> beanClass) throws IOException {
		if (map == null) {
			return null;
		}

		ObjectMapper objectMapper = new ObjectMapper();
		T obj = objectMapper.convertValue(map, beanClass);

		return obj;
	}

	public static Map<String, Object> objectToMap(Object obj) {
		if (obj == null) {
			return null;
		}

		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> mappedObject = objectMapper.convertValue(obj, Map.class);

		return mappedObject;
	}

	public static Map<String, Object> stringToMap(String str)
			throws JsonParseException, JsonMappingException, IOException {
		if (str == null) {
			return null;
		}

		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> mappedObject = null;

		mappedObject = objectMapper.readValue(str, Map.class);

		return mappedObject;
	}
	
	public static String randomInt(int width) {
		Random random = new Random();
		String result="";
		for (int i=0;i<width;i++)
		{
			result+=random.nextInt(10);

		}
		return result;
	}

}
	
	
