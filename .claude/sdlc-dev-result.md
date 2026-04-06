# Implementation Result

## Files Changed

| File | Action | Description |
|------|--------|-------------|
| `flyway/.../V2026.04.06.16.00.00__add_user_login_fields.sql` | CREATE | T_USER에 login_id, password, role 컬럼 추가 + 더미 데이터 업데이트 |
| `common_api/.../passport/dto/UserPassportDto.java` | CREATE | userId, loginId, role 필드의 Passport DTO |
| `common_api/.../passport/PassportSerializer.java` | CREATE | Base64(JSON) 직렬화/역직렬화 유틸 (static) |
| `common_api/.../passport/UserContext.java` | CREATE | ThreadLocal 기반 사용자 컨텍스트 |
| `common_api/.../passport/PassportRequestInterceptor.java` | CREATE | Feign 요청 시 X-User-Passport 헤더 자동 전파 |
| `common_api/.../passport/PassportFeignConfig.java` | CREATE | PassportRequestInterceptor를 Spring Bean으로 등록 |
| `common_api/build.gradle` | UNMODIFIED | 기존 의존성으로 충분 |
| `common_web/build.gradle` | MODIFY | spring-security, common_api, spring-cloud BOM 추가 |
| `common_web/.../config/PassportAuthenticationFilter.java` | CREATE | X-User-Passport 헤더 파싱 → UserContext 및 SecurityContext 설정 |
| `common_web/.../config/SecurityConfig.java` | CREATE | Spring Security 설정 (CSRF 비활성화, 세션 STATELESS, 경로별 인가) |
| `user/build.gradle` | MODIFY | JJWT, spring-security-test 추가 |
| `user/.../entity/UserRole.java` | CREATE | ROLE_USER, ROLE_ADMIN 열거형 |
| `user/.../entity/User.java` | MODIFY | loginId, password, role 필드 + create() 팩토리 메서드 추가 |
| `user/.../repository/UserJpaRepository.java` | CREATE | findByLoginId() Spring Data JPA 레포지토리 |
| `user/.../security/JwtProperties.java` | CREATE | @ConfigurationProperties("jwt") |
| `user/.../security/JwtTokenProvider.java` | CREATE | JJWT로 JWT 생성 |
| `user/.../dto/req/LoginRequest.java` | CREATE | 로그인 요청 DTO (validation 포함) |
| `user/.../dto/res/LoginResponse.java` | CREATE | 로그인 응답 DTO |
| `user/.../service/AuthService.java` | CREATE | BCrypt 검증 + JWT 생성 + LoginResult 반환 |
| `user/.../controller/AuthController.java` | CREATE | POST /api/v1/auth/login, HttpOnly 쿠키 설정 |
| `user/UserDetailApplication.java` | MODIFY | @EnableConfigurationProperties(JwtProperties.class) 추가 |
| `user/src/main/resources/application.yaml` | MODIFY | jwt.secret, jwt.expiration-ms 추가 |
| `user/src/test/resources/user-test-data.sql` | MODIFY | T_USER INSERT에 login_id, password, role 추가 |
| `user/.../controller/UserControllerTest.java` | MODIFY | SecurityConfig/PassportFilter import + @WithMockUser 추가 |
| `user/.../controller/InternalUserControllerTest.java` | MODIFY | SecurityConfig/PassportFilter import 추가 |
| `gateway/build.gradle` | MODIFY | JJWT, spring-boot-starter-security 추가 |
| `gateway/.../security/dto/UserPassportDto.java` | CREATE | Gateway 전용 Passport DTO |
| `gateway/.../security/JwtProperties.java` | CREATE | @ConfigurationProperties("jwt") |
| `gateway/.../security/JwtTokenValidator.java` | CREATE | JJWT 서명 검증 + Claims 파싱 |
| `gateway/.../security/PassportSerializer.java` | CREATE | UserPassportDto → Base64(JSON) |
| `gateway/.../filter/JwtToPassportFilter.java` | CREATE | 쿠키에서 JWT 추출 → 검증 → X-User-Passport 헤더 추가 (order=-1) |
| `gateway/.../config/SecurityConfig.java` | CREATE | WebFlux Security (CSRF 비활성화, permitAll) |
| `gateway/GatewayApplication.java` | MODIFY | @EnableConfigurationProperties(JwtProperties.class) 추가 |
| `gateway/src/main/resources/application.yaml` | MODIFY | jwt 설정 + /api/v1/auth/** 라우팅 추가 |
| `common_core/.../exception/ErrorCode.java` | MODIFY | LOGIN_FAILED(302, 401) 추가 |
| `problem/build.gradle` | MODIFY | spring-security-test 추가 |
| `problem/.../controller/ProblemControllerTest.java` | MODIFY | SecurityConfig/PassportFilter import + @WithMockUser 추가 |
| `problem/.../ProblemApplicationIntegrationTest.java` | MODIFY | X-User-Passport 헤더를 요청에 포함하도록 수정 |
| `chapter/build.gradle` | MODIFY | spring-security-test 추가 |
| `chapter/.../controller/ChapterControllerTest.java` | MODIFY | SecurityConfig/PassportFilter import + @WithMockUser 추가 |

## Deviations from Plan

1. **UserRepository 분리**: 계획에서는 `UserRepository.findByLoginId()` 추가를 언급했으나, 기존 `UserRepository`는 QueryDSL 기반이라 JPA Repository 인터페이스 역할을 할 수 없음. `UserJpaRepository` (Spring Data JPA)를 별도 생성해 `findByLoginId()`를 제공.

2. **PassportFeignConfig 추가**: `PassportRequestInterceptor`를 Spring Bean으로 등록하는 `PassportFeignConfig` 클래스를 common_api에 추가. 계획서에는 없었으나 자동 등록을 위해 필요.

3. **common_web/build.gradle에 Spring Cloud BOM 추가**: `common_web`이 `common_api`를 의존하면서 spring-cloud openfeign 버전 해결이 필요해 BOM 추가.

4. **PassportAuthenticationFilter에 SecurityContext 설정 추가**: X-User-Passport 헤더가 있을 때 `SecurityContextHolder`에 `UsernamePasswordAuthenticationToken`을 설정. 없으면 `anyRequest().authenticated()` 정책으로 403 반환됨.

5. **기존 테스트 수정**: Spring Security 도입으로 인해 기존 컨트롤러 테스트(user, problem, chapter) 및 통합 테스트(problem)가 깨짐. SecurityConfig, PassportAuthenticationFilter import + @WithMockUser/@WithMockUser 또는 Passport 헤더 추가로 수정.

6. **spring-security-test 의존성 추가**: user, problem, chapter build.gradle에 `testImplementation` 추가.

## Build Status

- Compilation: **PASS** (모든 모듈)
- Tests: **PASS** (전체 빌드 성공, Checkstyle warning 1건)
- Checkstyle: warning 1건 (error 없음)

## Notes for QA

1. **JWT 쿠키 인증 흐름**: `POST /api/v1/auth/login` → 응답 쿠키 `access_token` → 이후 요청에서 Gateway가 쿠키 검증 → X-User-Passport 헤더로 각 서비스에 전달
2. **JWT Secret 동기화**: gateway와 user 서비스 모두 `JWT_SECRET` 환경변수 또는 기본값 `study-platform-jwt-secret-key-must-be-256bit-or-more!!` 사용. 반드시 동일한 값이어야 함.
3. **더미 사용자 로그인 정보**: kim_java / lee_spring / park_algo (비밀번호: password123)
4. **내부 API (/internal/v1/**)는 인증 없이 접근 가능**: Feign 내부 통신용
5. **`/api/v1/auth/**`는 Gateway에서 JWT 검증 스킵**: 로그인 요청이 통과될 수 있음
6. **Flyway 마이그레이션**: `ddl-auto: update`와 동시 실행 시 순서 충돌 없도록 DEFAULT '' 지정됨
