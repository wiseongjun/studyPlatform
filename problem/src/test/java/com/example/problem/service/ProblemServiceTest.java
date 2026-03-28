package com.example.problem.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.api.problem.dto.ProblemSummaryResponse;
import com.example.api.user.client.UserFeignClient;
import com.example.constants.AnswerType;
import com.example.constants.ProblemType;
import com.example.exception.CustomException;
import com.example.exception.ErrorCode;
import com.example.problem.domain.ProblemSelector;
import com.example.problem.dto.internal.ProblemAnswerDto;
import com.example.problem.dto.internal.ProblemInfoDto;
import com.example.problem.dto.req.RandomProblemFilter;
import com.example.problem.dto.req.SubmitProblemRequest;
import com.example.problem.dto.res.ProblemResponse;
import com.example.problem.dto.res.SubmitProblemResponse;
import com.example.problem.repository.ProblemCacheRepository;
import com.example.problem.repository.ProblemRepository;

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

	@Test
	@DisplayName("lastSkipped가 없을 때 풀지 않은 문제를 랜덤으로 반환한다")
	void getRandomProblem_withNoLastSkipped_returnsRandomProblem() {
		RandomProblemFilter filter = new RandomProblemFilter(1L, 1L);
		List<Long> solvedIds = new ArrayList<>(List.of(2L, 3L));
		List<Long> problemIds = List.of(1L, 2L, 3L, 4L);
		ProblemInfoDto info = new ProblemInfoDto(1L, "title", "content", ProblemType.SINGLE_CHOICE, List.of("A", "B"),
			0, 0);

		given(problemCacheRepository.getLastSkippedId(1L, 1L)).willReturn(null);
		given(userFeignClient.getUserSolvedProblemIdList(1L, 1L)).willReturn(solvedIds);
		given(problemRepository.getProblemIds(1L)).willReturn(problemIds);
		given(problemSelector.selectProblemId(problemIds, solvedIds)).willReturn(1L);
		given(problemRepository.getProblemInfoById(1L)).willReturn(info);

		ProblemResponse response = problemService.getRandomProblem(filter);

		assertThat(response.getProblemId()).isEqualTo(1L);
		assertThat(response.getAnswerCorrectRate()).isNull();
		then(problemCacheRepository).should().saveLastSkippedId(1L, 1L, 1L);
	}

	@Test
	@DisplayName("lastSkipped가 있을 때 exclusionList에 포함되어 전달된다")
	void getRandomProblem_withLastSkipped_includesInExclusionList() {
		RandomProblemFilter filter = new RandomProblemFilter(1L, 1L);
		List<Long> solvedIds = new ArrayList<>(List.of(2L));
		List<Long> problemIds = List.of(1L, 2L, 3L, 4L);
		ProblemInfoDto info = new ProblemInfoDto(4L, "title", "content", ProblemType.SINGLE_CHOICE, List.of("A", "B"),
			0, 0);

		given(problemCacheRepository.getLastSkippedId(1L, 1L)).willReturn(3L);
		given(userFeignClient.getUserSolvedProblemIdList(1L, 1L)).willReturn(solvedIds);
		given(problemRepository.getProblemIds(1L)).willReturn(problemIds);
		given(problemSelector.selectProblemId(problemIds, List.of(2L, 3L))).willReturn(4L);
		given(problemRepository.getProblemInfoById(4L)).willReturn(info);

		problemService.getRandomProblem(filter);

		then(problemSelector).should().selectProblemId(problemIds, List.of(2L, 3L));
	}

	@Test
	@DisplayName("풀 수 있는 문제가 없으면 예외가 전파된다")
	void getRandomProblem_noCandidates_throwsException() {
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

	@Test
	@DisplayName("totalAttempts가 30 이상이면 정답률이 계산되어 반환된다")
	void getRandomProblem_withEnoughAttempts_returnsCalculatedRate() {
		RandomProblemFilter filter = new RandomProblemFilter(1L, 1L);
		ProblemInfoDto info = new ProblemInfoDto(1L, "title", "content", ProblemType.SINGLE_CHOICE, List.of(), 30, 20);

		given(problemCacheRepository.getLastSkippedId(1L, 1L)).willReturn(null);
		given(userFeignClient.getUserSolvedProblemIdList(1L, 1L)).willReturn(new ArrayList<>());
		given(problemRepository.getProblemIds(1L)).willReturn(List.of(1L));
		given(problemSelector.selectProblemId(any(), any())).willReturn(1L);
		given(problemRepository.getProblemInfoById(1L)).willReturn(info);

		ProblemResponse response = problemService.getRandomProblem(filter);

		assertThat(response.getAnswerCorrectRate()).isEqualTo(67);
	}

	@Test
	@DisplayName("문제 제출 시 정답이면 CORRECT와 해설을 반환한다")
	void submitProblem_correctAnswer_returnsCorrectResponse() {
		SubmitProblemRequest request = new SubmitProblemRequest(1L, 1L, ProblemType.SINGLE_CHOICE, List.of(2), null);
		ProblemAnswerDto answerDto = new ProblemAnswerDto(1L, ProblemType.SINGLE_CHOICE, List.of(2), null, "해설입니다");

		given(problemRepository.getProblemAnswer(1L)).willReturn(answerDto);

		SubmitProblemResponse response = problemService.submitProblem(1L, request);

		assertThat(response.getAnswerStatus()).isEqualTo(AnswerType.CORRECT);
		assertThat(response.getExplanation()).isEqualTo("해설입니다");
		then(problemWriteService).should().updateStatus(1L, AnswerType.CORRECT);
		then(problemAsyncService).should().saveAttempt(any());
	}

	@Test
	@DisplayName("문제 제출 시 오답이면 INCORRECT를 반환하고 상태를 업데이트한다")
	void submitProblem_incorrectAnswer_updatesStatus() {
		SubmitProblemRequest request = new SubmitProblemRequest(1L, 1L, ProblemType.SINGLE_CHOICE, List.of(1), null);
		ProblemAnswerDto answerDto = new ProblemAnswerDto(1L, ProblemType.SINGLE_CHOICE, List.of(2), null, "해설입니다");

		given(problemRepository.getProblemAnswer(1L)).willReturn(answerDto);

		SubmitProblemResponse response = problemService.submitProblem(1L, request);

		assertThat(response.getAnswerStatus()).isEqualTo(AnswerType.INCORRECT);
		then(problemWriteService).should().updateStatus(1L, AnswerType.INCORRECT);
	}

	@Test
	@DisplayName("totalAttempts가 30 미만이면 정답률이 null로 반환된다")
	void getRandomProblem_withInsufficientAttempts_returnsNullRate() {
		RandomProblemFilter filter = new RandomProblemFilter(1L, 1L);
		ProblemInfoDto info = new ProblemInfoDto(1L, "title", "content", ProblemType.SINGLE_CHOICE, List.of(), 29, 20);

		given(problemCacheRepository.getLastSkippedId(1L, 1L)).willReturn(null);
		given(userFeignClient.getUserSolvedProblemIdList(1L, 1L)).willReturn(new ArrayList<>());
		given(problemRepository.getProblemIds(1L)).willReturn(List.of(1L));
		given(problemSelector.selectProblemId(any(), any())).willReturn(1L);
		given(problemRepository.getProblemInfoById(1L)).willReturn(info);

		ProblemResponse response = problemService.getRandomProblem(filter);

		assertThat(response.getAnswerCorrectRate()).isNull();
	}

	@Test
	@DisplayName("문제 제출 시 부분 정답이면 PARTIAL_CORRECT를 반환하고 상태를 갱신한다")
	void submitProblem_partialCorrect_updatesStatus() {
		SubmitProblemRequest request = new SubmitProblemRequest(1L, 1L, ProblemType.MULTI_CHOICE, List.of(1, 3), null);
		ProblemAnswerDto answerDto = new ProblemAnswerDto(1L, ProblemType.MULTI_CHOICE, List.of(1, 2), null, "해설");

		given(problemRepository.getProblemAnswer(1L)).willReturn(answerDto);

		SubmitProblemResponse response = problemService.submitProblem(1L, request);

		assertThat(response.getAnswerStatus()).isEqualTo(AnswerType.PARTIAL_CORRECT);
		then(problemWriteService).should().updateStatus(1L, AnswerType.PARTIAL_CORRECT);
		then(problemAsyncService).should().saveAttempt(any());
	}

	@Test
	@DisplayName("챕터별 문제 목록이 비어 있으면 빈 리스트를 반환한다")
	void getProblemListByChapter_empty_returnsEmptyList() {
		given(problemRepository.findByChapterId(9L)).willReturn(List.of());

		List<ProblemResponse> result = problemService.getProblemListByChapter(9L);

		assertThat(result).isEmpty();
		then(problemRepository).should(never()).getChoicesMap(anyList());
	}

	@Test
	@DisplayName("getProblemsIncludingDeleted — problemIds가 비어 있으면 빈 목록, IN 조회 호출 없음")
	void getProblemsIncludingDeleted_empty_skipsRepository() {
		List<ProblemSummaryResponse> result = problemService.getProblemsIncludingDeleted(List.of());

		assertThat(result).isEmpty();
		then(problemRepository).should(never()).findByIds(anyList());
		then(problemRepository).should(never()).getChoicesMap(anyList());
	}

	@Test
	@DisplayName("getProblemsIncludingDeleted — problemIds가 null이면 빈 목록, IN 조회 호출 없음")
	void getProblemsIncludingDeleted_null_skipsRepository() {
		List<ProblemSummaryResponse> result = problemService.getProblemsIncludingDeleted(null);

		assertThat(result).isEmpty();
		then(problemRepository).should(never()).findByIds(anyList());
		then(problemRepository).should(never()).getChoicesMap(anyList());
	}
}
