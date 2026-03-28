package com.example.user.dto.internal;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SaveAnswerCommand {

	private Long attemptId;
	private List<Integer> userChoices;
	private String userTextAnswer;
}
