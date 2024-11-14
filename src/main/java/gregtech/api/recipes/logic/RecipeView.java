package gregtech.api.recipes.logic;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.lookup.property.PropertySet;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface RecipeView {

    int getParallel();

    /**
     * Use sparingly. If the recipe view has an available method for what you need, use that whenever possible.
     */
    @NotNull
    Recipe getRecipe();

    default int getActualDuration() {
        return getRecipe().getDuration();
    }

    default long getActualVoltage() {
        return getRecipe().getVoltage();
    }

    default long getActualAmperage() {
        return getRecipe().getAmperage() * getParallel();
    }

    default long getActualEUt() {
        return getActualAmperage() * getActualVoltage();
    }

    @NotNull
    List<ItemStack> getConsumedItems(int rollBoost);

    long @NotNull [] getItemArrayConsumption(int rollBoost);

    @NotNull
    List<FluidStack> getConsumedFluids(int rollBoost);

    long @NotNull [] getFluidArrayConsumption(int rollBoost);

    @NotNull
    List<ItemStack> rollItems(PropertySet properties, int recipeTier, int machineTier);

    @NotNull
    List<FluidStack> rollFluids(PropertySet properties, int recipeTier, int machineTier);
}
