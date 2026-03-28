package com.example.problem.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import com.example.constants.AnswerType;
import com.example.exception.CustomException;
import com.example.exception.ErrorCode;
import com.example.problem.dto.internal.ProblemAnswerDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProblemMarker {

	public static AnswerType mark(List<Integer> selectedChoices, String textAnswer, ProblemAnswerDto answer) {
		return switch (answer.getType()) { // 기능이 복잡해질경우 상태 패턴 고려
			case SINGLE_CHOICE -> markSingleChoice(selectedChoices, answer.getCorrectChoices());
			case MULTI_CHOICE -> markMultiChoice(selectedChoices, answer.getCorrectChoices());
			case SHORT_ANSWER -> markShortAnswer(textAnswer, answer.getCorrectTextAnswer());
		};
	}

	private static AnswerType markSingleChoice(List<Integer> selected, List<Integer> correct) {
		if (selected == null || selected.isEmpty()) {
			throw new CustomException(ErrorCode.INVALID_INPUT);
		}
		return correct.contains(selected.get(0)) ? AnswerType.CORRECT : AnswerType.INCORRECT;
	}

	private static AnswerType markMultiChoice(List<Integer> selected, List<Integer> correct) {
		if (selected == null || selected.isEmpty()) {
			throw new CustomException(ErrorCode.INVALID_INPUT);
		}
		Set<Integer> correctSet = new HashSet<>(correct);
		Set<Integer> selectedSet = new HashSet<>(selected);

		boolean hasAnyCorrect = selected.stream().anyMatch(correctSet::contains);
		if (!hasAnyCorrect) {
			return AnswerType.INCORRECT;
		}
		return selectedSet.equals(correctSet) ? AnswerType.CORRECT : AnswerType.PARTIAL_CORRECT;
	}

	private static AnswerType markShortAnswer(String userText, String correctText) {
		if (userText == null) {
			throw new CustomException(ErrorCode.INVALID_INPUT);
		}
		return userText.trim().equals(correctText.trim()) ? AnswerType.CORRECT : AnswerType.INCORRECT;
	}
}
