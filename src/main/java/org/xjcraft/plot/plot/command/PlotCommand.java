package org.xjcraft.plot.plot.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cat73.bukkitboot.annotation.command.Command;
import org.cat73.bukkitboot.annotation.core.Bean;
import org.cat73.bukkitboot.annotation.core.Inject;
import org.xjcraft.plot.XJPlot;
import org.xjcraft.plot.common.exception.RollbackException;
import org.xjcraft.plot.plot.entity.Plot;
import org.xjcraft.plot.plot.mapper.PlotMapper;
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
        String worldName = player.getWorld().getName();

        // 坐标范围
        int xMin = Math.min(x1, x2);
        int xMax = Math.max(x1, x2);
        int zMin = Math.min(z1, z2);
        int zMax = Math.max(z1, z2);

        // 重叠校验
        boolean rangeOverlap = this.plugin.transaction(PlotMapper.class, mapper -> {
            return mapper.rangeOverlap(worldName, xMin, zMin, xMax, zMax);
        });
        if (rangeOverlap) {
            player.sendMessage(ChatColor.RED + "创建失败，输入的范围与其他地块重叠");
            return;
        }

        // 创建地块实体
        Plot plot = new Plot()
                .setWorldName(worldName)
                .setX1(xMin)
                .setZ1(zMin)
                .setX2(xMax)
                .setZ2(zMax)
                .setAddtime(LocalDateTime.now());

        // 将地块插入到数据库中
        int plotNo = this.plugin.transaction(PlotMapper.class, mapper -> {
            mapper.save(plot);
            return mapper.lastId();
        });

        // 提示玩家创建成功
        player.sendMessage(String.format("%s创建成功，地块编号：%d, 范围：(%s, (%d, %d), (%d, %d))", ChatColor.GREEN, plotNo, worldName, xMin, zMin, xMax, zMax));
    }

    /**
     * 编辑一个已有地块的范围
     * @param sender 命令的执行者
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
            aliases = "pe"
    )
    public void plotEdit(CommandSender sender, int plotNo, int x1, int z1, int x2, int z2) {
        // 坐标范围
        int xMin = Math.min(x1, x2);
        int xMax = Math.max(x1, x2);
        int zMin = Math.min(z1, z2);
        int zMax = Math.max(z1, z2);

        // 查出旧地块
        Plot plot = this.plugin.transaction(PlotMapper.class, mapper -> {
            return mapper.getById(plotNo);
        });
        if (plot == null) {
            sender.sendMessage(ChatColor.RED + "编辑失败，旧地块不存在");
            return;
        }

        this.plugin.transaction(PlotMapper.class, mapper -> {
            // 移除旧地块
            mapper.removeById(plotNo);

            // 重叠校验
            if (mapper.rangeOverlap(plot.getWorldName(), xMin, zMin, xMax, zMax)) {
                sender.sendMessage(ChatColor.RED + "编辑失败，新范围与其他地块重叠");
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
        });



        // 提示玩家创建成功
        sender.sendMessage(String.format("%s编辑成功，地块编号：%d, 新范围：(%s, (%d, %d), (%d, %d))", ChatColor.GREEN, plotNo, plot.getWorldName(), xMin, zMin, xMax, zMax));
    }
}
