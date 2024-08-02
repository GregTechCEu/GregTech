package gregtech.api.recipes.machines;

import com.google.common.collect.Iterators;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.recipes.ui.RecipeMapUIFunction;
import gregtech.api.util.SingletonIterator;
import gregtech.api.util.SingletonLazyIterator;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@ApiStatus.Internal
public class RecipeMapFluidCanner extends RecipeMap<SimpleRecipeBuilder> {

    public RecipeMapFluidCanner(@NotNull String unlocalizedName, @NotNull SimpleRecipeBuilder defaultRecipeBuilder,
                                @NotNull RecipeMapUIFunction recipeMapUI) {
        super(unlocalizedName, defaultRecipeBuilder, recipeMapUI, 2, 2, 1, 1);
        setSound(GTSoundEvents.BATH);
    }

    @Override
    public @NotNull Iterator<@NotNull Recipe> findRecipe(long voltage, @NotNull List<ItemStack> inputs, @NotNull List<FluidStack> fluidInputs, boolean exactVoltage) {
        var iter = super.findRecipe(voltage, inputs, fluidInputs, exactVoltage);

        var additional = new SingletonLazyIterator<>(() -> {
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
        });

        return Iterators.concat(additional, iter);
    }
}
