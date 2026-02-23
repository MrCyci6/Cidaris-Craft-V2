package fr.cidaris.craft.hook;

import fr.cidaris.craft.CidarisCraftPlugin;
import java.util.HashMap;
import java.util.Map;

public class HookManager {

    private final CidarisCraftPlugin plugin;
    private final Map<Class<? extends PluginHook>, PluginHook> hooks = new HashMap<>();

    public HookManager(CidarisCraftPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerHook(PluginHook hook) {
        if (hook.setup(plugin)) {
            hooks.put(hook.getClass(), hook);
            plugin.getLogger().info("✔ " + hook.getPluginName() + " accroché avec succès !");
        } else {
            plugin.getLogger().warning("✖ " + hook.getPluginName() + " introuvable ou erreur d'initialisation.");
        }
    }

    public <T extends PluginHook> T getHook(Class<T> hookClass) {
        return hookClass.cast(hooks.get(hookClass));
    }
}