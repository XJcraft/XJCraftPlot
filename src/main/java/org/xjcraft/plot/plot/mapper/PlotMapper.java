package org.xjcraft.plot.plot.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.xjcraft.plot.plot.entity.Plot;

/**
 * 地块的 Mapper 接口
 */
public interface PlotMapper {
    @Select({
            "SELECT",
            "    id, x1, z1, x2, z2",
            "FROM xjplot_plot",
            "WHERE id = #{id}"
    })
    Plot getById(@Param("id") Integer id);
}
