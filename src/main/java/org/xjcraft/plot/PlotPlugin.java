package org.xjcraft.plot;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.cat73.bukkitboot.annotation.core.BukkitBootPlugin;
import org.cat73.bukkitboot.util.Lang;
import org.cat73.bukkitboot.util.reflect.Scans;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.sql.DataSource;

/**
 * 插件主类
 */
@BukkitBootPlugin
public class PlotPlugin extends JavaPlugin {
    /**
     * 数据库连接的 SqlSession 工厂
     */
    @Getter
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public void onLoad() {
        this.initDatabase();
    }

    /**
     * 初始化数据库连接
     */
    public void initDatabase() {
        ConfigurationSection config = this.getConfig();

        // 构造数据源
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(config.getString("db.driver"));
        hikariConfig.setJdbcUrl(config.getString("db.url"));
        hikariConfig.setUsername(config.getString("db.username"));
        hikariConfig.setPassword(config.getString("db.password"));
        DataSource dataSource = new HikariDataSource(hikariConfig);

        // 构造各类配置
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        // 注册 Mapper
        try {
            for (Class<?> clazz : Scans.scanClass(PlotPlugin.class)) {
                if (clazz.isInterface() && clazz.getSimpleName().endsWith("Mapper") && clazz.getPackage().getName().contains("mapper")) {
                    configuration.addMapper(clazz);
                }
            }
        } catch (IOException e) {
            throw Lang.impossible();
        }

        // 获得 SqlSessionFactory
        this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    /**
     * 获取一个 SqlSession，注意一定记得关闭，不然会造成资源泄漏
     * @return 获取到的 SqlSession
     */
    public SqlSession getSqlSession() {
        return this.sqlSessionFactory.openSession();
    }

    /**
     * 执行一个事务
     * @param code 事务代码，无需在代码中关闭 SqlSession
     * @param <T> 返回值类型
     * @return 事务代码的返回值
     */
    public <T> T transaction(Function<SqlSession, T> code) {
        SqlSession sqlSession = this.sqlSessionFactory.openSession(false);
        T result;
        try {
            result = code.apply(sqlSession);
            sqlSession.commit();
        } catch (Exception e) {
            sqlSession.rollback();
            throw Lang.wrapThrow(e);
        } finally {
            sqlSession.close();
        }

        return result;
    }

    /**
     * 执行一个事务
     * @param code 事务代码，无需在代码中关闭 SqlSession
     */
    public void transaction(Consumer<SqlSession> code) {
        this.transaction(sqlSession -> {
            code.accept(sqlSession);
            return null;
        });
    }
}