package com.example.problem.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.example.exception.CustomException;
import com.example.exception.ErrorCode;

@Component
@RequiredArgsConstructor
public class ProblemSelector {

	private final Random random; // 기능이 여러가지 생길 경우 전략패턴 고려

	public Long selectProblemId(List<Long> problemIds, List<Long> excludedIds) {
		if (problemIds == null || problemIds.isEmpty()) {
			throw new CustomException(ErrorCode.NO_PROBLEM_AVAILABLE);
		}

		if (excludedIds == null || excludedIds.isEmpty()) {
			return pickRandom(problemIds);
		}

		List<Long> candidates = filterCandidates(problemIds, excludedIds);

		if (candidates.isEmpty()) {
			throw new CustomException(ErrorCode.NO_PROBLEM_AVAILABLE);
		}

		return pickRandom(candidates);
	}

	private List<Long> filterCandidates(List<Long> problemIds, List<Long> excludedIds) {
		Set<Long> excludedSet = new HashSet<>(excludedIds);

		return problemIds.stream()
			.filter(id -> !excludedSet.contains(id))
			.toList();
	}

	private Long pickRandom(List<Long> ids) {
		int size = ids.size();
		return ids.get(random.nextInt(size));
	}
}
