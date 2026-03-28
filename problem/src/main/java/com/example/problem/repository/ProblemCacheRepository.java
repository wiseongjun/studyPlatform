package com.example.problem.repository;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProblemCacheRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProblemCacheRepository.class);

	private static final String LAST_SKIPPED_KEY = "lastSkipped:%d:%d"; // userId:chapterId
	private static final Duration LAST_SKIPPED_TTL = Duration.ofHours(1);

	private final RedisTemplate<String, Object> redisTemplate;

	public Long getLastSkippedId(Long userId, Long chapterId) {
		String key = String.format(LAST_SKIPPED_KEY, userId, chapterId);
		try {
			Object value = redisTemplate.opsForValue().get(key);
			return value != null ? Long.parseLong(value.toString()) : null;
		} catch (Exception e) {
			LOGGER.warn("Redis get 실패(lastSkipped) — userId={}, chapterId={}, message={}",
				userId, chapterId, e.getMessage());
			return null;
		}
	}

	public void saveLastSkippedId(Long userId, Long chapterId, Long problemId) {
		String key = String.format(LAST_SKIPPED_KEY, userId, chapterId);
		try {
			redisTemplate.opsForValue().set(key, problemId, LAST_SKIPPED_TTL);
		} catch (Exception e) {
			LOGGER.warn("Redis set 실패(lastSkipped) — userId={}, chapterId={}, message={}",
				userId, chapterId, e.getMessage());
		}
	}
}
