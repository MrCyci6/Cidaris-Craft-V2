package fr.cidaris.craft.database;

import fr.cidaris.craft.CidarisCraftPlugin;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqliteDatabase extends AbstractSqlDatabase {

    public SqliteDatabase(CidarisCraftPlugin plugin) {
        super(plugin);
        init();
    }

    @Override
    protected void connect() throws SQLException {
        File dataFolder = new File(plugin.getDataFolder(), "database.db");
        String url = "jdbc:sqlite:" + dataFolder;

        try { Class.forName("org.sqlite.JDBC"); } catch (ClassNotFoundException ignored) {}

        this.connection = DriverManager.getConnection(url);
    }
}