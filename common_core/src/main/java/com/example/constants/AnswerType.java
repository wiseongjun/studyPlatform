package com.example.constants;

import com.example.exception.CustomException;
import com.example.exception.ErrorCode;

public enum AnswerType {

	CORRECT("정답"),
	PARTIAL_CORRECT("부분 정답"),
	INCORRECT("오답");

	private final String label;

	AnswerType(String label) {
		this.label = label;
	}

	public static AnswerType from(String label) {
		for (AnswerType status : values()) {
			if (status.label.equals(label)) {
				return status;
			}
		}
		throw new CustomException(ErrorCode.INVALID_INPUT);
	}

	public String getLabel() {
		return label;
	}
}
