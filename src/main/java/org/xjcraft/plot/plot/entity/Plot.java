package org.xjcraft.plot.plot.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 地块
 */
@Data
@Accessors(chain = true)
public class Plot {
    /**
     * 地块的 ID
     */
    private Integer id;
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

    // TODO 创建时间
}
