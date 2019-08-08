package org.xjcraft.plot.log.service;

import org.cat73.bukkitboot.annotation.core.Bean;
import org.xjcraft.plot.log.entity.Log;
import org.xjcraft.plot.log.mapper.LogMapper;
import org.xjcraft.plot.util.DB;
import java.time.LocalDateTime;

/**
 * 操作记录的业务层
 */
@Bean
public class LogService {
    /**
     * 记录操作日志
     * @param playerName 进行操作的玩家名(不区分大小写)
     * @param type 操作类型
     * @param remark 日志内容
     */
    public void log(String playerName, Log.LogType type, String remark) {
        DB.tran(() -> {
            var logMapper = DB.getMapper(LogMapper.class);

            // 玩家名应该是全小写的
            var lowerPlayerName = playerName.toLowerCase();

            // 创建日志实体类
            var log = new Log()
                    .setPlayerName(lowerPlayerName)
                    .setAddtime(LocalDateTime.now())
                    .setType(type)
                    .setRemark(remark);

            // 将日志保存到数据库中
            logMapper.save(log);
        });
    }

    /**
     * 记录系统操作日志
     * @param type 操作类型
     * @param remark 日志内容
     */
    public void sysLog(Log.LogType type, String remark) {
        this.log("<System>", type, remark);
    }
}
