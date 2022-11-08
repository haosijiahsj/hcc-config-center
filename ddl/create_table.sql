CREATE TABLE `application` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `app_code` varchar(64) NOT NULL COMMENT '应用编码',
  `app_name` varchar(64) NOT NULL COMMENT '应用名称',
  `app_status` varchar(32) NOT NULL COMMENT '状态',
  `app_mode` varchar(32) NOT NULL COMMENT '应用模式，推、拉',
  `secret_key` varchar(128) NOT NULL COMMENT '密钥',
  `owner` varchar(32) DEFAULT NULL COMMENT '责任人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uqe_app_code` (`app_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='应用表';

CREATE TABLE `application_config` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `application_id` bigint(20) unsigned NOT NULL COMMENT '应用id',
  `_key` varchar(64) NOT NULL COMMENT 'key',
  `_value` varchar(1024) DEFAULT NULL COMMENT 'value',
  `_comment` varchar(255) DEFAULT NULL COMMENT '注释',
  `version` int(11) DEFAULT NULL COMMENT '版本',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uqe_applicationId_key` (`application_id`,`_key`),
  KEY `idx_application_id` (`application_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COMMENT='应用配置表';

CREATE TABLE `application_config_history` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `application_config_id` bigint(20) unsigned NOT NULL,
  `_value` varchar(1024) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `operate_type` varchar(32) NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_application_config_id` (`application_config_id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `application_config_push_record` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `application_config_id` bigint(20) unsigned NOT NULL COMMENT '配置id',
  `_value` varchar(1024) DEFAULT NULL COMMENT '配置值',
  `version` int(11) NOT NULL COMMENT '版本',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_application_config_id` (`application_config_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;