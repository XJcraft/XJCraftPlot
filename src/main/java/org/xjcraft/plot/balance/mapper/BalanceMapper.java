package org.xjcraft.plot.balance.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.xjcraft.plot.balance.entity.Balance;

/**
 * 玩家余额的 Mapper 接口
 */
public interface BalanceMapper {
    String INSERT_FIELDS = "id, player_name, balance, freeze";
    String SELECT_FIELDS = "id, player_name AS playerName, balance, freeze";
    String TABLE_NAME = "xjplot_balance";

    /**
     * 基于玩家名查询玩家的余额
     * @param playerName 玩家名
     * @return 查询到的余额，如果没查到则返回 null
     */
    @Select({
            "SELECT",
              SELECT_FIELDS,
            "FROM " + TABLE_NAME,
            "WHERE player_name = #{playerName}"
    })
    Balance getByPlayer(@Param("playerName") String playerName);

    /**
     * 插入一条新记录
     * @param balance 被插入的余额实体
     */
    @Insert({
            "INSERT INTO ",
              TABLE_NAME, "(",
                INSERT_FIELDS,
              ") VALUES (#{balance.id}, #{balance.playerName}, #{balance.balance}, #{balance.freeze})"
    })
    void save(@Param("balance") Balance balance);

    /**
     * 基于 ID 更新一条记录
     * @param balance 被更新的余额的实体，会使用其 id 字段作为更新条件
     */
    @Update({
            "UPDATE " + TABLE_NAME + " SET ",
              "player_name=#{balance.playerName}",
              "balance=#{balance.balance}",
              "freeze=#{balance.freeze}",
            "WHERE id=#{balance.id}"
    })
    void updateById(@Param("balance") Balance balance);
}
