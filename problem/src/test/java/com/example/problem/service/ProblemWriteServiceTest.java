package com.example.problem.service;

import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.constants.AnswerType;
import com.example.problem.repository.ProblemRepository;

@ExtendWith(MockitoExtension.class)
class ProblemWriteServiceTest {

	@InjectMocks
	private ProblemWriteService problemWriteService;

	@Mock
	private ProblemRepository problemRepository;

	@Test
	@DisplayName("updateStatus는 incrementAttemptCounts를 한 번 호출한다")
	void updateStatus_delegatesToRepository() {
		problemWriteService.updateStatus(99L, AnswerType.PARTIAL_CORRECT);

		then(problemRepository).should().incrementAttemptCounts(99L, AnswerType.PARTIAL_CORRECT);
	}
}
