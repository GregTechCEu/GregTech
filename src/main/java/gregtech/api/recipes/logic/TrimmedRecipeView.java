package gregtech.api.recipes.logic;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.chance.boost.ChanceBoostFunction;
import gregtech.api.recipes.ingredients.match.MatchCalculation;
import gregtech.api.recipes.lookup.property.PropertySet;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TrimmedRecipeView extends StandardRecipeView {

    protected final int maxItems;
    protected final int maxFluids;

    public TrimmedRecipeView(@NotNull Recipe recipe, @NotNull MatchCalculation<ItemStack> itemMatch,
                             @NotNull MatchCalculation<FluidStack> fluidMatch, double voltageDiscount,
                             int initialParallel, int maxItems, int maxFluids) {
        super(recipe, itemMatch, fluidMatch, voltageDiscount, initialParallel);
        this.maxItems = maxItems;
        this.maxFluids = maxFluids;
    }

    @Override
    public @NotNull List<ItemStack> rollItems(PropertySet properties, int recipeTier, int machineTier,
                                              ChanceBoostFunction boostFunction) {
        return recipe.getItemOutputProvider().computeOutputs(items, fluids, properties, recipeTier, machineTier,
                boostFunction, parallel, maxItems);
    }

    @Override
    public @NotNull List<FluidStack> rollFluids(PropertySet properties, int recipeTier, int machineTier,
                                                ChanceBoostFunction boostFunction) {
        return recipe.getFluidOutputProvider().computeOutputs(items, fluids, properties, recipeTier, machineTier,
                boostFunction, parallel, maxFluids);
    }

    @Override
    public List<ItemStack> getMaximumItems() {
        if (iOut != null) return iOut;
        return (iOut = recipe.getItemOutputProvider().getCompleteOutputs(parallel, maxItems, items, fluids));
    }

    @Override
    public List<FluidStack> getMaximumFluids() {
        if (fOut != null) return fOut;
        return (fOut = recipe.getFluidOutputProvider().getCompleteOutputs(parallel, maxFluids, items, fluids));
    }
}
