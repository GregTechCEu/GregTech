package gregtech.api.recipes.machines;

import com.google.common.collect.Iterators;

import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.recipes.ui.RecipeMapUIFunction;
import gregtech.api.util.GTUtility;
import gregtech.api.util.SingletonLazyIterator;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

@ApiStatus.Internal
public class RecipeMapFurnace extends RecipeMap<SimpleRecipeBuilder> {

    public static final int RECIPE_EUT = 4;
    public static final int RECIPE_DURATION = 128;

    public RecipeMapFurnace(@NotNull String unlocalizedName, @NotNull SimpleRecipeBuilder defaultRecipeBuilder,
                            @NotNull RecipeMapUIFunction recipeMapUI) {
        super(unlocalizedName, defaultRecipeBuilder, recipeMapUI, 1, 1, 0, 0);
        setSound(GTSoundEvents.FURNACE);
    }

    @Override
    public @NotNull Iterator<@NotNull Recipe> findRecipe(long voltage, @NotNull List<ItemStack> inputs, @NotNull List<FluidStack> fluidInputs, boolean exactVoltage) {
        var iter = super.findRecipe(voltage, inputs, fluidInputs, exactVoltage);
        if (inputs.isEmpty()) {
            return iter;
        }

        var additional = new SingletonLazyIterator<>(() -> {
            for (ItemStack input : inputs) {
                ItemStack output = ModHandler.getSmeltingOutput(input);

                if (!output.isEmpty()) {
                    return this.recipeBuilder()
                            .inputs(GTUtility.copy(1, input))
                            .outputs(output)
                            .duration(RECIPE_DURATION).EUt(RECIPE_EUT)
                            .build().getResult();
                }
            }
            return null;
        });

        return Iterators.concat(additional, iter);
    }
}
