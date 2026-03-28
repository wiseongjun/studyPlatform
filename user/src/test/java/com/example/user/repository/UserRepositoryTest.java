package com.example.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import com.example.config.QueryDslConfig;
import com.example.user.dto.internal.AttemptWithAnswersDto;

@DataJpaTest
@Import({QueryDslConfig.class, UserRepository.class})
@Sql("/user-test-data.sql")
class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Test
	@DisplayName("userId 기준 풀었던 문제 ID 목록을 중복 없이 반환한다")
	void getSolvedProblemIds_byUser_returnsDistinctIds() {
		List<Long> ids = userRepository.getSolvedProblemIds(1L);

		assertThat(ids).containsExactlyInAnyOrder(10L, 20L, 30L);
	}

	@Test
	@DisplayName("userId + chapterId 기준 풀었던 문제 ID를 중복 없이 반환한다")
	void getSolvedProblemIds_byUserAndChapter_returnsDistinctIds() {
		List<Long> ids = userRepository.getSolvedProblemIds(1L, 1L);

		assertThat(ids).containsExactlyInAnyOrder(10L, 20L);
		assertThat(ids).doesNotContain(30L);
	}

	@Test
	@DisplayName("getLastAttemptDetail은 가장 최근 시도를 반환한다")
	void getLastAttemptDetail_returnsLatestAttempt() {
		AttemptWithAnswersDto result = userRepository.getLastAttemptDetail(1L, 10L);

		assertThat(result).isNotNull();
		assertThat(result.getAttemptId()).isEqualTo(2L);
	}

	@Test
	@DisplayName("getLastAttemptDetail은 선택지 답안을 포함해 반환한다")
	void getLastAttemptDetail_includesUserChoices() {
		AttemptWithAnswersDto result = userRepository.getLastAttemptDetail(1L, 20L);

		assertThat(result.getUserChoices()).containsExactlyInAnyOrder(1, 2);
		assertThat(result.getUserTextAnswer()).isNull();
	}

	@Test
	@DisplayName("getLastAttemptDetail은 주관식 답안을 포함해 반환한다")
	void getLastAttemptDetail_includesTextAnswer() {
		AttemptWithAnswersDto result = userRepository.getLastAttemptDetail(1L, 30L);

		assertThat(result.getUserTextAnswer()).isEqualTo("틀린 답");
		assertThat(result.getUserChoices()).isEmpty();
	}

	@Test
	@DisplayName("시도 기록이 없으면 null을 반환한다")
	void getLastAttemptDetail_noAttempt_returnsNull() {
		AttemptWithAnswersDto result = userRepository.getLastAttemptDetail(1L, 999L);

		assertThat(result).isNull();
	}
}
