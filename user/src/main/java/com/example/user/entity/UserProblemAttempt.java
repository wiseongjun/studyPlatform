package com.example.user.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.constants.AnswerType;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "T_USER_PROBLEM_ATTEMPT")
public class UserProblemAttempt {

	public UserProblemAttempt(Long userId, Long problemId, Long chapterId, AnswerType answerType) {
		this.userId = userId;
		this.problemId = problemId;
		this.chapterId = chapterId;
		this.answerType = answerType;
	}
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "problem_id", nullable = false)
	private Long problemId;

	@Column(name = "chapter_id", nullable = false)
	private Long chapterId;

	@Enumerated(EnumType.STRING)
	@Column(name = "answer_status", nullable = false)
	private AnswerType answerType;

	@CreationTimestamp
	@Column(name = "attempted_at", nullable = false, updatable = false)
	private LocalDateTime attemptedAt;
}
