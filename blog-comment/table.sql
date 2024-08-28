CREATE TABLE `tb_blog`
(
    `id`          BIGINT(20) UNSIGNED                                            NOT NULL AUTO_INCREMENT COMMENT '主键',
    `shop_id`     BIGINT(20)                                                     NOT NULL COMMENT '商户id',
    `user_id`     BIGINT(20) UNSIGNED                                            NOT NULL COMMENT '用户id',
    `title`       VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '标题',
    `images`      VARCHAR(2048)                                                  NOT NULL COMMENT '图片，最多九张，以 "," 隔开',
    `content`     VARCHAR(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '笔记内容',
    `liked`       INT(8) UNSIGNED ZEROFILL                                                DEFAULT '00000000' COMMENT '点赞数量',
    `comments`    INT(8) UNSIGNED ZEROFILL                                                DEFAULT NULL COMMENT '评论数量',
    `create_time` TIMESTAMP                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 9
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `tb_user`
(
    `id`       BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`     VARCHAR(20)         NOT NULL COMMENT '名称',
    `nickname` VARCHAR(20)         NOT NULL COMMENT '昵称',
    `avatar`   VARCHAR(255)        NOT NULL COMMENT '头像',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

INSERT INTO `tb_user` (name, nickname, avatar)
VALUES ('12345678912', 'tom', 'http://127.0.0.1:9000/image/2024-08-27/9c69723401b6445bb91a187f0d9e5e54-zw7p7.gif');
INSERT INTO `tb_user` (name, nickname, avatar)
VALUES ('12345678913', 'jerry', 'http://127.0.0.1:9000/image/2024-08-27/9c69723401b6445bb91a187f0d9e5e54-zw7p7.gif');
INSERT INTO `tb_user` (name, nickname, avatar)
VALUES ('12345678914', 'spark', 'http://127.0.0.1:9000/image/2024-08-27/9c69723401b6445bb91a187f0d9e5e54-zw7p7.gif');

CREATE TABLE `tb_follow`
(
    `id`             BIGINT(20) AUTO_INCREMENT NOT NULL COMMENT '主键',
    `user_id`        BIGINT(20) UNSIGNED       NOT NULL COMMENT '用户id',
    `follow_user_id` BIGINT(20) UNSIGNED       NOT NULL COMMENT '关注的用户id',
    `create_time`    TIMESTAMP                 NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

INSERT INTO `tb_follow` (user_id, follow_user_id)
VALUES (1, 2),
       (1, 3),
       (2, 3);