package com.example.user.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.example.api.problem.client.ProblemFeignClient;
import com.example.api.problem.dto.ProblemDetailResponse;
import com.example.api.problem.dto.ProblemSummaryResponse;
import com.example.api.user.dto.SaveAttemptRequest;
import com.example.exception.CustomException;
import com.example.exception.ErrorCode;
import com.example.user.dto.internal.AttemptWithAnswersDto;
import com.example.user.dto.internal.SaveAnswerCommand;
import com.example.user.dto.internal.SaveAttemptCommand;
import com.example.user.dto.res.AttemptDetailResponse;
import com.example.user.dto.res.SolvedProblemResponse;
import com.example.user.repository.UserCommandRepository;
import com.example.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
	private final ProblemFeignClient problemFeignClient;
	private final UserRepository userRepository;
	private final UserCommandRepository userCommandRepository;

	public List<SolvedProblemResponse> getSolvedProblemList(Long userId) {
		List<Long> problemIds = userRepository.getSolvedProblemIds(userId);
		if (problemIds.isEmpty()) {
			return Collections.emptyList();
		}
		List<ProblemSummaryResponse> summaries = problemFeignClient.getProblemsIncludingDeleted(problemIds);
		return summaries.stream()
			.map(this::toSolvedProblemResponse)
			.toList();
	}

	public AttemptDetailResponse getSolvedProblemDetail(Long problemId, Long userId) {
		AttemptWithAnswersDto attempt = userRepository.getLastAttemptDetail(userId, problemId);
		if (attempt == null) {
			throw new CustomException(ErrorCode.ENTITY_NOT_FOUND);
		}
		ProblemDetailResponse problem = problemFeignClient.getProblemDetail(problemId);
		return toAttemptDetailResponse(attempt, problem);
	}

	public List<Long> getSolvedProblemIds(Long userId, Long chapterId) {
		return userRepository.getSolvedProblemIds(userId, chapterId);
	}

	@Transactional
	public void saveAttempt(SaveAttemptRequest request) {
		SaveAttemptCommand attemptCommand = new SaveAttemptCommand(
			request.getUserId(),
			request.getProblemId(),
			request.getChapterId(),
			request.getAnswerType()
		);
		Long attemptId = userCommandRepository.saveAttempt(attemptCommand);

		SaveAnswerCommand answerCommand = new SaveAnswerCommand(
			attemptId,
			request.getUserChoices(),
			request.getUserTextAnswer()
		);

		userCommandRepository.saveUserAnswer(answerCommand);
	}

	private AttemptDetailResponse toAttemptDetailResponse(AttemptWithAnswersDto attempt,
		ProblemDetailResponse problem) {
		return new AttemptDetailResponse(
			problem.getProblemId(),
			problem.getTitle(),
			problem.getContent(),
			problem.getType(),
			attempt.getAnswerType(),
			problem.getExplanation(),
			problem.getCorrectChoices(),
			problem.getCorrectTextAnswer(),
			attempt.getUserChoices(),
			attempt.getUserTextAnswer(),
			problem.getAnswerCorrectRate()
		);
	}

	private SolvedProblemResponse toSolvedProblemResponse(ProblemSummaryResponse summary) {
		return new SolvedProblemResponse(
			summary.getProblemId(),
			summary.getTitle(),
			summary.getContent(),
			summary.getType(),
			summary.getChoices(),
			summary.getAnswerCorrectRate()
		);
	}
}
