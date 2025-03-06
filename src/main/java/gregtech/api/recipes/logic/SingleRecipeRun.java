package gregtech.api.recipes.logic;

import gregtech.api.recipes.lookup.property.PropertySet;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SingleRecipeRun implements RecipeRun {

    private final RecipeView view;
    private final List<ItemStack> itemsOut;
    private final List<FluidStack> fluidsOut;
    private final List<ItemStack> itemsIn;
    private final List<FluidStack> fluidsIn;

    private final int parallel;
    private final int overclocks;

    private final double duration;
    private final long requiredVoltage;
    private final long requiredAmperage;
    private final boolean generating;

    private final long @Nullable [] itemArray;
    private final long @Nullable [] fluidArray;

    public SingleRecipeRun(RecipeView view, int recipeTier, int machineTier,
                           PropertySet properties, double voltageMultiplier, double duration) {
        this.view = view;
        overclocks = machineTier - recipeTier;
        assert overclocks >= 0;
        this.parallel = view.getParallel();
        this.duration = duration;
        itemsIn = view.getConsumedItems(overclocks);
        fluidsIn = view.getConsumedFluids(overclocks);
        itemsOut = view.rollItems(properties, recipeTier, machineTier);
        fluidsOut = view.rollFluids(properties, recipeTier, machineTier);
        requiredVoltage = (long) (view.getActualVoltage() * voltageMultiplier);
        requiredAmperage = view.getActualAmperage();
        generating = view.getRecipe().isGenerating();
        itemArray = view.getItemArrayConsumption(overclocks);
        fluidArray = view.getFluidArrayConsumption(overclocks);
    }

    @Override
    public @NotNull RecipeView getRecipeView() {
        return view;
    }

    @Override
    public @NotNull List<ItemStack> getItemsOut() {
        return itemsOut;
    }

    @Override
    public @NotNull List<FluidStack> getFluidsOut() {
        return fluidsOut;
    }

    @Override
    public @NotNull List<ItemStack> getItemsConsumed() {
        return itemsIn;
    }

    @Override
    public long @Nullable [] getItemArrayConsumption() {
        return itemArray;
    }

    @Override
    public @NotNull List<FluidStack> getFluidsConsumed() {
        return fluidsIn;
    }

    @Override
    public long @Nullable [] getFluidArrayConsumption() {
        return fluidArray;
    }

    @Override
    public int getParallel() {
        return parallel;
    }

    @Override
    public int getOverclocks() {
        return overclocks;
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public long getRequiredVoltage() {
        return requiredVoltage;
    }

    @Override
    public long getRequiredAmperage() {
        return requiredAmperage;
    }

    @Override
    public boolean isGenerating() {
        return generating;
    }
}
