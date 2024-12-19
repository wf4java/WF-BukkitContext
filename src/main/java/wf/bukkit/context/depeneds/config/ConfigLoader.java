package wf.bukkit.context.depeneds.config;

import org.bukkit.plugin.java.JavaPlugin;
import wf.bukkit.context.BukkitContext;
import wf.bukkit.context.depeneds.config.annotation.Config;
import wf.bukkit.context.depeneds.config.model.AbstractConfig;
import wf.bukkit.context.depeneds.config.model.ConfigManager;

import java.util.List;

public class ConfigLoader {


    public static List<Class<?>> getBeans() {
        return List.of(
                ConfigManager.class
        );
    }

    public static void configure(BukkitContext bukkitContext, JavaPlugin plugin) {
        ConfigLoader.getBeans().forEach(bukkitContext::addPreloadedClass);
    }


    public static Object instanceOfConfig(BukkitContext context, Config config, Class<?> clazz) {
        ConfigManager configManager = context.getBean(ConfigManager.class);

        if(!AbstractConfig.class.isAssignableFrom(clazz))
            throw new RuntimeException("Class: \"" + clazz.getName() + "\" not extended from \"AbstractConfig\"");

        //noinspection unchecked
        return configManager.getYml().load(config.configName(), (Class<? extends AbstractConfig>) clazz);
    }

}
