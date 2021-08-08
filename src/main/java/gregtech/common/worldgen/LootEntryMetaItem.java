package gregtech.common.worldgen;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;

public class LootEntryMetaItem extends AbstractItemLootEntry {

    private final MetaValueItem metaValueItem;

    protected LootEntryMetaItem(MetaValueItem metaValueItem, int weightIn, int qualityIn, LootFunction[] functionsIn, LootCondition[] conditionsIn, String entryName) {
        super(weightIn, qualityIn, functionsIn, conditionsIn, entryName);
        this.metaValueItem = metaValueItem;
    }

    @Override
    protected ItemStack createItemStack() {
        return metaValueItem == null ? ItemStack.EMPTY : metaValueItem.getStackForm();
    }

    public static MetaValueItem getMetaItem(String name) {
        return MetaItem.getMetaItems().stream()
                .flatMap(item -> item.getAllItems().stream())
                .map(item -> (MetaValueItem) item)
                .filter(item -> item.unlocalizedName.equals(name))
                .findFirst().orElse(null);
    }

    public static LootEntryMetaItem deserialize(JsonObject object, JsonDeserializationContext context, int weight, int quality, LootCondition[] conditions) {
        String entryName = net.minecraftforge.common.ForgeHooks.readLootEntryName(object, "item");
        LootFunction[] lootFunctions;
        if (object.has("functions")) {
            lootFunctions = JsonUtils.deserializeClass(object, "functions", context, LootFunction[].class);
        } else {
            lootFunctions = new LootFunction[0];
        }
        String metaItemName = JsonUtils.getString(object, "name");
        MetaValueItem metaValueItem = getMetaItem(metaItemName);
        if (metaValueItem == null) {
            throw new IllegalArgumentException("Unknown meta item: '" + metaItemName + "'");
        }
        return new LootEntryMetaItem(metaValueItem, weight, quality, lootFunctions, conditions, entryName);
    }
}
