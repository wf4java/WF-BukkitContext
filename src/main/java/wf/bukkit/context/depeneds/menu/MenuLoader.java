package wf.bukkit.context.depeneds.menu;

import org.bukkit.plugin.java.JavaPlugin;
import ru.cwcode.tkach.refreshmenu.MenuManager;
import wf.bukkit.context.BukkitContext;


import java.util.List;

public class MenuLoader {


    private static List<Class<?>> getBeans() {
        return List.of(
                MenuManager.class
        );
    }



    public static void configure(BukkitContext bukkitContext, JavaPlugin plugin) {
        getBeans().forEach(bukkitContext::addPreloadedClass);
    }



}
