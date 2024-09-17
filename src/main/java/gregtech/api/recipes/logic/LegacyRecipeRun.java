package gregtech.api.recipes.logic;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Collections;
import java.util.List;

public final class LegacyRecipeRun implements RecipeRun {

    private final double maxProgressTime;
    private final long recipeEUt;
    private final boolean generating;

    private final List<ItemStack> itemOutputs;

    private final List<FluidStack> fluidOutputs;

    public LegacyRecipeRun(NBTTagCompound compound) {
        this.maxProgressTime = compound.getInteger("MaxProgress");
        long eu = compound.getLong("RecipeEUt");
        if (eu < 0) {
            this.recipeEUt = -eu;
            this.generating = true;
        } else {
            this.recipeEUt = eu;
            this.generating = false;
        }
        NBTTagList itemOutputsList = compound.getTagList("ItemOutputs", Constants.NBT.TAG_COMPOUND);
        this.itemOutputs = new ObjectArrayList<>(itemOutputsList.tagCount());
        for (int i = 0; i < itemOutputsList.tagCount(); i++) {
            this.itemOutputs.add(new ItemStack(itemOutputsList.getCompoundTagAt(i)));
        }
        NBTTagList fluidOutputsList = compound.getTagList("FluidOutputs", Constants.NBT.TAG_COMPOUND);
        this.fluidOutputs = new ObjectArrayList<>(fluidOutputsList.tagCount());
        for (int i = 0; i < fluidOutputsList.tagCount(); i++) {
            this.fluidOutputs.add(FluidStack.loadFluidStackFromNBT(fluidOutputsList.getCompoundTagAt(i)));
        }
    }

    @Override
    public @NotNull List<ItemStack> getItemsOut() {
        return itemOutputs;
    }

    @Override
    public @NotNull List<FluidStack> getFluidsOut() {
        return fluidOutputs;
    }

    @Override
    public @NotNull List<ItemStack> getItemsConsumed() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull List<FluidStack> getFluidsConsumed() {
        return Collections.emptyList();
    }

    @Override
    public @Range(from = 1, to = Integer.MAX_VALUE) int getParallel() {
        return 1;
    }

    @Override
    public @Range(from = 0, to = Integer.MAX_VALUE) int getOverclocks() {
        return 0;
    }

    @Override
    public double getDuration() {
        return maxProgressTime;
    }

    @Override
    public @Range(from = 0, to = Long.MAX_VALUE) long getRequiredVoltage() {
        return recipeEUt;
    }

    @Override
    public @Range(from = 0, to = Long.MAX_VALUE) long getRequiredAmperage() {
        return 1;
    }

    @Override
    public boolean isGenerating() {
        return generating;
    }

    @Override
    public String getRegistryName() {
        return "Legacy";
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("MaxProgress", (int) maxProgressTime);
        compound.setLong("RecipeEUt", generating ? -this.recipeEUt : this.recipeEUt);
        NBTTagList itemOutputsList = new NBTTagList();
        for (ItemStack itemOutput : itemOutputs) {
            itemOutputsList.appendTag(itemOutput.writeToNBT(new NBTTagCompound()));
        }
        NBTTagList fluidOutputsList = new NBTTagList();
        for (FluidStack fluidOutput : fluidOutputs) {
            fluidOutputsList.appendTag(fluidOutput.writeToNBT(new NBTTagCompound()));
        }
        compound.setTag("ItemOutputs", itemOutputsList);
        compound.setTag("FluidOutputs", fluidOutputsList);
        return compound;
    }
}
