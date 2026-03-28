package com.example.problem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "T_PROBLEM_CHOICE")
public class ProblemChoice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "problem_id", nullable = false)
	private Long problemId;

	@Column(name = "choice_number", nullable = false)
	private Integer choiceNumber;

	@Column(name = "choice_text", nullable = false, length = 500)
	private String choiceText;
}
