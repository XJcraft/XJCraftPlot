package org.xjcraft.plot.plot.service;

import org.cat73.bukkitboot.annotation.core.Bean;
import org.cat73.bukkitboot.annotation.core.Inject;
import org.xjcraft.plot.common.exception.RollbackException;
import org.xjcraft.plot.log.entity.Log;
import org.xjcraft.plot.log.service.LogService;
import org.xjcraft.plot.plot.entity.Plot;
import org.xjcraft.plot.plot.mapper.PlotMapper;
import org.xjcraft.plot.util.DB;
import org.xjcraft.plot.util.Result;
import java.time.LocalDateTime;

/**
 * 地块的业务层
 */
@Bean
public class PlotService {
    @Inject
    private LogService logService;

    /**
     * 根据 ID 查询地块
     * @param id 地块的 ID
     * @return 查询到的地块，如果没查到则返回 null
     */
    public Plot getById(int id) {
        return DB.tranr(() -> {
            var plotMapper = DB.getMapper(PlotMapper.class);
            return plotMapper.getById(id);
        });
    }

    /**
     * 根据坐标查询地块
     * @param worldName 世界名
     * @param x x 坐标
     * @param z z 坐标
     * @return 查到的地块，如果没查到则返回 null
     */
    public Plot getByPos(String worldName, int x, int z) {
        return DB.tranr(() -> {
            var plotMapper = DB.getMapper(PlotMapper.class);
            return plotMapper.getByPos(worldName, x, z);
        });
    }

    /**
     * 创建一个地块
     * @param worldName 地块所处的世界名
     * @param xMin x 坐标中较小的数字
     * @param zMin z 坐标中较小的数字
     * @param xMax x 坐标中较大的数字
     * @param zMax z 坐标中较大的数字
     * @param operator 操作人的名字
     * @return 创建结果，数据为创建的地块
     */
    public Result<Plot> createPlot(String worldName, int xMin, int zMin, int xMax, int zMax, String operator) {
        return DB.tranr(() -> {
            var plotMapper = DB.getMapper(PlotMapper.class);

            // 校验不允许存在重复地块
            if (plotMapper.rangeOverlap(worldName, xMin, zMin, xMax, zMax)) {
                return Result.fail("创建失败，范围与其他地块重叠");
            }

            // 创建地块实体
            var plot = new Plot()
                    .setWorldName(worldName)
                    .setX1(xMin)
                    .setZ1(zMin)
                    .setX2(xMax)
                    .setZ2(zMax)
                    .setAddtime(LocalDateTime.now())
                    .setSellType(Plot.SellType.UNDEFINED)
                    .setSellPrice(0);

            // 将地块保存到数据库中
            plotMapper.save(plot);
            // 查询地块编号并写入尸体
            var plotNo = plotMapper.lastId();
            plot.setId(plotNo);

            // 记录日志
            this.logService.log(operator, Log.LogType.CREATE_PLOT, String.format("地块编号：%d, 范围：(%s, (%d, %d), (%d, %d))", plotNo, worldName, xMin, zMin, xMax, zMax));

            // 返回结果
            return Result.success(plot);
        });
    }

    /**
     * 编辑一个地块
     * @param plotNo 被编辑的地块的编号
     * @param xMin x 坐标中较小的数字
     * @param zMin z 坐标中较小的数字
     * @param xMax x 坐标中较大的数字
     * @param zMax z 坐标中较大的数字
     * @param operator 操作人的名字
     * @return 操作结果，数据为编辑后的地块
     */
    public Result<Plot> editPlot(int plotNo, int xMin, int xMax, int zMin, int zMax, String operator) {
        return DB.tranr(() -> {
            var plotMapper = DB.getMapper(PlotMapper.class);

            // 查出旧地块
            var plot = plotMapper.getById(plotNo);
            if (plot == null) {
                return Result.fail("编辑失败，旧地块不存在");
            }

            // 移除旧地块
            plotMapper.removeById(plotNo);

            // 重叠校验
            if (plotMapper.rangeOverlap(plot.getWorldName(), xMin, zMin, xMax, zMax)) {
                throw new RollbackException() // 回滚事务，撤销地块移除
                        .setData(Result.fail("编辑失败，新范围与其他地块重叠"));
            }

            // 编辑地块实体
            plot
                    .setX1(xMin)
                    .setZ1(zMin)
                    .setX2(xMax)
                    .setZ2(zMax);

            // 将地块插入到数据库中
            plotMapper.save(plot);

            // 记录日志
            this.logService.log(operator, Log.LogType.EDIT_PLOT, String.format("地块编号：%d, 新范围：(%s, (%d, %d), (%d, %d))", plotNo, plot.getWorldName(), xMin, zMin, xMax, zMax));

            // 返回结果
            return Result.success(plot);
        });
    }

    /**
     * 移除一个地块
     * @param plotNo 被移除的地块的编号
     * @param operator 操作人的名字
     * @return 操作结果，数据为删除前的地块
     */
    public Result<Plot> removePlot(Integer plotNo, String operator) {
        return DB.tranr(() -> {
            var plotMapper = DB.getMapper(PlotMapper.class);

            // TODO 检查地块必须未被出售

            // 查出原地块
            var plot = plotMapper.getById(plotNo);

            // 移除地块
            plotMapper.removeById(plotNo);

            // 记录日志
            this.logService.log(operator, Log.LogType.REMOVE_PLOT, String.format("地块编号：%d, 原范围：(%s, (%d, %d), (%d, %d))", plotNo, plot.getWorldName(), plot.getX1(), plot.getZ1(), plot.getX2(), plot.getZ2()));

            // 返回结果
            return Result.success(plot);
        });
    }

    /**
     * 调整一个地块的销售方式
     * @param plotNo 地块编号
     * @param sellType 新的出售方式
     * @param sellPrice 新的出售方式 - 价格
     * @param operator 操作人的名字
     * @return 操作结果
     */
    public Result<?> changeSellType(Integer plotNo, Plot.SellType sellType, int sellPrice, String operator) {
        return DB.tranr(() -> {
            var plotMapper = DB.getMapper(PlotMapper.class);

            // 查出原地块
            var plot = plotMapper.getById(plotNo);

            // TODO 检查地块必须未被出售

            // 移除旧地块
            plotMapper.removeById(plotNo);

            // 编辑地块实体
            plot.setSellType(sellType)
                    .setSellPrice(sellPrice);

            // 将地块插入到数据库中
            plotMapper.save(plot);

            // 记录日志
            this.logService.log(operator, Log.LogType.REMOVE_PLOT, String.format("地块编号：%d, 新的出租方式和参数：(%s, %d)", plotNo, sellType.getDisplayName(), sellPrice));

            // 返回结果
            return Result.success();
        });
    }
}
