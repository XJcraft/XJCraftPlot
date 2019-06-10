package org.xjcraft.plot.util;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.cat73.bukkitboot.util.PlayerSession;
import org.cat73.bukkitboot.util.Plugins;
import java.util.Optional;

/**
 * 一个可过期的操作
 * <p>过期后会执行 timeoutAction</p>
 * <p>可主动调用 cancel 取消其一切操作</p>
 * <p>或调用 doAction 来执行预定的 action</p>
 */
public class ExpireAction {
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
        if (tickCount <= 0) {
            throw new IllegalArgumentException("tickCount <= 0");
        }

        this.action = action;
        this.timeoutAction = timeoutAction;
        this.task = Bukkit.getServer().getScheduler().runTaskLater(Plugins.current(), this::timeout, tickCount);
    }

    /**
     * 设置这个操作已经过期
     */
    private void timeout() {
        if (!this.isTimeout()) {
            this.timeout = true;
            Optional.ofNullable(this.timeoutAction).ifPresent(Runnable::run);
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
            Optional.ofNullable(this.action).ifPresent(Runnable::run);
        }
    }

    /**
     * 获取一个自动设置可过期操作的 Builder
     * <p>一般用于给一个玩家设置一个带有效期的、需要确认的操作</p>
     * @param player 操作的玩家
     * @return Builder 的实例
     */
    public static ExpireActionBuilder auto(Player player) {
        return new ExpireActionBuilder(player);
    }

    /**
     * 自动设置可过期操作的 Builder
     */
    @Setter
    @Accessors(chain = true, fluent = true)
    public static class ExpireActionBuilder {
        /**
         * 操作的玩家
         */
        private final Player player;
        /**
         * 过期时间，默认为 30s(600 tick)
         */
        private int tickCount = 30 * 20;
        /**
         * 玩家确认时要执行的操作
         */
        private Runnable action;
        /**
         * 过期时执行的操作
         */
        private Runnable timeoutAction;
        /**
         * 设置失败(由于有正在等待确认的操作)时进行的操作，通常用于提示玩家失败
         */
        private Runnable failAction;
        /**
         * 设置成功时进行的操作，通常用于提示玩家确认
         */
        private Runnable successAction;

        private ExpireActionBuilder(Player player) {
            this.player = player;
            this.failAction = () -> this.player.sendMessage(ChatColor.RED + "有正在等待确认的操作，请确认或等待上一个操作过期后重试");
            this.successAction = () -> this.player.sendMessage(ChatColor.GREEN + "如果您确认要进行操作，请输入 /xjplot confirm 来确认");
        }

        public void start() {
            // 如果存在正在等待确认的操作，则执行失败操作
            ExpireAction currentAction = PlayerSession.forPlayer(this.player).get("waitAction");
            if (currentAction != null && !currentAction.isTimeout()) {
                Optional.ofNullable(this.failAction).ifPresent(Runnable::run);
                return;
            }

            // 添加等待确认的操作
            PlayerSession.forPlayer(player).set("waitAction", new ExpireAction(this.tickCount, this.action, this.timeoutAction));

            // 执行成功操作
            Optional.ofNullable(this.successAction).ifPresent(Runnable::run);
        }
    }
}
