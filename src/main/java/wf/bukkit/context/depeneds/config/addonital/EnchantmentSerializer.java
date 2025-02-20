package wf.bukkit.context.depeneds.config.addonital;

import org.bukkit.enchantments.Enchantment;
import ru.cwcode.tkach.config.relocate.com.fasterxml.jackson.core.JsonGenerator;
import ru.cwcode.tkach.config.relocate.com.fasterxml.jackson.databind.JsonSerializer;
import ru.cwcode.tkach.config.relocate.com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class EnchantmentSerializer extends JsonSerializer<Enchantment> {

    @Override
    public void serialize(Enchantment value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeObject(value.getKey());
    }
}
