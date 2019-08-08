package org.xjcraft.plot;

import org.bukkit.plugin.java.JavaPlugin;
import org.cat73.bukkitboot.annotation.core.BukkitBootPlugin;
import org.cat73.bukkitboot.util.Lang;
import org.cat73.bukkitboot.util.Logger;
import org.xjcraft.plot.util.DB;

/**
 * 插件主类
 */
@BukkitBootPlugin
public class XJPlot extends JavaPlugin {
    @Override
    public void onLoad() {
        this.saveDefaultConfig();
        try {
            DB.initDatabase(this.getConfig());
        } catch(Exception e) {
            Logger.error("初始化数据库连接失败，请检查数据库地址、账号、密码是否正确");
            throw Lang.throwAny(e);
        }
    }
}
