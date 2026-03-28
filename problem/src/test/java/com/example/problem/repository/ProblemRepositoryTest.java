package com.example.problem.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import com.example.config.QueryDslConfig;
import com.example.exception.CustomException;
import com.example.exception.ErrorCode;
import com.example.problem.entity.Problem;

@DataJpaTest
@Import({QueryDslConfig.class, ProblemRepository.class})
@Sql("/test-data.sql")
class ProblemRepositoryTest {

	@Autowired
	private ProblemRepository problemRepository;

	@Test
	@DisplayName("chapterId 기준으로 삭제되지 않은 문제 ID 목록을 반환한다")
	void getProblemIds_returnsNonDeletedProblems() {
		List<Long> ids = problemRepository.getProblemIds(1L);

		assertThat(ids).containsExactlyInAnyOrder(1L, 2L);
	}

	@Test
	@DisplayName("다른 챕터의 문제는 포함되지 않는다")
	void getProblemIds_excludesOtherChapterProblems() {
		List<Long> ids = problemRepository.getProblemIds(1L);

		assertThat(ids).doesNotContain(4L);
	}

	@Test
	@DisplayName("삭제된 문제는 ID 목록에서 제외된다")
	void getProblemIds_excludesDeletedProblems() {
		List<Long> ids = problemRepository.getProblemIds(1L);

		assertThat(ids).doesNotContain(3L);
	}

	@Test
	@DisplayName("findByIdWithChoices — 선택지가 choiceNumber 오름차순으로 반환된다")
	void findByIdWithChoices_choicesOrderedByChoiceNumber() {
		Problem problem = problemRepository.findByIdWithChoices(1L);

		assertThat(problem.getChoiceTexts()).containsExactly("Choice A", "Choice B", "Choice C");
	}

	@Test
	@DisplayName("findByIdWithChoices — status도 함께 조회된다")
	void findByIdWithChoices_includesStatus() {
		Problem problem = problemRepository.findByIdWithChoices(1L);

		assertThat(problem.getProblemStatus()).isNotNull();
		assertThat(problem.getProblemStatus().getTotalAttempts()).isEqualTo(29);
	}

	@Test
	@DisplayName("findByIdWithChoices — 존재하지 않는 문제 ID 조회 시 예외가 발생한다")
	void findByIdWithChoices_notFound_throwsException() {
		assertThatThrownBy(() -> problemRepository.findByIdWithChoices(999L))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.PROBLEM_NOT_FOUND.getCode());
	}

	@Test
	@DisplayName("findByIdWithAnswers — 정답 데이터가 조회된다")
	void findByIdWithAnswers_returnsAnswers() {
		Problem problem = problemRepository.findByIdWithAnswers(1L);

		assertThat(problem.getCorrectChoiceNumbers()).containsExactly(1);
	}

	@Test
	@DisplayName("findByIdWithAnswers — 존재하지 않는 문제 ID 조회 시 예외가 발생한다")
	void findByIdWithAnswers_notFound_throwsException() {
		assertThatThrownBy(() -> problemRepository.findByIdWithAnswers(999L))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.PROBLEM_NOT_FOUND.getCode());
	}

	@Test
	@DisplayName("findByIdWithAnswersAndStatus — 정답과 status가 모두 조회된다")
	void findByIdWithAnswersAndStatus_returnsBoth() {
		Problem problem = problemRepository.findByIdWithAnswersAndStatus(2L);

		assertThat(problem.getCorrectChoiceNumbers()).containsExactlyInAnyOrder(1, 2);
		assertThat(problem.getProblemStatus()).isNotNull();
		assertThat(problem.getProblemStatus().getTotalAttempts()).isEqualTo(30);
	}

	@Test
	@DisplayName("findByIdsWithChoices — ID 목록이 비어 있으면 빈 리스트")
	void findByIdsWithChoices_emptyList_returnsEmpty() {
		assertThat(problemRepository.findByIdsWithChoices(List.of())).isEmpty();
	}

	@Test
	@DisplayName("findByIdsWithChoices — ID 목록이 null이면 빈 리스트")
	void findByIdsWithChoices_null_returnsEmpty() {
		assertThat(problemRepository.findByIdsWithChoices(null)).isEmpty();
	}

	@Test
	@DisplayName("findByChapterIdWithChoices — 삭제되지 않은 문제만 반환된다")
	void findByChapterIdWithChoices_returnsNonDeletedWithChoices() {
		List<Problem> problems = problemRepository.findByChapterIdWithChoices(1L);

		assertThat(problems).hasSize(2);
		assertThat(problems).extracting(Problem::getId).containsExactlyInAnyOrder(1L, 2L);
	}
}
