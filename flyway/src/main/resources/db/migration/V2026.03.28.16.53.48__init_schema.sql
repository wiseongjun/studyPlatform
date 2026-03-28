CREATE TABLE T_USER
(
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE T_CHAPTER
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100)                                               NOT NULL,
    category   VARCHAR(20) NOT NULL,
    is_delete  TINYINT(1)                                                 NOT NULL DEFAULT 0,
    created_at DATETIME                                                   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME                                                   NULL
);

CREATE TABLE T_PROBLEM
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    chapter_id  BIGINT                                          NOT NULL,
    title       VARCHAR(255)                                    NOT NULL,
    content     TEXT                                            NOT NULL,
    explanation TEXT                                            NOT NULL,
    type        VARCHAR(20) NOT NULL,
    is_delete   TINYINT(1)                                      NOT NULL DEFAULT 0,
    created_at  DATETIME                                        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at  DATETIME                                        NULL
);

CREATE TABLE T_PROBLEM_CHOICE
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id    BIGINT       NOT NULL,
    choice_number INT          NOT NULL,
    choice_text   VARCHAR(500) NOT NULL
);

CREATE TABLE T_PROBLEM_ANSWER
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id    BIGINT       NOT NULL,
    answer_number INT          NULL,
    answer_text   VARCHAR(500) NULL
);

CREATE TABLE T_PROBLEM_STATUS
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id       BIGINT NOT NULL UNIQUE,
    total_attempts   INT    NOT NULL DEFAULT 0,
    correct_attempts INT    NOT NULL DEFAULT 0
);

CREATE TABLE T_USER_PROBLEM_ATTEMPT
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT                                          NOT NULL,
    problem_id    BIGINT                                          NOT NULL,
    chapter_id    BIGINT                                          NOT NULL,
    answer_status VARCHAR(20) NOT NULL,
    attempted_at  DATETIME                                        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE T_USER_PROBLEM_ANSWER
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    attempt_id    BIGINT       NOT NULL,
    choice_number INT          NULL,
    answer_text   VARCHAR(500) NULL
);
