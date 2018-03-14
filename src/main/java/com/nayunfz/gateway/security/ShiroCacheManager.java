package com.nayunfz.gateway.security;

import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.data.redis.core.RedisTemplate;

public class ShiroCacheManager extends AbstractCacheManager {

    private RedisTemplate<String, byte[]> redisTemplate;
    private int cacheTimeout;

    @Override
    protected Cache createCache(String s) throws CacheException {
        return new ShiroCache("shiro_cache:", redisTemplate, cacheTimeout);
    }

    public RedisTemplate<String, byte[]> getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate<String, byte[]> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public int getCacheTimeout() {
        return cacheTimeout;
    }

    public void setCacheTimeout(int cacheTimeout) {
        this.cacheTimeout = cacheTimeout;
    }
}
