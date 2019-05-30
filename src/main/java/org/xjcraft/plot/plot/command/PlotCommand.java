package org.xjcraft.plot.plot.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cat73.bukkitboot.annotation.command.Command;
import org.cat73.bukkitboot.annotation.core.Bean;
import org.cat73.bukkitboot.annotation.core.Inject;
import org.xjcraft.plot.PlotPlugin;
import org.xjcraft.plot.plot.entity.Plot;
import org.xjcraft.plot.plot.mapper.PlotMapper;
import java.time.LocalDateTime;

/**
 * 地块相关的命令
 */
@Bean
public class PlotCommand {
    @Inject
    private PlotPlugin plotPlugin;

    /**
     * 创建一个新地块
     * @param player 命令的执行者
     * @param x1 第一个角落的横坐标
     * @param z1 第一个角落的纵坐标
     * @param x2 第二个角落的横坐标
     * @param z2 第二个角落的纵坐标
     */
    @Command(
            permission = "plot.admin",
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
        boolean rangeOverlap = this.plotPlugin.transaction(PlotMapper.class, mapper -> {
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
        int plotId = this.plotPlugin.transaction(PlotMapper.class, mapper -> {
            mapper.save(plot);
            return mapper.lastId();
        });

        // 提示玩家创建成功
        player.sendMessage(String.format("%s创建成功，地块编号：%d, 坐标：(%s, %d, %d, %d, %d)", ChatColor.GREEN, plotId, worldName, xMin, zMin, xMax, zMax));
    }
}
