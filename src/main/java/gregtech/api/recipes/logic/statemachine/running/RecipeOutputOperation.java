package gregtech.api.recipes.logic.statemachine.running;

import gregtech.api.statemachine.GTStateMachineOperator;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Consumer;

public class RecipeOutputOperation implements GTStateMachineOperator {

    protected final Consumer<List<ItemStack>> outputItemConsumer;
    protected final Consumer<List<FluidStack>> outputFluidConsumer;
    protected final String keyRecipe;

    public RecipeOutputOperation(Consumer<List<ItemStack>> outputItemConsumer,
                                 Consumer<List<FluidStack>> outputFluidConsumer) {
        this.outputItemConsumer = outputItemConsumer;
        this.outputFluidConsumer = outputFluidConsumer;
        this.keyRecipe = RecipeCleanupOperation.STANDARD_RECIPE_KEY;
    }

    public RecipeOutputOperation(Consumer<List<ItemStack>> outputItemConsumer,
                                 Consumer<List<FluidStack>> outputFluidConsumer, String keyRecipe) {
        this.outputItemConsumer = outputItemConsumer;
        this.outputFluidConsumer = outputFluidConsumer;
        this.keyRecipe = keyRecipe;
    }

    @Override
    public void operate(NBTTagCompound data) {
        NBTTagCompound recipe = RecipeCleanupOperation.selected(data);
        List<ItemStack> itemsOut = RecipeFinalizer.itemsOut(recipe);
        List<FluidStack> fluidsOut = RecipeFinalizer.fluidsOut(recipe);
        if (!itemsOut.isEmpty()) outputItemConsumer.accept(itemsOut);
        if (!fluidsOut.isEmpty()) outputFluidConsumer.accept(fluidsOut);
    }
}
