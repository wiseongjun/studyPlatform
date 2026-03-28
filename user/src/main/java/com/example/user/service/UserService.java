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
import com.example.user.dto.internal.AttemptWithAnswersDto;
import com.example.user.dto.internal.SaveAnswerCommand;
import com.example.user.dto.internal.SaveAttemptCommand;
import com.example.user.dto.res.AttemptDetailResponse;
import com.example.user.dto.res.SolvedProblemResponse;
import com.example.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

	private final ProblemFeignClient problemFeignClient;
	private final UserRepository userRepository;

	public List<SolvedProblemResponse> getSolvedProblemList(Long userId) {
		List<Long> problemIds = userRepository.getSolvedProblemIds(userId);
		if (problemIds.isEmpty()) {
			return Collections.emptyList();
		}
		List<ProblemSummaryResponse> list = problemFeignClient.getProblemsIncludingDeleted(problemIds);
		// TODO: ProblemSummaryResponse → SolvedProblemResponse 변환 구현
		return null;
	}

	public AttemptDetailResponse getSolvedProblemDetail(Long problemId, Long userId) {
		AttemptWithAnswersDto attemptQuery = userRepository.getLastAttemptDetail(userId, problemId);
		ProblemDetailResponse problemDetail = problemFeignClient.getProblemDetail(problemId);
		// TODO: AttemptWithAnswersDto + ProblemDetailResponse → AttemptDetailResponse 변환 구현
		return null;
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
		SaveAnswerCommand answerCommand = new SaveAnswerCommand(
			request.getUserId(),
			request.getProblemId(),
			request.getUserChoices(),
			request.getUserTextAnswer()
		);
		userRepository.saveAttempt(attemptCommand);
		userRepository.saveUserAnswer(answerCommand);
	}
}
