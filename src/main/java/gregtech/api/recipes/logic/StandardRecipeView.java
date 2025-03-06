package gregtech.api.recipes.logic;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.ingredients.match.MatchCalculation;
import gregtech.api.recipes.lookup.property.PropertySet;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

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
    protected @Unmodifiable List<ItemStack> matchedItems;
    protected @Unmodifiable List<FluidStack> matchedFluids;
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
        matchedItems = null;
        matchedFluids = null;
        items = null;
        fluids = null;
        iOut = null;
        fOut = null;
        this.parallel = parallel;
        return this;
    }

    protected void computeMatches() {
        if (matchedItems == null || matchedFluids == null) {
            matchedItems = itemMatch.getMatched(parallel);
            matchedFluids = fluidMatch.getMatched(parallel);
        }
    }

    protected void computeConsumptions(int rollBoost) {
        if (items == null || fluids == null) {
            items = itemMatch.getConsumed(parallel, rollBoost);
            fluids = fluidMatch.getConsumed(parallel, rollBoost);
        }
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
    public @NotNull List<ItemStack> getConsumedItems(int rollBoost) {
        computeConsumptions(rollBoost);
        return items;
    }

    @Override
    public long @NotNull [] getItemArrayConsumption(int rollBoost) {
        long[] arr = itemMatch.getConsumeResultsForScaleAndBoost(parallel, rollBoost);
        return arr == null ? new long[0] : arr;
    }

    @Override
    public @NotNull List<FluidStack> getConsumedFluids(int rollBoost) {
        computeConsumptions(rollBoost);
        return fluids;
    }

    @Override
    public long @NotNull [] getFluidArrayConsumption(int rollBoost) {
        long[] arr = fluidMatch.getConsumeResultsForScaleAndBoost(parallel, rollBoost);
        return arr == null ? new long[0] : arr;
    }

    @Override
    public @NotNull List<ItemStack> rollItems(PropertySet properties, int recipeTier, int machineTier) {
        computeConsumptions(machineTier - recipeTier);
        return recipe.getItemOutputProvider().computeOutputs(items, fluids, properties, recipeTier, machineTier,
                parallel);
    }

    @Override
    public @NotNull List<FluidStack> rollFluids(PropertySet properties, int recipeTier, int machineTier) {
        computeConsumptions(machineTier - recipeTier);
        return recipe.getFluidOutputProvider().computeOutputs(items, fluids, properties, recipeTier, machineTier,
                parallel);
    }

    public List<ItemStack> getMaximumItems() {
        if (iOut != null) return iOut;
        computeMatches();
        return (iOut = recipe.getItemOutputProvider().getCompleteOutputs(parallel, Integer.MAX_VALUE));
    }

    public List<FluidStack> getMaximumFluids() {
        if (fOut != null) return fOut;
        computeMatches();
        return (fOut = recipe.getFluidOutputProvider().getCompleteOutputs(parallel, Integer.MAX_VALUE));
    }
}
