package com.example.problem.repository;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProblemCacheRepository {

	private static final String LAST_SKIPPED_KEY = "lastSkipped:%d:%d"; // userId:chapterId
	private static final Duration LAST_SKIPPED_TTL = Duration.ofHours(1);

	private final RedisTemplate<String, Object> redisTemplate;

	public Long getLastSkippedId(Long userId, Long chapterId) {
		String key = String.format(LAST_SKIPPED_KEY, userId, chapterId);
		Object value = redisTemplate.opsForValue().get(key);
		return value != null ? Long.parseLong(value.toString()) : null;
	}

	public void saveLastSkippedId(Long userId, Long chapterId, Long problemId) {
		String key = String.format(LAST_SKIPPED_KEY, userId, chapterId);
		redisTemplate.opsForValue().set(key, problemId, LAST_SKIPPED_TTL);
	}
}
