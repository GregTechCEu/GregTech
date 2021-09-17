package gregtech.api.capability.impl;

import gregtech.api.capability.IMultipleTankHandler;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Recommended to use this with {@link NotifiableFluidTankFromList} to ensure
 * proper behavior of the "allowSameFluidFill" setting, but not required.
 */
public class FluidTankList implements IFluidHandler, IMultipleTankHandler, INBTSerializable<NBTTagCompound> {

    protected final List<IFluidTank> fluidTanks;
    protected IFluidTankProperties[] properties;
    private final boolean allowSameFluidFill;
    private IFluidTankProperties[] fluidTankProperties;

    public FluidTankList(boolean allowSameFluidFill, IFluidTank... fluidTanks) {
        this.fluidTanks = Arrays.asList(fluidTanks);
        this.allowSameFluidFill = allowSameFluidFill;
    }

    public FluidTankList(boolean allowSameFluidFill, List<? extends IFluidTank> fluidTanks) {
        this.fluidTanks = new ArrayList<>(fluidTanks);
        this.allowSameFluidFill = allowSameFluidFill;
    }

    public FluidTankList(boolean allowSameFluidFill, FluidTankList parent, IFluidTank... additionalTanks) {
        this.fluidTanks = new ArrayList<>();
        this.fluidTanks.addAll(parent.fluidTanks);
        this.fluidTanks.addAll(Arrays.asList(additionalTanks));
        this.allowSameFluidFill = allowSameFluidFill;
    }

    public List<IFluidTank> getFluidTanks() {
        return Collections.unmodifiableList(fluidTanks);
    }

    @Nonnull
    @Override
    public Iterator<IFluidTank> iterator() {
        return getFluidTanks().iterator();
    }

    @Override
    public int getTanks() {
        return fluidTanks.size();
    }

    @Override
    public IFluidTank getTankAt(int index) {
        return fluidTanks.get(index);
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        if (fluidTankProperties == null) {
            ArrayList<IFluidTankProperties> propertiesList = new ArrayList<>();
            for (IFluidTank fluidTank : fluidTanks) {
                if (fluidTank instanceof IFluidHandler) {
                    IFluidHandler fluidHandler = (IFluidHandler) fluidTank;
                    propertiesList.addAll(Arrays.asList(fluidHandler.getTankProperties()));
                }
            }
            this.fluidTankProperties = propertiesList.toArray(new IFluidTankProperties[0]);
        }
        return fluidTankProperties;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0) {
            return 0;
        }
        return fillTanksImpl(resource.copy(), doFill);
    }

    //fills exactly one tank if multi-filling is not allowed
    //and as much tanks as possible otherwise
    //note that it will always try to fill tanks with same fluid first
    private int fillTanksImpl(FluidStack resource, boolean doFill) {
        int totalFilled = 0;
        //first, try to fill tanks that already have same fluid type
        for (IFluidTank handler : fluidTanks) {
            if (resource.isFluidEqual(handler.getFluid())) {
                int filledAmount = handler.fill(resource, doFill);
                totalFilled += filledAmount;
                resource.amount -= filledAmount;
                //if filling multiple tanks is not allowed, or resource is empty, return now
                if (!allowSameFluidFill() || resource.amount == 0)
                    return totalFilled;
            }
        }
        //otherwise, try to fill empty tanks
        for (IFluidTank handler : fluidTanks) {
            if (handler.getFluidAmount() == 0) {
                int filledAmount = handler.fill(resource, doFill);
                totalFilled += filledAmount;
                resource.amount -= filledAmount;
                if (!allowSameFluidFill() || resource.amount == 0)
                    return totalFilled;
            }
        }
        return totalFilled;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (resource == null || resource.amount <= 0) {
            return null;
        }
        resource = resource.copy();
        FluidStack totalDrained = null;
        for (IFluidTank handler : fluidTanks) {
            if (!resource.isFluidEqual(handler.getFluid())) {
                continue;
            }
            FluidStack drain = handler.drain(resource.amount, doDrain);
            if (drain == null) {
                continue;
            }
            if (totalDrained == null) {
                totalDrained = drain;
            } else totalDrained.amount += drain.amount;

            resource.amount -= drain.amount;
            if (resource.amount == 0) break;
        }
        return totalDrained;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (maxDrain == 0) {
            return null;
        }
        FluidStack totalDrained = null;
        for (IFluidTank handler : fluidTanks) {
            if (totalDrained == null) {
                totalDrained = handler.drain(maxDrain, doDrain);
                if (totalDrained != null)
                    maxDrain -= totalDrained.amount;
            } else {
                FluidStack copy = totalDrained.copy();
                copy.amount = maxDrain;
                if (!copy.isFluidEqual(handler.getFluid())) continue;
                FluidStack drain = handler.drain(copy.amount, doDrain);
                if (drain != null) {
                    totalDrained.amount += drain.amount;
                    maxDrain -= drain.amount;
                }
            }
            if (maxDrain <= 0) break;
        }
        return totalDrained;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound fluidInventory = new NBTTagCompound();
        fluidInventory.setInteger("TankAmount", this.getTanks());

        NBTTagList tanks = new NBTTagList();
        for (int i = 0; i < this.getTanks(); i++) {
            NBTBase writeTag;
            IFluidTank fluidTank = fluidTanks.get(i);
            if (fluidTank instanceof FluidTank) {
                writeTag = ((FluidTank) fluidTank).writeToNBT(new NBTTagCompound());
            } else if (fluidTank instanceof INBTSerializable) {
                writeTag = ((INBTSerializable) fluidTank).serializeNBT();
            } else writeTag = new NBTTagCompound();

            tanks.appendTag(writeTag);
        }
        fluidInventory.setTag("Tanks", tanks);
        return fluidInventory;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        NBTTagList tanks = nbt.getTagList("Tanks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < Math.min(fluidTanks.size(), nbt.getInteger("TankAmount")); i++) {
            NBTBase nbtTag = tanks.get(i);
            IFluidTank fluidTank = fluidTanks.get(i);
            if (fluidTank instanceof FluidTank) {
                ((FluidTank) fluidTank).readFromNBT((NBTTagCompound) nbtTag);
            } else if (fluidTank instanceof INBTSerializable) {
                ((INBTSerializable) fluidTank).deserializeNBT(nbtTag);
            }
        }
    }

    protected void validateTankIndex(int tank) {
        if (tank < 0 || tank >= fluidTanks.size())
            throw new RuntimeException("Tank " + tank + " not in valid range - (0," + fluidTanks.size() + "]");
    }

    @Override
    public int getIndexOfFluid(FluidStack fluidStack) {
        for (int i = 0; i < fluidTanks.size(); i++) {
            FluidStack tankStack = fluidTanks.get(i).getFluid();
            if (tankStack != null && tankStack.isFluidEqual(fluidStack)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean allowSameFluidFill() {
        return allowSameFluidFill;
    }
}
