package com.example.user.repository.support;

import com.querydsl.core.types.dsl.BooleanExpression;

import com.example.user.entity.QUserProblemAnswer;
import com.example.user.entity.QUserProblemAttempt;

public final class UserQuerySupport {

	private UserQuerySupport() {
	}

	public static BooleanExpression attemptUserIdEq(Long userId) {
		return userId != null ? QUserProblemAttempt.userProblemAttempt.userId.eq(userId) : null;
	}

	public static BooleanExpression attemptChapterIdEq(Long chapterId) {
		return chapterId != null ? QUserProblemAttempt.userProblemAttempt.chapterId.eq(chapterId) : null;
	}

	public static BooleanExpression attemptProblemIdEq(Long problemId) {
		return problemId != null ? QUserProblemAttempt.userProblemAttempt.problemId.eq(problemId) : null;
	}

	public static BooleanExpression answerAttemptIdEq(Long attemptId) {
		return attemptId != null ? QUserProblemAnswer.userProblemAnswer.attemptId.eq(attemptId) : null;
	}

	public static BooleanExpression answerChoiceNumberIsNotNull() {
		return QUserProblemAnswer.userProblemAnswer.choiceNumber.isNotNull();
	}

	public static BooleanExpression answerTextIsNotNull() {
		return QUserProblemAnswer.userProblemAnswer.answerText.isNotNull();
	}
}
