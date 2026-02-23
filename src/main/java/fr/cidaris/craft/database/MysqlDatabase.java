package fr.cidaris.craft.database;

import fr.cidaris.craft.CidarisCraftPlugin;
import fr.cidaris.craft.config.files.MainConfig;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.DriverManager;
import java.sql.SQLException;

public class MysqlDatabase extends AbstractSqlDatabase {

    public MysqlDatabase(CidarisCraftPlugin plugin) {
        super(plugin);
        init(); // Appelle la création de table définie dans AbstractSqlDatabase
    }

    @Override
    protected void connect() throws SQLException {
        FileConfiguration config = plugin.getConfigManager().getConfig(MainConfig.class).get();
        String host = config.getString("database.host");
        int port = config.getInt("database.port");
        String db = config.getString("database.database");
        String user = config.getString("database.username");
        String pass = config.getString("database.password");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&autoReconnect=true";
        this.connection = DriverManager.getConnection(url, user, pass);
    }
}