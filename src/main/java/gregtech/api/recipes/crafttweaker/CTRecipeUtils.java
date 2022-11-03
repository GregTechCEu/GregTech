package gregtech.api.recipes.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTLog;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ZenClass("mods.gregtech.recipe.helpers")
@ZenRegister
@SuppressWarnings("unused")
public class CTRecipeUtils {

    // TODO YEET

    @ZenMethod("removeRecipeByOutput")
    public static void removeRecipeByOutput(RecipeMap<?> recipeMap, IItemStack[] outputs, ILiquidStack[] fluidOutputs, boolean useAmounts) {
        List<Recipe> recipesToRemove = new ArrayList<>();
        List<ItemStack> mcItemOutputs = outputs == null ? Collections.emptyList() :
                Arrays.stream(outputs)
                        .map(CraftTweakerMC::getItemStack)
                        .collect(Collectors.toList());

        List<FluidStack> mcFluidOutputs = fluidOutputs == null ? Collections.emptyList() :
                Arrays.stream(fluidOutputs)
                        .map(CraftTweakerMC::getLiquidStack)
                        .collect(Collectors.toList());

        for (Object recipe : recipeMap.getRecipeList()) {
            if (recipe instanceof Recipe) {
                if (!mcItemOutputs.isEmpty()) {
                    for (ItemStack output : ((Recipe) recipe).getOutputs()) {
                        for (ItemStack itemStack : mcItemOutputs) {
                            if (output.isItemEqual(itemStack) && output.getMetadata() == itemStack.getMetadata()) {
                                if (useAmounts) {
                                    if (output.getCount() == itemStack.getCount()) {
                                        recipesToRemove.add((Recipe) recipe);
                                        GTLog.logger.info(output.getDisplayName());
                                    }
                                } else {
                                    recipesToRemove.add((Recipe) recipe);
                                    GTLog.logger.info(output.getDisplayName());
                                }
                            }
                        }
                    }
                }
                if (!mcFluidOutputs.isEmpty()) {
                    for (FluidStack fluidOutput : ((Recipe) recipe).getFluidOutputs()) {
                        for (FluidStack fluidStack : mcFluidOutputs) {
                            if (fluidOutput.isFluidEqual(fluidStack)) {
                                if (useAmounts) {
                                    if (fluidOutput.amount == fluidStack.amount) {
                                        recipesToRemove.add((Recipe) recipe);
                                    }
                                } else {
                                    recipesToRemove.add((Recipe) recipe);
                                }
                            }
                        }
                    }
                }
            }
        }
        for (Recipe recipe : recipesToRemove) {
            recipeMap.removeRecipe(recipe);
        }
    }
}
