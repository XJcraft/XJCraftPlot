package org.xjcraft.plot.balance.service;

import org.cat73.bukkitboot.annotation.core.Bean;
import org.cat73.bukkitboot.annotation.core.Inject;
import org.xjcraft.plot.balance.entity.Balance;
import org.xjcraft.plot.balance.entity.BalanceLog;
import org.xjcraft.plot.balance.mapper.BalanceLogMapper;
import org.xjcraft.plot.balance.mapper.BalanceMapper;
import org.xjcraft.plot.log.entity.Log;
import org.xjcraft.plot.log.service.LogService;
import org.xjcraft.plot.util.DB;
import org.xjcraft.plot.util.Result;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 玩家余额的业务层
 */
@Bean
public class BalanceService {
    @Inject
    private LogService logService;

    /**
     * 查询玩家的余额
     * @param playerName 玩家名(不区分大小写)
     * @return 查到的余额
     */
    public Balance getByPlayer(String playerName) {
        return DB.tranr(() -> {
            var balanceMapper = DB.getMapper(BalanceMapper.class);

            // 玩家名应该是全小写的
            var lowerPlayerName = playerName.toLowerCase();

            // 查询余额
            var balance = balanceMapper.getByPlayer(lowerPlayerName);
            // 如果没查到则初始化一个
            if (balance == null) {
                balance = new Balance()
                        .setPlayerName(lowerPlayerName)
                        .setBalance(BigDecimal.ZERO)
                        .setFreeze(BigDecimal.ZERO);

                balanceMapper.save(balance);
            }

            // 返回结果
            return balance;
        });
    }

    /**
     * 判断是否可以扣款(可用余额是否充足)
     * @param playerName 玩家名(不区分大小写)
     * @param money 希望扣的钱数
     * @return 是否可以扣款
     */
    public boolean hasCharge(String playerName, BigDecimal money) {
        var balance = this.getByPlayer(playerName);
        return balance.getBalance().subtract(balance.getFreeze()).compareTo(money) >= 0;
    }

    /**
     * 判断是否可以扣冻结额度
     * @param playerName 玩家名(不区分大小写)
     * @param money 希望扣的钱数
     * @return 是否可以扣款
     */
    public boolean hasChargeFreeze(String playerName, BigDecimal money) {
        var balance = this.getByPlayer(playerName);
        return balance.getFreeze().compareTo(money) >= 0;
    }

    /**
     * 进行一次余额操作并记录账本操作日志
     * @param playerName 玩家名(不区分大小写)
     * @param type 操作类型
     * @param balanceChange 余额变更值
     * @param freezeChange 冻结额度变更值
     * @param remark 操作备注
     */
    public void log(String playerName, BalanceLog.AccountLogType type, BigDecimal balanceChange, BigDecimal freezeChange, String remark) {
        DB.tran(() -> {
            var balanceMapper = DB.getMapper(BalanceMapper.class);
            var balanceLogMapper = DB.getMapper(BalanceLogMapper.class);
            var time = LocalDateTime.now();

            // 更新玩家的余额
            var balance = this.getByPlayer(playerName);
            var beforeBalance = balance.getBalance();
            var beforeFreeze = balance.getFreeze();
            balance.setBalance(balance.getBalance().add(balanceChange));
            balance.setFreeze(balance.getFreeze().add(freezeChange));
            balanceMapper.updateById(balance);

            // 记录操作日志
            var log = new BalanceLog()
                    .setBalanceId(balance.getId())
                    .setAddtime(time)
                    .setType(type)
                    .setBeforeBalance(beforeBalance)
                    .setBeforeFreeze(beforeFreeze)
                    .setChangeBalance(balanceChange)
                    .setChangeFreeze(freezeChange)
                    .setRemark(remark);
            balanceLogMapper.save(log);
        });
    }

    /**
     * 充值
     * @param playerName 被充值的玩家的名字
     * @param money 充值的金额
     * @param reason 充值原因
     * @param operator 操作人
     */
    public Result<Balance> recharge(String playerName, BigDecimal money, String reason, String operator) {
        return DB.tranr(() -> {
            var balanceMapper = DB.getMapper(BalanceMapper.class);

            // 充值
            this.log(playerName, BalanceLog.AccountLogType.OP_RECHARGE, money, BigDecimal.ZERO, reason);
            // 记录日志
            this.logService.log(operator, Log.LogType.OP_RECHARGE, String.format("为 %s 充值 %s 元，充值原因: %s", playerName, money, reason));
            // 返回结果
            return Result.success(this.getByPlayer(playerName));
        });
    }

    // TODO 查询操作记录
}
