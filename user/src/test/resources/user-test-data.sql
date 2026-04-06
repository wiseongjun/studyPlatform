INSERT INTO T_USER (id, name, login_id, password, role) VALUES (1, 'testUser', 'test_user', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_USER');

INSERT INTO T_USER_PROBLEM_ATTEMPT (id, user_id, problem_id, chapter_id, answer_status, attempted_at)
VALUES (1, 1, 10, 1, 'CORRECT',   '2026-01-01 10:00:00'),
       (2, 1, 10, 1, 'INCORRECT', '2026-01-01 11:00:00'),
       (3, 1, 20, 1, 'CORRECT',   '2026-01-01 12:00:00'),
       (4, 1, 30, 2, 'INCORRECT', '2026-01-01 13:00:00');

INSERT INTO T_USER_PROBLEM_ANSWER (id, attempt_id, choice_number, answer_text)
VALUES (1, 1, 2,    null),
       (2, 2, 1,    null),
       (3, 3, 1,    null),
       (4, 3, 2,    null),
       (5, 4, null, '틀린 답');
