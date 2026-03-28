package com.example.problem.config;

import java.util.Random;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProblemConfig {
	// ProblemSelector 에서 사용할 Random Bean 등록
	// 랜덤이 필요없거나 여러가지 방법 필요할시 삭제 후 Strategy 생성 필요
	@Bean
	public Random random() { // 랜덤 조회에 사용되는 랜덤객체
		return new Random();
	}
}
