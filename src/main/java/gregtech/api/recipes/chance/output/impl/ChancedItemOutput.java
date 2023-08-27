package gregtech.api.recipes.chance.output.impl;

import gregtech.api.recipes.chance.output.BoostableChanceOutput;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation for a chanced item output
 */
public class ChancedItemOutput extends BoostableChanceOutput<ItemStack> {

    public ChancedItemOutput(@NotNull ItemStack ingredient, int chance, int chanceBoost) {
        super(ingredient, chance, chanceBoost);
    }

    @Override
    public @NotNull ChancedItemOutput copy() {
        return new ChancedItemOutput(getIngredient().copy(), getChance(), getChanceBoost());
    }
}
