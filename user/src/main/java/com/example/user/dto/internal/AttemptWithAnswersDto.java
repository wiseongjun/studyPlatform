package com.example.user.dto.internal;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.constants.AnswerType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AttemptWithAnswersDto {

	private Long attemptId;
	private Long userId;
	private Long problemId;
	private AnswerType answerType;
	private List<Integer> userChoices;
	private String userTextAnswer;
	private LocalDateTime attemptedAt;
}
