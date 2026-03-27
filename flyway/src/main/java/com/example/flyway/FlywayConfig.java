package com.example.flyway;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {
	@Value("${spring.flyway.target}")
	private String target;

	@Bean
	public Flyway flyway(DataSource dataSource) {
		MigrationVersion version;
		if (target.equals("0")) {
			version = MigrationVersion.LATEST;
		} else {
			version = MigrationVersion.fromVersion(target);
		}

		Flyway flyway = Flyway
			.configure()
			.dataSource(dataSource)
			.outOfOrder(true)
			.target(version)
			.load();

		flyway.repair();

		return flyway;
	}
}
