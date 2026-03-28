package com.example.problem.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.exception.CustomException;
import com.example.exception.ErrorCode;

class ProblemSelectorTest {

	private ProblemSelector problemSelector;

	@BeforeEach
	void setUp() {
		problemSelector = new ProblemSelector(new Random(42L));
	}

	@Test
	@DisplayName("제외 목록이 없으면 전체 문제 중 하나를 반환한다")
	void selectProblemId_withNoExclusion_returnsAnyProblem() {
		List<Long> problemIds = List.of(1L, 2L, 3L, 4L, 5L);

		Long selected = problemSelector.selectProblemId(problemIds, List.of());

		assertThat(selected).isIn(problemIds);
	}

	@Test
	@DisplayName("제외 목록을 제외한 문제 중 하나를 반환한다")
	void selectProblemId_withExclusion_returnsNonExcludedProblem() {
		List<Long> problemIds = List.of(1L, 2L, 3L, 4L, 5L);
		List<Long> excludedIds = List.of(1L, 2L, 3L);

		Long selected = problemSelector.selectProblemId(problemIds, excludedIds);

		assertThat(selected).isIn(4L, 5L);
	}

	@Test
	@DisplayName("제외 목록이 null이면 전체 문제 중 하나를 반환한다")
	void selectProblemId_withNullExclusion_returnsAnyProblem() {
		List<Long> problemIds = List.of(1L, 2L, 3L);

		Long selected = problemSelector.selectProblemId(problemIds, null);

		assertThat(selected).isIn(problemIds);
	}

	@Test
	@DisplayName("모든 문제가 제외되면 NO_PROBLEM_AVAILABLE 예외가 발생한다")
	void selectProblemId_allExcluded_throwsException() {
		List<Long> problemIds = List.of(1L, 2L, 3L);
		List<Long> excludedIds = List.of(1L, 2L, 3L);

		assertThatThrownBy(() -> problemSelector.selectProblemId(problemIds, excludedIds))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.NO_PROBLEM_AVAILABLE.getCode());
	}

	@Test
	@DisplayName("문제 목록이 비어있으면 NO_PROBLEM_AVAILABLE 예외가 발생한다")
	void selectProblemId_emptyProblemIds_throwsException() {
		assertThatThrownBy(() -> problemSelector.selectProblemId(List.of(), List.of()))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.NO_PROBLEM_AVAILABLE.getCode());
	}

	@Test
	@DisplayName("문제 목록이 null이면 NO_PROBLEM_AVAILABLE 예외가 발생한다")
	void selectProblemId_nullProblemIds_throwsException() {
		assertThatThrownBy(() -> problemSelector.selectProblemId(null, List.of()))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.NO_PROBLEM_AVAILABLE.getCode());
	}
}
