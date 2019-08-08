package org.xjcraft.plot.plot.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cat73.bukkitboot.annotation.command.Command;
import org.cat73.bukkitboot.annotation.core.Bean;
import org.cat73.bukkitboot.annotation.core.Inject;
import org.xjcraft.plot.XJPlot;
import org.xjcraft.plot.log.service.LogService;
import org.xjcraft.plot.plot.entity.Plot;
import org.xjcraft.plot.plot.service.PlotService;
import org.xjcraft.plot.util.ExpireAction;

/**
 * 地块相关的命令
 */
@Bean
public class PlotCommand {
    @Inject
    private PlotService plotService;
    @Inject
    private LogService logService;
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

        // 创建地块
        var result = this.plotService.createPlot(worldName, xMin, zMin, xMax, zMax, player.getName());
        if (result.isSuccess()) {
            player.sendMessage(String.format("%s创建成功，地块编号：%d, 范围：(%s, (%d, %d), (%d, %d))", ChatColor.GREEN, result.getData().getId(), worldName, xMin, zMin, xMax, zMax));
        } else {
            player.sendMessage(ChatColor.RED + result.getMessage());
        }
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

        var result = this.plotService.editPlot(plotNo, xMin, xMax, zMin, zMax, player.getName());
        if (result.isSuccess()) {
            player.sendMessage(String.format("%s编辑成功，地块编号：%d, 新范围：(%s, (%d, %d), (%d, %d))", ChatColor.GREEN, plotNo, result.getData().getWorldName(), xMin, zMin, xMax, zMax));
        } else {
            player.sendMessage(ChatColor.RED + result.getMessage());
        }
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
            plot = this.plotService.getById(plotNo);
        } else {
            var location = player.getLocation();
            var worldName = player.getWorld().getName();
            plot = this.plotService.getByPos(worldName, location.getBlockX(), location.getBlockZ());
        }
        if (plot == null) {
            if (plotNo != null) {
                player.sendMessage(ChatColor.RED + "地块编号不存在");
            } else {
                player.sendMessage(ChatColor.RED + "您需要站在一个地块上或输入一个地块编号来移除地块");
            }

            return;
        }

        // 设置一个需要玩家二次确认的移除地块操作
        ExpireAction.auto(player)
                .action(() -> {
                    // 移除地块
                    var result = this.plotService.removePlot(plotNo, player.getName());
                    if (result.isSuccess()) {
                        player.sendMessage(String.format("%s已移除地块：%d, 原范围：(%s, (%d, %d), (%d, %d))", ChatColor.GREEN, plotNo, plot.getWorldName(), plot.getX1(), plot.getZ1(), plot.getX2(), plot.getZ2()));
                    } else {
                        player.sendMessage(ChatColor.RED + result.getMessage());
                    }
                })
                .successAction(() -> {
                    // 提示玩家确认 // TODO 更多信息？粒子标定范围？
                    player.sendMessage(String.format("%s您要移除的地块编号为：%d, 范围：(%s, (%d, %d), (%d, %d))", ChatColor.GREEN, plotNo, plot.getWorldName(), plot.getX1(), plot.getZ1(), plot.getX2(), plot.getZ2()));
                    player.sendMessage(String.format("%s如果您确认要移除，请输入 /xjplot confirm 来确认", ChatColor.GREEN));
                })
                .start();
    }

    // TODO 租赁方式调整
    //  不能调整已出租地块的租赁方式，但可调整续租租金
    // TODO 地块查看
    // TODO 查看周围地块
}
