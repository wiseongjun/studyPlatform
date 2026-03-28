package com.example.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

public class RedisCacheErrorHandler implements CacheErrorHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheErrorHandler.class);

	@Override
	public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
		LOGGER.warn("Redis cache get 실패 — cache={}, key={}, message={}",
			cache.getName(), key, exception.getMessage());
	}

	@Override
	public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
		LOGGER.warn("Redis cache put 실패 — cache={}, key={}, message={}",
			cache.getName(), key, exception.getMessage());
	}

	@Override
	public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
		LOGGER.warn("Redis cache evict 실패 — cache={}, key={}, message={}",
			cache.getName(), key, exception.getMessage());
	}

	@Override
	public void handleCacheClearError(RuntimeException exception, Cache cache) {
		LOGGER.warn("Redis cache clear 실패 — cache={}, message={}", cache.getName(), exception.getMessage());
	}
}
