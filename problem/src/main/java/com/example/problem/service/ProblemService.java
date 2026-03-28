package com.example.problem.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.example.api.problem.dto.ProblemDetailResponse;
import com.example.api.problem.dto.ProblemSummaryResponse;
import com.example.api.user.client.UserFeignClient;
import com.example.api.user.dto.SaveAttemptRequest;
import com.example.constants.AnswerType;
import com.example.problem.dto.req.RandomProblemFilter;
import com.example.problem.dto.req.SubmitProblemRequest;
import com.example.problem.dto.res.ProblemResponse;
import com.example.problem.dto.res.SubmitProblemResponse;
import com.example.problem.entity.Problem;
import com.example.problem.repository.ProblemCacheRepository;
import com.example.problem.repository.ProblemRepository;
import com.example.problem.utils.ProblemHelper;
import com.example.problem.utils.ProblemSelector;

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

		Problem problem = problemRepository.findByIdWithChoices(selectedId);
		return problemApiMapper.toProblemResponse(problem);
	}

	public SubmitProblemResponse submitProblem(Long problemId, SubmitProblemRequest userAnswer) {
		Problem problem = problemRepository.findByIdWithAnswers(problemId);

		AnswerType answerType = problem.grade(userAnswer.getSelectedChoices(), userAnswer.getTextAnswer());

		problemWriteService.updateStatus(problemId, answerType);

		problemAsyncService.saveAttempt(new SaveAttemptRequest(
			userAnswer.getUserId(),
			problemId,
			problem.getChapterId(),
			answerType,
			userAnswer.getSelectedChoices(),
			userAnswer.getTextAnswer()
		));

		return new SubmitProblemResponse(
			problemId,
			answerType,
			problem.getExplanation(),
			problem.getCorrectChoiceNumbers(),
			problem.getCorrectTextAnswer()
		);
	}

	public List<ProblemSummaryResponse> getProblemsIncludingDeleted(List<Long> problemIds) {
		if (problemIds == null || problemIds.isEmpty()) {
			return List.of();
		}
		List<Problem> problems = problemRepository.findByIdsWithChoices(problemIds);
		return problems.stream()
			.map(problemApiMapper::toSummaryResponse)
			.toList();
	}

	public List<ProblemResponse> getProblemListByChapter(Long chapterId) {
		List<Problem> problems = problemRepository.findByChapterIdWithChoices(chapterId);
		return problems.stream()
			.map(problemApiMapper::toProblemResponse)
			.toList();
	}

	public ProblemDetailResponse getProblemDetail(Long problemId) {
		Problem problem = problemRepository.findByIdWithAnswersAndStatus(problemId);
		return problemApiMapper.toProblemDetailResponse(problem);
	}
}
