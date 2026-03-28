package com.example.problem.dto.req;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.constants.ProblemType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubmitProblemRequest {

	@Schema(description = "사용자 ID", example = "1")
	@NotNull(message = "사용자 ID는 필수입니다.")
	private Long userId;

	@Schema(description = "단원 ID", example = "1")
	@NotNull(message = "단원 ID는 필수입니다.")
	private Long chapterId;

	@Schema(description = "문제 유형", example = "SINGLE_CHOICE")
	@NotNull(message = "문제 유형은 필수입니다.")
	private ProblemType answerType;

	@Schema(description = "선택지 번호 목록 (객관식)", example = "[1, 2]")
	private List<Integer> selectedChoices;

	@Schema(description = "주관식 답안", example = "자바")
	private String textAnswer;
}
