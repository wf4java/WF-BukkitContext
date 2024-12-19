package wf.bukkit.context.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BukkitConfiguration {

    boolean enableConfig() default false;

    boolean enableMenu() default false;

    boolean enableCommand() default false;

}
