package gregtech.api.recipes.recipes;

import crafttweaker.annotations.ZenRegister;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;

import java.util.HashMap;
import java.util.List;

@ZenClass("mods.gregtech.recipe.FuelRecipe")
@ZenRegister
public class FuelRecipe2 extends Recipe {

    // Maps each boost FluidStack to an Integer[] array of {boostDuration, boostFactor}
    private final HashMap<FluidStack, Integer[]> boostFluids;

    public FuelRecipe2(List<CountableIngredient> inputs, List<ItemStack> outputs, List<ChanceEntry> chancedOutputs,
                       List<FluidStack> fluidInputs, List<FluidStack> fluidOutputs, HashMap<FluidStack, Integer[]> boostFluids,
                       int duration, int EUt, boolean hidden) {
        super(inputs, outputs, chancedOutputs, fluidInputs, fluidOutputs, duration, EUt, hidden);
        this.boostFluids = boostFluids;
    }
}
