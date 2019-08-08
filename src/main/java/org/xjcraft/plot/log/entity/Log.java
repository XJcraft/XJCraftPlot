package org.xjcraft.plot.log.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

/**
 * 玩家余额的操作记录
 */
@Data
@Accessors(chain = true)
public class Log {
    private Integer id;
    /**
     * 余额表的 ID
     */
    private String playerName;
    /**
     * 操作时间
     */
    private LocalDateTime addtime;
    /**
     * 操作类型
     */
    private LogType type;
    /**
     * 备注
     */
    private String remark;

    /**
     * 操作类型
     */
    public enum LogType {
        /**
         * OP 充值
         */
        OP_RECHARGE,
        /**
         * 充值
         */
        RECHARGE,
    }
}
