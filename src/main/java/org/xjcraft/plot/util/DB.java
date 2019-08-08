package org.xjcraft.plot.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.bukkit.configuration.Configuration;
import org.cat73.bukkitboot.util.Lang;
import org.cat73.bukkitboot.util.Strings;
import org.cat73.bukkitboot.util.reflect.Scans;
import org.xjcraft.plot.XJPlot;
import org.xjcraft.plot.common.exception.RollbackException;
import java.io.IOException;
import java.sql.SQLException;
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
     * 数据库连接的 SqlSession 工厂
     */
    private static SqlSessionFactory sqlSessionFactory;
    /**
     * SqlSession 的 ThreadLocal
     */
    private static ThreadLocal<SqlSession> sqlSessionThreadLocal = new ThreadLocal<>();

    /**
     * 初始化数据库连接
     */
    public static synchronized void initDatabase(Configuration config) {
        if (DB.sqlSessionFactory != null) {
            throw new IllegalStateException("initDatabase 只允许调用一次");
        }

        // 构造数据源
        var hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(config.getString("db.driver"));
        hikariConfig.setJdbcUrl(config.getString("db.url"));
        hikariConfig.setUsername(config.getString("db.username"));
        hikariConfig.setPassword(config.getString("db.password"));
        var dataSource = new HikariDataSource(hikariConfig);

        // 构造各类配置
        var transactionFactory = new JdbcTransactionFactory();
        var environment = new Environment("development", transactionFactory, dataSource);
        var configuration = new org.apache.ibatis.session.Configuration(environment);
        // 注册 Mapper
        try {
            for (var clazz : Scans.scanClass(XJPlot.class)) {
                if (clazz.isInterface() && clazz.getSimpleName().endsWith("Mapper") && clazz.getPackage().getName().contains("mapper")) {
                    configuration.addMapper(clazz);
                }
            }
        } catch (IOException e) {
            throw Lang.impossible();
        }

        // 获得 SqlSessionFactory
        DB.sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

        // 自动创建表结构
        DB.createTables();
    }

    /**
     * 初始化表结构(执行 mysql.base.sql)
     */
    private static void createTables() {
        String sqls;
        try {
            sqls = new String(Streams.readAllAsBytes(DB.class.getResourceAsStream("/mysql.base.sql")));
        } catch (IOException e) {
            throw Lang.impossible();
        }

        try (var sqlSession = DB.getSqlSession()) {
            try (var statement = sqlSession.getConnection().createStatement()) {
                for (var sql : sqls.split(";")) {
                    if (Strings.notBlank(sql)) {
                        statement.executeQuery(sql);
                    }
                }
            } catch (SQLException e) {
                throw Lang.wrapThrow(e);
            }
        }
    }

    /**
     * 获取一个 SqlSession，注意一定记得关闭，不然会造成资源泄漏
     * @return 获取到的 SqlSession
     */
    private static SqlSession getSqlSession0() {
        return DB.sqlSessionFactory.openSession();
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
     * <p>用完后无需关闭</p>
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
     * @param code 事务代码
     * @param <T> 返回值的类型
     * @return 事务代码的返回值
     */
    public static <T> T tranr(Supplier<T> code) {
        if (DB.isInTransaction()) {
            // 自身处在事务里
            return code.get();
        } else {
            // 自身未处在事务里
            var session = DB.getSqlSession0();
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
     * @param code 事务代码
     */
    public static void tran(Runnable code) {
        DB.tranr(() -> {
            code.run();
            return null;
        });
    }
}
