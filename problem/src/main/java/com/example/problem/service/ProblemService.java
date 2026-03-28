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
import com.example.problem.dto.internal.ProblemDetailDto;
import com.example.problem.dto.internal.ProblemInfoDto;
import com.example.problem.dto.req.RandomProblemFilter;
import com.example.problem.dto.req.SubmitProblemRequest;
import com.example.problem.dto.res.ProblemResponse;
import com.example.problem.dto.res.SubmitProblemResponse;
import com.example.problem.entity.Problem;
import com.example.problem.entity.ProblemStatus;
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

	public ProblemResponse getRandomProblem(RandomProblemFilter filter) {
		List<Long> solvedIds = userFeignClient.getUserSolvedProblemIdList(filter.getUserId(), filter.getChapterId());
		Long lastSkippedId = problemCacheRepository.getLastSkippedId(filter.getUserId(), filter.getChapterId());
		// 푼 문제와 스킵한 문제는 제외 Id List에 담는다.
		List<Long> exclusionList = ProblemHelper.combine(solvedIds, lastSkippedId);
		List<Long> problemIds = problemRepository.getProblemIds(filter.getChapterId());
		// 문제들 중 제외 ID List를 제외하고 랜덤으로 문제를 하나 선택한다.
		Long selectedId = problemSelector.selectProblemId(problemIds, exclusionList);
		// 현재 문제를 직전 선택 문제 캐시에 저장한다.
		problemCacheRepository.saveLastSkippedId(filter.getUserId(), filter.getChapterId(), selectedId);

		ProblemInfoDto info = problemRepository.getProblemInfoById(selectedId);
		Integer correctRate = ProblemHelper.calculateRate(info.getTotalAttempts(), info.getCorrectAttempts());
		return toProblemResponse(info, correctRate);
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
		List<Problem> problems = problemRepository.findByIds(problemIds);
		Map<Long, List<String>> choicesMap = problemRepository.getChoicesMap(problemIds);
		return problems.stream()
			.map(problem -> toSummaryResponse(
					problem,
					choicesMap.getOrDefault(problem.getId(), List.of()),
					calculateCorrectRate(problem)
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
			.map(problem -> toProblemResponse(
					problem,
					choicesMap.getOrDefault(problem.getId(), List.of()),
					calculateCorrectRate(problem)
				)
			)
			.toList();
	}

	public ProblemDetailResponse getProblemDetail(Long problemId) {
		ProblemDetailDto dto = problemRepository.getProblemDetail(problemId);
		Integer correctRate = ProblemHelper.calculateRate(dto.getTotalAttempts(), dto.getCorrectAttempts());
		return toProblemDetailResponse(dto, correctRate);
	}

	private Integer calculateCorrectRate(Problem problem) {
		ProblemStatus status = problem.getProblemStatus();
		int total = status != null ? status.getTotalAttempts() : 0;
		int correct = status != null ? status.getCorrectAttempts() : 0;
		return ProblemHelper.calculateRate(total, correct);
	}

	private ProblemDetailResponse toProblemDetailResponse(ProblemDetailDto dto, Integer correctRate) {
		return new ProblemDetailResponse(
			dto.getProblemId(),
			dto.getTitle(),
			dto.getContent(),
			dto.getExplanation(),
			dto.getType(),
			dto.getCorrectChoices(),
			dto.getCorrectTextAnswer(),
			correctRate
		);
	}

	private ProblemResponse toProblemResponse(ProblemInfoDto info, Integer correctRate) {
		return new ProblemResponse(
			info.getProblemId(),
			info.getTitle(),
			info.getContent(),
			info.getType(),
			info.getChoices(),
			correctRate
		);
	}

	private ProblemResponse toProblemResponse(Problem problem, List<String> choices, Integer correctRate) {
		return new ProblemResponse(
			problem.getId(),
			problem.getTitle(),
			problem.getContent(),
			problem.getType(),
			choices,
			correctRate
		);
	}

	private ProblemSummaryResponse toSummaryResponse(Problem problem, List<String> choices, Integer correctRate) {
		return new ProblemSummaryResponse(
			problem.getId(),
			problem.getTitle(),
			problem.getContent(),
			problem.getType(),
			choices,
			correctRate
		);
	}
}
