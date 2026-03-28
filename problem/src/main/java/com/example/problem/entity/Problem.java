package com.example.problem.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.constants.ProblemType;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "T_PROBLEM")
public class Problem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "chapter_id", nullable = false)
	private Long chapterId;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String explanation;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ProblemType type;

	@Column(name = "is_delete", nullable = false)
	private boolean deleted;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@OneToOne(mappedBy = "problem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private ProblemStatus problemStatus;
}
