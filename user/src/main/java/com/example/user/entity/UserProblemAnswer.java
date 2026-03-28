package com.example.user.entity;

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
@Table(name = "T_USER_PROBLEM_ANSWER")
public class UserProblemAnswer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "attempt_id", nullable = false)
	private Long attemptId;

	@Column(name = "choice_number")
	private Integer choiceNumber;

	@Column(name = "answer_text", length = 500)
	private String answerText;
}
