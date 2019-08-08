package org.xjcraft.plot.util;

import org.apache.ibatis.session.SqlSession;
import org.cat73.bukkitboot.util.Lang;
import org.xjcraft.plot.XJPlot;
import org.xjcraft.plot.common.exception.RollbackException;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 数据库操作工具类
 * <p>所有操作均应在事务中进行</p>
 */
public class DB {
    private DB() {
        throw new UnsupportedOperationException();
    }

    /**
     * SqlSession 的 ThreadLocal
     */
    private static ThreadLocal<SqlSession> sqlSessionThreadLocal = new ThreadLocal<>();
    private static XJPlot pluginInstance;

    public static synchronized void setPluginInstance(XJPlot pluginInstance) {
        if (DB.pluginInstance != null) {
            throw new IllegalStateException("pluginInstance 只允许设置一次");
        }
        DB.pluginInstance = pluginInstance;
    }

    /**
     * 判断当前是否处在一个事务里
     * @return 当前是否处在一个事务里
     */
    public static boolean isInTransaction() {
        var session = DB.sqlSessionThreadLocal.get();
        return session != null;
    }

    /**
     * 获取当前事务的 SqlSession，若未在事务中，则会抛出 NullPointerException
     * @return 当前事务的 SqlSession
     */
    public static SqlSession getSqlSession() {
        var session = DB.sqlSessionThreadLocal.get();
        return Objects.requireNonNull(session);
    }

    /**
     * 在当前事务中获取一个 Mapper 的实例，若未在事务中，则会抛出 NullPointerException
     * @param mapperClass Mapper 的类型
     * @param <T> Mapper 的类型
     * @return 获取到的 Mapper 的实例
     */
    public static <T> T getMapper(Class<? extends T> mapperClass) {
        var session = DB.getSqlSession();
        var mapper = session.getMapper(mapperClass);
        return Objects.requireNonNull(mapper);
    }

    /**
     * 在事务包装中执行一段代码
     * <p>支持嵌套执行，嵌套时只会存在一个 SqlSession，嵌套时不会提交，只有最外层的事务才会提交</p>
     * @param code 事务代码，无需在代码中关闭 SqlSession
     * @param <T> 返回值的类型
     * @return 事务代码的返回值
     */
    public static <T> T tranr(Supplier<T> code) {
        if (DB.isInTransaction()) {
            // 自身处在事务里
            return code.get();
        } else {
            // 自身未处在事务里
            var session = DB.pluginInstance.getSqlSession();
            try {
                DB.sqlSessionThreadLocal.set(session);
                var result = code.get();
                session.commit();
                return result;
            } catch (RollbackException e) {
                session.rollback();
                @SuppressWarnings("unchecked")
                var result = (T) e.getData();
                return result;
            } catch (Exception e) {
                session.rollback();
                throw Lang.wrapThrow(e);
            } finally {
                DB.sqlSessionThreadLocal.remove();
                session.close();
            }
        }
    }

    /**
     * 在事务中执行一段代码
     * <p>支持嵌套执行，嵌套时只会存在一个 SqlSession，嵌套时不会提交，只有最外层的事务才会提交</p>
     * @param code 事务代码，无需在代码中关闭 SqlSession
     */
    public static void tran(Runnable code) {
        DB.tranr(() -> {
            code.run();
            return null;
        });
    }
}
