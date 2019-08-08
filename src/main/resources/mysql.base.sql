-- -----------------------------------
-- MySQL 数据库中的基础数据
-- 现在这种方式对于仅结构初始化是没有问题的，但如果需要同时带记录，就需要再改改了。。。
-- -----------------------------------

-- -----------------------------------
-- 地块表
-- -----------------------------------
CREATE TABLE IF NOT EXISTS `xjplot_plot` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '地块编号',
  `world_name` varchar(255) NOT NULL COMMENT '世界名',
  `x1` int(11) NOT NULL COMMENT 'x 坐标中较小的数字',
  `z1` int(11) NOT NULL COMMENT 'z 坐标中较小的数字',
  `x2` int(11) NOT NULL COMMENT 'x 坐标中较大的数字',
  `z2` int(11) NOT NULL COMMENT 'z 坐标中较大的数字',
  `addtime` datetime(3) NOT NULL COMMENT '创建时间',
  `sell_type` varchar(255) NOT NULL COMMENT '租赁方式',
  `sell_price` int(11) NOT NULL COMMENT '租赁方式 - 价格',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '地块';

-- -----------------------------------
-- 余额表
-- -----------------------------------
CREATE TABLE IF NOT EXISTS `xjplot_balance` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `player_name` varchar(255) NOT NULL COMMENT '玩家名',
  `balance` decimal(16, 6) NOT NULL COMMENT '余额',
  `freeze` decimal(16, 6) NOT NULL COMMENT '冻结额度',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `unique_player_name`(`player_name`)
) ENGINE = InnoDB COMMENT = '余额';

-- -----------------------------------
-- 余额操作记录表
-- -----------------------------------
CREATE TABLE IF NOT EXISTS `xjplot_balance_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `balance_id` int(11) NOT NULL,
  `addtime` datetime(3) NOT NULL COMMENT '操作时间',
  `type` varchar(64) NOT NULL COMMENT '操作类型',
  `before_balance` decimal(16, 6) NOT NULL COMMENT '操作前的余额',
  `before_freeze` decimal(16, 6) NOT NULL COMMENT '操作前的冻结额度',
  `change_balance` decimal(16, 6) NOT NULL COMMENT '余额的变更值(入正出负)',
  `change_freeze` decimal(16, 6) NOT NULL COMMENT '冻结额度的变更值(入正出负)',
  `remark` varchar(255) NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '余额操作记录';

-- -----------------------------------
-- 操作日志表
-- -----------------------------------
CREATE TABLE IF NOT EXISTS `xjplot_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `player_name` varchar(255) NOT NULL COMMENT '玩家名',
  `addtime` datetime(3) NOT NULL COMMENT '创建时间',
  `type` varchar(255) NOT NULL COMMENT '日志类型',
  `remark` varchar(255) NOT NULL COMMENT '日志内容',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_name_type`(`player_name`, `type`) USING BTREE
) ENGINE = InnoDB COMMENT = '操作日志';

-- -----------------------------------
-- 地块出售表
-- -----------------------------------
CREATE TABLE IF NOT EXISTS `xjplot_sell` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '地块编号',
  `player_name` varchar(255) NOT NULL COMMENT '当前租下的玩家名',
  `addtime` datetime(3) NOT NULL COMMENT '租赁开始时间',
  `auto_withdrawal` tinyint(1) NOT NULL COMMENT '是否自动退租（到期后取消租赁）',
  `cb_pos` varchar(255) NOT NULL COMMENT '如果 cb 在这块地内，则格式为 x,y,z，否则留空',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_player_name`(`player_name`) USING BTREE
) ENGINE = InnoDB COMMENT = '地块出售';
