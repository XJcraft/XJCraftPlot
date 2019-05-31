package org.xjcraft.plot.common.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cat73.bukkitboot.annotation.command.Command;
import org.cat73.bukkitboot.annotation.core.Bean;
import org.cat73.bukkitboot.util.PlayerSession;
import org.xjcraft.plot.util.ExpireAction;

/**
 * 公共命令
 */
@Bean
public class CommonCommand {
    /**
     * 确认执行一个操作
     * @param player 命令的执行者
     */
    @Command(
            desc = "确认执行一个操作",
            aliases = "c",
            target = Command.Target.PLAYER
    )
    public void confirm(Player player) {
        ExpireAction currentAction = PlayerSession.forPlayer(player).get("waitAction");
        if (currentAction == null || currentAction.isTimeout()) {
            player.sendMessage(ChatColor.RED + "您没有等待确认的操作");
            return;
        }

        currentAction.doAction();
    }
}
