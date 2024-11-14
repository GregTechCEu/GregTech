package gregtech.api.recipes.logic;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.chance.boost.ChanceBoostFunction;
import gregtech.api.recipes.ingredients.match.MatchCalculation;
import gregtech.api.recipes.lookup.property.PropertySet;

import gregtech.api.util.GTUtility;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class StandardRecipeView implements RecipeView {

    protected final @NotNull Recipe recipe;
    protected final long actualVoltage;
    protected final @NotNull MatchCalculation<ItemStack> itemMatch;
    protected final @NotNull MatchCalculation<FluidStack> fluidMatch;
    protected int parallel;
    protected @Unmodifiable List<ItemStack> items;
    protected @Unmodifiable List<FluidStack> fluids;
    protected @Unmodifiable @Nullable List<ItemStack> iOut;
    protected @Unmodifiable @Nullable List<FluidStack> fOut;

    public StandardRecipeView(@NotNull Recipe recipe, @NotNull MatchCalculation<ItemStack> itemMatch,
                              @NotNull MatchCalculation<FluidStack> fluidMatch,
                              double voltageDiscount, int initialParallel) {
        this.recipe = recipe;
        this.itemMatch = itemMatch;
        this.fluidMatch = fluidMatch;
        setParallel(initialParallel);
        this.actualVoltage = (long) (voltageDiscount * recipe.getVoltage());
    }

    @Contract("_ -> this")
    public StandardRecipeView setParallel(int parallel) {
        if (parallel == this.parallel) return this;
        items = itemMatch.getConsumed(parallel);
        fluids = fluidMatch.getConsumed(parallel);
        iOut = null;
        fOut = null;
        this.parallel = parallel;
        return this;
    }

    @Override
    public int getParallel() {
        return parallel;
    }

    @Override
    public @NotNull Recipe getRecipe() {
        return recipe;
    }

    public @NotNull MatchCalculation<ItemStack> getItemMatch() {
        return itemMatch;
    }

    public @NotNull MatchCalculation<FluidStack> getFluidMatch() {
        return fluidMatch;
    }

    @Override
    public long getActualVoltage() {
        return actualVoltage;
    }

    @Override
    public @NotNull List<ItemStack> getConsumedItems() {
        return items;
    }

    @Override
    public @NotNull List<FluidStack> getConsumedFluids() {
        return fluids;
    }

    @Override
    public @NotNull List<ItemStack> rollItems(PropertySet properties, int recipeTier, int machineTier,
                                              ChanceBoostFunction boostFunction) {
        return recipe.getItemOutputProvider().computeOutputs(items, fluids, properties, recipeTier, machineTier,
                boostFunction,
                parallel);
    }

    @Override
    public @NotNull List<FluidStack> rollFluids(PropertySet properties, int recipeTier, int machineTier,
                                                ChanceBoostFunction boostFunction) {
        return recipe.getFluidOutputProvider().computeOutputs(items, fluids, properties, recipeTier, machineTier,
                boostFunction,
                parallel);
    }

    public List<ItemStack> getMaximumItems() {
        if (iOut != null) return iOut;
        return (iOut = recipe.getItemOutputProvider().getCompleteOutputs(parallel, Integer.MAX_VALUE, items, fluids));
    }

    public List<FluidStack> getMaximumFluids() {
        if (fOut != null) return fOut;
        return (fOut = recipe.getFluidOutputProvider().getCompleteOutputs(parallel, Integer.MAX_VALUE, items, fluids));
    }
}
