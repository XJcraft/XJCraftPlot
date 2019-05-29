package org.xjcraft.plot;

import org.bukkit.event.Listener;
import org.cat73.bukkitboot.annotation.core.Bean;
import org.cat73.bukkitboot.annotation.core.PostConstruct;
import org.cat73.bukkitboot.util.Logger;
import org.xjcraft.plot.plot.entity.Plot;
import org.xjcraft.plot.plot.mapper.PlotMapper;

@Bean
public class Demo implements Listener {
    @PostConstruct
    public void demo(PlotPlugin plotPlugin) {
        plotPlugin.transaction(sqlSession -> {
            PlotMapper plotMapper = sqlSession.getMapper(PlotMapper.class);
            Plot plot = plotMapper.getById(1);

            Logger.debug("%s, %d, %d, %d, %d", plot.getId(), plot.getX1(), plot.getZ1(), plot.getX2(), plot.getZ2());
        });
    }
}
