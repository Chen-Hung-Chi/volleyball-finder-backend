-- ─────────────────── 先清除舊表 ───────────────────
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS activity_participants;
DROP TABLE IF EXISTS activities;
DROP TABLE IF EXISTS users;

-- ─────────────────── users ───────────────────
CREATE TABLE IF NOT EXISTS users
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    line_id        VARCHAR(255) NOT NULL UNIQUE,
    real_name      VARCHAR(255),
    nickname       VARCHAR(255),
    gender         ENUM ('MALE', 'FEMALE')                                 DEFAULT 'MALE',
    position       ENUM ('SPIKER', 'SETTER', 'LIBERO', 'NONE')             DEFAULT 'NONE',
    level          ENUM ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT') DEFAULT 'BEGINNER',
    volleyball_age INT                                                     DEFAULT 0,
    avatar         VARCHAR(255),
    city           VARCHAR(50),
    district       VARCHAR(50),
    introduction   TEXT,
    created_at     DATETIME                                                DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME                                                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ─────────────────── activities ───────────────────
CREATE TABLE IF NOT EXISTS activities
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '活動 ID，自動遞增主鍵',
    title                VARCHAR(50)                    NOT NULL COMMENT '活動標題',
    description          TEXT COMMENT '活動描述',
    date_time            DATETIME                       NOT NULL COMMENT '活動開始時間',
    duration             INT                                     DEFAULT 60 COMMENT '活動持續時間（分鐘），預設為 60',
    location             VARCHAR(50)                    NOT NULL COMMENT '活動地點',
    net_type             ENUM ('MEN', 'WOMEN', 'MIXED') NOT NULL DEFAULT 'MEN' COMMENT '網高類型（MEN: 男網, WOMEN: 女網, MIXED: 綜合網）',
    max_participants     INT                            NOT NULL COMMENT '最多報名人數',
    current_participants INT                            NOT NULL DEFAULT 1 COMMENT '現在報名人數',
    male_quota           INT                                     DEFAULT 0 COMMENT '男生名額上限（-1: 禁止, 0: 不限制, >0: 限制）',
    female_quota         INT                                     DEFAULT 0 COMMENT '女生名額上限（-1: 禁止, 0: 不限制, >0: 限制）',
    male_count           INT                                     DEFAULT 0 COMMENT '目前報名的男生人數',
    female_count         INT                                     DEFAULT 0 COMMENT '目前報名的女生人數',
    female_priority      BOOLEAN                        NOT NULL DEFAULT FALSE COMMENT '女生優先候補（true 為啟用）',
    amount               INT                                     DEFAULT 0 COMMENT '報名費用',

    city                 VARCHAR(50)                    NOT NULL COMMENT '城市',
    district             VARCHAR(50)                    NOT NULL COMMENT '行政區',

    created_by           BIGINT                         NOT NULL COMMENT '活動發起人 ID',
    created_at           DATETIME                                DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at           DATETIME                                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',

    FOREIGN KEY (created_by) REFERENCES users (id),
    INDEX idx_activities_date_time (date_time DESC)
) COMMENT ='排球活動表';

-- ─────────────────── activity_participants ───────────────────
CREATE TABLE IF NOT EXISTS activity_participants
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '參與紀錄 ID，自動遞增',
    activity_id BIGINT NOT NULL COMMENT '活動 ID',
    user_id     BIGINT NOT NULL COMMENT '使用者 ID',
    is_captain  BOOLEAN  DEFAULT FALSE COMMENT '是否為隊長',
    is_waiting  BOOLEAN  DEFAULT FALSE COMMENT '是否為候補（true 為候補）',
    is_deleted  BOOLEAN  DEFAULT FALSE COMMENT '是否已取消（軟刪除）',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '報名時間',
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最後更新時間',

    FOREIGN KEY (activity_id) REFERENCES activities (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    UNIQUE KEY unique_participant (activity_id, user_id)
);

-- 快速查找指定活動與使用者是否為隊長
CREATE INDEX idx_ap_activity_user_captain
    ON activity_participants (activity_id, user_id, is_captain);

-- ─────────────────── notification ───────────────────
CREATE TABLE IF NOT EXISTS notification
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    title      VARCHAR(255) NOT NULL,
    content    TEXT         NOT NULL,
    is_read    BOOLEAN  DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ─────────────────── 測試資料：100 位使用者 ───────────────────
INSERT INTO users (line_id, real_name, nickname, position, level, volleyball_age,
                   avatar, city, district, introduction)
SELECT CONCAT('line_id_', n),
       CONCAT('User ', n),
       CONCAT('Player ', n),
       ELT(MOD(n, 4) + 1, 'SPIKER', 'SETTER', 'LIBERO', 'NONE'),
       CASE MOD(n, 5)
           WHEN 0 THEN NULL
           WHEN 1 THEN 'BEGINNER'
           WHEN 2 THEN 'INTERMEDIATE'
           WHEN 3 THEN 'ADVANCED'
           WHEN 4 THEN 'EXPERT'
           END,
       CASE WHEN MOD(n, 10) = 0 THEN NULL ELSE FLOOR(RAND() * 10) END,
       CONCAT('https://api.dicebear.com/7.x/avataaars/svg?seed=', n),
       ELT(MOD(n, 3) + 1, 'taipei', 'newtaipei', 'taoyuan'),
       ELT(MOD(n, 3) + 1, 'neihu', 'banqiao', 'zhongli'),
       CONCAT('Introduction for user ', n)
FROM (SELECT 1 + numbers.n + seq.n * 10 AS n
      FROM (SELECT 0 n
            UNION
            SELECT 1
            UNION
            SELECT 2
            UNION
            SELECT 3
            UNION
            SELECT 4
            UNION
            SELECT 5
            UNION
            SELECT 6
            UNION
            SELECT 7
            UNION
            SELECT 8
            UNION
            SELECT 9) numbers,
           (SELECT 0 n
            UNION
            SELECT 1
            UNION
            SELECT 2
            UNION
            SELECT 3
            UNION
            SELECT 4
            UNION
            SELECT 5
            UNION
            SELECT 6
            UNION
            SELECT 7
            UNION
            SELECT 8
            UNION
            SELECT 9) seq) numbers;

-- ─────────────── 測試資料：100 場活動 ───────────────
INSERT INTO activities (title, description, date_time, duration, location,
                        max_participants, current_participants, amount,
                        city, district, created_by,
                        net_type, male_count, female_count, female_priority)
SELECT CONCAT('Volleyball Game ', n),
       CONCAT('Description for game ', n),
       CASE
           WHEN n <= 50
               THEN DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY) -- 過去
           ELSE DATE_ADD(NOW(), INTERVAL FLOOR(RAND() * 30) DAY) -- 未來
           END,
       FLOOR(RAND() * 180) + 30,  -- duration: 30~210 分鐘
       CONCAT('Location ', n),
       12,
       CASE WHEN n <= 10 THEN 12 ELSE FLOOR(RAND() * 12) END,
       FLOOR(RAND() * 500) + 100, -- amount
       ELT(MOD(n, 3) + 1, 'taipei', 'newtaipei', 'taoyuan'),
       ELT(MOD(n, 3) + 1, 'neihu', 'banqiao', 'zhongli'),
       FLOOR(RAND() * 100) + 1,
       ELT(MOD(n, 3) + 1, 'MEN', 'WOMEN', 'MIXED'),
       FLOOR(RAND() * 7),         -- maleCount
       FLOOR(RAND() * 7),         -- femaleCount
       RAND() < 0.3               -- 30% femalePriority = true
FROM (SELECT 1 + numbers.n + seq.n * 10 AS n
      FROM (SELECT 0 n
            UNION
            SELECT 1
            UNION
            SELECT 2
            UNION
            SELECT 3
            UNION
            SELECT 4
            UNION
            SELECT 5
            UNION
            SELECT 6
            UNION
            SELECT 7
            UNION
            SELECT 8
            UNION
            SELECT 9) numbers,
           (SELECT 0 n
            UNION
            SELECT 1
            UNION
            SELECT 2
            UNION
            SELECT 3
            UNION
            SELECT 4
            UNION
            SELECT 5
            UNION
            SELECT 6
            UNION
            SELECT 7
            UNION
            SELECT 8
            UNION
            SELECT 9) seq) numbers;
-- ─────────────────── 測試資料：參與者 ───────────────────
INSERT INTO activity_participants (activity_id, user_id, is_captain)
WITH RECURSIVE
    activity_users AS (SELECT a.id                                                  AS activity_id,
                              u.id                                                  AS user_id,
                              ROW_NUMBER() OVER (PARTITION BY a.id ORDER BY RAND()) AS rn,
                              a.max_participants,
                              a.current_participants
                       FROM activities a
                                CROSS JOIN users u
                       WHERE u.id <> a.created_by),
    filtered_users AS (SELECT activity_id, user_id, rn, max_participants, current_participants
                       FROM activity_users
                       WHERE rn <= current_participants + 6)
SELECT activity_id, user_id, FALSE
FROM filtered_users
WHERE RAND() < 0.7;
-- 70% 成為參與者

-- 確保活動建立者一定是參與者／隊長
INSERT INTO activity_participants (activity_id, user_id, is_captain)
SELECT id, created_by, TRUE
FROM activities;
