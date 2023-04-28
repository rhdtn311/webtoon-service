DROP TABLE IF EXISTS `users`;

CREATE TABLE `users`
(
    `id`         bigint AUTO_INCREMENT PRIMARY KEY,
    `login_id`   varchar(15)  NOT NULL UNIQUE,
    `name`       varchar(20)  NOT NULL,
    `email`      varchar(320) NOT NULL UNIQUE,
    `nickname`   varchar(15)  NOT NULL,
    `password`   varchar(255) NOT NULL,
    `authority`  varchar(20)  NOT NULL,
    `set_alarm`  boolean      NOT NULL,
    `created_at` Timestamp    NOT NULL,
    `updated_at` Timestamp    NOT NULL,
    `deleted_at` Timestamp    NULL
);

DROP TABLE IF EXISTS `author`;

CREATE TABLE `author`
(
    `id`           bigint AUTO_INCREMENT PRIMARY KEY,
    `author_name`  varchar(20)  NOT NULL,
    `introduction` varchar(500) NOT NULL,
    `belong`       varchar(15)  NULL,
    `created_at`   Timestamp    NOT NULL,
    `updated_at`   Timestamp    NOT NULL,
    `deleted_at`   Timestamp    NULL,
    `user_id`      bigint       NOT NULL
);

DROP TABLE IF EXISTS `alarm`;

CREATE TABLE `alarm`
(
    `id`         bigint AUTO_INCREMENT PRIMARY KEY,
    `check`      boolean   NOT NULL,
    `created_at` Timestamp NOT NULL,
    `deleted_at` Timestamp NULL,
    `user_id`    bigint    NOT NULL,
    `comic_id`   bigint    NOT NULL
);

DROP TABLE IF EXISTS `comic`;

CREATE TABLE `comic`
(
    `id`                  bigint AUTO_INCREMENT PRIMARY KEY,
    `name`                varchar(30)  NOT NULL,
    `genre`               varchar(10)  NOT NULL,
    `summary`             varchar(500) NOT NULL,
    `publish_day_of_week` varchar(3)   NOT NULL,
    'is_complete'         boolean      NOT NULL,
    `created_at`          Timestamp    NOT NULL,
    `updated_at`          Timestamp    NOT NULL,
    `deleted_at`          Timestamp    NULL,
    `author_id`           bigint       NOT NULL
);

DROP TABLE IF EXISTS `thumbnail`;

CREATE TABLE `thumbnail`
(
    `id`             bigint AUTO_INCREMENT PRIMARY KEY,
    `thumbnail_type` varchar(10)   NOT NULL,
    `image_url`      varchar(2048) NOT NULL,
    `comic_id`       bigint        NOT NULL,
    `created_at`     Timestamp     NOT NULL,
    `updated_at`     Timestamp     NOT NULL,
    `deleted_at`     Timestamp     NULL
);


DROP TABLE IF EXISTS `episode`;

CREATE TABLE `episode`
(
    `id`             bigint AUTO_INCREMENT PRIMARY KEY,
    `title`          varchar(255)  NOT NULL,
    `episode_number` int           NOT NULL,
    `thumbnail_url`  varchar(2048) NOT NULL,
    `comic_id`       bigint        NOT NULL,
    `created_at`     Timestamp     NOT NULL,
    `updated_at`     Timestamp     NOT NULL,
    `deleted_at`     Timestamp     NULL
);

DROP TABLE IF EXISTS `episode_image`;

create table episode_image
(
    id                bigint auto_increment,
    content_image_url varchar(2048) not null,
    content_order     int default 0 not null,
    episode_id        bigint        null,
    `created_at`      Timestamp     NOT NULL,
    `updated_at`      Timestamp     NOT NULL,
    `deleted_at`      Timestamp     NULL,
    constraint episode_image_pk
        primary key (id),
    constraint episode_image_episode__fk
        foreign key (episode_id) references episode (id)
);

DROP TABLE IF EXISTS `follow`;

CREATE TABLE `follow`
(
    `id`         bigint AUTO_INCREMENT PRIMARY KEY,
    `created_at` Timestamp NOT NULL,
    `user_id`    bigint    NOT NULL,
    `comic_id`   bigint    NOT NULL
);


DROP TABLE IF EXISTS `view`;

CREATE TABLE `view`
(
    `id`                bigint AUTO_INCREMENT PRIMARY KEY,
    `user_id`           bigint    NOT NULL,
    `episode_id`        bigint    NOT NULL,
    `first_access_time` Timestamp NOT NULL,
    `last_access_time`  Timestamp NOT NULL
);


DROP TABLE IF EXISTS `comment`;

CREATE TABLE `comment`
(
    `id`         bigint AUTO_INCREMENT PRIMARY KEY,
    `content`    varchar(500) NOT NULL,
    `parent_id`  bigint       NULL,
    `visible`    boolean      NOT NULL,
    `created_at` Timestamp    NOT NULL,
    `updated_at` Timestamp    NOT NULL,
    `deleted_at` Timestamp    NULL,
    `user_id`    bigint       NOT NULL,
    `episode_id` bigint       NOT NULL
);


DROP TABLE IF EXISTS `likes`;

CREATE TABLE `likes`
(
    `id`           bigint AUTO_INCREMENT PRIMARY KEY,
    `like_type`    varchar(15) NOT NULL,
    `reference_id` bigint      NOT NULL,
    `user_id`      bigint      NOT NULL,
    `created_at`   Timestamp   NOT NULL
);


DROP TABLE IF EXISTS `star`;

CREATE TABLE `star`
(
    `id`         bigint AUTO_INCREMENT PRIMARY KEY,
    `score`      int       NOT NULL,
    `created_at` Timestamp NOT NULL,
    `updated_at` Timestamp NOT NULL,
    `user_id`    bigint    NOT NULL,
    `episode_id` bigint    NOT NULL
);

DROP TABLE IF EXISTS `comment_hide`;

CREATE TABLE `comment_hide`
(
    `id`         bigint AUTO_INCREMENT PRIMARY KEY,
    `created_at` Timestamp NOT NULL,
    `deleted_at` Timestamp NULL,
    `user_id`    bigint    NOT NULL,
    `comment_id` bigint    NOT NULL
);

DROP TABLE IF EXISTS `comment_report`;

CREATE TABLE `comment_report`
(
    `id`            bigint AUTO_INCREMENT PRIMARY KEY,
    `report_reason` varchar(50) NULL,
    `created_at`    Timestamp   NOT NULL,
    `deleted_at`    Timestamp   NULL,
    `user_id`       bigint      NOT NULL,
    `comment_id`    bigint      NOT NULL
);

ALTER TABLE `author` ADD CONSTRAINT `FK_user_TO_author_1` FOREIGN KEY (
	`user_id`
)
REFERENCES `users` (
	`id`
);

ALTER TABLE `alarm` ADD CONSTRAINT `FK_users_TO_alarm_1` FOREIGN KEY (
	`user_id`
)
REFERENCES `users` (
	`id`
);

ALTER TABLE `alarm` ADD CONSTRAINT `FK_comic_TO_alarm_1` FOREIGN KEY (
	`comic_id`
)
REFERENCES `comic` (
	`id`
);

ALTER TABLE `comic` ADD CONSTRAINT `FK_author_TO_comic_1` FOREIGN KEY (
	`author_id`
)
REFERENCES `author` (
	`id`
);

ALTER TABLE `thumbnail` ADD CONSTRAINT `FK_comic_TO_comic thumbnail_1` FOREIGN KEY (
	`comic_id`
)
REFERENCES `comic` (
	`id`
);

ALTER TABLE `episode` ADD CONSTRAINT `FK_comic_TO_episode_1` FOREIGN KEY (
	`comic_id`
)
REFERENCES `comic` (
	`id`
);

ALTER TABLE `follow` ADD CONSTRAINT `FK_users_TO_follow_1` FOREIGN KEY (
	`user_id`
)
REFERENCES `users` (
	`id`
);

ALTER TABLE `follow` ADD CONSTRAINT `FK_comic_TO_follow_1` FOREIGN KEY (
	`comic_id`
)
REFERENCES `comic` (
	`id`
);

ALTER TABLE `view` ADD CONSTRAINT `FK_users_TO_view_1` FOREIGN KEY (
	`user_id`
)
REFERENCES `users` (
	`id`
);

ALTER TABLE `view` ADD CONSTRAINT `FK_episode_TO_view_1` FOREIGN KEY (
	`episode_id`
)
REFERENCES `episode` (
	`id`
);

ALTER TABLE `comment` ADD CONSTRAINT `FK_users_TO_comment_1` FOREIGN KEY (
	`user_id`
)
REFERENCES `users` (
	`id`
);

ALTER TABLE `comment` ADD CONSTRAINT `FK_episode_TO_comment_1` FOREIGN KEY (
	`episode_id`
)
REFERENCES `episode` (
	`id`
);

ALTER TABLE `likes` ADD CONSTRAINT `FK_users_TO_likes_1` FOREIGN KEY (
	`user_id`
)
REFERENCES `users` (
	`id`
);

ALTER TABLE `star` ADD CONSTRAINT `FK_users_TO_star_1` FOREIGN KEY (
	`user_id`
)
REFERENCES `users` (
	`id`
);

ALTER TABLE `star` ADD CONSTRAINT `FK_episode_TO_star_1` FOREIGN KEY (
	`episode_id`
)
REFERENCES `episode` (
	`id`
);

ALTER TABLE `comment_hide` ADD CONSTRAINT `FK_users_TO_comment_hide_1` FOREIGN KEY (
	`user_id`
)
REFERENCES `users` (
	`id`
);

ALTER TABLE `comment_hide` ADD CONSTRAINT `FK_comment_TO_comment_hide_1` FOREIGN KEY (
	`comment_id`
)
REFERENCES `comment` (
	`id`
);

ALTER TABLE `comment_report` ADD CONSTRAINT `FK_users_TO_comment_report_1` FOREIGN KEY (
	`user_id`
)
REFERENCES `users` (
	`id`
);

ALTER TABLE `comment_report` ADD CONSTRAINT `FK_comment_TO_comment_report_1` FOREIGN KEY (
	`comment_id`
)
REFERENCES `comment` (
	`id`
);


