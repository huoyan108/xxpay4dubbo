package org.xxpay.dubbo.service.redis;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

/**
 * redis的配置文件
 * 
 * @author qiyu
 * @date 2017年12月1日 下午7:40:18
 * @version 1.0
 */
@Component
public class RedisService {

	@Autowired
	StringRedisTemplate stringRedisTemplate;

	@Resource(name = "stringRedisTemplate")
	ValueOperations<String, String> valOpsStr;

	@Autowired
	RedisTemplate<Object, Object> redisTemplate;

	@Resource(name = "redisTemplate")
	ValueOperations<Object, Object> valOpsObj;

	/**
	 * 根据key拿value
	 * 
	 * @param key
	 * @return
	 */
	public String getStr(String key) {
		return valOpsStr.get(key);
	}

	/**
	 * 设置缓存
	 * 
	 * @param key
	 * @param val
	 */
	public void setStar(String key, String value) {
		valOpsStr.set(key, value);
	}

	/**
	 * 删除key
	 * 
	 * @param key
	 */
	public void delKey(String key) {
		stringRedisTemplate.delete(key);
	}

	/**
	 * 根据类获取Object
	 * 
	 * @param obj
	 * @return
	 */
	public Object getObj(Object obj) {
		return valOpsObj.get(obj);
	}

	/**
	 * 设置缓存
	 * 
	 * @param objOne
	 * @param objTwo
	 */
	public void setObj(Object objKey, Object objVal) {
		valOpsObj.set(objKey, objVal);
	}

	/**
	 * 删除缓存类
	 * 
	 * @param obj
	 */
	public void delObj(Object obj) {
		redisTemplate.delete(obj);
	}

	/**
	 * 缓存对象时间,时间单位:秒
	 * 
	 * @param key
	 * @param value
	 * @param expireTime
	 * @return
	 */
	public boolean setExpire(final String key, Object value, Long expireTime) {
		boolean result = false;
		try {
			ValueOperations<Object, Object> oprations = redisTemplate.opsForValue();
			oprations.set(key, value);
			redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
			result = true;
		} catch (Exception e) {
			new RuntimeException("缓存失败,key为:" + key + ",对象为:" + value + ",时间为:" + expireTime);
		}
		return result;
	}

	/**
	 * 拿到存活时间
	 * 
	 * @param key
	 * @return
	 */
	public String getTTL(final String key) {
			Long time = redisTemplate.getExpire(key);
		return time.toString();
	};

	/**
	 * 判断缓存中是否有对应的value
	 * 
	 * @param key
	 * @return
	 */
	public boolean exists(final String key) {
		return redisTemplate.hasKey(key);
	}

	/**
	 * 批量删除key
	 * 
	 * @param pattern
	 */
	public void removePattern(final String pattern) {
		Set<Object> keys = redisTemplate.keys(pattern);
		if (keys.size() > 0)
			redisTemplate.delete(keys);
	}

	/**
	 * 哈希 添加
	 * 
	 * @param key
	 * @param hashKey
	 * @param value
	 */
	public void hmSet(String key, Object hashKey, Object value) {
		HashOperations<Object, Object, Object> hash = redisTemplate.opsForHash();
		hash.put(key, hashKey, value);
	}

	/**
	 * 哈希获取数据
	 * 
	 * @param key
	 * @param hashKey
	 * @return
	 */
	public Object hmGet(String key, Object hashKey) {
		HashOperations<Object, Object, Object> hash = redisTemplate.opsForHash();
		return hash.get(key, hashKey);
	}

	/**
	 * 列表添加
	 * 
	 * @param k
	 * @param v
	 */
	public void lPush(String k, Object v) {
		ListOperations<Object, Object> list = redisTemplate.opsForList();
		list.rightPush(k, v);
	}

	/**
	 * 列表获取
	 * 
	 * @param k
	 * @param l
	 * @param l1
	 * @return
	 */
	public List<Object> lRange(String k, long l, long l1) {
		ListOperations<Object, Object> list = redisTemplate.opsForList();
		return list.range(k, l, l1);
	}

	/**
	 * 集合添加
	 * 
	 * @param key
	 * @param value
	 */
	public void add(String key, Object value) {
		SetOperations<Object, Object> set = redisTemplate.opsForSet();
		set.add(key, value);
	}

	/**
	 * 集合获取
	 * 
	 * @param key
	 * @return
	 */
	public Set<Object> setMembers(String key) {
		SetOperations<Object, Object> set = redisTemplate.opsForSet();
		return set.members(key);
	}

	/**
	 * 有序集合添加
	 * 
	 * @param key
	 * @param value
	 * @param scoure
	 */
	public void zAdd(String key, Object value, double scoure) {
		ZSetOperations<Object, Object> zset = redisTemplate.opsForZSet();
		zset.add(key, value, scoure);
	}

	/**
	 * 有序集合获取
	 * 
	 * @param key
	 * @param scoure
	 * @param scoure1
	 * @return
	 */
	public Set<Object> rangeByScore(String key, double scoure, double scoure1) {
		ZSetOperations<Object, Object> zset = redisTemplate.opsForZSet();
		return zset.rangeByScore(key, scoure, scoure1);
	}

}
