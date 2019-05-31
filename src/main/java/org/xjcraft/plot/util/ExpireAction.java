package org.xjcraft.plot.util;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.cat73.bukkitboot.util.Plugins;
import org.xjcraft.plot.XJPlot;

/**
 * 一个可过期的操作
 * <p>过期后会执行 timeoutAction</p>
 * <p>可主动调用 cancel 取消其一切操作</p>
 * <p>或调用 doAction 来执行预定的 action</p>
 */
public class ExpireAction {
    /**
     * 插件本体的实例
     */
    private static XJPlot plugin = Plugins.current();

    /**
     * 在 Bukkit 注册的定时任务
     */
    private final BukkitTask task;
    /**
     * 要执行的操作
     */
    private final Runnable action;
    /**
     * 过期时执行的操作
     */
    private final Runnable timeoutAction;
    /**
     * 是否已过期
     */
    private boolean timeout = false;

    /**
     * 构造一个可过期的操作
     * @param tickCount 过期时间(tick)
     * @param action 要执行的操作
     */
    public ExpireAction(int tickCount, Runnable action) {
        this(tickCount, action, null);
    }

    /**
     * 构造一个可过期的操作
     * @param tickCount 过期时间(tick)
     * @param action 要执行的操作
     * @param timeoutAction 过期时要执行的操作
     */
    public ExpireAction(int tickCount, Runnable action, Runnable timeoutAction) {
        this.action = action;
        this.timeoutAction = timeoutAction;
        this.task = Bukkit.getServer().getScheduler().runTaskLater(plugin, this::timeout, tickCount);
    }

    /**
     * 设置这个操作已经过期
     */
    private void timeout() {
        if (!this.isTimeout()) {
            this.timeout = true;
            if (this.timeoutAction != null) {
                this.timeoutAction.run();
            }
        }
    }

    /**
     * 取消这个操作
     */
    public void cancel() {
        this.task.cancel();
    }

    /**
     * 判断这个操作是否已过期
     * @return 这个操作是否已过期
     */
    public boolean isTimeout() {
        return this.timeout;
    }

    /**
     * 执行操作(并同时取消过期计时器)
     */
    public void doAction() {
        if (!this.isTimeout()) {
            this.cancel();
            if (this.action != null) {
                this.action.run();
            }
        }
    }
}
