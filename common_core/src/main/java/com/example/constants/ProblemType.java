package com.example.constants;

import com.example.exception.CustomException;
import com.example.exception.ErrorCode;

public enum ProblemType {

	SINGLE_CHOICE("단일 정답"),
	MULTI_CHOICE("복수 정답"),
	SHORT_ANSWER("주관식");

	private final String label;

	ProblemType(String label) {
		this.label = label;
	}

	public static ProblemType from(String label) {
		for (ProblemType type : values()) {
			if (type.label.equals(label)) {
				return type;
			}
		}
		throw new CustomException(ErrorCode.INVALID_INPUT);
	}

	public String getLabel() {
		return label;
	}
}
