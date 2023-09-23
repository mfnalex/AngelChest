package de.jeff_media.angelchest.handlers;

import com.jeff_media.jefflib.ConfigUtils;
import com.jeff_media.jefflib.data.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class MySqlManager {
    private final HikariConfig config = new HikariConfig();
    private HikariDataSource ds;

    public MySqlManager(Config fileConfig) {
        Config fileConf = ConfigUtils.getConfig("database.yml");
        config.setJdbcUrl("jdbc:mysql://" + fileConf.getString("host") + ":" + fileConf.getString("port") + "/" + fileConf.getString("database"));
        config.setUsername(fileConf.getString("username"));
        config.setPassword(fileConf.getString("password"));
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
