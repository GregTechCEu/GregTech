package gregtech.api.recipes.logic;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.List;

public interface RecipeRun {

    /**
     * Use sparingly. If the recipe run has an available method for what you need, use that whenever possible.
     */
    @NotNull
    RecipeView getRecipeView();

    @NotNull
    List<ItemStack> getItemsOut();

    @NotNull
    List<FluidStack> getFluidsOut();

    @NotNull
    List<ItemStack> getItemsConsumed();

    long @Nullable [] getItemArrayConsumption();

    @NotNull
    List<FluidStack> getFluidsConsumed();

    long @Nullable [] getFluidArrayConsumption();

    @Range(from = 1, to = Integer.MAX_VALUE)
    int getParallel();

    @Range(from = 0, to = Integer.MAX_VALUE)
    int getOverclocks();

    double getDuration();

    @Range(from = 0, to = Long.MAX_VALUE)
    long getRequiredVoltage();

    @Range(from = 0, to = Long.MAX_VALUE)
    long getRequiredAmperage();

    boolean isGenerating();
}
