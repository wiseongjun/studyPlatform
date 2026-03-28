package com.example.problem.dto.res;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.constants.ProblemType;

@Schema(description = "랜덤 문제 조회 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemResponse {

	@Schema(description = "문제 ID", example = "1")
	private Long problemId;

	@Schema(description = "문제 제목", example = "GC의 역할")
	private String title;

	@Schema(description = "문제 내용", example = "Java에서 GC의 역할은 무엇인가요?")
	private String content;

	@Schema(description = "문제 유형")
	private ProblemType answerType;

	@Schema(description = "선택지 목록 (객관식)", example = "[\"메모리 할당\", \"메모리 해제\", \"스레드 관리\", \"예외 처리\", \"파일 입출력\"]")
	private List<String> choices;

	@Schema(description = "정답률 (30명 미만 풀었을 경우 null)", example = "67")
	private Integer answerCorrectRate;
}
