package com.example.chapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example")
public class ChapterApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChapterApplication.class, args);
	}
}
