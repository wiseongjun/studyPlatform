package com.example.problem.dto.internal;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.constants.ProblemType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemAnswerDto {
	private Long problemId;
	private ProblemType type;
	private List<Integer> correctChoices;
	private String correctTextAnswer;
	private String explanation;
}
