package com.example.problem.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.example.api.problem.dto.ProblemDetailResponse;
import com.example.api.problem.dto.ProblemSummaryResponse;
import com.example.api.user.client.UserFeignClient;
import com.example.api.user.dto.SaveAttemptRequest;
import com.example.constants.AnswerType;
import com.example.problem.domain.ProblemHelper;
import com.example.problem.domain.ProblemMarker;
import com.example.problem.domain.ProblemSelector;
import com.example.problem.dto.internal.ProblemAnswerDto;
import com.example.problem.dto.req.RandomProblemFilter;
import com.example.problem.dto.req.SubmitProblemRequest;
import com.example.problem.dto.res.ProblemResponse;
import com.example.problem.dto.res.SubmitProblemResponse;
import com.example.problem.entity.Problem;
import com.example.problem.repository.ProblemCacheRepository;
import com.example.problem.repository.ProblemRepository;

@Service
@RequiredArgsConstructor
public class ProblemService {

	private final ProblemRepository problemRepository;
	private final ProblemCacheRepository problemCacheRepository;
	private final ProblemWriteService problemWriteService;
	private final ProblemAsyncService problemAsyncService;
	private final ProblemSelector problemSelector;
	private final UserFeignClient userFeignClient;
	private final ProblemApiMapper problemApiMapper;

	public ProblemResponse getRandomProblem(RandomProblemFilter filter) {
		List<Long> solvedIds = userFeignClient.getUserSolvedProblemIdList(filter.getUserId(), filter.getChapterId());
		Long lastSkippedId = problemCacheRepository.getLastSkippedId(filter.getUserId(), filter.getChapterId());
		List<Long> exclusionList = ProblemHelper.combine(solvedIds, lastSkippedId);
		List<Long> problemIds = problemRepository.getProblemIds(filter.getChapterId());
		Long selectedId = problemSelector.selectProblemId(problemIds, exclusionList);
		problemCacheRepository.saveLastSkippedId(filter.getUserId(), filter.getChapterId(), selectedId);

		return problemApiMapper.toProblemResponse(problemRepository.getProblemInfoById(selectedId));
	}

	public SubmitProblemResponse submitProblem(Long problemId, SubmitProblemRequest userAnswer) {
		ProblemAnswerDto problemAnswerDto = problemRepository.getProblemAnswer(problemId);
		AnswerType answerType = ProblemMarker.mark(userAnswer.getSelectedChoices(), userAnswer.getTextAnswer(),
			problemAnswerDto);

		problemWriteService.updateStatus(problemId, answerType);

		// 추후 정합성 처리를 위해 Kafka 로 변경 - ack, rollback 처리
		problemAsyncService.saveAttempt(new SaveAttemptRequest(
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
		if (problemIds == null || problemIds.isEmpty()) {
			return List.of();
		}
		List<Problem> problems = problemRepository.findByIds(problemIds);
		Map<Long, List<String>> choicesMap = problemRepository.getChoicesMap(problemIds);
		return problems.stream()
			.map(problem -> problemApiMapper.toSummaryResponse(
					problem,
					choicesMap.getOrDefault(problem.getId(), List.of())
				)
			)
			.toList();
	}

	public List<ProblemResponse> getProblemListByChapter(Long chapterId) {
		List<Problem> problems = problemRepository.findByChapterId(chapterId);
		if (problems.isEmpty()) {
			return List.of();
		}
		List<Long> problemIds = problems.stream().map(Problem::getId).toList();
		Map<Long, List<String>> choicesMap = problemRepository.getChoicesMap(problemIds);
		return problems.stream()
			.map(problem -> problemApiMapper.toProblemResponse(
					problem,
					choicesMap.getOrDefault(problem.getId(), List.of())
				)
			)
			.toList();
	}

	public ProblemDetailResponse getProblemDetail(Long problemId) {
		return problemApiMapper.toProblemDetailResponse(problemRepository.getProblemDetail(problemId));
	}
}
