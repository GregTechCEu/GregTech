package gregtech.api.recipes.logic;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.chance.boost.ChanceBoostFunction;
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
    List<ItemStack> getConsumedItems();

    @NotNull
    List<FluidStack> getConsumedFluids();

    @NotNull
    List<ItemStack> rollItems(PropertySet properties, int recipeTier, int machineTier,
                              ChanceBoostFunction boostFunction);

    @NotNull
    List<FluidStack> rollFluids(PropertySet properties, int recipeTier, int machineTier,
                                ChanceBoostFunction boostFunction);
}
