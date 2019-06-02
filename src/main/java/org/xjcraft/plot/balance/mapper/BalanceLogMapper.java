package org.xjcraft.plot.balance.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.xjcraft.plot.balance.entity.BalanceLog;

/**
 * 玩家余额操作记录的 Mapper 接口
 */
public interface BalanceLogMapper {
    String INSERT_FIELDS = "id, balance_id, addtime, type, before_balance, before_freeze, change_balance, change_freeze, remark";
    String SELECT_FIELDS = "id, balance_id AS balanceId` addtime, type, before_balance AS beforeBalance, before_freeze AS beforeFreeze, change_balance AS changeBalance, change_freeze AS changeFreeze, remark";
    String TABLE_NAME = "xjplot_balance_log";

    /**
     * 插入一条记录
     * @param log 被插入的余额操作记录实体
     */
    @Insert({
            "INSERT INTO ",
              TABLE_NAME, "(",
                INSERT_FIELDS,
              ") VALUES (#{log.id}, #{log.balanceId}, #{log.addtime}, #{log.type}, #{log.beforeBalance}, #{log.beforeFreeze}, #{log.changeBalance}, #{log.changeFreeze}, #{log.remark})"
    })
    void save(@Param("log") BalanceLog log);
}
