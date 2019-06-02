package org.xjcraft.plot.balance.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.cat73.bukkitboot.annotation.command.Command;
import org.cat73.bukkitboot.annotation.core.Bean;
import org.cat73.bukkitboot.annotation.core.Inject;
import org.cat73.bukkitboot.util.Strings;
import org.xjcraft.plot.XJPlot;
import org.xjcraft.plot.balance.entity.BalanceLog;
import org.xjcraft.plot.balance.service.BalanceService;
import java.math.BigDecimal;

/**
 * 余额相关的命令
 */
@Bean
public class BalanceCommand {
    @Inject
    private BalanceService service;
    @Inject
    private XJPlot plugin;

    /**
     * 为玩家充值余额
     * @param player 命令的执行者
     * @param playerName 被充值的玩家名
     * @param moneyStr 充值的金额
     * @param reason 充值理由
     */
    @Command(
            permission = "xjplot.admin",
            usage = "<playerName> <money> <reason>",
            desc = "为玩家充值余额",
            aliases = "br",
            target = Command.Target.PLAYER
    )
    public void balanceRecharge(Player player, String playerName, String moneyStr, String reason) { // TODO 参数注入暂不支持 BigDecimal，因此先拿 String 接收后自己转换
        var money = new BigDecimal(moneyStr);
        if (money.compareTo(BigDecimal.ZERO) <= 0) {
            player.sendMessage(ChatColor.RED + "充值金额不能小于或等于 0");
            return;
        }
        if (money.scale() > 6) {
            player.sendMessage(ChatColor.RED + "最多允许 6 位小数");
            return;
        }

        // 充值
        var balance = this.plugin.tranr(session -> {
            this.service.log(session, playerName, BalanceLog.AccountLogType.OP_RECHARGE, money, BigDecimal.ZERO, reason);
            return this.service.getByPlayer(session, playerName);
        });

        player.sendMessage(String.format("%s为玩家 %s 充值 %s 元成功，充值后的余额为 %s", ChatColor.GREEN, playerName, money, balance.getBalance()));

        // TODO 记录日志
    }

    /**
     * 查询玩家的余额
     * @param player 命令的执行者
     * @param playerName 查询的玩家名，如未传，则会查询执行命令的玩家的余额，只有管理员允许查询他人的余额
     */
    @Command(
            usage = "[playerName]",
            desc = "查询玩家的余额",
            aliases = "b",
            target = Command.Target.PLAYER
    )
    public void balance(Player player, @Inject(required = false) String playerName) {
        String targetPlayerName;
        if (Strings.notBlank(playerName) && player.hasPermission("xjplot.admin")) {
            targetPlayerName = playerName;
        } else {
            targetPlayerName = player.getName();
        }

        // 充值
        var balance = this.plugin.tranr(session -> this.service.getByPlayer(session, targetPlayerName));

        player.sendMessage(String.format("%s为玩家 %s 的余额为 %s 元，其中，已冻结的额度为 %s 元", ChatColor.GREEN, playerName, balance.getBalance(), balance.getFreeze()));
    }
}
