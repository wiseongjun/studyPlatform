package com.example.user.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import com.example.user.dto.internal.AttemptWithAnswersDto;
import com.example.user.entity.QUserProblemAnswer;
import com.example.user.entity.QUserProblemAttempt;
import com.example.user.entity.UserProblemAttempt;
import com.example.user.repository.support.UserQuerySupport;

@Repository
@RequiredArgsConstructor
public class UserRepository {

	private final JPAQueryFactory queryFactory;

	public List<Long> getSolvedProblemIds(Long userId) {
		QUserProblemAttempt qAttempt = QUserProblemAttempt.userProblemAttempt;
		return queryFactory
			.select(qAttempt.problemId).distinct()
			.from(qAttempt)
			.where(UserQuerySupport.attemptUserIdEq(userId))
			.fetch();
	}

	public List<Long> getSolvedProblemIds(Long userId, Long chapterId) {
		QUserProblemAttempt qAttempt = QUserProblemAttempt.userProblemAttempt;
		return queryFactory
			.select(qAttempt.problemId).distinct()
			.from(qAttempt)
			.where(UserQuerySupport.attemptUserIdEq(userId), UserQuerySupport.attemptChapterIdEq(chapterId))
			.fetch();
	}

	public AttemptWithAnswersDto getLastAttemptDetail(Long userId, Long problemId) {
		QUserProblemAttempt qAttempt = QUserProblemAttempt.userProblemAttempt;
		QUserProblemAnswer qAnswer = QUserProblemAnswer.userProblemAnswer;

		UserProblemAttempt attempt = queryFactory
			.selectFrom(qAttempt)
			.where(UserQuerySupport.attemptUserIdEq(userId), UserQuerySupport.attemptProblemIdEq(problemId))
			.orderBy(qAttempt.attemptedAt.desc())
			.limit(1)
			.fetchOne();

		if (attempt == null) {
			return null;
		}

		List<Integer> userChoices = queryFactory
			.select(qAnswer.choiceNumber)
			.from(qAnswer)
			.where(UserQuerySupport.answerAttemptIdEq(attempt.getId()), UserQuerySupport.answerChoiceNumberIsNotNull())
			.fetch();

		String userTextAnswer = queryFactory
			.select(qAnswer.answerText)
			.from(qAnswer)
			.where(UserQuerySupport.answerAttemptIdEq(attempt.getId()), UserQuerySupport.answerTextIsNotNull())
			.fetchFirst();

		return new AttemptWithAnswersDto(
			attempt.getId(),
			attempt.getUserId(),
			attempt.getProblemId(),
			attempt.getAnswerType(),
			userChoices,
			userTextAnswer,
			attempt.getAttemptedAt()
		);
	}
}
