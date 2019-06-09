package org.xjcraft.plot.balance.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 玩家余额的操作记录
 */
@Data
@Accessors(chain = true)
public class BalanceLog {
    private Integer id;
    /**
     * 余额表的 ID
     */
    private Integer balanceId;
    /**
     * 操作时间
     */
    private LocalDateTime addtime;
    /**
     * 操作类型
     */
    private AccountLogType type;

    /**
     * 操作前的余额
     */
    private BigDecimal beforeBalance;
    /**
     * 操作前的冻结额度
     */
    private BigDecimal beforeFreeze;
    /**
     * 余额的变更值(入正出负)
     */
    private BigDecimal changeBalance;
    /**
     * 冻结额度的变更值(入正出负)
     */
    private BigDecimal changeFreeze;

    /**
     * 备注
     */
    private String remark;

    /**
     * 操作类型
     */
    public enum AccountLogType {
        /**
         * OP 充值
         */
        OP_RECHARGE,
        /**
         * 充值
         */
        RECHARGE
    }
}
