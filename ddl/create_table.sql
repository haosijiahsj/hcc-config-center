CREATE TABLE `application` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `app_code` varchar(64) NOT NULL COMMENT '应用编码',
  `app_name` varchar(64) NOT NULL COMMENT '应用名称',
  `app_status` varchar(32) NOT NULL COMMENT '状态',
  `secret_key` varchar(128) NOT NULL COMMENT '密钥',
  `owner` varchar(32) DEFAULT NULL COMMENT '责任人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='应用表';

CREATE TABLE `application_config` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `application_id` bigint(20) unsigned NOT NULL COMMENT '应用id',
  `_key` varchar(64) NOT NULL COMMENT 'key',
  `_value` varchar(255) DEFAULT NULL COMMENT 'value',
  `_comment` varchar(255) DEFAULT NULL COMMENT '注释',
  `version` int(11) DEFAULT NULL COMMENT '版本',
  `dynamic` tinyint(4) NOT NULL COMMENT '是否动态值',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='应用配置表';