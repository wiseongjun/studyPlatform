package com.example.flyway;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class FlywayRunner implements CommandLineRunner {

	@Value("${spring.flyway.baseline}")
	private Boolean isBaseLine;

	@Autowired
	Flyway flyway;

	@Override
	public void run(String... args) throws Exception {
		if (isBaseLine) {
			flyway.baseline();
		}
		flyway.migrate();
	}
}
