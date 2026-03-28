package com.example.problem.service;

import org.springframework.stereotype.Component;

import com.example.api.problem.dto.ProblemDetailResponse;
import com.example.api.problem.dto.ProblemSummaryResponse;
import com.example.problem.dto.res.ProblemResponse;
import com.example.problem.entity.Problem;

@Component
public class ProblemApiMapper {

	public ProblemResponse toProblemResponse(Problem problem) {
		return new ProblemResponse(
			problem.getId(),
			problem.getTitle(),
			problem.getContent(),
			problem.getType(),
			problem.getChoiceTexts(),
			problem.getCorrectRate()
		);
	}

	public ProblemSummaryResponse toSummaryResponse(Problem problem) {
		return new ProblemSummaryResponse(
			problem.getId(),
			problem.getTitle(),
			problem.getContent(),
			problem.getType(),
			problem.getChoiceTexts(),
			problem.getCorrectRate()
		);
	}

	public ProblemDetailResponse toProblemDetailResponse(Problem problem) {
		return new ProblemDetailResponse(
			problem.getId(),
			problem.getTitle(),
			problem.getContent(),
			problem.getExplanation(),
			problem.getType(),
			problem.getCorrectChoiceNumbers(),
			problem.getCorrectTextAnswer(),
			problem.getCorrectRate()
		);
	}
}
