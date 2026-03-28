package com.example.problem.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "랜덤 문제 조회 필터")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RandomProblemFilter {

	@Schema(description = "단원 ID", example = "1")
	@NotNull(message = "단원 ID는 필수입니다.")
	private Long chapterId;

	@Schema(description = "사용자 ID", example = "1")
	@NotNull(message = "사용자 ID는 필수입니다.")
	private Long userId;
}
