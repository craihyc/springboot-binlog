SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(30) CHARACTER SET utf8 DEFAULT NULL,
  `value` varchar(100) CHARACTER SET utf8 NOT NULL,
  `price` decimal(12,2) DEFAULT NULL,
  `create_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=124 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of sys_config
-- ----------------------------

-- ----------------------------
-- Table structure for sys_config_copy
-- ----------------------------
DROP TABLE IF EXISTS `sys_config_copy`;
CREATE TABLE `sys_config_copy` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(30) CHARACTER SET utf8 DEFAULT NULL,
  `value` varchar(100) CHARACTER SET utf8 DEFAULT NULL,
  `price` decimal(12,2) DEFAULT NULL,
  `create_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=124 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of sys_config_copy
-- ----------------------------

-- ----------------------------
-- Table structure for t_sync_config
-- ----------------------------
DROP TABLE IF EXISTS `t_sync_config`;
CREATE TABLE `t_sync_config` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `key` varchar(30) CHARACTER SET utf8 NOT NULL COMMENT '键值',
  `value` varchar(100) CHARACTER SET utf8 NOT NULL COMMENT '值',
  PRIMARY KEY (`id`),
  UNIQUE KEY `key` (`key`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of t_sync_config
-- ----------------------------
INSERT INTO `t_sync_config` VALUES ('1', 'BIN_LOG_FILE_NAME', '');
INSERT INTO `t_sync_config` VALUES ('2', 'BIN_LOG_NEXT_POSITION', '');
