package fr.cidaris.craft;

import fr.cidaris.craft.blueprint.BlueprintManager;
import fr.cidaris.craft.command.CommandManager;
import fr.cidaris.craft.database.DatabaseProvider;
import fr.cidaris.craft.database.JsonDatabase;
import fr.cidaris.craft.database.YamlDatabase;
import fr.cidaris.craft.hook.HookManager;
import fr.cidaris.craft.hook.impl.HeadDatabaseHook;
import fr.cidaris.craft.hook.impl.VaultHook;
import fr.cidaris.craft.listener.BlueprintInteractListener;
import fr.cidaris.craft.listener.GuiListener;
import fr.cidaris.craft.listener.PlayerConnectionListener;
import fr.cidaris.craft.manager.CraftManager;
import fr.cidaris.craft.manager.EconomyManager;
import fr.cidaris.craft.manager.PlayerDataManager;
import fr.cidaris.craft.nms.nbt.NbtAdapter;
import fr.cidaris.craft.nms.nbt.v1_8_R3.NbtAdapter_v1_8_R3;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class CidarisCraftPlugin extends JavaPlugin {

    private NbtAdapter nbt;
    private CraftManager craftManager;
    private PlayerDataManager playerDataManager;
    private BlueprintManager blueprintManager;
    private HookManager hookManager;
    private EconomyManager economyManager;


    @Override
    public void onEnable() {
        saveDefaultConfig();

        nbt = new NbtAdapter_v1_8_R3();
        getCommand("ccraft").setExecutor(new CommandManager(this));

        craftManager = new CraftManager(this);
        craftManager.loadCrafts();

        playerDataManager = new PlayerDataManager(new JsonDatabase(this));

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

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (playerDataManager != null) {
                playerDataManager.saveAll();
                getLogger().info("Sauvegarde automatique des profils joueurs effectuée.");
            }
        }, 20 * 60 * 5L, 20 * 60 * 5L);
    }

    @Override
    public void onDisable() {
        if (this.playerDataManager != null) {
            this.playerDataManager.saveAll();
        }
    }

    public NbtAdapter getNbt() {
        return nbt;
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
}