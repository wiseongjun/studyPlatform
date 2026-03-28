package com.example.problem.domain;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ProblemHelperTest {

	@Nested
	@DisplayName("buildExclusionList")
	class BuildExclusionList {

		@Test
		@DisplayName("lastSkippedId가 null이면 solvedIds만 반환한다")
		void noLastSkipped() {
			List<Long> solvedIds = new ArrayList<>(List.of(1L, 2L));

			List<Long> result = ProblemHelper.combine(solvedIds, null);

			assertThat(result).containsExactly(1L, 2L);
		}

		@Test
		@DisplayName("lastSkippedId가 있으면 solvedIds에 추가되어 반환한다")
		void withLastSkipped() {
			List<Long> solvedIds = new ArrayList<>(List.of(1L, 2L));

			List<Long> result = ProblemHelper.combine(solvedIds, 3L);

			assertThat(result).containsExactly(1L, 2L, 3L);
		}

		@Test
		@DisplayName("원본 리스트를 변경하지 않는다")
		void doesNotMutateOriginal() {
			List<Long> solvedIds = new ArrayList<>(List.of(1L));

			ProblemHelper.combine(solvedIds, 2L);

			assertThat(solvedIds).containsExactly(1L);
		}
	}

	@Nested
	@DisplayName("calculateRate")
	class CalculateRate {

		@Test
		@DisplayName("totalAttempts가 30 이상이면 정답률을 반환한다")
		void returnsRateWhenEnoughAttempts() {
			Integer rate = ProblemHelper.calculateRate(30, 15);

			assertThat(rate).isEqualTo(50);
		}

		@Test
		@DisplayName("totalAttempts가 30 미만이면 null을 반환한다")
		void returnsNullWhenInsufficientAttempts() {
			Integer rate = ProblemHelper.calculateRate(29, 15);

			assertThat(rate).isNull();
		}

		@Test
		@DisplayName("totalAttempts가 0이면 null을 반환한다")
		void returnsNullWhenZeroAttempts() {
			Integer rate = ProblemHelper.calculateRate(0, 0);

			assertThat(rate).isNull();
		}
	}
}
