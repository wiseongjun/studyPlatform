package com.example.problem.dto.internal;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.constants.ProblemType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemInfoDto {
	private Long problemId;
	private String title;
	private String content;
	private ProblemType type;
	private List<String> choices;
	private int totalAttempts;
	private int correctAttempts;
}