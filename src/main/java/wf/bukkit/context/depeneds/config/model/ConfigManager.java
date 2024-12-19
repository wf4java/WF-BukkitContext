package wf.bukkit.context.depeneds.config.model;


import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.cwcode.tkach.config.jackson.yaml.YmlConfig;
import ru.cwcode.tkach.config.jackson.yaml.YmlConfigManager;
import ru.cwcode.tkach.config.paper.PaperPluginConfigPlatform;

import java.time.Duration;

public class ConfigManager {
    private final YmlConfigManager yml;


    private ConfigManager(JavaPlugin plugin) {
        this.yml = new YmlConfigManager(new PaperPluginConfigPlatform(plugin));
        this.yml.scheduleAutosave(Duration.ofMinutes(5), configPersistOptions -> { });
    }


    public YmlConfigManager getYml() {
        return this.yml;
    }

    public <Config extends YmlConfig> Config load(@NotNull final String path, @NotNull final Class<Config> config) {
        return this.getYml().load(path, config);
    }

}
