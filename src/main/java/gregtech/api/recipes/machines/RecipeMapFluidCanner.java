package gregtech.api.recipes.machines;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RecipeMapFluidCanner extends RecipeMap<SimpleRecipeBuilder> {

    public RecipeMapFluidCanner(String unlocalizedName, int maxInputs, int maxOutputs, int maxFluidInputs,
                                int maxFluidOutputs, SimpleRecipeBuilder defaultRecipe, boolean isHidden) {
        super(unlocalizedName, maxInputs, maxOutputs, maxFluidInputs, maxFluidOutputs, defaultRecipe, isHidden);
    }

    @Override
    @Nullable
    public Recipe findRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs, boolean exactVoltage) {
        Recipe recipe = super.findRecipe(voltage, inputs, fluidInputs, exactVoltage);
        if (recipe != null) return recipe;

        for (ItemStack input : inputs) {
            if (input != null && input != ItemStack.EMPTY &&
                    input.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {

                // Make a copy to use for creating recipes
                ItemStack inputStack = input.copy();
                inputStack.setCount(1);

                // Make another copy to use for draining and filling
                ItemStack fluidHandlerItemStack = inputStack.copy();
                IFluidHandlerItem fluidHandlerItem = fluidHandlerItemStack
                        .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if (fluidHandlerItem == null)
                    return null;

                FluidStack containerFluid = fluidHandlerItem.drain(Integer.MAX_VALUE, true);
                if (containerFluid != null) {
                    // if we actually drained something, then it's draining recipe
                    return recipeBuilder()
                            // we can reuse recipe as long as input container stack fully matches our one
                            .inputs(new GTRecipeItemInput(inputStack, 1))
                            .outputs(fluidHandlerItem.getContainer())
                            .fluidOutputs(containerFluid)
                            .duration(Math.max(16, containerFluid.amount / 64)).EUt(4)
                            .build().getResult();
                }

                // if we didn't drain anything, try filling container
                if (!fluidInputs.isEmpty()) {
                    for (FluidStack inputFluid : fluidInputs) {
                        if (inputFluid == null) {
                            continue;
                        }
                        inputFluid = inputFluid.copy();
                        inputFluid.amount = fluidHandlerItem.fill(inputFluid, true);
                        if (inputFluid.amount > 0) {
                            return recipeBuilder()
                                    // we can reuse recipe as long as input container stack fully matches our one
                                    .inputs(new GTRecipeItemInput(inputStack, 1))
                                    .fluidInputs(inputFluid)
                                    .outputs(fluidHandlerItem.getContainer())
                                    .duration(Math.max(16, inputFluid.amount / 64)).EUt(4)
                                    .build().getResult();
                        }
                    }
                }
            }
        }
        return null;
    }
}
