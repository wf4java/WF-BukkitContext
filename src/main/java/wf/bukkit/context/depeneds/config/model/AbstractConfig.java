package wf.bukkit.context.depeneds.config.model;



import ru.cwcode.tkach.config.jackson.yaml.YmlConfig;

public abstract class AbstractConfig extends YmlConfig {

    public String[] header() {
        return new String[]{"Configuration file"};
    }

}
