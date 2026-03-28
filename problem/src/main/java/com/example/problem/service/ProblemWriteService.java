package com.example.problem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.example.constants.AnswerType;
import com.example.problem.repository.ProblemRepository;

@Service
@RequiredArgsConstructor
public class ProblemWriteService {

	private final ProblemRepository problemRepository;

	@Transactional
	public void updateStatus(Long problemId, AnswerType answerType) {
		problemRepository.incrementAttemptCounts(problemId, answerType);
	}
}
