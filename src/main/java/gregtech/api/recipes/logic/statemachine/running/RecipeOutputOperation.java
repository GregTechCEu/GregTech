package gregtech.api.recipes.logic.statemachine.running;

import gregtech.api.statemachine.GTStateMachineOperator;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

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
        NBTTagCompound recipe = data.getCompoundTag(keyRecipe);
        NBTTagList list = recipe.getTagList("ItemsOut", Constants.NBT.TAG_COMPOUND);
        List<ItemStack> itemsOut = new ObjectArrayList<>(list.tagCount());
        for (int i = 0; i < list.tagCount(); i++) {
            itemsOut.add(new ItemStack(list.getCompoundTagAt(i)));
        }
        list = recipe.getTagList("FluidsOut", Constants.NBT.TAG_COMPOUND);
        List<FluidStack> fluidsOut = new ObjectArrayList<>(list.tagCount());
        for (int i = 0; i < list.tagCount(); i++) {
            fluidsOut.add(FluidStack.loadFluidStackFromNBT(list.getCompoundTagAt(i)));
        }
        if (!itemsOut.isEmpty()) outputItemConsumer.accept(itemsOut);
        if (!fluidsOut.isEmpty()) outputFluidConsumer.accept(fluidsOut);
    }
}
