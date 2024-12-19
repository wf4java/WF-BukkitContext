package wf.bukkit.context.utils;


import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtils {







    public static List<Class<?>> scan(File jarFile, Class<?> clazz) {
        return scan(jarFile, clazz.getPackage().getName());
    }



    public static List<Class<?>> scan(File jarFile, String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        try {
            JarFile file = new JarFile(jarFile);
            for (Enumeration<JarEntry> entry = file.entries(); entry.hasMoreElements();) {
                JarEntry jarEntry = entry.nextElement();
                String name = jarEntry.getName().replace("/", ".");
                if(name.startsWith(packageName) && name.endsWith(".class"))
                    classes.add(Class.forName(name.substring(0, name.length() - 6)));
            }
            file.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return classes;
    }
    

    


    








}
