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
        // 账目
        /**
         * OP 充值
         */
        OP_RECHARGE,
        /**
         * 充值
         */
        RECHARGE,
        // 地块
        /**
         * 创建地块
         */
        CREATE_PLOT,
        /**
         * 编辑地块
         */
        EDIT_PLOT,
        /**
         * 移除地块
         */
        REMOVE_PLOT,
        /**
         * 调整地块出租方式
         */
        PLOT_CHANGE_SELL_TYPE,
    }
}
