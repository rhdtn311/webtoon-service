CREATE TABLE IF NOT EXISTS users
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    login_id   VARCHAR(15)  NOT NULL,
    name       VARCHAR(20)  NOT NULL,
    email      VARCHAR(320) NOT NULL,
    nickname   VARCHAR(15)  NOT NULL,
    password   VARCHAR(255) NOT NULL,
    authority  VARCHAR(20)  NOT NULL,
    set_alarm  BOOLEAN      NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL,
    deleted_at TIMESTAMP    NULL,
    CONSTRAINT users_email_uindex UNIQUE (email),
    CONSTRAINT users_login_id_uindex UNIQUE (login_id)
);

CREATE TABLE IF NOT EXISTS author
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_name  VARCHAR(20)  NOT NULL,
    introduction VARCHAR(500) NOT NULL,
    belong       VARCHAR(15)  NULL,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL,
    deleted_at   TIMESTAMP    NULL,
    user_id      BIGINT       NOT NULL,
    CONSTRAINT FK_user_TO_author_1 FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS comic
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(30)  NOT NULL,
    genre               VARCHAR(20)  NOT NULL,
    summary             VARCHAR(500) NOT NULL,
    publish_day_of_week VARCHAR(3)   NOT NULL,
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP    NOT NULL,
    deleted_at          TIMESTAMP    NULL,
    author_id           BIGINT       NOT NULL,
    is_complete         BOOLEAN      NOT NULL,
    CONSTRAINT FK_author_TO_comic_1 FOREIGN KEY (author_id) REFERENCES author (id)
);

CREATE TABLE IF NOT EXISTS episode
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    title          VARCHAR(255)  NOT NULL,
    episode_number INT           NOT NULL,
    thumbnail_url  VARCHAR(2048) NOT NULL,
    comic_id       BIGINT        NOT NULL,
    created_at     TIMESTAMP     NOT NULL,
    updated_at     TIMESTAMP     NOT NULL,
    deleted_at     TIMESTAMP     NULL,
    CONSTRAINT FK_comic_TO_episode_1 FOREIGN KEY (comic_id) REFERENCES comic (id)
);

CREATE TABLE IF NOT EXISTS episode_image
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    content_image_url VARCHAR(2048) NOT NULL,
    content_order     INT DEFAULT 0 NOT NULL,
    episode_id        BIGINT        NULL,
    created_at        TIMESTAMP     NOT NULL,
    updated_at        TIMESTAMP     NOT NULL,
    deleted_at        TIMESTAMP     NULL,
    CONSTRAINT episode_image_episode__fk FOREIGN KEY (episode_id) REFERENCES episode (id)
);

CREATE TABLE IF NOT EXISTS realtime_comic_ranking
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_time VARCHAR(10) NOT NULL,
    ranks       INT         NOT NULL,
    views       INT         NOT NULL,
    comic_id    BIGINT      NOT NULL,
    created_at  TIMESTAMP   NOT NULL,
    updated_at  TIMESTAMP   NOT NULL,
    record_date DATE        NOT NULL,
    CONSTRAINT FK_realtime_rank_TO_comic FOREIGN KEY (comic_id) REFERENCES comic (id)
);

CREATE TABLE IF NOT EXISTS thumbnail
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    thumbnail_type VARCHAR(10)  NOT NULL,
    image_url      VARCHAR(255) NOT NULL,
    comic_id       BIGINT       NOT NULL,
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL,
    deleted_at     TIMESTAMP    NULL,
    CONSTRAINT FK_comic_TO_comic_thumbnail_1 FOREIGN KEY (comic_id) REFERENCES comic (id)
);

CREATE TABLE IF NOT EXISTS view
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id           BIGINT    NOT NULL,
    episode_id        BIGINT    NOT NULL,
    first_access_time TIMESTAMP NOT NULL,
    last_access_time  TIMESTAMP NOT NULL,
    CONSTRAINT FK_episode_TO_view_1 FOREIGN KEY (episode_id) REFERENCES episode (id),
    CONSTRAINT FK_users_TO_view_1 FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX comic_id_AND_thumbnail_type ON thumbnail (comic_id, thumbnail_type);

CREATE INDEX comic_id_index_by_thumbnail ON thumbnail (comic_id);

CREATE INDEX last_access_time ON view (last_access_time);

CREATE INDEX last_access_time_index ON view (last_access_time);
