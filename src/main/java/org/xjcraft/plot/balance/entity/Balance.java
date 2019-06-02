package org.xjcraft.plot.balance.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import java.math.BigDecimal;

/**
 * 玩家的余额
 */
@Data
@Accessors(chain = true)
public class Balance {
    private Integer id;
    /**
     * 玩家名(在确定 UUID 在盗版服的生成机制之前还是这种安全一些吧 0.0)
     */
    private String playerName;
    /**
     * 余额
     */
    private BigDecimal balance;
    /**
     * 冻结额度
     */
    private BigDecimal freeze;
}
