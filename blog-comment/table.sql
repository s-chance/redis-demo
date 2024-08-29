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

CREATE TABLE `tb_shop`
(
    `id`        BIGINT(20) AUTO_INCREMENT NOT NULL COMMENT '主键',
    `name`      VARCHAR(255)              NOT NULL COMMENT '商铺名称',
    `type_id`   INT(8) UNSIGNED           NOT NULL COMMENT '商铺分类',
    `images`    VARCHAR(255)              NOT NULL COMMENT '商铺图片',
    `area`      VARCHAR(255)              NOT NULL COMMENT '地区',
    `address`   VARCHAR(255)              NOT NULL COMMENT '地址',
    `x`         DECIMAL(9, 6)             NOT NULL COMMENT '经度',
    `y`         DECIMAL(8, 6)             NOT NULL COMMENT '纬度',
    `avg_price` INT(8)           DEFAULT NULL COMMENT '平均价格',
    `sold`      INT(10) UNSIGNED DEFAULT NULL COMMENT '销量',
    `comments`  INT(8) UNSIGNED  DEFAULT NULL COMMENT '评论数量',
    `score`     TINYINT(1)       DEFAULT NULL COMMENT '评分1-5',
    PRIMARY KEY (`id`)
);

INSERT INTO `tb_shop` (`name`, `type_id`, `images`, `area`, `address`, `x`, `y`, `avg_price`, `sold`, `comments`,
                       `score`)
VALUES ('店铺A', 1, 'image1.jpg', '北京', '北京市朝阳区', 116.46, 39.92, 100, 150, 20, 4),
       ('店铺B', 1, 'image2.jpg', '上海', '上海市浦东新区', 121.47, 31.23, 200, 300, 50, 5),
       ('店铺C', 1, 'image3.jpg', '广州', '广州市天河区', 113.33, 23.13, 150, 250, 30, 3),
       ('店铺D', 1, 'image4.jpg', '深圳', '深圳市南山区', 114.07, 22.54, 300, 400, 60, 2),
       ('店铺E', 1, 'image5.jpg', '杭州', '杭州市西湖区', 120.16, 30.26, 250, 350, 45, 5),
       ('店铺F', 1, 'image6.jpg', '成都', '成都市武侯区', 104.07, 30.67, 180, 280, 35, 4),
       ('店铺G', 1, 'image7.jpg', '武汉', '武汉市武昌区', 114.35, 30.56, 120, 220, 15, 3),
       ('店铺H', 2, 'image8.jpg', '西安', '西安市雁塔区', 108.95, 34.27, 90, 180, 25, 2),
       ('店铺I', 2, 'image9.jpg', '重庆', '重庆市渝北区', 106.54, 29.59, 160, 260, 35, 5),
       ('店铺J', 2, 'image10.jpg', '天津', '天津市和平区', 117.2, 39.13, 130, 230, 20, 4);
