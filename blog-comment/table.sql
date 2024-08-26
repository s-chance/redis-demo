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