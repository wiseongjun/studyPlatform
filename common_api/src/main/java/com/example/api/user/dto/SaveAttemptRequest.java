package com.example.api.user.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.constants.AnswerType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SaveAttemptRequest {

	@NotNull(message = "사용자 ID는 필수입니다.")
	private Long userId;

	@NotNull(message = "문제 ID는 필수입니다.")
	private Long problemId;

	@NotNull(message = "단원 ID는 필수입니다.")
	private Long chapterId;

	@NotNull(message = "정답 유형은 필수입니다.")
	private AnswerType answerType;

	private List<Integer> userChoices;

	private String userTextAnswer;
}
