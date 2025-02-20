package wf.bukkit.context;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import wf.bukkit.context.annotations.Autowired;
import wf.bukkit.context.annotations.BukkitConfiguration;
import wf.bukkit.context.annotations.Component;
import wf.bukkit.context.annotations.EventListener;
import wf.bukkit.context.annotations.Init;
import wf.bukkit.context.depeneds.config.ConfigLoader;
import wf.bukkit.context.depeneds.config.annotation.Config;
import wf.bukkit.context.depeneds.menu.MenuLoader;
import wf.bukkit.context.utils.ClassUtils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public class BukkitContext {

    private final List<Class<?>> classes = new ArrayList<>();
    private final List<Class<?>> sortedClasses = new ArrayList<>();
    private final Map<Class<?>, Object> beans = new HashMap<>();
    private final Map<Class<?>, List<Class<?>>> dependencyGraph = new HashMap<>();

    private final BukkitConfiguration bukkitConfiguration;

    public static BukkitContext run(JavaPlugin plugin) {
        return new BukkitContext(plugin);
    }

    private BukkitContext(JavaPlugin plugin) {
        this.bukkitConfiguration = plugin.getClass().getAnnotation(BukkitConfiguration.class);

        beans.put(Plugin.class, plugin);
        beans.put(JavaPlugin.class, plugin);
        beans.put(plugin.getClass(), plugin);
        beans.put(BukkitContext.class, this);

        configure(plugin);

        classes.addAll(loadClasses(getFileFromPlugin(plugin), plugin.getClass()));
        buildDependencyGraph();
        sortedClasses.addAll(topologicalSort());
        createBeans();
        autowiredAll(plugin, plugin.getClass());
        callInit();
    }


    private void configure(JavaPlugin plugin) {
        if (bukkitConfiguration == null) return;

        if (bukkitConfiguration.enableConfig())
            ConfigLoader.configure(this, plugin);

        if (bukkitConfiguration.enableMenu())
            MenuLoader.configure(this, plugin);
    }


    private void buildDependencyGraph() {
        for (Class<?> clazz : classes) {
            if (clazz.isInterface() || clazz.isEnum() || clazz.isAnnotation())
                continue;

            Component component = getComponentAnnotation(clazz);
            if (component == null)
                continue;

            dependencyGraph.putIfAbsent(clazz, new ArrayList<>());

            for (Class<?> dependency : clazz.getDeclaredConstructors()[0].getParameterTypes())
                dependencyGraph.get(clazz).add(dependency);
        }
    }

    private List<Class<?>> topologicalSort() {
        List<Class<?>> sorted = new ArrayList<>();
        Set<Class<?>> visited = new HashSet<>();
        Set<Class<?>> visiting = new HashSet<>();

        for (Class<?> clazz : classes)
            if (!visited.contains(clazz))
                visit(clazz, visited, visiting, sorted);

        return sorted;
    }


    private void createBeans() {
        for (Class<?> clazz : sortedClasses) {
            if (beans.containsKey(clazz))
                continue;

            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            if (constructors.length == 0)
                throw new RuntimeException("Not found constructor for class: " + clazz.getName());

            Constructor<?> constructor = constructors[0];
            constructor.setAccessible(true);

            Class<?>[] parameterTypes = constructor.getParameterTypes();
            Object[] parameters = new Object[parameterTypes.length];

            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> paramType = parameterTypes[i];
                Object dependency = beans.get(paramType);

                if (dependency == null)
                    throw new RuntimeException("Dependency not found for class: " + paramType.getName());

                parameters[i] = dependency;
            }


            beans.put(clazz, newInstanceOfObject(clazz, constructor, parameters));
        }
    }

    private void autowiredAll(JavaPlugin plugin, Class<?> clazz) {
        for (Class<?> c : getAllClasses(getFileFromPlugin(plugin), clazz)) {
            for (Field field : c.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) continue;

                Autowired autowired = field.getAnnotation(Autowired.class);
                if (autowired == null) continue;

                Object bean = getBean(field.getType());
                if (bean == null)
                    throw new RuntimeException("Bean of class: " + field.getType().getSimpleName() + " for autowired to: " + c.getSimpleName() + " not founded!");

                field.setAccessible(true);
                try {
                    field.set(null, bean);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void callInit() {
        for (Class<?> clazz : sortedClasses) {
            for (Method method : clazz.getDeclaredMethods()) {
                Init init = method.getAnnotation(Init.class);
                if (init == null) continue;

                method.setAccessible(true);
                try {
                    method.invoke(getBean(clazz));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private File getFileFromPlugin(JavaPlugin javaPlugin) {
        try {
            Field field = JavaPlugin.class.getDeclaredField("file");
            field.setAccessible(true);

            return (File) field.get(javaPlugin);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public <T> T getBean(Class<T> clazz) {
        return clazz.cast(beans.get(clazz));
    }


    public void addBean(Object object) {
        beans.put(object.getClass(), object);
    }

    public void addPreloadedClass(Class<?> clazz) {
        classes.add(clazz);
    }


    private Object newInstanceOfObject(Class<?> clazz, Constructor<?> constructor, Object[] parameters) {
        if (bukkitConfiguration != null) {
            if (bukkitConfiguration.enableConfig()) {
                Config config = clazz.getAnnotation(Config.class);
                if (config != null)
                    return ConfigLoader.instanceOfConfig(this, config, clazz);
            }

        }


        Object instance;
        try {
            instance = constructor.newInstance(parameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        if (clazz.getAnnotation(EventListener.class) != null) {
            if (instance instanceof Listener listener)
                Bukkit.getPluginManager().registerEvents(listener, getBean(JavaPlugin.class));

            else
                Bukkit.getPluginManager().registerEvents(
                        (Listener) Proxy.newProxyInstance(
                                instance.getClass().getClassLoader(),
                                new Class<?>[]{Listener.class},
                                (proxy, method, args) -> method.invoke(instance, args)),
                        getBean(JavaPlugin.class));
        }


        return instance;
    }


    private static List<Class<?>> loadClasses(File jarFile, Class<?> clazz) {
        return ClassUtils.scan(jarFile, clazz).stream()
                .filter((c) -> {
                    if (c.isInterface() || c.isEnum() || c.isAnnotation())
                        return false;

                    return getComponentAnnotation(c) != null;
                })
                .toList();
    }


    private static List<Class<?>> getAllClasses(File jarFile, Class<?> clazz) {
        return ClassUtils.scan(jarFile, clazz).stream()
                .filter((c) -> !c.isInterface() && !c.isEnum() && !c.isAnnotation())
                .toList();
    }


    private static Component getComponentAnnotation(Class<?> clazz) {
        Component component = clazz.getAnnotation(Component.class);
        if (component != null)
            return component;

        for (Annotation annotation : clazz.getAnnotations()) {
            component = annotation.annotationType().getAnnotation(Component.class);
            if (component != null)
                return component;
        }

        return null;
    }


    private void visit(Class<?> clazz, Set<Class<?>> visited, Set<Class<?>> visiting, List<Class<?>> sorted) {
        if (visiting.contains(clazz))
            throw new RuntimeException("Cyclic dependency detected for class: " + clazz.getName());

        if (visited.contains(clazz))
            return;

        visiting.add(clazz);
        for (Class<?> dependency : dependencyGraph.getOrDefault(clazz, Collections.emptyList()))
            visit(dependency, visited, visiting, sorted);

        visiting.remove(clazz);
        visited.add(clazz);
        sorted.add(clazz);
    }


}
