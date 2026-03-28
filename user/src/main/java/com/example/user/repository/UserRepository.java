package com.example.user.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import com.example.user.dto.internal.AttemptWithAnswersDto;
import com.example.user.dto.internal.SaveAnswerCommand;
import com.example.user.dto.internal.SaveAttemptCommand;

@Repository
@RequiredArgsConstructor
public class UserRepository {

	private final JPAQueryFactory queryFactory;

	public List<Long> getSolvedProblemIds(Long userId) {
		// TODO: T_USER_PROBLEM_ATTEMPT에서 userId 기준 problemId 목록 조회
		return null;
	}

	public AttemptWithAnswersDto getLastAttemptDetail(Long userId, Long problemId) {
		// TODO: T_USER_PROBLEM_ATTEMPT + T_USER_PROBLEM_ANSWER 조인 조회 (userId, problemId 기준 최신 attempt)
		return null;
	}

	public List<Long> getSolvedProblemIds(Long userId, Long chapterId) {
		// TODO: T_USER_PROBLEM_ATTEMPT에서 userId, chapterId 기준 미삭제 problemId 목록 조회 (WHERE is_delete = false)
		return null;
	}

	public void saveAttempt(SaveAttemptCommand command) {
		// TODO: T_USER_PROBLEM_ATTEMPT 저장
	}

	public void saveUserAnswer(SaveAnswerCommand command) {
		// TODO: T_USER_PROBLEM_ANSWER 저장
	}
}
