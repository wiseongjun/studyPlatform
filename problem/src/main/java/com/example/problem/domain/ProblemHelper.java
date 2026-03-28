package com.example.problem.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import com.example.util.MathUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProblemHelper {

	private static final int MIN_ATTEMPTS_FOR_RATE = 30;

	public static List<Long> combine(List<Long> idList, Long id) {
		List<Long> resultList = new ArrayList<>(idList);
		if (id != null) {
			resultList.add(id);
		}
		return resultList;
	}

	public static Integer calculateRate(int total, int correct) {
		return total >= MIN_ATTEMPTS_FOR_RATE ? MathUtils.roundPercentage(total, correct) : null;
	}
}
