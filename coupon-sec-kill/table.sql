CREATE TABLE IF NOT EXISTS `tb_voucher`
(
    `id`           BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `shop_id`      BIGINT(20) UNSIGNED          DEFAULT NULL COMMENT '商铺id',
    `title`        VARCHAR(255)        NOT NULL COMMENT '代金券标题',
    `sub_title`    VARCHAR(255)                 DEFAULT NULL COMMENT '副标题',
    `rules`        VARCHAR(1024)                DEFAULT NULL COMMENT '活动规则',
    `pay_value`    BIGINT(10) UNSIGNED NOT NULL COMMENT '支付金额，单位：分',
    `actual_value` BIGINT(10)          NOT NULL COMMENT '抵扣金额，单位：分',
    `type`         TINYINT(1) UNSIGNED NOT NULL DEFAULT '0' COMMENT '0：普通券 1：优惠券',
    `status`       TINYINT(1) UNSIGNED NOT NULL DEFAULT '1' COMMENT '1：上架 2：下架 3：过期',
    `create_time`  TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 7
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `tb_sec_kill_voucher`
(
    `voucher_id`  BIGINT(20) UNSIGNED NOT NULL COMMENT '关联的优惠券id',
    `stock`       INT(8) UNSIGNED     NOT NULL COMMENT '库存',
    `create_time` TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `begin_time`  TIMESTAMP           NOT NULL DEFAULT '1970-01-01 00:00:01' COMMENT '生效时间',
    `end_time`    TIMESTAMP           NOT NULL DEFAULT '1970-01-01 00:00:01' COMMENT '失效时间',
    `update_time` TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`voucher_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='秒杀优惠券';

CREATE TABLE IF NOT EXISTS `tb_voucher_order`
(
    `id`          BIGINT(20)          NOT NULL COMMENT '主键',
    `user_id`     BIGINT(20) UNSIGNED NOT NULL COMMENT '下单用户id',
    `voucher_id`  BIGINT(20) UNSIGNED NOT NULL COMMENT '代金券id',
    `pay_type`    TINYINT(1) UNSIGNED NOT NULL DEFAULT '1' COMMENT '支付方式：1.余额 2.支付宝',
    `status`      TINYINT(1) UNSIGNED NOT NULL DEFAULT '1' COMMENT '订单状态：1.未支付 2.已支付',
    `create_time` TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `pay_time`    TIMESTAMP           NULL     DEFAULT NULL COMMENT '支付时间',
    `use_time`    TIMESTAMP           NULL     DEFAULT NULL COMMENT '核销时间',
    `refund_time` TIMESTAMP           NULL     DEFAULT NULL COMMENT '退款时间',
    `update_time` TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;