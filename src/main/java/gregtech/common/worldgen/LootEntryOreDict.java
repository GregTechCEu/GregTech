package gregtech.common.worldgen;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import gregtech.api.unification.OreDictUnifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;

public class LootEntryOreDict extends AbstractItemLootEntry {

    private final String oreDictName;

    protected LootEntryOreDict(String oreDictName, int weightIn, int qualityIn, LootFunction[] functionsIn, LootCondition[] conditionsIn, String entryName) {
        super(weightIn, qualityIn, functionsIn, conditionsIn, entryName);
        this.oreDictName = oreDictName;
    }

    @Override
    protected ItemStack createItemStack() {
        return OreDictUnifier.get(oreDictName);
    }

    public static LootEntryOreDict deserialize(JsonObject object, JsonDeserializationContext context, int weight, int quality, LootCondition[] conditions) {

        String entryName = net.minecraftforge.common.ForgeHooks.readLootEntryName(object, "item");
        LootFunction[] lootFunctions;
        if (object.has("functions")) {
            lootFunctions = JsonUtils.deserializeClass(object, "functions", context, LootFunction[].class);
        } else {
            lootFunctions = new LootFunction[0];
        }
        String oreDictName = JsonUtils.getString(object, "name");
        return new LootEntryOreDict(oreDictName, weight, quality, lootFunctions, conditions, entryName);
    }
}
