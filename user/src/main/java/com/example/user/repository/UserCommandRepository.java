package com.example.user.repository;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.example.user.dto.internal.SaveAnswerCommand;
import com.example.user.dto.internal.SaveAttemptCommand;
import com.example.user.entity.UserProblemAnswer;
import com.example.user.entity.UserProblemAttempt;

@Repository
public class UserCommandRepository {

	@PersistenceContext
	private EntityManager entityManager;

	public Long saveAttempt(SaveAttemptCommand command) {
		UserProblemAttempt attempt = new UserProblemAttempt(
			command.getUserId(),
			command.getProblemId(),
			command.getChapterId(),
			command.getAnswerType()
		);
		entityManager.persist(attempt);
		entityManager.flush();
		return attempt.getId();
	}

	public void saveUserAnswer(SaveAnswerCommand command) {
		if (command.getUserChoices() != null && !command.getUserChoices().isEmpty()) {
			for (Integer choice : command.getUserChoices()) {
				entityManager.persist(new UserProblemAnswer(command.getAttemptId(), choice, null));
			}
		} else if (command.getUserTextAnswer() != null) {
			entityManager.persist(new UserProblemAnswer(command.getAttemptId(), null, command.getUserTextAnswer()));
		}
	}
}
