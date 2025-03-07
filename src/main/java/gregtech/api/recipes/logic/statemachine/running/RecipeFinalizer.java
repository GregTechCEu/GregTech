package gregtech.api.recipes.logic.statemachine.running;

import gregtech.api.recipes.logic.RecipeRun;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

public class RecipeFinalizer {

    public static final String STANDARD_RECIPES_KEY = "ActiveRecipes";

    public static final RecipeFinalizer STANDARD_INSTANCE = new RecipeFinalizer();

    public @NotNull NBTTagCompound finalize(@NotNull RecipeRun run) {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setDouble("Duration", run.getDuration());
        tag.setLong("Voltage", run.getRequiredVoltage());
        tag.setLong("Amperage", run.getRequiredAmperage());
        tag.setBoolean("Generating", run.isGenerating());
        tag.setInteger("Parallel", run.getParallel());
        tag.setInteger("Overclock", run.getOverclocks());

        NBTTagList list = new NBTTagList();
        for (ItemStack item : run.getItemsConsumed()) {
            if (item == null) continue;
            list.appendTag(item.writeToNBT(new NBTTagCompound()));
        }
        tag.setTag("ItemsIn", list);

        list = new NBTTagList();
        for (FluidStack fluid : run.getFluidsConsumed()) {
            if (fluid == null) continue;
            list.appendTag(fluid.writeToNBT(new NBTTagCompound()));
        }
        tag.setTag("FluidsIn", list);

        list = new NBTTagList();
        for (ItemStack item : run.getItemsOut()) {
            if (item == null) continue;
            list.appendTag(item.writeToNBT(new NBTTagCompound()));
        }
        tag.setTag("ItemsOut", list);

        list = new NBTTagList();
        for (FluidStack fluid : run.getFluidsOut()) {
            if (fluid == null) continue;
            list.appendTag(fluid.writeToNBT(new NBTTagCompound()));
        }
        tag.setTag("FluidsOut", list);

        return tag;
    }
}
