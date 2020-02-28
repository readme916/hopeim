package com.tianyoukeji.parent.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.tianyoukeji.parent.common.RedisUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

/**
 * RedisTemplate操作工具类
 *
 * @author ly
 * @version 1.0
 */
public class NamespaceRedisService {

	private RedisUtils redisUtils;
	
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@PostConstruct
	private void init() {
		this.redisUtils = new RedisUtils();
		this.redisUtils.setStringRedisTemplate(stringRedisTemplate);
		this.redisUtils.setRedisTemplate(redisTemplate);
	}

	
	/** -------------------key相关操作--------------------- */
	
	
	/**
	 * 删除key
	 * 
	 * @param key
	 */
	public void delete(RedisNamespace namespace , String key) {
		redisUtils.delete(namespace + ":" + key);
	}
	
	/**
	 * 批量删除key
	 * 
	 * @param keys
	 */
	public void delete(RedisNamespace namespace , Collection<String> keys) {
		List<String> collect = keys.stream().map(key -> namespace + ":" + key).collect(Collectors.toList());
		redisUtils.delete(collect);
	}
	
	/**
	 * key是否存在
	 *
	 * @param namespace
	 * @param key
	 */
	public boolean exists(RedisNamespace namespace, String key) {
		return redisUtils.hasKey(namespace + ":" + key);
	}
	
	/**
	 * 给一个指定的 key 值附加过期时间
	 *
	 * @param key
	 * @param time
	 * @return
	 */
	public boolean expire(RedisNamespace namespace, String key, long expire) {
		return redisUtils.expire(namespace + ":" + key, expire, TimeUnit.SECONDS);
	}

	
	/**
	 * 设置过期时间
	 * 
	 * @param key
	 * @param date
	 * @return
	 */
	public boolean expireAt(RedisNamespace namespace, String key, Date date) {
		return redisUtils.expireAt(namespace + ":" + key, date);
	}
	
	/**
	 * 返回 key 的剩余的过期时间
	 * 
	 * @param key
	 * @return
	 */
	public Long getExpire(RedisNamespace namespace , String key) {
		return redisUtils.getExpire(namespace + ":" + key);
	}
	
	/** -------------------string相关操作--------------------- */
	
	
	/**
	 * 读取缓存
	 *
	 * @param key
	 * @return
	 */
	public String get(RedisNamespace namespace, String key) {
		return redisUtils.get(namespace + ":" + key);
	}

	public Object getObject(RedisNamespace namespace, String key) {
		return redisUtils.getObject(namespace + ":" + key);
	}
	
	/**
	 * 写入缓存
	 *
	 * @param key
	 * @param value
	 * @param expire
	 */
	public void set(RedisNamespace namespace, String key, String value, long expire) {
		redisUtils.setEx(namespace + ":" + key, value, expire, TimeUnit.SECONDS);
	}
	
	public void setObject(RedisNamespace namespace, String key, Object value) {
		redisUtils.setObject(namespace + ":" + key, value);
	}
	
	/**
	 * 将给定 key 的值设为 value ，并返回 key 的旧值(old value)
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public String getAndSet(RedisNamespace namespace , String key, String value) {
		return redisUtils.getAndSet(namespace + ":" + key, value);
	}

	/**
	 * 批量获取
	 * 
	 * @param keys
	 * @return
	 */
	public List<String> multiGet(RedisNamespace namespace , Collection<String> keys) {
		return redisUtils.multiGet(keys.stream().map(key -> namespace + ":" + key).collect(Collectors.toList()));
	}

	
	/**
	 * 批量添加
	 * 
	 * @param maps
	 */
	public void multiSet(RedisNamespace namespace , Map<String, String> maps) {
		Map<String, String> collect = maps.keySet().stream().collect(Collectors.toMap(k -> namespace + ":" + k, k -> maps.get(k)));
		redisUtils.multiSet(collect);
	}
	
	/**
	 * 只有在 key 不存在时设置 key 的值
	 * 
	 * @param key
	 * @param value
	 * @return 之前已经存在返回false,不存在返回true
	 */
	public boolean setIfAbsent(RedisNamespace namespace , String key, String value) {
		return redisUtils.setIfAbsent(namespace + ":" + key, value);
	}

	
	/**
	 * 增加(自增长), 负数则为自减
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Long incrBy(RedisNamespace namespace , String key, long increment) {
		return redisUtils.incrBy(namespace + ":" + key, increment);
	}
	
	
	
	/** -------------------hash相关操作------------------------- */

	/**
	 * 获取存储在哈希表中指定字段的值
	 * 
	 * @param key
	 * @param field
	 * @return
	 */
	public Object hGet(RedisNamespace namespace , String key, String field) {
		return redisUtils.hGet(namespace + ":" + key, field);
	}
	
	/**
	 * 存储在哈希表中指定字段的值
	 * 
	 * @param key
	 * @param field
	 * @return
	 */
	public void hPut(RedisNamespace namespace , String key, String field, String value) {
		redisUtils.hPut(namespace + ":" + key, field, value);
	}
	/**
	 * 删除一个或多个哈希表字段
	 * 
	 * @param key
	 * @param fields
	 * @return
	 */
	public Long hDelete(RedisNamespace namespace , String key, Object... fields) {
		return redisUtils.hDelete(namespace + ":" + key, fields);
	}

	/**
	 * 查看哈希表 key 中，指定的字段是否存在
	 * 
	 * @param key
	 * @param field
	 * @return
	 */
	public boolean hExists(RedisNamespace namespace , String key, String field) {
		return redisUtils.hExists(namespace + ":" + key, field);
	}
	
	
	/** --------------------set相关操作-------------------------- */

	
	/**
	 * set list
	 * 
	 * @param key
	 * @return
	 */
	public Set<String> sMembers(RedisNamespace namespace , String key) {
		return redisUtils.sMembers(namespace + ":" + key);
	}
	
	/**
	 * set添加元素
	 * 
	 * @param key
	 * @param values
	 * @return
	 */
	public Long sAdd(RedisNamespace namespace , String key, String... values) {
		return redisUtils.sAdd(namespace + ":" + key, values);
	}

	/**
	 * set移除元素
	 * 
	 * @param key
	 * @param values
	 * @return
	 */
	public Long sRemove(RedisNamespace namespace , String key, Object... values) {
		return redisUtils.sRemove(namespace + ":" + key, values);
	}

	/**
	 * 移除并返回集合的一个随机元素
	 * 
	 * @param key
	 * @return
	 */
	public String sPop(RedisNamespace namespace , String key) {
		return redisUtils.sPop(namespace + ":" + key);
	}
	
	/**
	 * 判断集合是否包含value
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Boolean sIsMember(RedisNamespace namespace , String key, Object value) {
		return redisUtils.sIsMember(namespace + ":" + key, value);
	}
	
	public enum RedisNamespace {
		LOGIN_SMS,USER_TOKEN
	}

}