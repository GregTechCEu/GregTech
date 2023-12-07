package gregtech.common.worldgen;

import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunction;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Random;

public abstract class AbstractItemLootEntry extends LootEntry {

    private final LootFunction[] functions;

    protected AbstractItemLootEntry(int weightIn, int qualityIn, LootFunction[] functionsIn,
                                    LootCondition[] conditionsIn, String entryName) {
        super(weightIn, qualityIn, conditionsIn, entryName);
        this.functions = functionsIn;
    }

    protected abstract ItemStack createItemStack();

    @Override
    public void addLoot(@NotNull Collection<ItemStack> stacks, @NotNull Random rand, @NotNull LootContext context) {
        ItemStack itemStack = createItemStack();
        for (LootFunction lootfunction : this.functions) {
            if (LootConditionManager.testAllConditions(lootfunction.getConditions(), rand, context)) {
                itemStack = lootfunction.apply(itemStack, rand, context);
            }
        }
        if (!itemStack.isEmpty()) {
            if (itemStack.getCount() < itemStack.getItem().getItemStackLimit(itemStack)) {
                stacks.add(itemStack);
            } else {
                int i = itemStack.getCount();

                while (i > 0) {
                    ItemStack itemstack1 = itemStack.copy();
                    itemstack1.setCount(Math.min(itemStack.getMaxStackSize(), i));
                    i -= itemstack1.getCount();
                    stacks.add(itemstack1);
                }
            }
        }
    }

    @Override
    protected final void serialize(@NotNull JsonObject json, @NotNull JsonSerializationContext context) {
        throw new UnsupportedOperationException("Unsupported by custom loot entries");
    }
}
