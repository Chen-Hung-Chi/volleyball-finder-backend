-- ─────────────────── Drop old tables first ───────────────────
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS activity_participants;
DROP TABLE IF EXISTS activities;
DROP TABLE IF EXISTS sponsors;
DROP TABLE IF EXISTS users;

-- ─────────────────── users ───────────────────
CREATE TABLE IF NOT EXISTS users
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    line_id        VARCHAR(255) NOT NULL UNIQUE,
    fcm_token      VARCHAR(512),
    real_name      VARCHAR(255),
    nickname       VARCHAR(255),
    phone          VARCHAR(20),
    is_verified    BOOLEAN                                                 DEFAULT FALSE,
    role           ENUM ('USER', 'SPONSOR', 'ADMIN')                       DEFAULT 'USER',
    gender         ENUM ('MALE', 'FEMALE')                                 DEFAULT 'MALE',
    position       ENUM ('SPIKER', 'SETTER', 'LIBERO', 'NONE')             DEFAULT 'NONE',
    level          ENUM ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT') DEFAULT 'BEGINNER',
    volleyball_age INT                                                     DEFAULT 0,
    avatar         VARCHAR(512),
    city           VARCHAR(50),
    district       VARCHAR(50),
    introduction   TEXT,

    created_at     DATETIME                                                DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME                                                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_nickname (nickname),
    INDEX idx_city (city),
    INDEX idx_district (district),
    INDEX idx_line_id (line_id),
    INDEX idx_created_at (created_at)
);

-- ─────────────────── sponsors ───────────────────
CREATE TABLE IF NOT EXISTS sponsors
(
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '贊助商主鍵 ID',
    user_id                 BIGINT       NOT NULL COMMENT '對應的使用者 ID（sponsors 登入帳號）',
    name                    VARCHAR(255) NOT NULL COMMENT '贊助商名稱（如球館或品牌名稱）',
    contact_email           VARCHAR(255) COMMENT '聯絡用電子信箱',
    phone                   VARCHAR(50) COMMENT '聯絡電話',
    description             TEXT COMMENT '贊助商介紹/描述',
    logo_url                VARCHAR(512) COMMENT 'Logo 圖片網址',
    website_url             VARCHAR(512) COMMENT '網站連結',
    is_active               BOOLEAN                        DEFAULT TRUE COMMENT '是否啟用（可用來停用贊助商帳號）',
    use_line_pay            BOOLEAN                        DEFAULT FALSE COMMENT '是否啟用 LINE Pay 功能',
    line_pay_channel_id     VARCHAR(255) COMMENT 'LINE Pay Channel ID',
    line_pay_channel_secret VARBINARY(255) COMMENT 'LINE Pay Channel Secret',
    line_pay_mode           ENUM ('SANDBOX', 'PRODUCTION') DEFAULT 'SANDBOX' COMMENT 'LINE Pay 環境（測試或正式）',
    created_at              DATETIME                       DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at              DATETIME                       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最後更新時間',

    FOREIGN KEY (user_id) REFERENCES users (id),

    INDEX idx_user_id (user_id),
    INDEX idx_name (name),
    INDEX idx_is_active (is_active)
) COMMENT ='贊助商（Sponsor）資訊表，包含 LINE Pay 設定與合作資訊';

-- ─────────────────── activities ───────────────────
CREATE TABLE IF NOT EXISTS activities
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Activity ID, auto-increment primary key',
    title                VARCHAR(50)                    NOT NULL COMMENT 'Activity title',
    description          TEXT COMMENT 'Activity description',
    date_time            DATETIME                       NOT NULL COMMENT 'Activity start time',
    duration             INT                                     DEFAULT 60 COMMENT 'Activity duration (minutes), default 60',
    location             VARCHAR(50)                    NOT NULL COMMENT 'Activity location',
    net_type             ENUM ('MEN', 'WOMEN', 'MIXED') NOT NULL DEFAULT 'MEN' COMMENT 'Net height type (MEN, WOMEN, MIXED)',
    max_participants     INT                            NOT NULL COMMENT 'Maximum number of participants',
    current_participants INT                            NOT NULL DEFAULT 1 COMMENT 'Current number of participants',
    male_quota           INT                                     DEFAULT 0 COMMENT 'Male quota limit (-1: forbidden, 0: unlimited, >0: limit)',
    female_quota         INT                                     DEFAULT 0 COMMENT 'Female quota limit (-1: forbidden, 0: unlimited, >0: limit)',
    male_count           INT                                     DEFAULT 0 COMMENT 'Current count of male participants',
    female_count         INT                                     DEFAULT 0 COMMENT 'Current count of female participants',
    female_priority      BOOLEAN                        NOT NULL DEFAULT FALSE COMMENT 'Female priority for waitlist (true if enabled)',
    amount               INT                                     DEFAULT 0 COMMENT 'Registration fee',
    require_verification BOOLEAN                        NOT NULL DEFAULT FALSE COMMENT '是否需要實名認證才能報名',
    city
                         VARCHAR(50)                    NOT NULL COMMENT 'City',
    district             VARCHAR(50)                    NOT NULL COMMENT 'District',

    created_by           BIGINT                         NOT NULL COMMENT 'Activity creator ID',
    created_at           DATETIME                                DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_at           DATETIME                                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',

    FOREIGN KEY (created_by) REFERENCES users (id),
    INDEX idx_activities_date_time (date_time DESC)
) COMMENT ='Volleyball activities table';

-- ─────────────────── activity_participants ───────────────────
CREATE TABLE IF NOT EXISTS activity_participants
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Participation record ID, auto-increment',
    activity_id BIGINT NOT NULL COMMENT 'Activity ID',
    user_id     BIGINT NOT NULL COMMENT 'User ID',
    is_captain  BOOLEAN  DEFAULT FALSE COMMENT 'Is captain',
    is_waiting  BOOLEAN  DEFAULT FALSE COMMENT 'Is on waitlist (true if waitlisted)',
    is_deleted  BOOLEAN  DEFAULT FALSE COMMENT 'Is cancelled (soft delete)',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Registration time',
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',

    FOREIGN KEY (activity_id) REFERENCES activities (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    UNIQUE KEY uq_activity_user_active (activity_id, user_id)
);

-- Index for quickly finding if a user is the captain for an activity
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

-- ─────────────────── Test Data: 100 Users ───────────────────
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

-- ─────────────────── Test Data: 100 Activities ───────────────────
INSERT INTO activities (title, description, date_time, duration, location,
                        max_participants, current_participants, amount,
                        city, district, created_by,
                        net_type, male_count, female_count, female_priority)
SELECT CONCAT('Volleyball Game ', n),
       CONCAT('Description for game ', n),
       CASE
           WHEN n <= 50
               THEN DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY) -- Past
           ELSE DATE_ADD(NOW(), INTERVAL FLOOR(RAND() * 30) DAY) -- Future
           END,
       FLOOR(RAND() * 180) + 30,  -- duration: 30-210 minutes
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
-- ─────────────────── Test Data: Participants ───────────────────
INSERT INTO activity_participants (activity_id, user_id, is_captain)
WITH RECURSIVE
    activity_users AS (
        SELECT a.id AS activity_id,
               u.id AS user_id,
               ROW_NUMBER() OVER (PARTITION BY a.id ORDER BY CRC32(CONCAT(a.id, '-', u.id))) AS rn, -- MySQL 9.4 requires a single deterministic numeric/temporal ORDER BY in window
               a.max_participants,
               a.current_participants
        FROM activities a
                 CROSS JOIN users u
        WHERE u.id <> a.created_by
    ),
    filtered_users AS (
        SELECT activity_id, user_id, rn, max_participants, current_participants
        FROM activity_users
        WHERE rn <= current_participants + 6
    )
SELECT activity_id, user_id, FALSE
FROM filtered_users
WHERE RAND() < 0.7;
-- 70% become participants

-- Ensure activity creator is always a participant/captain
INSERT INTO activity_participants (activity_id, user_id, is_captain)
SELECT id, created_by, TRUE
FROM activities;
