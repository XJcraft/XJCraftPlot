package org.xjcraft.plot.balance.command;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.cat73.bukkitboot.annotation.command.Command;
import org.cat73.bukkitboot.annotation.core.Bean;
import org.cat73.bukkitboot.annotation.core.Inject;
import org.cat73.bukkitboot.util.Strings;
import org.xjcraft.plot.XJPlot;
import org.xjcraft.plot.balance.entity.BalanceLog;
import org.xjcraft.plot.balance.service.BalanceService;
import org.xjcraft.plot.balance.util.Bonds;
import org.xjcraft.plot.log.entity.Log;
import org.xjcraft.plot.log.service.LogService;
import org.xjcraft.plot.util.DB;
import org.xjcraft.plot.util.ExpireAction;
import java.math.BigDecimal;

/**
 * 余额相关的命令
 */
@Bean
public class BalanceCommand {
    @Inject
    private BalanceService balanceService;
    @Inject
    private LogService logService;
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
        var result = this.balanceService.recharge(playerName, money, reason, player.getName());
        if (result.isSuccess()) {
            player.sendMessage(String.format("%s为 %s 充值 %s 元成功，充值后的余额为 %s", ChatColor.GREEN, playerName, money, result.getData().getBalance()));
        } else {
            player.sendMessage(ChatColor.RED + result.getMessage());
        }
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
        // 查询目标：管理员可以查任何玩家的，普通玩家只能查询自己的
        String targetPlayerName;
        if (Strings.notBlank(playerName) && player.hasPermission("xjplot.admin")) {
            targetPlayerName = playerName;
        } else {
            targetPlayerName = player.getName();
        }

        // 查询余额
        var balance = this.balanceService.getByPlayer(targetPlayerName);

        // 提示玩家
        player.sendMessage(String.format("%s玩家 %s 的余额为 %s 元", ChatColor.GREEN, targetPlayerName, balance.getBalance()));
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
        var money = this.sumBondsMoney(player.getInventory());
        if (money == 0) {
            player.sendMessage(ChatColor.RED + "您的背包中没有国债，请先持有一些国债再进行充值");
            return;
        }

        // 设置一个需要玩家二次确认的充值操作
        ExpireAction.auto(player)
                .action(() -> {
                    // 再此计算背包中的国债额度，防止玩家在确认之前进行过背包操作
                    var rechargeMoney = this.sumBondsMoney(player.getInventory());
                    if (rechargeMoney == 0) {
                        player.sendMessage(ChatColor.RED + "您的背包中没有国债，请先持有一些国债再进行充值");
                    }
                    var finalMoney = BigDecimal.valueOf(rechargeMoney);

                    var result = DB.tranr(() -> {
                        // 充值
                        this.balanceService.log(player.getName(), BalanceLog.AccountLogType.RECHARGE, finalMoney, BigDecimal.ZERO, null);
                        // 记录日志
                        this.logService.log(player.getName(), Log.LogType.RECHARGE, String.format("自行充值 %s 元", finalMoney));
                        // 查询充值后的账本
                        var balance = this.balanceService.getByPlayer(player.getName());

                        // 移除玩家背包中的国债
                        for (var itemStack : player.getInventory().getContents()) {
                            var curMoney = Bonds.getValue(itemStack);

                            // 移除国债
                            if (curMoney != 0) {
                                itemStack.setType(Material.AIR);
                            }
                        }

                        // 返回结果
                        return balance;
                    });

                    // 提示玩家充值结果
                    player.sendMessage(String.format("%s充值 %s 元成功，充值后余额 %s 元", ChatColor.GREEN, finalMoney, result.getBalance()));
                })
                .successAction(() -> {
                    // 提示玩家确认
                    player.sendMessage(String.format("%s您将要把背包中的所有国债(%d元)充值为余额", ChatColor.GREEN, money));
                    player.sendMessage(String.format("%s如果您确认要充值，请输入 /xjplot confirm 来确认", ChatColor.GREEN));
                })
                .start();
    }

    /**
     * 计算一个容器中的国债额度之和
     * @param inventory 容器
     * @return 国债额度之和
     */
    private int sumBondsMoney(Inventory inventory) {
        int money = 0;
        for (var itemStack : inventory.getContents()) {
            if (itemStack != null) {
                var curMoney = Bonds.getValue(itemStack);
                curMoney *= itemStack.getAmount();
                money += curMoney;
            }
        }
        return money;
    }
}
