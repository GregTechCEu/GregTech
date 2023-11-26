package gregtech.api.recipes.machines;

import gregtech.api.recipes.Recipe;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public interface IScannerRecipeMap {

    /**
     * @return A list of all representative recipes detailed by registered {@link ICustomScannerLogic} implementations.
     */
    @NotNull
    default List<Recipe> getRepresentativeRecipes() {
        return Collections.emptyList();
    }

    interface ICustomScannerLogic {

        /**
         * @return A custom recipe to run given the current Scanner's inputs. Will be called only if a registered
         *         recipe is not found to run. Return null if no recipe should be run by your logic.
         */
        @Nullable
        Recipe createCustomRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs,
                                  boolean exactVoltage);

        /**
         * @return A list of Recipes that are never registered, but are added to JEI to demonstrate the custom logic.
         *         Not required, can return empty or null to not add any.
         */
        @Nullable
        default List<Recipe> getRepresentativeRecipes() {
            return null;
        }
    }
}
