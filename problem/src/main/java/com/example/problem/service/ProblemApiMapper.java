package com.example.problem.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.api.problem.dto.ProblemDetailResponse;
import com.example.api.problem.dto.ProblemSummaryResponse;
import com.example.problem.domain.ProblemHelper;
import com.example.problem.dto.internal.ProblemDetailDto;
import com.example.problem.dto.internal.ProblemInfoDto;
import com.example.problem.dto.res.ProblemResponse;
import com.example.problem.entity.Problem;
import com.example.problem.entity.ProblemStatus;

@Component
public class ProblemApiMapper { // 추후 불편하면 MapStruct 고려

	public ProblemResponse toProblemResponse(ProblemInfoDto info) {
		Integer correctRate = ProblemHelper.calculateRate(info.getTotalAttempts(), info.getCorrectAttempts());
		return new ProblemResponse(
			info.getProblemId(),
			info.getTitle(),
			info.getContent(),
			info.getType(),
			info.getChoices(),
			correctRate
		);
	}

	public ProblemResponse toProblemResponse(Problem problem, List<String> choices) {
		return new ProblemResponse(
			problem.getId(),
			problem.getTitle(),
			problem.getContent(),
			problem.getType(),
			choices,
			calculateCorrectRate(problem)
		);
	}

	public ProblemSummaryResponse toSummaryResponse(Problem problem, List<String> choices) {
		return new ProblemSummaryResponse(
			problem.getId(),
			problem.getTitle(),
			problem.getContent(),
			problem.getType(),
			choices,
			calculateCorrectRate(problem)
		);
	}

	public ProblemDetailResponse toProblemDetailResponse(ProblemDetailDto dto) {
		Integer correctRate = ProblemHelper.calculateRate(dto.getTotalAttempts(), dto.getCorrectAttempts());
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

	private Integer calculateCorrectRate(Problem problem) {
		ProblemStatus status = problem.getProblemStatus();
		int total = status != null ? status.getTotalAttempts() : 0;
		int correct = status != null ? status.getCorrectAttempts() : 0;
		return ProblemHelper.calculateRate(total, correct);
	}
}
