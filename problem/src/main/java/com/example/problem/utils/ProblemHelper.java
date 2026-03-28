package com.example.problem.utils;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProblemHelper {

	public static List<Long> combine(List<Long> idList, Long id) {
		List<Long> resultList = new ArrayList<>(idList);
		if (id != null) {
			resultList.add(id);
		}
		return resultList;
	}
}
