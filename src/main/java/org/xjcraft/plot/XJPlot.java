package org.xjcraft.plot;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.bukkit.plugin.java.JavaPlugin;
import org.cat73.bukkitboot.annotation.core.BukkitBootPlugin;
import org.cat73.bukkitboot.util.Lang;
import org.cat73.bukkitboot.util.Logger;
import org.cat73.bukkitboot.util.Strings;
import org.cat73.bukkitboot.util.reflect.Scans;
import org.xjcraft.plot.common.exception.RollbackException;
import org.xjcraft.plot.util.Streams;
import java.io.IOException;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 插件主类
 */
@BukkitBootPlugin
public class XJPlot extends JavaPlugin {
    /**
     * 数据库连接的 SqlSession 工厂
     */
    @Getter
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public void onLoad() {
        this.saveDefaultConfig();
        try {
            this.initDatabase();
        } catch(Exception e) {
            Logger.error("初始化数据库连接失败，请检查数据库地址、账号、密码是否正确");
            throw Lang.throwAny(e);
        }
    }

    /**
     * 初始化数据库连接
     */
    public void initDatabase() {
        var config = this.getConfig();

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
        var configuration = new Configuration(environment);
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
        this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

        // 自动创建表结构
        this.createTables();
    }

    /**
     * 初始化表结构(执行 mysql.base.sql)
     */
    private void createTables() {
        String sqls;
        try {
            sqls = new String(Streams.readAllAsBytes(XJPlot.class.getResourceAsStream("/mysql.base.sql")));
        } catch (IOException e) {
            throw Lang.impossible();
        }

        try (var sqlSession = this.getSqlSession()) {
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
    public SqlSession getSqlSession() {
        return this.sqlSessionFactory.openSession();
    }

    /**
     * 在事务中执行一段代码
     * @param code 事务代码，无需在代码中关闭 SqlSession
     * @param <T> 返回值类型
     * @return 事务代码的返回值
     */
    public <T> T tranr(Function<SqlSession, T> code) {
        var sqlSession = this.sqlSessionFactory.openSession(false);
        T result;
        try {
            result = code.apply(sqlSession);
            sqlSession.commit();
        } catch (RollbackException e) {
            sqlSession.rollback();
            return null;
        } catch (Exception e) {
            sqlSession.rollback();
            throw Lang.wrapThrow(e);
        } finally {
            sqlSession.close();
        }

        return result;
    }

    /**
     * 在事务中执行一段代码
     * @param code 事务代码，无需在代码中关闭 SqlSession
     */
    public void tran(Consumer<SqlSession> code) {
        this.tranr(sqlSession -> {
            code.accept(sqlSession);
            return null;
        });
    }

    /**
     * 在事务中执行一段代码
     * @param clazz 使用的 Mapper 的类型
     * @param code 事务代码
     * @param <T> Mapper 的类型
     * @param <R> 返回值的类型
     * @return 事务代码的返回值
     */
    public <T, R> R tranr(Class<? extends T> clazz, Function<T, R> code) {
        return this.tranr(sqlSession -> {
            var mapper = sqlSession.getMapper(clazz);
            return code.apply(mapper);
        });
    }

    /**
     * 在事务中执行一段代码
     * @param clazz 使用的 Mapper 的类型
     * @param code 事务代码
     * @param <T> Mapper 的类型
     */
    public <T> void tran(Class<? extends T> clazz, Consumer<T> code) {
        this.tranr(clazz, mapper -> {
            code.accept(mapper);
            return null;
        });
    }
}
