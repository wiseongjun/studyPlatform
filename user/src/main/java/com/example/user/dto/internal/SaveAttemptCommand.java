package com.example.user.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.constants.AnswerType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SaveAttemptCommand {

	private Long userId;
	private Long problemId;
	private Long chapterId;
	private AnswerType answerType;
}
