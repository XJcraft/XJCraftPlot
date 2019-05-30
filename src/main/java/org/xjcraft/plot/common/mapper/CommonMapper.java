package org.xjcraft.plot.common.mapper;

import org.apache.ibatis.annotations.Select;

/**
 * 公共 Mapper
 */
public interface CommonMapper {
    /**
     * 查询当前事务中最后一次插入的记录自动生成的 ID
     * @return 查询到的 ID
     */
    @Select("SELECT LAST_INSERT_ID();")
    int lastId();
}
