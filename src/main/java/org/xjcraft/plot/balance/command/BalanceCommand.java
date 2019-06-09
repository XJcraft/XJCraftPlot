package org.xjcraft.plot.balance.command;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.cat73.bukkitboot.annotation.command.Command;
import org.cat73.bukkitboot.annotation.core.Bean;
import org.cat73.bukkitboot.annotation.core.Inject;
import org.cat73.bukkitboot.util.PlayerSession;
import org.cat73.bukkitboot.util.Strings;
import org.xjcraft.plot.XJPlot;
import org.xjcraft.plot.balance.entity.BalanceLog;
import org.xjcraft.plot.balance.service.BalanceService;
import org.xjcraft.plot.balance.util.Bonds;
import org.xjcraft.plot.util.ExpireAction;
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
            aliases = "bro",
            target = Command.Target.PLAYER
    )
    public void balanceRechargeOP(Player player, String playerName, String moneyStr, String reason) { // TODO 参数注入暂不支持 BigDecimal，因此先拿 String 接收后自己转换
        var money = new BigDecimal(moneyStr);
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

        // 查询余额
        var balance = this.plugin.tranr(session -> this.service.getByPlayer(session, targetPlayerName));

        // 提示玩家
        player.sendMessage(String.format("%s为玩家 %s 的余额为 %s 元，其中，已冻结的额度为 %s 元", ChatColor.GREEN, targetPlayerName, balance.getBalance(), balance.getFreeze()));
    }

    /**
     * 将背包中的国债全部充值为余额
     * @param player 命令的执行者
     */
    @Command(
            desc = "将背包中的国债全部充值为余额",
            aliases = "br",
            target = Command.Target.PLAYER
    )
    public void balanceRecharge(Player player) {
        // 统计背包中的国债额度
        int money = 0;
        for (var itemStack : player.getInventory().getContents()) {
            if (itemStack != null) {
                var curMoney = Bonds.getValue(itemStack);
                curMoney *= itemStack.getAmount();
                money += curMoney;
            }
        }
        if (money == 0) {
            player.sendMessage(ChatColor.RED + "您的背包中没有国债，请先持有一些国债再进行充值");
            return;
        }

        // 如果存在正在等待确认的操作，则提示错误
        ExpireAction currentAction = PlayerSession.forPlayer(player).get("waitAction");
        if (currentAction != null && !currentAction.isTimeout()) {
            player.sendMessage(ChatColor.RED + "有正在等待确认的操作，请确认或等待上一个操作过期后重试");
            return;
        }

        // 添加等待确认的操作
        PlayerSession.forPlayer(player).set("waitAction", new ExpireAction(30 * 20, () -> {
            // 在此计算背包中的国债额度，放置玩家在确认之前进行过背包操作
            // 同时顺便移除掉背包中的国债
            int rechargeMoney = 0;
            for (var itemStack : player.getInventory().getContents()) {
                if (itemStack != null) {
                    var curMoney = Bonds.getValue(itemStack);
                    curMoney *= itemStack.getAmount();
                    rechargeMoney += curMoney;

                    // 移除国债
                    if (curMoney != 0) {
                        itemStack.setType(Material.AIR);
                    }
                }
            }

            if (rechargeMoney == 0) {
                player.sendMessage(ChatColor.RED + "您的背包中没有国债，请先持有一些国债再进行充值");
                return;
            }

            // 将充值的额度存入数据库中
            var finalMoney = BigDecimal.valueOf(rechargeMoney);
            var newBalance = this.plugin.tranr(session -> {
                this.service.log(session, player.getName(), BalanceLog.AccountLogType.RECHARGE, finalMoney, BigDecimal.ZERO, null);
                return this.service.getByPlayer(session, player.getName());
            }).getBalance();

            // TODO 记录日志

            // 提示玩家充值结果
            player.sendMessage(String.format("%s充值 %s元成功，充值后余额 %s元", ChatColor.GREEN, finalMoney, newBalance));
        }));

        // 提示玩家确认
        player.sendMessage(String.format("%s您将要把背包中的所有国债(%d元)充值为余额", ChatColor.GREEN, money));
        player.sendMessage(String.format("%s如果您确认要充值，请输入 /xjplot confirm 来确认", ChatColor.GREEN));
    }
}
