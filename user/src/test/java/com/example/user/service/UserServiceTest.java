package com.example.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.api.problem.client.ProblemFeignClient;
import com.example.api.problem.dto.ProblemDetailResponse;
import com.example.api.problem.dto.ProblemSummaryResponse;
import com.example.constants.AnswerType;
import com.example.constants.ProblemType;
import com.example.exception.CustomException;
import com.example.exception.ErrorCode;
import com.example.user.dto.internal.AttemptWithAnswersDto;
import com.example.user.dto.res.AttemptDetailResponse;
import com.example.user.dto.res.SolvedProblemResponse;
import com.example.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@InjectMocks
	private UserService userService;

	@Mock
	private UserRepository userRepository;
	@Mock
	private ProblemFeignClient problemFeignClient;

	@Test
	@DisplayName("풀었던 문제 상세 조회 시 시도 정보와 문제 정보를 합쳐 반환한다")
	void getSolvedProblemDetail_returnsCombinedResponse() {
		AttemptWithAnswersDto attempt = new AttemptWithAnswersDto(
			1L, 1L, 10L, AnswerType.CORRECT, List.of(2), null, LocalDateTime.now()
		);
		ProblemDetailResponse problem = new ProblemDetailResponse(
			10L, "제목", "내용", "해설", ProblemType.SINGLE_CHOICE, List.of(2), null, 67
		);

		given(userRepository.getLastAttemptDetail(1L, 10L)).willReturn(attempt);
		given(problemFeignClient.getProblemDetail(10L)).willReturn(problem);

		AttemptDetailResponse response = userService.getSolvedProblemDetail(10L, 1L);

		assertThat(response.getProblemId()).isEqualTo(10L);
		assertThat(response.getAnswerType()).isEqualTo(AnswerType.CORRECT);
		assertThat(response.getUserChoices()).containsExactly(2);
		assertThat(response.getAnswerCorrectRate()).isEqualTo(67);
	}

	@Test
	@DisplayName("시도 기록이 없으면 ENTITY_NOT_FOUND 예외가 발생한다")
	void getSolvedProblemDetail_noAttempt_throwsException() {
		given(userRepository.getLastAttemptDetail(1L, 10L)).willReturn(null);

		assertThatThrownBy(() -> userService.getSolvedProblemDetail(10L, 1L))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.ENTITY_NOT_FOUND.getCode());
	}

	@Test
	@DisplayName("문제를 풀지 않은 경우 빈 목록을 반환한다")
	void getSolvedProblemList_noSolvedProblems_returnsEmpty() {
		given(userRepository.getSolvedProblemIds(1L)).willReturn(List.of());

		List<SolvedProblemResponse> result = userService.getSolvedProblemList(1L);

		assertThat(result).isEmpty();
		then(problemFeignClient).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("풀었던 문제 목록을 반환한다")
	void getSolvedProblemList_returnsMappedResponses() {
		ProblemSummaryResponse summary = new ProblemSummaryResponse(
			10L, "제목", "내용", ProblemType.SINGLE_CHOICE, List.of("A", "B"), null
		);

		given(userRepository.getSolvedProblemIds(1L)).willReturn(List.of(10L));
		given(problemFeignClient.getProblemsIncludingDeleted(List.of(10L))).willReturn(List.of(summary));

		List<SolvedProblemResponse> result = userService.getSolvedProblemList(1L);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getProblemId()).isEqualTo(10L);
		assertThat(result.get(0).getAnswerCorrectRate()).isNull();
	}

	@Test
	@DisplayName("문제 제출 시 attempt와 answer가 모두 저장된다")
	void saveAttempt_savesAttemptAndAnswers() {
		com.example.api.user.dto.SaveAttemptRequest request = new com.example.api.user.dto.SaveAttemptRequest(
			1L, 10L, 1L, AnswerType.CORRECT, List.of(2), null
		);
		given(userRepository.saveAttempt(any())).willReturn(1L);

		userService.saveAttempt(request);

		then(userRepository).should().saveAttempt(any());
		then(userRepository).should().saveUserAnswer(any());
	}
}
