package org.xjcraft.plot.balance.service;

import org.apache.ibatis.session.SqlSession;
import org.cat73.bukkitboot.annotation.core.Bean;
import org.xjcraft.plot.balance.entity.Balance;
import org.xjcraft.plot.balance.entity.BalanceLog;
import org.xjcraft.plot.balance.mapper.BalanceLogMapper;
import org.xjcraft.plot.balance.mapper.BalanceMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 玩家余额的业务层
 */
@Bean
public class BalanceService {
    /**
     * 查询玩家的余额
     * @param session SqlSession
     * @param playerName 玩家名(不区分大小写)
     * @return 查到的余额
     */
    public Balance getByPlayer(SqlSession session, String playerName) {
        var mapper = session.getMapper(BalanceMapper.class);

        // 玩家名应该是全小写的
        playerName = playerName.toLowerCase();

        // 查询余额
        var balance = mapper.getByPlayer(playerName);
        // 如果没查到则初始化一个
        if (balance == null) {
            balance = new Balance()
                    .setPlayerName(playerName)
                    .setBalance(BigDecimal.ZERO)
                    .setFreeze(BigDecimal.ZERO);

            mapper.save(balance);
        }

        // 返回结果
        return balance;
    }

    /**
     * 判断是否可以扣款(可用余额是否充足)
     * @param session SqlSession
     * @param playerName 玩家名(不区分大小写)
     * @param money 希望扣的钱数
     * @return 是否可以扣款
     */
    public boolean hasCharge(SqlSession session, String playerName, BigDecimal money) {
        var balance = this.getByPlayer(session, playerName);
        return balance.getBalance().subtract(balance.getFreeze()).compareTo(money) >= 0;
    }

    /**
     * 进行一次余额操作并记录日志
     * @param session SqlSession
     * @param playerName 玩家名(不区分大小写)
     * @param type 操作类型
     * @param balanceChange 余额变更值
     * @param freezeChange 冻结额度变更值
     * @param remark 操作备注
     */
    public void log(SqlSession session, String playerName, BalanceLog.AccountLogType type, BigDecimal balanceChange, BigDecimal freezeChange, String remark) {
        var balanceMapper = session.getMapper(BalanceMapper.class);
        var balanceLogMapper = session.getMapper(BalanceLogMapper.class);
        var time = LocalDateTime.now();

        // 更新玩家的余额
        var balance = this.getByPlayer(session, playerName);
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
    }

    // TODO 查询操作记录
}
