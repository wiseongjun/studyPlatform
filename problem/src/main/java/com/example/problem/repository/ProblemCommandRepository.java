package com.example.problem.repository;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import com.example.constants.AnswerType;
import com.example.problem.entity.QProblemStatus;
import com.example.problem.repository.support.ProblemQuerySupport;

@Repository
@RequiredArgsConstructor
public class ProblemCommandRepository {

	private final JPAQueryFactory queryFactory;

	// 동시성 이슈 방지: SELECT → UPDATE 대신 DB 레벨 atomic update 사용
	// dirty checking으로 하면 동시 요청 시 totalAttempts 유실 가능
	public void incrementAttemptCounts(Long problemId, AnswerType answerType) {
		QProblemStatus qStatus = QProblemStatus.problemStatus;
		queryFactory.update(qStatus)
			.set(qStatus.totalAttempts, qStatus.totalAttempts.add(1))
			.set(qStatus.correctAttempts,
				answerType == AnswerType.CORRECT
					? qStatus.correctAttempts.add(1)
					: qStatus.correctAttempts)
			.where(ProblemQuerySupport.statusLinkedToProblemId(problemId))
			.execute();
	}
}
