package gregtech.api.recipes.logic;

import gregtech.api.recipes.lookup.property.PropertySet;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PrimitiveRecipeRun implements RecipeRun {

    private final RecipeView view;
    private final List<ItemStack> itemsOut;
    private final List<FluidStack> fluidsOut;
    private final List<ItemStack> itemsIn;
    private final List<FluidStack> fluidsIn;

    private final int parallel;

    private final double duration;

    private final long @Nullable [] itemArray;
    private final long @Nullable [] fluidArray;

    public PrimitiveRecipeRun(RecipeView view, PropertySet properties, double duration) {
        this.view = view;
        this.parallel = view.getParallel();
        this.duration = duration;
        itemsIn = view.getConsumedItems(0);
        fluidsIn = view.getConsumedFluids(0);
        itemsOut = view.rollItems(properties, 0, 0);
        fluidsOut = view.rollFluids(properties, 0, 0);
        itemArray = view.getItemArrayConsumption(0);
        fluidArray = view.getFluidArrayConsumption(0);
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
        return 0;
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public long getRequiredVoltage() {
        return 0;
    }

    @Override
    public long getRequiredAmperage() {
        return 0;
    }

    @Override
    public boolean isGenerating() {
        return false;
    }
}
