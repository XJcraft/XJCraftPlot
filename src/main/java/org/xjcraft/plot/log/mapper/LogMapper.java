package org.xjcraft.plot.log.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.xjcraft.plot.log.entity.Log;

/**
 * 操作记录的 Mapper 接口
 */
public interface LogMapper {
    String INSERT_FIELDS = "player_name, addtime, type, remark";
    String SELECT_FIELDS = "id, player_name AS `playerName`, addtime, type, remark";
    String TABLE_NAME = "xjplot_log";

    /**
     * 插入一条记录
     * @param log 被插入的余额操作记录实体
     */
    @Insert({
            "INSERT INTO ",
              TABLE_NAME, "(",
                INSERT_FIELDS,
              ") VALUES (#{log.playerName}, #{log.addtime}, #{log.type}, #{log.remark})"
    })
    void save(@Param("log") Log log);
}
