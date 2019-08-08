package org.xjcraft.plot.log.service;

import org.apache.ibatis.session.SqlSession;
import org.cat73.bukkitboot.annotation.core.Bean;
import org.xjcraft.plot.log.entity.Log;
import org.xjcraft.plot.log.mapper.LogMapper;
import java.time.LocalDateTime;

/**
 * 操作记录的业务层
 */
@Bean
public class LogService {
    /**
     * 记录操作日志
     * @param session SqlSession
     * @param playerName 进行操作的玩家名(不区分大小写)
     * @param type 操作类型
     * @param remark 日志内容
     */
    public void log(SqlSession session, String playerName, Log.LogType type, String remark) {
        var mapper = session.getMapper(LogMapper.class);

        // 玩家名应该是全小写的
        playerName = playerName.toLowerCase();

        var log = new Log()
                .setPlayerName(playerName)
                .setAddtime(LocalDateTime.now())
                .setType(type)
                .setRemark(remark);

        mapper.save(log);
    }

    /**
     * 记录系统操作日志
     * @param session SqlSession
     * @param type 操作类型
     * @param remark 日志内容
     */
    public void sysLog(SqlSession session, Log.LogType type, String remark) {
        this.log(session, "<System>", type, remark);
    }
}
