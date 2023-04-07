DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
	`id`	bigint	NOT NULL,
	`login_id`	varchar(15)	NOT NULL,
	`name`	varchar(20)	NOT NULL,
	`email`	varchar(320)	NOT NULL,
	`nickname`	varchar(15)	NOT NULL,
	`password`	varchar(255)	NOT NULL,
	`authority`	varchar(20)	NOT NULL,
	`set_alarm`	boolean	NOT NULL,
	`created_at`	Timestamp	NOT NULL,
	`updated_at`	Timestamp	NOT NULL,
	`deleted_at`	Timestamp	NULL
);

DROP TABLE IF EXISTS `author`;

CREATE TABLE `author` (
	`id`	bigint	NOT NULL,
	`author_name`	varchar(20) NOT	NULL,
	`introduction`	varchar(500)	NOT NULL,
	`belong`	varchar(15)	NULL,
	`created_at`	Timestamp	NOT NULL,
	`updated_at`	Timestamp	NOT NULL,
	`deleted_at`	Timestamp	NULL,
	`user_id`	bigint	NOT NULL
);

DROP TABLE IF EXISTS `alarm`;

CREATE TABLE `alarm` (
	`id`	bigint	NOT NULL,
	`check`	boolean	NOT NULL,
	`created_at`	Timestamp	NOT NULL,
	`deleted_at`	Timestamp	NULL,
	`user_id`	bigint	NOT NULL,
	`comic_id`	bigint	NOT NULL
);

DROP TABLE IF EXISTS `comic`;

CREATE TABLE `comic` (
	`id`	bigint	NOT NULL,
	`name`	varchar(30)	NOT NULL,
	`genre`	varchar(10)	NOT NULL,
	`summary`	varchar(500)	NOT NULL,
	`publish_day_of_week`	varchar(3)	NOT NULL,
	`follower_count`	int	NOT NULL,
	`created_at`	Timestamp	NOT NULL,
	`updated_at`	Timestamp	NOT NULL,
	`deleted_at`	Timestamp	NULL,
	`author_id`	bigint	NOT NULL
);

DROP TABLE IF EXISTS `thumbnail`;

CREATE TABLE `thumbnail` (
	`id`	bigint	NOT NULL,
	`thumbnail_type`	varchar(10)	NOT NULL,
	`image_url`	varchar(2048)	NOT NULL,
	`comic_id`	bigint	NOT NULL,
	`created_at`	Timestamp	NOT NULL,
	`updated_at`	Timestamp	NOT NULL,
	`deleted_at`	Timestamp	NULL
);

DROP TABLE IF EXISTS `episode`;

CREATE TABLE `episode` (
	`id`	bigint	NOT NULL,
	`title`	varchar(255) NOT	NULL,
	`episode_number`	int	NOT NULL,
	`content_image_url`	varchar(2048)	NOT NULL,
	`thumbnail_url`	varchar(2048)	NOT NULL,
	`like_count`	int	NOT NULL,
	`comic_id`	bigint	NOT NULL,
	`created_at`	Timestamp	NOT NULL,
	`updated_at`	Timestamp	NOT NULL,
	`deleted_at`	Timestamp	NULL
);

DROP TABLE IF EXISTS `follow`;

CREATE TABLE `follow` (
	`id`	bigint	NOT NULL,
	`created_at`	Timestamp	NOT NULL,
	`user_id`	bigint	NOT NULL,
	`comic_id`	bigint	NOT NULL
);

DROP TABLE IF EXISTS `view`;

CREATE TABLE `view` (
	`id`	bigint	NOT NULL,
	`user_id`	bigint	NOT NULL,
	`episode_id`	bigint	NOT NULL,
	`first_access_time`	Timestamp NOT NULL,
	`last_access_time`	Timestamp NOT NULL
);

DROP TABLE IF EXISTS `comment`;

CREATE TABLE `comment` (
	`id`	bigint	NOT NULL,
	`content`	varchar(500)	NOT NULL,
	`parent_id`	bigint	NULL,
	`like_count`	int	NOT NULL,
	`visible`	boolean	NOT NULL,
	`created_at`	Timestamp	NOT NULL,
	`updated_at`	Timestamp	NOT NULL,
	`deleted_at`	Timestamp	NULL,
	`user_id`	bigint	NOT NULL,
	`episode_id`	bigint	NOT NULL
);

DROP TABLE IF EXISTS `likes`;

CREATE TABLE `likes` (
	`id`	bigint	NOT NULL,
	`like_type`	varchar(15)	NOT NULL,
	`reference_id`	bigint	NOT NULL,
	`user_id`	bigint	NOT NULL,
	`created_at`	Timestamp	NOT NULL
);

DROP TABLE IF EXISTS `star`;

CREATE TABLE `star` (
	`id`	bigint	NOT NULL,
	`score`	int	NOT NULL,
	`created_at`	Timestamp	NOT NULL,
	`updated_at`	Timestamp	NOT NULL,
	`user_id`	bigint	NOT NULL,
	`episode_id`	bigint	NOT NULL
);

DROP TABLE IF EXISTS `comment_hide`;

CREATE TABLE `comment_hide` (
	`id`	bigint	NOT NULL,
	`created_at`	Timestamp	NOT NULL,
	`deleted_at`	Timestamp	NULL,
	`user_id`	bigint	NOT NULL,
	`comment_id`	bigint	NOT NULL
);

DROP TABLE IF EXISTS `comment_report`;

CREATE TABLE `comment_report` (
	`id`	bigint	NOT NULL,
	`report_reason`	varchar(50)	NULL,
	`created_at`	Timestamp	NOT NULL,
	`deleted_at`	Timestamp	NULL,
	`user_id`	bigint	NOT NULL,
	`comment_id`	bigint	NOT NULL
);

ALTER TABLE `users` ADD CONSTRAINT `PK_user` PRIMARY KEY (
	`id`
);

ALTER TABLE `author` ADD CONSTRAINT `PK_author` PRIMARY KEY (
	`id`
);

ALTER TABLE `alarm` ADD CONSTRAINT `PK_alarm` PRIMARY KEY (
	`id`
);

ALTER TABLE `comic` ADD CONSTRAINT `PK_comic` PRIMARY KEY (
	`id`
);

ALTER TABLE `thumbnail` ADD CONSTRAINT `PK_thumbnail` PRIMARY KEY (
	`id`
);

ALTER TABLE `episode` ADD CONSTRAINT `PK_episode` PRIMARY KEY (
	`id`
);

ALTER TABLE `follow` ADD CONSTRAINT `PK_follow` PRIMARY KEY (
	`id`
);

ALTER TABLE `view` ADD CONSTRAINT `PK_view` PRIMARY KEY (
	`id`
);

ALTER TABLE `comment` ADD CONSTRAINT `PK_comment` PRIMARY KEY (
	`id`
);

ALTER TABLE `likes` ADD CONSTRAINT `PK_like` PRIMARY KEY (
	`id`
);

ALTER TABLE `star` ADD CONSTRAINT `PK_star` PRIMARY KEY (
	`id`
);

ALTER TABLE `comment_hide` ADD CONSTRAINT `PK_comment_hide` PRIMARY KEY (
	`id`
);

ALTER TABLE `comment_report` ADD CONSTRAINT `PK_comment_report` PRIMARY KEY (
	`id`
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


