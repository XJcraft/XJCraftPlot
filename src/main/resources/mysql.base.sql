-- -----------------------------------
-- MySQL 数据库中的基础数据
-- -----------------------------------

-- TODO 自动初始化
-- TODO 字段、表注释

-- -----------------------------------
-- 地块表
-- -----------------------------------
CREATE TABLE IF NOT EXISTS xjplot_plot (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `world_name` varchar(255) NOT NULL,
  `x1` int(11) NOT NULL,
  `z1` int(11) NOT NULL,
  `x2` int(11) NOT NULL,
  `z2` int(11) NOT NULL,
  `addtime` datetime(3) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB;
