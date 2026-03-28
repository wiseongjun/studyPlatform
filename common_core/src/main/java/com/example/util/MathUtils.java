package com.example.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MathUtils {

	/**
	 * 정답률을 소수점 첫째 자리에서 반올림한 정수로 반환합니다.
	 */
	public static int roundPercentage(int total, int count) {
		if (total == 0)
			return 0;
		return (int)Math.round(count * 100.0 / total);
	}
}
