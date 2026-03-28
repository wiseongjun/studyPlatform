package com.example.problem.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.api.user.client.UserFeignClient;
import com.example.constants.AnswerType;
import com.example.constants.ProblemType;
import com.example.exception.CustomException;
import com.example.exception.ErrorCode;
import com.example.problem.dto.req.RandomProblemFilter;
import com.example.problem.dto.req.SubmitProblemRequest;
import com.example.problem.dto.res.ProblemResponse;
import com.example.problem.dto.res.SubmitProblemResponse;
import com.example.problem.entity.Problem;
import com.example.problem.repository.ProblemCacheRepository;
import com.example.problem.repository.ProblemRepository;
import com.example.problem.utils.ProblemSelector;

@ExtendWith(MockitoExtension.class)
class ProblemServiceTest {

	@InjectMocks
	private ProblemService problemService;

	@Mock
	private ProblemRepository problemRepository;
	@Mock
	private ProblemCacheRepository problemCacheRepository;
	@Mock
	private ProblemWriteService problemWriteService;
	@Mock
	private ProblemAsyncService problemAsyncService;
	@Mock
	private ProblemSelector problemSelector;
	@Mock
	private UserFeignClient userFeignClient;
	@Spy
	private ProblemApiMapper problemApiMapper = new ProblemApiMapper();

	@Mock
	private Problem problem;

	private void stubProblemResponse(Long problemId) {
		given(problem.getId()).willReturn(problemId);
		given(problem.getTitle()).willReturn("title");
		given(problem.getContent()).willReturn("content");
		given(problem.getType()).willReturn(ProblemType.SINGLE_CHOICE);
		given(problem.getChoiceTexts()).willReturn(List.of("A", "B"));
		given(problem.getCorrectRate()).willReturn(null);
	}

	private void stubSubmitDependencies(List<Integer> choices, String textAnswer, AnswerType result) {
		given(problemRepository.findByIdWithAnswers(1L)).willReturn(problem);
		given(problem.grade(choices, textAnswer)).willReturn(result);
		given(problem.getChapterId()).willReturn(1L);
		given(problem.getExplanation()).willReturn("해설");
		given(problem.getCorrectChoiceNumbers()).willReturn(List.of(2));
		given(problem.getCorrectTextAnswer()).willReturn(null);
	}

	@Nested
	@DisplayName("랜덤 문제 조회")
	class GetRandomProblem {

		@Test
		@DisplayName("lastSkipped가 없을 때 풀지 않은 문제를 랜덤으로 반환한다")
		void withNoLastSkipped_returnsRandomProblem() {
			RandomProblemFilter filter = new RandomProblemFilter(1L, 1L);
			List<Long> solvedIds = new ArrayList<>(List.of(2L, 3L));
			List<Long> problemIds = List.of(1L, 2L, 3L, 4L);

			given(problemCacheRepository.getLastSkippedId(1L, 1L)).willReturn(null);
			given(userFeignClient.getUserSolvedProblemIdList(1L, 1L)).willReturn(solvedIds);
			given(problemRepository.getProblemIds(1L)).willReturn(problemIds);
			given(problemSelector.selectProblemId(problemIds, solvedIds)).willReturn(1L);
			given(problemRepository.findByIdWithChoices(1L)).willReturn(problem);
			stubProblemResponse(1L);

			ProblemResponse response = problemService.getRandomProblem(filter);

			assertThat(response.getProblemId()).isEqualTo(1L);
			assertThat(response.getAnswerCorrectRate()).isNull();
			then(problemCacheRepository).should().saveLastSkippedId(1L, 1L, 1L);
		}

		@Test
		@DisplayName("lastSkipped가 있을 때 exclusionList에 포함되어 전달된다")
		void withLastSkipped_includesInExclusionList() {
			RandomProblemFilter filter = new RandomProblemFilter(1L, 1L);
			List<Long> solvedIds = new ArrayList<>(List.of(2L));
			List<Long> problemIds = List.of(1L, 2L, 3L, 4L);

			given(problemCacheRepository.getLastSkippedId(1L, 1L)).willReturn(3L);
			given(userFeignClient.getUserSolvedProblemIdList(1L, 1L)).willReturn(solvedIds);
			given(problemRepository.getProblemIds(1L)).willReturn(problemIds);
			given(problemSelector.selectProblemId(problemIds, List.of(2L, 3L))).willReturn(4L);
			given(problemRepository.findByIdWithChoices(4L)).willReturn(problem);
			stubProblemResponse(4L);

			problemService.getRandomProblem(filter);

			then(problemSelector).should().selectProblemId(problemIds, List.of(2L, 3L));
		}

		@Test
		@DisplayName("풀 수 있는 문제가 없으면 예외가 전파된다")
		void noCandidates_throwsException() {
			RandomProblemFilter filter = new RandomProblemFilter(1L, 1L);
			List<Long> solvedIds = new ArrayList<>(List.of(1L, 2L, 3L));
			List<Long> problemIds = List.of(1L, 2L, 3L);

			given(problemCacheRepository.getLastSkippedId(1L, 1L)).willReturn(null);
			given(userFeignClient.getUserSolvedProblemIdList(1L, 1L)).willReturn(solvedIds);
			given(problemRepository.getProblemIds(1L)).willReturn(problemIds);
			given(problemSelector.selectProblemId(any(), any()))
				.willThrow(new CustomException(ErrorCode.NO_PROBLEM_AVAILABLE));

			assertThatThrownBy(() -> problemService.getRandomProblem(filter))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.NO_PROBLEM_AVAILABLE.getCode());

			then(problemCacheRepository).should(never()).saveLastSkippedId(anyLong(), anyLong(), anyLong());
		}
	}

	@Nested
	@DisplayName("문제 제출")
	class SubmitProblem {

		@Test
		@DisplayName("정답이면 CORRECT와 해설을 반환한다")
		void correctAnswer_returnsCorrectResponse() {
			SubmitProblemRequest request = new SubmitProblemRequest(1L, List.of(2), null);
			stubSubmitDependencies(List.of(2), null, AnswerType.CORRECT);

			SubmitProblemResponse response = problemService.submitProblem(1L, request);

			assertThat(response.getAnswerStatus()).isEqualTo(AnswerType.CORRECT);
			assertThat(response.getExplanation()).isEqualTo("해설");
			then(problemWriteService).should().updateStatus(1L, AnswerType.CORRECT);
			then(problemAsyncService).should().saveAttempt(any());
		}

		@Test
		@DisplayName("오답이면 INCORRECT를 반환하고 상태를 업데이트한다")
		void incorrectAnswer_updatesStatus() {
			SubmitProblemRequest request = new SubmitProblemRequest(1L, List.of(1), null);
			stubSubmitDependencies(List.of(1), null, AnswerType.INCORRECT);

			SubmitProblemResponse response = problemService.submitProblem(1L, request);

			assertThat(response.getAnswerStatus()).isEqualTo(AnswerType.INCORRECT);
			then(problemWriteService).should().updateStatus(1L, AnswerType.INCORRECT);
		}

		@Test
		@DisplayName("부분 정답이면 PARTIAL_CORRECT를 반환하고 상태를 갱신한다")
		void partialCorrect_updatesStatus() {
			SubmitProblemRequest request = new SubmitProblemRequest(1L, List.of(1, 3), null);
			stubSubmitDependencies(List.of(1, 3), null, AnswerType.PARTIAL_CORRECT);
			given(problem.getCorrectChoiceNumbers()).willReturn(List.of(1, 2));

			SubmitProblemResponse response = problemService.submitProblem(1L, request);

			assertThat(response.getAnswerStatus()).isEqualTo(AnswerType.PARTIAL_CORRECT);
			then(problemWriteService).should().updateStatus(1L, AnswerType.PARTIAL_CORRECT);
			then(problemAsyncService).should().saveAttempt(any());
		}
	}
}
