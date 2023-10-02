CREATE TABLE app_user
(
    id                  BIGSERIAL PRIMARY KEY,
    telegram_user_id    BIGINT,
    first_login_data    TIMESTAMP,
    first_name          VARCHAR(255),
    last_name           VARCHAR(255),
    username            VARCHAR(255),
    role                VARCHAR(20),
    notification_status VARCHAR(3)
);

CREATE TABLE lessons
(
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(255),
    teacher      VARCHAR(255),
    office       INT,
    start_lesson TIME,
    end_lesson   TIME
);

CREATE TABLE schedules
(
    id              BIGSERIAL PRIMARY KEY,
    student_id      BIGINT,
    schedule_day_id BIGINT
);

CREATE TABLE schedule_days
(
    id           BIGSERIAL PRIMARY KEY,
    day_name     VARCHAR(255),
    is_even_week BOOLEAN
);

CREATE TABLE students
(
    id          BIGSERIAL PRIMARY KEY,
    first_name  VARCHAR(255),
    last_name   VARCHAR(255),
    app_user_id BIGINT
);

ALTER TABLE lessons
    ADD COLUMN schedule_day_id BIGINT;

ALTER TABLE lessons
    ADD CONSTRAINT fk_lessons_schedule_day
        FOREIGN KEY (schedule_day_id)
            REFERENCES schedule_days (id);

ALTER TABLE schedules
    ADD COLUMN student_id BIGINT;

ALTER TABLE schedules
    ADD COLUMN schedule_day_id BIGINT;

ALTER TABLE schedules
    ADD CONSTRAINT fk_schedules_student
        FOREIGN KEY (student_id)
            REFERENCES students (id);

ALTER TABLE schedules
    ADD CONSTRAINT fk_schedules_schedule_day
        FOREIGN KEY (schedule_day_id)
            REFERENCES schedule_days (id);

ALTER TABLE students
    ADD COLUMN app_user_id BIGINT;

ALTER TABLE students
    ADD CONSTRAINT fk_students_app_user
        FOREIGN KEY (app_user_id)
            REFERENCES app_user (id);
