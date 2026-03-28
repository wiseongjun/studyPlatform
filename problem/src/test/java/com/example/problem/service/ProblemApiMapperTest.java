package com.example.problem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.api.problem.dto.ProblemDetailResponse;
import com.example.api.problem.dto.ProblemSummaryResponse;
import com.example.constants.ProblemType;
import com.example.problem.dto.internal.ProblemDetailDto;
import com.example.problem.dto.internal.ProblemInfoDto;
import com.example.problem.dto.res.ProblemResponse;
import com.example.problem.entity.Problem;
import com.example.problem.entity.ProblemStatus;

@ExtendWith(MockitoExtension.class)
class ProblemApiMapperTest {

	private final ProblemApiMapper mapper = new ProblemApiMapper();

	@Mock
	private Problem problem;
	@Mock
	private ProblemStatus status;

	@Test
	@DisplayName("ProblemInfoDto → ProblemResponse, 시도 30 미만이면 정답률 null")
	void toProblemResponse_fromInfo_insufficientAttempts_rateNull() {
		ProblemInfoDto info = new ProblemInfoDto(1L, "t", "c", ProblemType.SINGLE_CHOICE, List.of("A"), 10, 5);

		ProblemResponse res = mapper.toProblemResponse(info);

		assertThat(res.getProblemId()).isEqualTo(1L);
		assertThat(res.getTitle()).isEqualTo("t");
		assertThat(res.getContent()).isEqualTo("c");
		assertThat(res.getAnswerType()).isEqualTo(ProblemType.SINGLE_CHOICE);
		assertThat(res.getChoices()).containsExactly("A");
		assertThat(res.getAnswerCorrectRate()).isNull();
	}

	@Test
	@DisplayName("ProblemInfoDto → ProblemResponse, 시도 30 이상이면 정답률 계산")
	void toProblemResponse_fromInfo_sufficientAttempts_rateCalculated() {
		ProblemInfoDto info = new ProblemInfoDto(1L, "t", "c", ProblemType.SINGLE_CHOICE, List.of(), 30, 20);

		ProblemResponse res = mapper.toProblemResponse(info);

		assertThat(res.getAnswerCorrectRate()).isEqualTo(67);
	}

	@Test
	@DisplayName("Problem + choices → ProblemResponse")
	void toProblemResponse_fromEntity_mapsFieldsAndRate() {
		given(problem.getId()).willReturn(5L);
		given(problem.getTitle()).willReturn("title");
		given(problem.getContent()).willReturn("body");
		given(problem.getType()).willReturn(ProblemType.MULTI_CHOICE);
		given(problem.getProblemStatus()).willReturn(status);
		given(status.getTotalAttempts()).willReturn(30);
		given(status.getCorrectAttempts()).willReturn(15);

		ProblemResponse res = mapper.toProblemResponse(problem, List.of("x", "y"));

		assertThat(res.getProblemId()).isEqualTo(5L);
		assertThat(res.getChoices()).containsExactly("x", "y");
		assertThat(res.getAnswerCorrectRate()).isEqualTo(50);
	}

	@Test
	@DisplayName("Problem + choices — status 없으면 정답률 null")
	void toProblemResponse_fromEntity_noStatus_rateNull() {
		given(problem.getId()).willReturn(1L);
		given(problem.getTitle()).willReturn("t");
		given(problem.getContent()).willReturn("c");
		given(problem.getType()).willReturn(ProblemType.SHORT_ANSWER);
		given(problem.getProblemStatus()).willReturn(null);

		ProblemResponse res = mapper.toProblemResponse(problem, List.of());

		assertThat(res.getAnswerCorrectRate()).isNull();
	}

	@Test
	@DisplayName("Problem → ProblemSummaryResponse")
	void toSummaryResponse_mapsFieldsAndRate() {
		given(problem.getId()).willReturn(2L);
		given(problem.getTitle()).willReturn("s");
		given(problem.getContent()).willReturn("d");
		given(problem.getType()).willReturn(ProblemType.SINGLE_CHOICE);
		given(problem.getProblemStatus()).willReturn(status);
		given(status.getTotalAttempts()).willReturn(30);
		given(status.getCorrectAttempts()).willReturn(10);

		ProblemSummaryResponse res = mapper.toSummaryResponse(problem, List.of("c"));

		assertThat(res.getProblemId()).isEqualTo(2L);
		assertThat(res.getChoices()).containsExactly("c");
		assertThat(res.getAnswerCorrectRate()).isEqualTo(33);
	}

	@Test
	@DisplayName("ProblemDetailDto → ProblemDetailResponse")
	void toProblemDetailResponse_mapsAll() {
		ProblemDetailDto dto = new ProblemDetailDto(
			9L, "tit", "con", "exp", ProblemType.SINGLE_CHOICE, List.of(2), null, 30, 21
		);

		ProblemDetailResponse res = mapper.toProblemDetailResponse(dto);

		assertThat(res.getProblemId()).isEqualTo(9L);
		assertThat(res.getTitle()).isEqualTo("tit");
		assertThat(res.getContent()).isEqualTo("con");
		assertThat(res.getExplanation()).isEqualTo("exp");
		assertThat(res.getType()).isEqualTo(ProblemType.SINGLE_CHOICE);
		assertThat(res.getCorrectChoices()).containsExactly(2);
		assertThat(res.getCorrectTextAnswer()).isNull();
		assertThat(res.getAnswerCorrectRate()).isEqualTo(70);
	}
}
