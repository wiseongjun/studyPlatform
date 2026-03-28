package com.example.problem.domain;

import java.util.List;

import com.example.constants.AnswerType;
import com.example.problem.dto.internal.ProblemAnswerDto;
import com.example.problem.dto.req.SubmitProblemRequest;

public class ProblemMarker {

	private ProblemMarker() { }

	public static AnswerType mark(SubmitProblemRequest request, ProblemAnswerDto answer) {
		// TODO: SINGLE_CHOICE / MULTI_CHOICE / SHORT_ANSWER 분기 구현
		return null;
	}
}
