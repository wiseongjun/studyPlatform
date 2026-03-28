package com.example.problem.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.constants.AnswerType;
import com.example.constants.ProblemType;
import com.example.exception.CustomException;
import com.example.exception.ErrorCode;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "T_PROBLEM")
public class Problem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "chapter_id", nullable = false)
	private Long chapterId;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String explanation;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ProblemType type;

	@Column(name = "is_delete", nullable = false)
	private boolean deleted;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "problem_id", updatable = false, insertable = false)
	@OrderBy("choiceNumber ASC")
	private List<ProblemChoice> choices = new ArrayList<>();

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "problem_id", updatable = false, insertable = false)
	private List<ProblemAnswer> answers = new ArrayList<>();

	@OneToOne(mappedBy = "problem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private ProblemStatus problemStatus;

	public AnswerType grade(List<Integer> selectedChoices, String textAnswer) {
		return switch (this.type) {
			case SINGLE_CHOICE -> gradeSingleChoice(selectedChoices);
			case MULTI_CHOICE -> gradeMultiChoice(selectedChoices);
			case SHORT_ANSWER -> gradeShortAnswer(textAnswer);
		};
	}

	public List<String> getChoiceTexts() {
		return choices.stream()
			.map(ProblemChoice::getChoiceText)
			.toList();
	}

	public List<Integer> getCorrectChoiceNumbers() {
		return answers.stream()
			.map(ProblemAnswer::getChoiceNumber)
			.filter(Objects::nonNull)
			.toList();
	}

	public String getCorrectTextAnswer() {
		return answers.stream()
			.map(ProblemAnswer::getAnswerText)
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
	}

	public Integer getCorrectRate() {
		return problemStatus != null ? problemStatus.calculateCorrectRate() : null;
	}

	private AnswerType gradeSingleChoice(List<Integer> selected) {
		validateChoicesNotEmpty(selected);
		List<Integer> correct = getCorrectChoiceNumbers();
		return correct.contains(selected.getFirst()) ? AnswerType.CORRECT : AnswerType.INCORRECT;
	}

	private AnswerType gradeMultiChoice(List<Integer> selected) {
		validateChoicesNotEmpty(selected);
		Set<Integer> correctSet = new HashSet<>(getCorrectChoiceNumbers());
		Set<Integer> selectedSet = new HashSet<>(selected);

		boolean hasAnyCorrect = selected.stream().anyMatch(correctSet::contains);
		if (!hasAnyCorrect) {
			return AnswerType.INCORRECT;
		}
		return selectedSet.equals(correctSet) ? AnswerType.CORRECT : AnswerType.PARTIAL_CORRECT;
	}

	private AnswerType gradeShortAnswer(String userText) {
		if (userText == null) {
			throw new CustomException(ErrorCode.INVALID_INPUT);
		}
		String correctText = getCorrectTextAnswer();
		if (correctText == null) {
			throw new CustomException(ErrorCode.INVALID_INPUT);
		}
		return userText.trim().equals(correctText.trim()) ? AnswerType.CORRECT : AnswerType.INCORRECT;
	}

	private void validateChoicesNotEmpty(List<Integer> selected) {
		if (selected == null || selected.isEmpty()) {
			throw new CustomException(ErrorCode.INVALID_INPUT);
		}
	}
}
