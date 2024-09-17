package gregtech.api.recipes.logic;

import gregtech.api.recipes.chance.boost.ChanceBoostFunction;
import gregtech.api.recipes.lookup.property.PropertySet;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SingleRecipeRun implements RecipeRun {

    public static final String NAME = "GTSingle";

    private final List<ItemStack> itemsOut;
    private final List<FluidStack> fluidsOut;
    private final List<ItemStack> itemsIn;
    private final List<FluidStack> fluidsIn;

    private final int parallel;
    private final int overclocks;

    private final double duration;
    private final long requiredVoltage;
    private final long requiredAmperage;
    private final  boolean generating;

    public SingleRecipeRun(RecipeView view, int recipeTier, int machineTier, ChanceBoostFunction boostFunction,
                           PropertySet properties, float overclockVoltageMultiplier, double duration) {
        overclocks = machineTier - recipeTier;
        assert overclocks >= 0;
        this.parallel = view.getParallel();
        this.duration = duration;
        itemsIn = view.getConsumedItems();
        fluidsIn = view.getConsumedFluids();
        itemsOut = view.rollItems(properties, recipeTier, machineTier, boostFunction);
        fluidsOut = view.rollFluids(properties, recipeTier, machineTier, boostFunction);
        requiredVoltage = (long) (view.getActualVoltage() * overclockVoltageMultiplier);
        requiredAmperage = view.getActualAmperage();
        generating = view.getRecipe().isGenerating();
    }

    public SingleRecipeRun(NBTTagCompound compound) {
        duration = compound.getDouble("Duration");
        requiredVoltage = compound.getLong("Voltage");
        requiredAmperage = compound.getLong("Amperage");
        generating = compound.getBoolean("Generating");
        parallel = compound.getInteger("Parallel");
        overclocks = compound.getInteger("Overclock");
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
    public @NotNull List<FluidStack> getFluidsConsumed() {
        return fluidsIn;
    }

    @Override
    public int getParallel() {
        return parallel;
    }

    @Override
    public int getOverclocks() {
        return overclocks;
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public long getRequiredVoltage() {
        return requiredVoltage;
    }

    @Override
    public long getRequiredAmperage() {
        return requiredAmperage;
    }

    @Override
    public boolean isGenerating() {
        return generating;
    }

    @Override
    public String getRegistryName() {
        return NAME;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setDouble("Duration", duration);
        compound.setLong("Voltage", requiredVoltage);
        compound.setLong("Amperage", requiredAmperage);
        compound.setBoolean("Generating", generating);
        compound.setInteger("Parallel", parallel);
        compound.setInteger("Overclock", overclocks);
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
