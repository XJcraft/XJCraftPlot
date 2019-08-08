package org.xjcraft.plot.sell.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

/**
 * 地块出售
 */
@Data
@Accessors(chain = true)
public class Lease {
    /**
     * 地块编号
     */
    private Integer id;
    /**
     * 玩家名
     */
    private String playerName;
    /**
     * 购买时间
     */
    private LocalDateTime addtime;
    /**
     * 是否自动退租(对于租赁, 到期后取消租赁)
     */
    private Boolean autoWithdrawal;
    /**
     * 命令方块坐标(x,y,z)，如果这块地内没有命令方块则留空
     */
    private String cbPos;
}
