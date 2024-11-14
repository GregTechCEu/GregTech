package gregtech.api.recipes.logic;

import gregtech.api.recipes.lookup.property.PropertySet;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PrimitiveRecipeRun implements RecipeRun {

    public static final String NAME = "GTPrimitive";

    private final List<ItemStack> itemsOut;
    private final List<FluidStack> fluidsOut;
    private final List<ItemStack> itemsIn;
    private final List<FluidStack> fluidsIn;

    private final int parallel;

    private final double duration;

    private final long @Nullable [] itemArray;
    private final long @Nullable [] fluidArray;

    public PrimitiveRecipeRun(RecipeView view, PropertySet properties, double duration) {
        this.parallel = view.getParallel();
        this.duration = duration;
        itemsIn = view.getConsumedItems(0);
        fluidsIn = view.getConsumedFluids(0);
        itemsOut = view.rollItems(properties, 0, 0);
        fluidsOut = view.rollFluids(properties, 0, 0);
        itemArray = view.getItemArrayConsumption(0);
        fluidArray = view.getFluidArrayConsumption(0);
    }

    public PrimitiveRecipeRun(NBTTagCompound compound) {
        duration = compound.getDouble("Duration");
        parallel = compound.getInteger("Parallel");
        NBTTagList list = compound.getTagList("ItemsIn", Constants.NBT.TAG_COMPOUND);
        this.itemsIn = new ObjectArrayList<>(list.tagCount());
        for (int i = 0; i < list.tagCount(); i++) {
            this.itemsIn.add(new ItemStack(list.getCompoundTagAt(i)));
        }
        list = compound.getTagList("FluidsIn", Constants.NBT.TAG_COMPOUND);
        this.fluidsIn = new ObjectArrayList<>(list.tagCount());
        for (int i = 0; i < list.tagCount(); i++) {
            this.fluidsIn.add(FluidStack.loadFluidStackFromNBT(list.getCompoundTagAt(i)));
        }
        list = compound.getTagList("ItemsOut", Constants.NBT.TAG_COMPOUND);
        this.itemsOut = new ObjectArrayList<>(list.tagCount());
        for (int i = 0; i < list.tagCount(); i++) {
            this.itemsOut.add(new ItemStack(list.getCompoundTagAt(i)));
        }
        list = compound.getTagList("FluidsOut", Constants.NBT.TAG_COMPOUND);
        this.fluidsOut = new ObjectArrayList<>(list.tagCount());
        for (int i = 0; i < list.tagCount(); i++) {
            this.fluidsOut.add(FluidStack.loadFluidStackFromNBT(list.getCompoundTagAt(i)));
        }
        itemArray = null;
        fluidArray = null;
    }

    @Override
    public @NotNull List<ItemStack> getItemsOut() {
        return itemsOut;
    }

    @Override
    public @NotNull List<FluidStack> getFluidsOut() {
        return fluidsOut;
    }

    @Override
    public @NotNull List<ItemStack> getItemsConsumed() {
        return itemsIn;
    }

    @Override
    public long @Nullable [] getItemArrayConsumption() {
        return itemArray;
    }

    @Override
    public @NotNull List<FluidStack> getFluidsConsumed() {
        return fluidsIn;
    }

    @Override
    public long @Nullable [] getFluidArrayConsumption() {
        return fluidArray;
    }

    @Override
    public int getParallel() {
        return parallel;
    }

    @Override
    public int getOverclocks() {
        return 0;
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public long getRequiredVoltage() {
        return 0;
    }

    @Override
    public long getRequiredAmperage() {
        return 0;
    }

    @Override
    public boolean isGenerating() {
        return false;
    }

    @Override
    public String getRegistryName() {
        return NAME;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setDouble("Duration", duration);
        compound.setInteger("Parallel", parallel);
        NBTTagList list = new NBTTagList();
        for (ItemStack itemOutput : itemsIn) {
            list.appendTag(itemOutput.writeToNBT(new NBTTagCompound()));
        }
        compound.setTag("ItemsIn", list);
        list = new NBTTagList();
        for (FluidStack fluidOutput : fluidsIn) {
            list.appendTag(fluidOutput.writeToNBT(new NBTTagCompound()));
        }
        compound.setTag("FluidsIn", list);
        list = new NBTTagList();
        for (ItemStack itemOutput : itemsOut) {
            list.appendTag(itemOutput.writeToNBT(new NBTTagCompound()));
        }
        compound.setTag("ItemsOut", list);
        list = new NBTTagList();
        for (FluidStack fluidOutput : fluidsOut) {
            list.appendTag(fluidOutput.writeToNBT(new NBTTagCompound()));
        }
        compound.setTag("FluidsOut", list);
        return compound;
    }
}
