package com.example.api.problem.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.constants.ProblemType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemSummaryResponse {
	private Long problemId;
	private String title;
	private String content;
	private ProblemType type;
	private List<String> choices;
	private Integer answerCorrectRate;
}
