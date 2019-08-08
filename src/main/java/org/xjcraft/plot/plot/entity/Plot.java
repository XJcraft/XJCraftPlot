package org.xjcraft.plot.plot.entity;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

/**
 * 地块
 */
@Data
@Accessors(chain = true)
public class Plot {
    /**
     * 地块编号
     */
    private Integer id;
    /**
     * 所在世界的名称
     */
    private String worldName;
    /**
     * 地块 x 坐标中较小的数字
     */
    private Integer x1;
    /**
     * 地块 z 坐标中较小的数字
     */
    private Integer z1;
    /**
     * 地块 x 坐标中较大的数字
     */
    private Integer x2;
    /**
     * 地块 z 坐标中较大的数字
     */
    private Integer z2;
    /**
     * 地块的创建时间
     */
    private LocalDateTime addtime;
    /**
     * 出售方式
     */
    private SellType sellType;
    /**
     * 出售方式 - 价格
     */
    private Integer sellPrice;

    /**
     * 出售方式
     */
    public enum SellType {
        /**
         * 未定义 (不允许出售)
         */
        UNDEFINED("暂不出售"),
        /**
         * 租赁
         */
        LEASE("租赁"),
        /**
         * 一口价
         */
        PERMANENT("一口价"),
        /**
         * 福利地块 (免费)
         */
        FREE("福利(免费)");

        @Getter
        private final String displayName;

        SellType(String displayName) {
            this.displayName = displayName;
        }
    }
}
