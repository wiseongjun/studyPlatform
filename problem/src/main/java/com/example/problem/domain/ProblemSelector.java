package com.example.problem.domain;

import java.util.List;

public class ProblemSelector {

	private ProblemSelector() { }

	public static Long selectProblemId(List<Long> problemIds, List<Long> excludedIds) {
		// TODO: excludedIds 제외 후 랜덤 선택, 모두 소진 시 예외 처리 구현
		return null;
	}
}
