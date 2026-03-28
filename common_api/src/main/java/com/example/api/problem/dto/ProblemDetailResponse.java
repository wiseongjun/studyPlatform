package com.example.api.problem.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.constants.ProblemType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemDetailResponse {
	private Long problemId;
	private String title;
	private String content;
	private String explanation;
	private ProblemType type;
	private List<Integer> correctChoices;
	private String correctTextAnswer;
	private Integer answerCorrectRate;
}
