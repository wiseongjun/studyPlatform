package com.example.api.problem.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "problem-service")
public interface ProblemFeignClient {
}
