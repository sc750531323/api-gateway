package com.nayunfz.gateway.security;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.SerializationUtils;

import java.util.*;

/**
 * 缓存 role permission，调用链：
 * Subject.hasRole() / @RequiredRole*注解 --> realm.doGetAuthorizationInfo()方法时，先调用父抽象类AuthorizingRealm，先从cache中拿，
 * 如果cache中没有，则调用自定义的realm.doGetAuthorizationInfo()，然后cache.put(key, info);其中key为定义的cache name + username，info为AuthorizationInfo，它包含了role和permission
 */
public class ShiroCache<K, V> implements Cache<K, V> {

    private String name;        //cache名字，redis key 前缀
    private RedisTemplate<String, byte[]> redisTemplate;
    private int cacheTimeout;

    public ShiroCache(String name, RedisTemplate<String, byte[]> redisTemplate, int cacheTimeout){
        this.name = name;
        this.redisTemplate = redisTemplate;
        this.cacheTimeout = cacheTimeout;
    }

    private String getKey(K k){
        return name + String.valueOf(k);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(K k) throws CacheException {
        try {
            byte[] value = redisTemplate.opsForValue().get((getKey(k)).getBytes());
            return (V) SerializationUtils.deserialize(value);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public V put(K k, V v) throws CacheException {
        try {
            redisTemplate.opsForValue().set(getKey(k), SerializationUtils.serialize(v), cacheTimeout);
        }catch (Exception e){
            e.printStackTrace();
        }
        return v;
    }

    @Override
    public V remove(K k) throws CacheException {
        try {
            V oldV = get(k);
            redisTemplate.delete(getKey(k));
            return oldV;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void clear() throws CacheException {
        Set<K> keys = keys();
        Set<String> bkeys = Collections.synchronizedSet(new HashSet<String>());
        for (K key : keys){
            bkeys.add((String) key);
        }
        redisTemplate.delete(bkeys);
    }

    @Override
    public int size() {
        return keys().size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<K> keys() {
        Set<K> r = Collections.synchronizedSet(new HashSet<K>());
        Set<String> keys = redisTemplate.keys((name + "*"));
        for (String key : keys){
            try {
                r.add((K) (key));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return r;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<V> values() {
        List<byte[]> values = redisTemplate.opsForValue().multiGet((Collection<String>) keys());
        Set<V> vs = new HashSet<>();
        for (byte[] v : values){
            try {
                vs.add((V) SerializationUtils.deserialize(v));
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return  vs;
    }
}
