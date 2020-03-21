package com.tianyoukeji.parent.common;

import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ContextUtils {

	private static final String REGEX_MOBILE ="((\\+86|0086)?\\s*)((134[0-8]\\d{7})|(((13([0-3]|[5-9]))|(14[5-9])|15([0-3]|[5-9])|(16(2|[5-7]))|17([0-3]|[5-8])|18[0-9]|19(1|[8-9]))\\d{8})|(14(0|1|4)0\\d{7})|(1740([0-5]|[6-9]|[10-12])\\d{7}))";

	
	/**
     * 判断是否是手机号
     * @param tel 手机号
     * @return boolean true:是  false:否
     */
    public static boolean isMobile(String tel) {
        if (StringUtils.isEmpty(tel)){ return false;}
        return Pattern.matches(REGEX_MOBILE, tel);
    }
	
    
    
    /**
     * 取得客户端client_id
     * @return
     */
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
	
	  /**
	 *	取得当前用户角色，没有登录默认为user
     * @return
     */
	public static String getRole() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		if (request != null) {
			Principal principal = request.getUserPrincipal();
			if (principal == null) {
				return "user";
			} else {
				Map<String, Object> objectToMap = objectToMap(principal);
				List authorities = (List) objectToMap.get("authorities");
				if (authorities != null && !authorities.isEmpty()) {
					Map role = (Map)authorities.get(0);
					return role.get("authority").toString();
				} else {
					return "user";
				}
			}
		} else {
			return "user";
		}
	}
	

	/**
	 * 取得当前登录用户名
	 * @return
	 */
	public static String getCurrentUserName() {

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		if (request != null) {
			Principal principal = request.getUserPrincipal();
			if (principal == null) {
				return "";
			} else {
				return principal.getName();
			}
		} else {
			return "";
		}
	}

	
	/**
	 * 获取当前登录人的ip
	 * @return
	 */
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
	
	/**
	 * 随机一个数字型字符串
	 * @param width 长度
	 * @return
	 */
	public static String randomInt(int width) {
		Random random = new Random();
		String result="";
		for (int i=0;i<width;i++)
		{
			result+=random.nextInt(10);

		}
		return result;
	}
	
	/**
	 * 	获取一个bean对象的属性列表，属性的值为null
	 * @param source
	 * @return
	 */
	public static String[] getNullPropertyNames(Object source) {
		final BeanWrapper src = new BeanWrapperImpl(source);
		java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

		Set<String> emptyNames = new HashSet<String>();
		for (java.beans.PropertyDescriptor pd : pds) {
			Object srcValue = src.getPropertyValue(pd.getName());
			if (srcValue == null)
				emptyNames.add(pd.getName());
		}
		String[] result = new String[emptyNames.size()];
		return emptyNames.toArray(result);
	}

	/**
	 * 	获取一个bean对象的属性列表，属性的值不为null
	 * @param source
	 * @return
	 */
	public static Set<String> getNotNullPropertyNames(Object source) {
		final BeanWrapper src = new BeanWrapperImpl(source);
		java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();
		Set<String> notEmptyNames = new HashSet<String>();
		for (java.beans.PropertyDescriptor pd : pds) {
			Object srcValue = src.getPropertyValue(pd.getName());
			if (!pd.getName().equals("class") && srcValue != null)
				notEmptyNames.add(pd.getName());
		}
		return notEmptyNames;
	}

	
	/**
	 * 	把一个对象copy到目标对象，只复制非null的属性
	 * @param src
	 * @param target
	 */
	public static void copyPropertiesIgnoreNull(Object src, Object target) {
		BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
	}
	
	
	/**
	 * 	根据keyset，copy对象到目标对象
	 * @param src
	 * @param target
	 * @param keySet
	 */
	public static void copyPropertiesWithKeys(Object src, Object target,Set<String> keySet) {

		final BeanWrapper source = new BeanWrapperImpl(src);
		java.beans.PropertyDescriptor[] pds = source.getPropertyDescriptors();
		Set<String> kes = new HashSet<String>();
		for (java.beans.PropertyDescriptor pd : pds) {
			Object srcValue = source.getPropertyValue(pd.getName());
			if(keySet.contains(pd.getName())&&srcValue!=null){
				continue;
			}
			kes.add(pd.getName());
		}
		BeanUtils.copyProperties(src, target, kes.toArray(new String[0]));
	}
	
	public static boolean isPackageClassObject(Object obj) {
		try {
			return ((Class<?>) obj.getClass().getField("TYPE").get(null)).isPrimitive();
		} catch (Exception e) {
			return false;
		}
	}

	
	/**
	 * 	一个类是否是包装类型，例如Long，Integer等
	 * @param cls
	 * @return
	 */
	public static boolean isPackageClass(Class<?> cls) {
		try {
			return ((Class<?>) cls.getField("TYPE").get(null)).isPrimitive();
		} catch (Exception e) {
			return false;
		}
	}
}
	
	
