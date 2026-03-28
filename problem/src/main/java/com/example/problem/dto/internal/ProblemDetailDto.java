package com.example.problem.dto.internal;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.api.problem.dto.ProblemDetailResponse;
import com.example.constants.ProblemType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemDetailDto {
	private Long problemId;
	private String title;
	private String content;
	private String explanation;
	private ProblemType type;
	private List<Integer> correctChoices;
	private String correctTextAnswer;
	private Integer answerCorrectRate;

	public ProblemDetailResponse toResponseDto() {
		return new ProblemDetailResponse(
			problemId,
			title,
			content,
			explanation,
			type,
			correctChoices,
			correctTextAnswer,
			answerCorrectRate
		);
	}
}
