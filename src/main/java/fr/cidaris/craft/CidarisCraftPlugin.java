package fr.cidaris.craft;

import fr.cidaris.craft.manager.BlueprintManager;
import fr.cidaris.craft.command.CommandManager;
import fr.cidaris.craft.config.ConfigManager;
import fr.cidaris.craft.config.files.GuisConfig;
import fr.cidaris.craft.config.files.MainConfig;
import fr.cidaris.craft.config.files.MessagesConfig;
import fr.cidaris.craft.database.*;
import fr.cidaris.craft.hook.HookManager;
import fr.cidaris.craft.hook.impl.HeadDatabaseHook;
import fr.cidaris.craft.hook.impl.VaultHook;
import fr.cidaris.craft.listener.BlueprintInteractListener;
import fr.cidaris.craft.listener.GuiListener;
import fr.cidaris.craft.listener.PlayerConnectionListener;
import fr.cidaris.craft.manager.*;
import fr.cidaris.craft.nms.nbt.NbtAdapter;
import fr.cidaris.craft.nms.nbt.v1_8_R3.NbtAdapter_v1_8_R3;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class CidarisCraftPlugin extends JavaPlugin {

    private NbtAdapter nbt;
    private ConfigManager configManager;
    private CraftManager craftManager;
    private PlayerDataManager playerDataManager;
    private BlueprintManager blueprintManager;
    private HookManager hookManager;
    private EconomyManager economyManager;
    private WebhookManager webhookManager;
    private DatabaseProvider databaseProvider;


    @Override
    public void onEnable() {
        saveDefaultConfig();

        nbt = new NbtAdapter_v1_8_R3();

        configManager = new ConfigManager();
        configManager.register(new MainConfig(this));
        configManager.register(new GuisConfig(this));
        configManager.register(new MessagesConfig(this));

        getCommand("ccraft").setExecutor(new CommandManager(this));

        craftManager = new CraftManager(this);
        craftManager.loadCrafts();

        String dbType = getConfigManager().getConfig(MainConfig.class).get().getString("database.type", "JSON").toUpperCase();
        try {
            switch (dbType) {
                case "MYSQL":
                case "MARIADB":
                    databaseProvider = new MysqlDatabase(this);
                    getLogger().info("Base de données MySQL/MariaDB initialisée !");
                    break;
                case "POSTGRESQL":
                    databaseProvider = new PostgresDatabase(this);
                    getLogger().info("Base de données PostgreSQL initialisée !");
                    break;
                case "SQLITE":
                    databaseProvider = new SqliteDatabase(this);
                    getLogger().info("Base de données SQLite initialisée !");
                    break;
                case "YAML":
                    databaseProvider = new YamlDatabase(this);
                    getLogger().info("Base de données YAML (fichiers locaux) initialisée !");
                    break;
                case "JSON":
                default:
                    databaseProvider = new JsonDatabase(this);
                    getLogger().info("Base de données JSON (fichiers locaux) initialisée !");
                    break;
            }
        } catch (Exception e) {
            getLogger().severe("Erreur fatale avec la base de données sélectionnée (" + dbType + "). Fallback sur JSON !");
            databaseProvider = new JsonDatabase(this);
        }

        playerDataManager = new PlayerDataManager(databaseProvider);

        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(), this);
        getServer().getPluginManager().registerEvents(new BlueprintInteractListener(this), this);

        for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
            playerDataManager.loadIntoCache(player.getUniqueId());
        }

        blueprintManager = new BlueprintManager(this);

        hookManager = new HookManager(this);
        hookManager.registerHook(new VaultHook());
        hookManager.registerHook(new HeadDatabaseHook());

        economyManager = new EconomyManager(this);

        webhookManager = new WebhookManager(this);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (playerDataManager != null) {
                playerDataManager.saveAll();
                getLogger().info("Sauvegarde automatique des profils joueurs effectuée.");
            }
        }, 20 * 60 * 5L, 20 * 60 * 5L);
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }

        if (databaseProvider instanceof AbstractSqlDatabase) {
            ((AbstractSqlDatabase) databaseProvider).close();
        }
    }

    public NbtAdapter getNbt() {
        return nbt;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CraftManager getCraftManager() {
        return craftManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public BlueprintManager getBlueprintManager() {
        return blueprintManager;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }


    public WebhookManager getWebhookManager() {
        return webhookManager;
    }
}