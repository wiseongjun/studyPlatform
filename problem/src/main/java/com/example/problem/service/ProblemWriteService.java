package com.example.problem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.example.constants.AnswerType;
import com.example.problem.repository.ProblemCommandRepository;

@Service
@RequiredArgsConstructor
public class ProblemWriteService {

	private final ProblemCommandRepository problemCommandRepository;

	@Transactional
	public void updateStatus(Long problemId, AnswerType answerType) {
		problemCommandRepository.incrementAttemptCounts(problemId, answerType);
	}
}
