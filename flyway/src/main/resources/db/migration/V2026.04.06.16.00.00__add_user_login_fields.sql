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
