package wf.bukkit.context.depeneds.config;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.java.JavaPlugin;
import ru.cwcode.tkach.config.jackson.yaml.YmlConfigManager;
import ru.cwcode.tkach.config.paper.PaperPluginConfigPlatform;
import ru.cwcode.tkach.config.relocate.com.fasterxml.jackson.databind.module.SimpleModule;
import wf.bukkit.context.BukkitContext;
import wf.bukkit.context.depeneds.config.addonital.EnchantmentDeserializer;
import wf.bukkit.context.depeneds.config.addonital.EnchantmentSerializer;
import wf.bukkit.context.depeneds.config.annotation.Config;
import wf.bukkit.context.depeneds.config.model.AbstractConfig;
import wf.bukkit.context.depeneds.config.model.ConfigManager;

import java.util.List;

public class ConfigLoader {





    public static void configure(BukkitContext bukkitContext, JavaPlugin plugin) {
        PaperPluginConfigPlatform paperPluginConfigPlatform = new PaperPluginConfigPlatform(plugin);

        SimpleModule enchantmentModule = new SimpleModule("Enchantment");
        enchantmentModule.addSerializer(Enchantment.class, new EnchantmentSerializer());
        enchantmentModule.addDeserializer(Enchantment.class, new EnchantmentDeserializer());
        paperPluginConfigPlatform.additionalJacksonModules().add(enchantmentModule);

        bukkitContext.addBean(new ConfigManager(new YmlConfigManager(paperPluginConfigPlatform)));
    }


    public static Object instanceOfConfig(BukkitContext context, Config config, Class<?> clazz) {
        ConfigManager configManager = context.getBean(ConfigManager.class);

        if(!AbstractConfig.class.isAssignableFrom(clazz))
            throw new RuntimeException("Class: \"" + clazz.getName() + "\" not extended from \"AbstractConfig\"");

        //noinspection unchecked
        return configManager.getYml().load(config.configName(), (Class<? extends AbstractConfig>) clazz);
    }

}
