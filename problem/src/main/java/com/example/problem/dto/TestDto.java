package com.example.problem.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class TestDto {
	private int id;
	private String value;

	public TestDto(int id, String value) {
		this.id = id;
		this.value = value;
	}
}
