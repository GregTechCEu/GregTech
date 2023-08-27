package gregtech.api.recipes.chance.output.impl;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.recipes.chance.output.BoostableChanceOutput;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation for a chanced fluid output
 */
public class ChancedFluidOutput extends BoostableChanceOutput<FluidStack> {

    public ChancedFluidOutput(@NotNull FluidStack ingredient, int chance, int chanceBoost) {
        super(ingredient, chance, chanceBoost);
    }

    @Override
    public boolean addToInventory(@NotNull IItemHandler itemHandler, @NotNull IMultipleTankHandler fluidHandler, boolean simulate) {
        return fluidHandler.fill(getIngredient(), !simulate) == getIngredient().amount;
    }
}
