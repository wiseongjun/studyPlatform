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
import com.example.problem.dto.internal.ProblemInfoDto;

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
	@DisplayName("totalAttempts가 30 미만이면 answerCorrectRate가 null이다")
	void getProblemInfoById_insufficientAttempts_rateIsNull() {
		ProblemInfoDto info = problemRepository.getProblemInfoById(1L);

		assertThat(info.getTotalAttempts()).isEqualTo(29);
		assertThat(info.getCorrectAttempts()).isEqualTo(20);
	}

	@Test
	@DisplayName("totalAttempts가 30 이상이면 시도 횟수와 정답 횟수를 반환한다")
	void getProblemInfoById_sufficientAttempts_returnsCounts() {
		ProblemInfoDto info = problemRepository.getProblemInfoById(2L);

		assertThat(info.getTotalAttempts()).isEqualTo(30);
		assertThat(info.getCorrectAttempts()).isEqualTo(20);
	}

	@Test
	@DisplayName("선택지가 choiceNumber 오름차순으로 반환된다")
	void getProblemInfoById_choicesOrderedByChoiceNumber() {
		ProblemInfoDto info = problemRepository.getProblemInfoById(1L);

		assertThat(info.getChoices()).containsExactly("Choice A", "Choice B", "Choice C");
	}

	@Test
	@DisplayName("존재하지 않는 문제 ID 조회 시 예외가 발생한다")
	void getProblemInfoById_notFound_throwsException() {
		assertThatThrownBy(() -> problemRepository.getProblemInfoById(999L))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.PROBLEM_NOT_FOUND.getCode());
	}
}
