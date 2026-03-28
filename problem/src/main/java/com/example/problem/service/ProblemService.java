package com.example.problem.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.example.api.problem.dto.ProblemDetailResponse;
import com.example.api.problem.dto.ProblemSummaryResponse;
import com.example.api.user.client.UserFeignClient;
import com.example.api.user.dto.SaveAttemptRequest;
import com.example.constants.AnswerType;
import com.example.problem.domain.ProblemMarker;
import com.example.problem.domain.ProblemSelector;
import com.example.problem.dto.internal.ProblemAnswerDto;
import com.example.problem.dto.internal.ProblemDetailDto;
import com.example.problem.dto.req.RandomProblemFilter;
import com.example.problem.dto.req.SubmitProblemRequest;
import com.example.problem.dto.res.ProblemResponse;
import com.example.problem.dto.res.SubmitProblemResponse;
import com.example.problem.repository.ProblemCacheRepository;
import com.example.problem.repository.ProblemRepository;

@Service
@RequiredArgsConstructor
public class ProblemService {

	private final ProblemRepository problemRepository;
	private final ProblemCacheRepository problemCacheRepository;
	private final ProblemWriteService problemWriteService;
	private final UserFeignClient userFeignClient;

	public ProblemResponse getRandomProblem(RandomProblemFilter filter) {
		Long lastSkippedId = problemCacheRepository.getLastSkippedId(filter.getUserId(), filter.getChapterId());

		List<Long> solvedIds = userFeignClient.getUserSolvedProblemIdList(filter.getUserId(), filter.getChapterId());
		if (lastSkippedId != null) {
			solvedIds.add(lastSkippedId);
		}

		List<Long> problemIds = problemRepository.getProblemIds(filter.getChapterId());
		Long id = ProblemSelector.selectProblemId(problemIds, solvedIds);

		problemCacheRepository.saveLastSkippedId(filter.getUserId(), filter.getChapterId(), id);

		return problemRepository.getProblemInfoById(id);
	}

	public SubmitProblemResponse submitProblem(Long problemId, SubmitProblemRequest userAnswer) {
		ProblemAnswerDto problemAnswerDto = problemRepository.getProblemAnswer(problemId);

		AnswerType answerType = ProblemMarker.mark(userAnswer, problemAnswerDto);

		problemWriteService.updateStatus(problemId, answerType);

		userFeignClient.saveAttempt(new SaveAttemptRequest(
			userAnswer.getUserId(),
			problemId,
			userAnswer.getChapterId(),
			answerType,
			userAnswer.getSelectedChoices(),
			userAnswer.getTextAnswer()
		));

		return new SubmitProblemResponse(
			problemId,
			answerType,
			problemAnswerDto.getExplanation(),
			problemAnswerDto.getCorrectChoices(),
			problemAnswerDto.getCorrectTextAnswer()
		);
	}

	public List<ProblemSummaryResponse> getProblemsIncludingDeleted(List<Long> problemIds) {
		return problemRepository.getProblemsIncludingDeleted(problemIds);
	}

	public List<ProblemResponse> getProblemListByChapter(Long chapterId) {
		// TODO: 챕터 기준 문제 목록 조회 구현
		return null;
	}

	public ProblemDetailResponse getProblemDetail(Long problemId) {
		ProblemDetailDto problemDetailDto = problemRepository.getProblemDetail(problemId);

		return problemDetailDto.toResponseDto();
	}
}
