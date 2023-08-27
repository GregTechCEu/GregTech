package gregtech.api.recipes.chance.output.impl;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.recipes.chance.output.BoostableChanceOutput;
import gregtech.api.util.GTTransferUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation for a chanced item output
 */
public class ChancedItemOutput extends BoostableChanceOutput<ItemStack> {

    public ChancedItemOutput(@NotNull ItemStack ingredient, int chance, int chanceBoost) {
        super(ingredient, chance, chanceBoost);
    }

    @Override
    public boolean addToInventory(@NotNull IItemHandler itemHandler, @NotNull IMultipleTankHandler fluidHandler, boolean simulate) {
        return GTTransferUtils.insertItem(itemHandler, getIngredient(), simulate).isEmpty();
    }
}
