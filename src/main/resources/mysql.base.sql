-- -----------------------------------
-- MySQL 数据库中的基础数据
-- 现在这种方式对于仅结构初始化是没有问题的，但如果需要同时带记录，就需要再改改了。。。
-- -----------------------------------

-- -----------------------------------
-- 地块表
-- -----------------------------------
CREATE TABLE IF NOT EXISTS xjplot_plot (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '地块编号',
  `world_name` varchar(255) NOT NULL COMMENT '世界名',
  `x1` int(11) NOT NULL COMMENT 'x 坐标中较小的数字',
  `z1` int(11) NOT NULL COMMENT 'z 坐标中较小的数字',
  `x2` int(11) NOT NULL COMMENT 'x 坐标中较大的数字',
  `z2` int(11) NOT NULL COMMENT 'z 坐标中较大的数字',
  `addtime` datetime(3) NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '地块';
