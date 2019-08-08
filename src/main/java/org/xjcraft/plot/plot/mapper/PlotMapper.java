package org.xjcraft.plot.plot.mapper;

import org.apache.ibatis.annotations.*;
import org.xjcraft.plot.common.mapper.CommonMapper;
import org.xjcraft.plot.plot.entity.Plot;

/**
 * 地块的 Mapper 接口
 */
public interface PlotMapper extends CommonMapper {
    String INSERT_FIELDS = "id, world_name, x1, z1, x2, z2, addtime, lease_type, lease_params";
    String SELECT_FIELDS = "id, world_name AS worldName, x1, z1, x2, z2, addtime, lease_type AS leaseType, lease_params AS leaseParams";
    String TABLE_NAME = "xjplot_plot";

    /**
     * 根据 ID 查询地块
     * @param id 地块的 ID
     * @return 查询到的地块，如果没查到则返回 null
     */
    @Select({
            "SELECT",
              SELECT_FIELDS,
            "FROM " + TABLE_NAME,
            "WHERE id = #{id}"
    })
    Plot getById(@Param("id") int id);

    /**
     * 根据坐标查询地块
     * @param worldName 世界名
     * @param x x 坐标
     * @param z z 坐标
     * @return 查到的地块，如果没查到则返回 null
     */
    @Select({
            "SELECT",
              SELECT_FIELDS,
            "FROM " + TABLE_NAME,
            "WHERE world_name = #{worldName}",
            "AND x1 >= #{x} AND x2 <= #{x}",
            "AND z1 >= #{z} AND z2 <= #{z}"
    })
    Plot getByPos(@Param("worldName") String worldName, @Param("x") int x, @Param("z") int z);

    /**
     * 插入一条新记录
     * @param plot 被插入的地块实体
     */
    @Insert({
            "INSERT INTO ",
              TABLE_NAME, "(",
                INSERT_FIELDS,
              ") VALUES (#{plot.id}, #{plot.worldName}, #{plot.x1}, #{plot.z1}, #{plot.x2}, #{plot.z2}, #{plot.addtime}, #{plot.leaseType}, #{plot.leaseParams})"
    })
    void save(@Param("plot") Plot plot);

    /**
     * 移除一个地块
     * @param id 地块的 ID
     */
    @Delete({
            "DELETE FROM",
              TABLE_NAME,
            "WHERE id = #{id}"
    })
    void removeById(@Param("id") int id);

    /**
     * 校验是否存在重叠的地块
     * @param worldName 世界名
     * @param x1 x 坐标中较小的数字
     * @param z1 z 坐标中较小的数字
     * @param x2 x 坐标中较大的数字
     * @param z2 z 坐标中较大的数字
     * @return 是否存在与之重叠的地块
     */
    @Select({
            "SELECT",
            "  COUNT(*) > 0",
            "FROM " + TABLE_NAME,
            "WHERE world_name = #{worldName}",
            "  AND x1 <= #{x2}",
            "  AND x2 >= #{x1}",
            "  AND z1 <= #{z2}",
            "  AND z2 >= #{z1}"
    })
    boolean rangeOverlap(@Param("worldName") String worldName, @Param("x1") int x1, @Param("z1") int z1, @Param("x2") int x2, @Param("z2") int z2);
}
