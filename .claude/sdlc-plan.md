# SDLC Plan: User 로그인 데이터 추가 + Gateway JWT 인증 + Passport 전파

## 작업 개요
1. User DB에 로그인에 필요한 필드 추가 (loginId, password, role)
2. Gateway에서 JWT 검증 후 UserPassportDto 빌드, `X-User-Passport` 헤더로 전달
3. 각 서비스에서 헤더를 읽어 UserContext에 저장, 컨트롤러에서 참조 가능
4. 노션 "Spring Security > API Gateway, Eureka, Passport" 패턴 적용

---

## 보호 해제 확인 (2026-04-06 사용자 승인)
- `build.gradle` ✅ 수정 가능
- `flyway/` ✅ 수정 가능
- `docker/` ✅ 수정 가능

---

## 라이브러리 추가 계획

### gateway/build.gradle 추가
```groovy
// JWT 처리 (JJWT 0.12.x)
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'

// Spring Security (Reactive - WebFlux)
implementation 'org.springframework.boot:spring-boot-starter-security'
```

### common_web/build.gradle 추가
```groovy
// Spring Security (Servlet MVC)
api 'org.springframework.boot:spring-boot-starter-security'
// Passport 인프라 사용 (UserPassportDto, UserContext 등)
implementation project(':common_api')
```

### user/build.gradle 추가
```groovy
// JWT 생성 (로그인 시)
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
```

---

## 모듈별 구현 계획

### 1. Flyway Migration (flyway 모듈)

#### 신규 마이그레이션 파일
파일: `flyway/src/main/resources/db/migration/V2026.04.06.16.00.00__add_user_login_fields.sql`
```sql
-- T_USER에 로그인 필드 추가
ALTER TABLE T_USER
    ADD COLUMN login_id VARCHAR(100) NOT NULL DEFAULT '' AFTER name,
    ADD COLUMN password  VARCHAR(255) NOT NULL DEFAULT '' AFTER login_id,
    ADD COLUMN role      VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER' AFTER password;

ALTER TABLE T_USER
    ADD UNIQUE INDEX idx_user_login_id (login_id);

-- 기존 더미 사용자에 로그인 정보 추가 (비밀번호: password123)
UPDATE T_USER SET login_id = 'kim_java',
    password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    role = 'ROLE_USER' WHERE id = 1;
UPDATE T_USER SET login_id = 'lee_spring',
    password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    role = 'ROLE_USER' WHERE id = 2;
UPDATE T_USER SET login_id = 'park_algo',
    password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    role = 'ROLE_USER' WHERE id = 3;
```

---

### 2. user 모듈

#### 2-1. User Entity 수정
파일: `user/src/main/java/com/example/user/entity/User.java`
- `loginId` (VARCHAR 100, UNIQUE, NOT NULL) 추가
- `password` (VARCHAR 255, NOT NULL) 추가
- `role` (UserRole enum, VARCHAR 20) 추가
- static factory method `User.create(loginId, password, role, name)` 추가

#### 2-2. UserRole 열거형 추가 (신규)
파일: `user/src/main/java/com/example/user/entity/UserRole.java`
```java
public enum UserRole { ROLE_USER, ROLE_ADMIN }
```

#### 2-3. UserRepository 수정
파일: `user/src/main/java/com/example/user/repository/UserRepository.java`
- `Optional<User> findByLoginId(String loginId)` 추가

#### 2-4. JwtTokenProvider 추가 (신규)
파일: `user/src/main/java/com/example/user/security/JwtTokenProvider.java`
- JJWT 라이브러리 사용
- `generateToken(Long userId, String loginId, String role)` → JWT String
- Claims: `sub`=userId, `loginId`, `role`, `iat`, `exp`

#### 2-5. JwtProperties 추가 (신규)
파일: `user/src/main/java/com/example/user/security/JwtProperties.java`
- `@ConfigurationProperties("jwt")`
- `secret` (String), `expirationMs` (long)

#### 2-6. AuthService 추가 (신규)
파일: `user/src/main/java/com/example/user/service/AuthService.java`
- `login(LoginRequest)` → `LoginResponse`
  - findByLoginId → User 조회
  - BCryptPasswordEncoder로 비밀번호 검증
  - JwtTokenProvider로 토큰 생성

#### 2-7. AuthController 추가 (신규)
파일: `user/src/main/java/com/example/user/controller/AuthController.java`
- POST `/api/v1/auth/login` → LoginResponse + HttpOnly 쿠키 설정
  - 쿠키명: `access_token`, HttpOnly, SameSite=Lax, MaxAge=86400s

#### 2-8. DTO 추가
- `user/.../dto/req/LoginRequest.java` → loginId, password (validation 포함)
- `user/.../dto/res/LoginResponse.java` → userId, loginId, role

#### 2-9. user 모듈 application 설정 추가
파일: `user/src/main/resources/application.yaml` (신규 또는 기존)
```yaml
spring:
  application:
    name: user-service
jwt:
  secret: ${JWT_SECRET:study-platform-jwt-secret-key-must-be-256bit-or-more!!}
  expiration-ms: 86400000
```

---

### 3. gateway 모듈

#### 3-1. UserPassportDto (신규)
파일: `gateway/src/main/java/com/example/gateway/security/dto/UserPassportDto.java`
- userId (Long), loginId (String), role (String)
- Jackson 역직렬화를 위한 기본 생성자 포함

#### 3-2. JwtProperties (신규)
파일: `gateway/src/main/java/com/example/gateway/security/JwtProperties.java`
- `@ConfigurationProperties("jwt")`

#### 3-3. JwtTokenValidator (신규)
파일: `gateway/src/main/java/com/example/gateway/security/JwtTokenValidator.java`
- JJWT로 검증: `validateToken(token)` → boolean
- `parsePayload(token)` → UserPassportDto

#### 3-4. PassportSerializer (신규)
파일: `gateway/src/main/java/com/example/gateway/security/PassportSerializer.java`
- `serialize(dto)` → Base64(JSON) 인코딩
- Jackson ObjectMapper 사용

#### 3-5. JwtToPassportFilter (신규) — 노션 패턴 적용
파일: `gateway/src/main/java/com/example/gateway/filter/JwtToPassportFilter.java`
```java
// 노션 패턴 그대로 적용
@Component
@RequiredArgsConstructor
public class JwtToPassportFilter implements GlobalFilter, Ordered {
    private static final String PASSPORT_HEADER = "X-User-Passport";
    private static final String ACCESS_TOKEN_NAME = "access_token"; // 쿠키명
    // order = -1
    // /api/v1/auth/** 스킵
    // JWT 검증 실패 → 401
    // Mono.fromCallable(() -> buildPassport(token))
    //     .subscribeOn(Schedulers.boundedElastic())
    //     .flatMap(passport -> addHeaderAndChain)
    //     .onErrorResume(e -> return 401)
}
```

#### 3-6. SecurityConfig (신규)
파일: `gateway/src/main/java/com/example/gateway/config/SecurityConfig.java`
```java
// WebFlux Security
// /api/v1/auth/** → permitAll()
// 나머지 → JWT 검증은 JwtToPassportFilter에서 처리하므로 permitAll()
// CSRF disable (API Gateway)
```

#### 3-7. application.yaml 수정
파일: `gateway/src/main/resources/application.yaml`
```yaml
jwt:
  secret: ${JWT_SECRET:study-platform-jwt-secret-key-must-be-256bit-or-more!!}
  expiration-ms: 86400000
spring:
  security:
    user:
      name: admin  # Spring Security 기본 사용자 비활성화용
```

---

### 4. common_api 모듈

#### 4-1. UserPassportDto (신규)
파일: `common_api/src/main/java/com/example/api/passport/dto/UserPassportDto.java`
- userId (Long), loginId (String), role (String)
- @Getter @AllArgsConstructor @NoArgsConstructor

#### 4-2. PassportSerializer (신규)
파일: `common_api/src/main/java/com/example/api/passport/PassportSerializer.java`
- `serialize(dto)` → Base64(JSON)
- `deserialize(encoded)` → UserPassportDto
- ObjectMapper (Jackson) 사용

#### 4-3. UserContext (신규) — 노션 패턴 적용
파일: `common_api/src/main/java/com/example/api/passport/UserContext.java`
```java
public class UserContext {
    private static final ThreadLocal<UserPassportDto> CONTEXT = new ThreadLocal<>();
    // getUserPassport(), setUserPassport(), clearUserPassport()
    // getCurrentUserId(), getCurrentUserLoginId(), isAdmin()
}
```

#### 4-4. PassportRequestInterceptor (신규) — 노션 패턴 적용
파일: `common_api/src/main/java/com/example/api/passport/PassportRequestInterceptor.java`
- Feign RequestInterceptor
- UserContext에서 Passport 읽어 X-User-Passport 헤더 추가
- Feign 내부 통신 시 Passport 자동 전파

---

### 5. common_web 모듈

#### 5-1. PassportAuthenticationFilter (신규) — 노션 패턴 적용
파일: `common_web/src/main/java/com/example/config/PassportAuthenticationFilter.java`
```java
@Component
@RequiredArgsConstructor
public class PassportAuthenticationFilter extends OncePerRequestFilter {
    // X-User-Passport 헤더 읽기
    // Base64 디코딩 → UserPassportDto 역직렬화
    // UserContext.setUserPassport(dto)
    // finally: UserContext.clearUserPassport() — 메모리 누수 방지
    // /api/v1/auth/** 경로는 Skip
}
```

#### 5-2. SecurityConfig (신규) — 노션 패턴 적용
파일: `common_web/src/main/java/com/example/config/SecurityConfig.java`
```java
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    // cors: allowedOriginPatterns from yml
    // csrf: disable
    // session: STATELESS
    // /api/v1/auth/**: permitAll
    // /swagger-ui/**: permitAll
    // /v3/api-docs/**: permitAll
    // /internal/v1/**: permitAll (서비스 간 내부 통신)
    // anyRequest: authenticated
    // addFilterBefore(passportAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
}
```

---

## 파일 목록 (신규/수정)

### 신규 파일
| 파일 | 모듈 | 비고 |
|------|------|------|
| `flyway/.../V2026.04.06.16.00.00__add_user_login_fields.sql` | flyway | |
| `user/.../entity/UserRole.java` | user | |
| `user/.../security/JwtTokenProvider.java` | user | JJWT |
| `user/.../security/JwtProperties.java` | user | |
| `user/.../service/AuthService.java` | user | |
| `user/.../controller/AuthController.java` | user | |
| `user/.../dto/req/LoginRequest.java` | user | |
| `user/.../dto/res/LoginResponse.java` | user | |
| `user/src/main/resources/application.yaml` | user | jwt 설정 |
| `gateway/.../security/dto/UserPassportDto.java` | gateway | |
| `gateway/.../security/JwtProperties.java` | gateway | |
| `gateway/.../security/JwtTokenValidator.java` | gateway | JJWT |
| `gateway/.../security/PassportSerializer.java` | gateway | |
| `gateway/.../filter/JwtToPassportFilter.java` | gateway | 노션 패턴 |
| `gateway/.../config/SecurityConfig.java` | gateway | WebFlux |
| `common_api/.../passport/dto/UserPassportDto.java` | common_api | |
| `common_api/.../passport/PassportSerializer.java` | common_api | |
| `common_api/.../passport/UserContext.java` | common_api | ThreadLocal |
| `common_api/.../passport/PassportRequestInterceptor.java` | common_api | Feign |
| `common_web/.../config/PassportAuthenticationFilter.java` | common_web | |
| `common_web/.../config/SecurityConfig.java` | common_web | Spring Security |

### 수정 파일
| 파일 | 변경 내용 |
|------|----------|
| `user/.../entity/User.java` | loginId, password, role 필드 추가 |
| `user/.../repository/UserRepository.java` | findByLoginId() 추가 |
| `gateway/build.gradle` | JJWT, spring-security 추가 |
| `gateway/src/main/resources/application.yaml` | jwt 설정 추가 |
| `common_web/build.gradle` | spring-security, common_api 추가 |
| `user/build.gradle` | JJWT 추가 |

---

## 아키텍처 흐름

```
클라이언트
  │  POST /api/v1/auth/login  (loginId, password)
  │  ─────────────────────────────────────→ [Gateway, JWT Skip]
  │                                         → [user-service: AuthController]
  │    ← Set-Cookie: access_token=<JWT>      └ BCrypt 검증, JJWT 토큰 생성
  │
  │  GET /api/v1/user/1/problem/solved/list
  │  Cookie: access_token=<JWT>
  │  ─→ [Gateway: JwtToPassportFilter (order=-1)]
  │       ① 쿠키에서 JWT 추출
  │       ② JJWT로 서명 검증 (HMAC-SHA256)
  │       ③ Claims → UserPassportDto 빌드
  │       ④ serialize → Base64(JSON) → X-User-Passport 헤더
  │  ─→ [user-service: PassportAuthenticationFilter]
  │       ① X-User-Passport 헤더 읽기
  │       ② Base64 디코딩 → UserPassportDto
  │       ③ UserContext.setUserPassport(dto)
  │       ④ Spring Security Authentication 설정
  │  ─→ [UserController / UserService]
  │       UserContext.getCurrentUserId() 로 사용자 식별
  │
  │  내부 서비스 간 Feign 호출
  │  ─→ [PassportRequestInterceptor]
  │       UserContext에서 Passport 읽어 X-User-Passport 헤더 자동 추가
```

---

## 주의사항

1. JWT Secret: `${JWT_SECRET}` 환경변수로 관리, 개발용 기본값 포함
2. Gateway의 `UserPassportDto`와 common_api의 `UserPassportDto`는 별개 클래스 (같은 필드명 유지)
3. Gateway는 common_api/common_web에 의존하지 않으므로 직렬화 포맷(JSON 필드명) 일치 필수
4. `ddl-auto: update` 활성화되어 있어 Entity 수정 시 컬럼 자동 추가됨
5. Flyway 마이그레이션도 동시 실행 → 순서 충돌 없도록 DEFAULT '' 추가
