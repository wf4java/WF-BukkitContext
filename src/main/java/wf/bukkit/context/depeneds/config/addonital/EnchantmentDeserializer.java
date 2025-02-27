package wf.bukkit.context.depeneds.config.addonital;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import ru.cwcode.tkach.config.relocate.com.fasterxml.jackson.core.JsonParser;
import ru.cwcode.tkach.config.relocate.com.fasterxml.jackson.databind.DeserializationContext;
import ru.cwcode.tkach.config.relocate.com.fasterxml.jackson.databind.JsonDeserializer;
import ru.cwcode.tkach.config.relocate.com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class EnchantmentDeserializer extends JsonDeserializer<Enchantment> {
    @Override
    public Enchantment deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode tree = ctxt.readTree(p);
        if (tree.isTextual()) {
            return Enchantment.getByName(ctxt.readTreeAsValue(tree, String.class));
        }

        if (tree.isObject()) {
            return Enchantment.getByKey(ctxt.readTreeAsValue(tree, NamespacedKey.class));
        }
        throw new IOException("Unknown type for field type" + tree.getNodeType().name());
    }
}
