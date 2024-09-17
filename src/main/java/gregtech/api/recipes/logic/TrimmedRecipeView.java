package gregtech.api.recipes.logic;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.chance.boost.ChanceBoostFunction;
import gregtech.api.recipes.ingredients.match.MatchCalculation;

import gregtech.api.recipes.lookup.property.PropertySet;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

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
        List<ItemStack> add = recipe.getMaximumItemOutputs(items, fluids);
        int count = Math.min(add.size(), maxItems);
        List<ItemStack> list = new ObjectArrayList<>(count * parallel);
        for (int i = 0; i < parallel; i++) {
            for (int j = 0; j < count; j++) {
                list.add(add.get(j));
            }
        }
        return (iOut = list);
    }

    @Override
    public List<FluidStack> getMaximumFluids() {
        if (fOut != null) return fOut;
        List<FluidStack> add = recipe.getMaximumFluidOutputs(items, fluids);
        int count = Math.min(add.size(), maxFluids);
        List<FluidStack> list = new ObjectArrayList<>(count * parallel);
        for (int i = 0; i < parallel; i++) {
            for (int j = 0; j < count; j++) {
                list.add(add.get(j));
            }
        }
        return (fOut = list);
    }
}
