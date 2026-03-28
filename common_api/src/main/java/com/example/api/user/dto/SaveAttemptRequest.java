package com.example.api.user.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.constants.AnswerType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SaveAttemptRequest {
	private Long userId;
	private Long problemId;
	private Long chapterId;
	private AnswerType answerType;
	private List<Integer> userChoices;
	private String userTextAnswer;
}
