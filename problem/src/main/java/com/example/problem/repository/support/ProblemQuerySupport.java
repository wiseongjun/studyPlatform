package com.example.problem.repository.support;

import java.util.List;

import com.querydsl.core.types.dsl.BooleanExpression;

import com.example.problem.entity.QProblem;
import com.example.problem.entity.QProblemAnswer;
import com.example.problem.entity.QProblemChoice;
import com.example.problem.entity.QProblemStatus;

public final class ProblemQuerySupport {

	private ProblemQuerySupport() {
	}

	public static BooleanExpression problemChapterIdEq(Long chapterId) {
		return chapterId != null ? QProblem.problem.chapterId.eq(chapterId) : null;
	}

	public static BooleanExpression problemNotDeleted() {
		return QProblem.problem.deleted.isFalse();
	}

	public static BooleanExpression problemIdEq(Long problemId) {
		return problemId != null ? QProblem.problem.id.eq(problemId) : null;
	}

	public static BooleanExpression problemIdIn(List<Long> problemIds) {
		return (problemIds == null || problemIds.isEmpty()) ? null : QProblem.problem.id.in(problemIds);
	}

	public static BooleanExpression choiceProblemIdEq(Long problemId) {
		return problemId != null ? QProblemChoice.problemChoice.problemId.eq(problemId) : null;
	}

	public static BooleanExpression choiceProblemIdIn(List<Long> problemIds) {
		return (problemIds == null || problemIds.isEmpty()) ? null
			: QProblemChoice.problemChoice.problemId.in(problemIds);
	}

	public static BooleanExpression answerProblemIdEq(Long problemId) {
		return problemId != null ? QProblemAnswer.problemAnswer.problemId.eq(problemId) : null;
	}

	public static BooleanExpression statusLinkedToProblemId(Long problemId) {
		return problemId != null ? QProblemStatus.problemStatus.problem.id.eq(problemId) : null;
	}
}
