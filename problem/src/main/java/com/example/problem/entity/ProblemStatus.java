package com.example.problem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.util.MathUtils;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "T_PROBLEM_STATUS")
public class ProblemStatus {

	private static final int MIN_ATTEMPTS_FOR_RATE = 30;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "problem_id", nullable = false, unique = true)
	private Problem problem;

	@Column(name = "total_attempts", nullable = false)
	private int totalAttempts;

	@Column(name = "correct_attempts", nullable = false)
	private int correctAttempts;

	public Integer calculateCorrectRate() {
		return totalAttempts >= MIN_ATTEMPTS_FOR_RATE
			? MathUtils.roundPercentage(totalAttempts, correctAttempts)
			: null;
	}
}
