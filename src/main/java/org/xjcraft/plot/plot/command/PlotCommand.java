package org.xjcraft.plot.plot.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cat73.bukkitboot.annotation.command.Command;
import org.cat73.bukkitboot.annotation.core.Bean;
import org.cat73.bukkitboot.annotation.core.Inject;
import org.cat73.bukkitboot.util.PlayerSession;
import org.xjcraft.plot.XJPlot;
import org.xjcraft.plot.common.exception.RollbackException;
import org.xjcraft.plot.plot.entity.Plot;
import org.xjcraft.plot.plot.mapper.PlotMapper;
import org.xjcraft.plot.util.ExpireAction;
import java.time.LocalDateTime;

/**
 * 地块相关的命令
 */
@Bean
public class PlotCommand {
    @Inject
    private XJPlot plugin;

    /**
     * 创建一个新地块
     * @param player 命令的执行者
     * @param x1 第一个角落的横坐标
     * @param z1 第一个角落的纵坐标
     * @param x2 第二个角落的横坐标
     * @param z2 第二个角落的纵坐标
     */
    @Command(
            permission = "xjplot.admin",
            usage = "<x1> <z1> <x2> <z2>",
            desc = "创建一个新地块",
            aliases = "pc",
            target = Command.Target.PLAYER
    )
    public void plotCreate(Player player, int x1, int z1, int x2, int z2) {
        // 所在的世界名
        var worldName = player.getWorld().getName();

        // 坐标范围
        var xMin = Math.min(x1, x2);
        var xMax = Math.max(x1, x2);
        var zMin = Math.min(z1, z2);
        var zMax = Math.max(z1, z2);

        // 重叠校验
        var rangeOverlap = this.plugin.tranr(PlotMapper.class, mapper ->
                mapper.rangeOverlap(worldName, xMin, zMin, xMax, zMax));
        if (rangeOverlap) {
            player.sendMessage(ChatColor.RED + "创建失败，输入的范围与其他地块重叠");
            return;
        }

        // 创建地块实体
        var plot = new Plot()
                .setWorldName(worldName)
                .setX1(xMin)
                .setZ1(zMin)
                .setX2(xMax)
                .setZ2(zMax)
                .setAddtime(LocalDateTime.now());

        // 将地块插入到数据库中
        var plotNo = this.plugin.tranr(PlotMapper.class, mapper -> {
            mapper.save(plot);
            return mapper.lastId();
        });

        // TODO 记录日志

        // 提示玩家创建成功
        player.sendMessage(String.format("%s创建成功，地块编号：%d, 范围：(%s, (%d, %d), (%d, %d))", ChatColor.GREEN, plotNo, worldName, xMin, zMin, xMax, zMax));
    }

    /**
     * 编辑一个已有地块的范围
     * @param player 命令的执行者
     * @param plotNo 地块编号
     * @param x1 新范围第一个角落的横坐标
     * @param z1 新范围第一个角落的纵坐标
     * @param x2 新范围第二个角落的横坐标
     * @param z2 新范围第二个角落的纵坐标
     */
    @Command(
            permission = "xjplot.admin",
            usage = "<plotNo> <x1> <z1> <x2> <z2>",
            desc = "编辑一个已有地块的范围",
            aliases = "pe",
            target = Command.Target.PLAYER
    )
    public void plotEdit(Player player, int plotNo, int x1, int z1, int x2, int z2) {
        // 坐标范围
        var xMin = Math.min(x1, x2);
        var xMax = Math.max(x1, x2);
        var zMin = Math.min(z1, z2);
        var zMax = Math.max(z1, z2);

        this.plugin.tran(PlotMapper.class, mapper -> {
            // 查出旧地块
            var plot = mapper.getById(plotNo);
            if (plot == null) {
                player.sendMessage(ChatColor.RED + "编辑失败，旧地块不存在");
                return;
            }

            // 移除旧地块
            mapper.removeById(plotNo);

            // 重叠校验
            if (mapper.rangeOverlap(plot.getWorldName(), xMin, zMin, xMax, zMax)) {
                player.sendMessage(ChatColor.RED + "编辑失败，新范围与其他地块重叠");
                throw new RollbackException(); // 回滚事务，撤销地块移除
            }

            // 编辑地块实体
            plot
                    .setX1(xMin)
                    .setZ1(zMin)
                    .setX2(xMax)
                    .setZ2(zMax);

            // 将地块插入到数据库中
            mapper.save(plot);

            // TODO 记录日志

            // 提示玩家创建成功
            player.sendMessage(String.format("%s编辑成功，地块编号：%d, 新范围：(%s, (%d, %d), (%d, %d))", ChatColor.GREEN, plotNo, plot.getWorldName(), xMin, zMin, xMax, zMax));
        });
    }

    /**
     * 移除一个已有的地块
     * <p>这个地块必须未被出租</p>
     * @param player 命令的执行者
     * @param plotNo 地块编号，可选，如未提供，则会使用当前所在的地块
     */
    @Command(
            permission = "xjplot.admin",
            usage = "[plotNo]",
            desc = "移除一个已有的地块，这个地块必须未被出租",
            aliases = "pr",
            target = Command.Target.PLAYER
    )
    public void plotRemove(Player player, @Inject(required = false) Integer plotNo) {
        // 查询地块信息
        Plot plot;
        if (plotNo != null) {
            plot = this.plugin.tranr(PlotMapper.class, mapper -> mapper.getById(plotNo));
        } else {
            // 所在的世界名
            var location = player.getLocation();
            var worldName = player.getWorld().getName();
            plot = this.plugin.tranr(PlotMapper.class, mapper -> mapper.getByPos(worldName, location.getBlockX(), location.getBlockZ()));
        }
        if (plot == null) {
            if (plotNo != null) {
                player.sendMessage(ChatColor.RED + "地块编号不存在");
            } else {
                player.sendMessage(ChatColor.RED + "您需要站在一个地块上或输入一个地块编号来移除地块");
            }

            return;
        }

        // TODO 检查地块必须未被出租

        // 如果存在正在等待确认的操作，则提示错误
        ExpireAction currentAction = PlayerSession.forPlayer(player).get("waitAction");
        if (currentAction != null && !currentAction.isTimeout()) {
            player.sendMessage(ChatColor.RED + "有正在等待确认的操作，请确认或等待上一个操作过期后重试");
            return;
        }

        // 添加等待确认的操作
        PlayerSession.forPlayer(player).set("waitAction", new ExpireAction(30 * 20, () -> {
            // 移除地块
            this.plugin.tran(PlotMapper.class, mapper -> mapper.removeById(plot.getId()));
            player.sendMessage(String.format("%s已移除地块：%d, 原范围：(%s, (%d, %d), (%d, %d))", ChatColor.GREEN, plotNo, plot.getWorldName(), plot.getX1(), plot.getZ1(), plot.getX2(), plot.getZ2()));

            // TODO 记录日志
        }));

        // 提示玩家确认 // TODO 更多信息？粒子标定范围？
        player.sendMessage(String.format("%s您要移除的地块编号为：%d, 范围：(%s, (%d, %d), (%d, %d))", ChatColor.GREEN, plotNo, plot.getWorldName(), plot.getX1(), plot.getZ1(), plot.getX2(), plot.getZ2()));
        player.sendMessage(String.format("%s如果您确认要移除，请输入 /xjplot confirm 来确认", ChatColor.GREEN));
    }
}
