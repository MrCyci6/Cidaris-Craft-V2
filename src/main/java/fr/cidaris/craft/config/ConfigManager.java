package fr.cidaris.craft.config;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final Map<Class<? extends AbstractConfig>, AbstractConfig> configs = new HashMap<>();

    public void register(AbstractConfig config) {
        config.load();
        configs.put(config.getClass(), config);
    }

    public <T extends AbstractConfig> T getConfig(Class<T> clazz) {
        return clazz.cast(configs.get(clazz));
    }

    public void reloadAll() {
        for (AbstractConfig config : configs.values()) {
            config.load();
        }
    }
}