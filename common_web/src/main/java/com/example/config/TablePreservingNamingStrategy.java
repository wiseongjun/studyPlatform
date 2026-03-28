package com.example.config;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * 테이블명은 @Table(name) 값 그대로 유지하고,
 * 컬럼명은 기존 camelCase → snake_case 변환을 유지하는 네이밍 전략.
 */
public class TablePreservingNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {

	@Override
	public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
		return name;
	}
}
