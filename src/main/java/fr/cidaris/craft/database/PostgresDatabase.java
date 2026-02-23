package fr.cidaris.craft.database;

import fr.cidaris.craft.CidarisCraftPlugin;
import fr.cidaris.craft.config.files.MainConfig;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgresDatabase extends AbstractSqlDatabase {

    public PostgresDatabase(CidarisCraftPlugin plugin) {
        super(plugin);
        init();
    }

    @Override
    protected void connect() throws SQLException {
        FileConfiguration config = plugin.getConfigManager().getConfig(MainConfig.class).get();
        String host = config.getString("database.host");
        int port = config.getInt("database.port");
        String db = config.getString("database.database");
        String user = config.getString("database.username");
        String pass = config.getString("database.password");

        String url = "jdbc:postgresql://" + host + ":" + port + "/" + db;

        try { Class.forName("org.postgresql.Driver"); } catch (ClassNotFoundException ignored) {}

        this.connection = DriverManager.getConnection(url, user, pass);
    }
}