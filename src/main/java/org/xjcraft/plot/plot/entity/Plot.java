package org.xjcraft.plot.plot.entity;

import lombok.Data;
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
     * 租赁方式
     */
    private LeaseType leaseType;
    /**
     * 租赁方式 - 参数
     */
    private String leaseParams;

    /**
     * 租赁方式
     */
    public enum LeaseType {
        /**
         * 未定义 (不允许出租)
         */
        UNDEFINED,
        /**
         * 租赁
         */
        LEASE,
        /**
         * 一口价
         */
        PERMANENT,
        /**
         * 福利地块 (免费)
         */
        FREE
    }
}
