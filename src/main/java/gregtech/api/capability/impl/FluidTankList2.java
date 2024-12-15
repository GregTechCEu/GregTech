package gregtech.api.capability.impl;

import gregtech.api.capability.IFilter;
import gregtech.api.capability.IFilteredFluidContainer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import com.google.common.collect.AbstractIterator;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class FluidTankList2 implements IFluidHandler, INBTSerializable<NBTTagCompound>, Iterable<IFluidTank> {

    public static Comparator<TankWrapper> ENTRY_COMPARATOR = (o1, o2) -> {
        // #1: non-empty tank first
        boolean empty1 = o1.getFluidAmount() <= 0;
        boolean empty2 = o2.getFluidAmount() <= 0;
        if (empty1 != empty2) return empty1 ? 1 : -1;

        // #2: filter priority
        IFilter<FluidStack> filter1 = o1.getFilter();
        IFilter<FluidStack> filter2 = o2.getFilter();
        if (filter1 == null) return filter2 == null ? 0 : 1;
        if (filter2 == null) return -1;
        return IFilter.FILTER_COMPARATOR.compare(filter1, filter2);
    };

    private final boolean allowSameFluidFill;
    private TankWrapper[] tanks = new TankWrapper[0];
    private IFluidTankProperties[] properties = new IFluidTankProperties[0];

    public FluidTankList2(boolean allowSameFluidFill, IFluidTank... fluidTanks) {
        if (!ArrayUtils.isEmpty(fluidTanks)) {
            tanks = new TankWrapper[fluidTanks.length];
            properties = new IFluidTankProperties[fluidTanks.length];
            Arrays.setAll(tanks, value -> {
                var tank = wrap(fluidTanks[value]);
                properties[value] = createProp(tank);
                return tank;
            });
        }
        this.allowSameFluidFill = allowSameFluidFill;
    }

    public FluidTankList2(boolean allowSameFluidFill, @NotNull List<? extends IFluidTank> fluidTanks) {
        this(allowSameFluidFill, fluidTanks.toArray(new IFluidTank[0]));
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return properties;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0) {
            return 0;
        }
        int totalInserted = 0;
        boolean inputFluidCopied = false;
        // flag value indicating whether the fluid was stored in 'distinct' slot at least once
        boolean distinctSlotVisited = false;

        var fluidTanks = this.tanks.clone();
        Arrays.sort(fluidTanks, ENTRY_COMPARATOR);

        // search for tanks with same fluid type first
        for (var tank : fluidTanks) {
            // if the fluid to insert matches the tank, insert the fluid
            if (resource.isFluidEqual(tank.getFluid())) {
                int inserted = tank.fill(resource, doFill);
                if (inserted > 0) {
                    totalInserted += inserted;
                    if (resource.amount - inserted <= 0) {
                        return totalInserted;
                    }
                    if (!inputFluidCopied) {
                        inputFluidCopied = true;
                        resource = resource.copy();
                    }
                    resource.amount -= inserted;
                }
                // regardless of whether the insertion succeeded, presence of identical fluid in
                // a slot prevents distinct fill to other slots
                if (!tank.allowSameFluidFill) {
                    distinctSlotVisited = true;
                }
            }
        }
        // if we still have fluid to insert, loop through empty tanks until we find one that can accept the fluid
        for (var tank : fluidTanks) {
            // if the tank uses distinct fluid fill (allowSameFluidFill disabled) and another distinct tank had
            // received the fluid, skip this tank
            boolean usesDistinctFluidFill = tank.allowSameFluidFill;
            if ((usesDistinctFluidFill || !distinctSlotVisited) && tank.getFluidAmount() == 0) {
                int inserted = tank.fill(resource, doFill);
                if (inserted > 0) {
                    totalInserted += inserted;
                    if (resource.amount - inserted <= 0) {
                        return totalInserted;
                    }
                    if (!inputFluidCopied) {
                        inputFluidCopied = true;
                        resource = resource.copy();
                    }
                    resource.amount -= inserted;
                    if (!usesDistinctFluidFill) {
                        distinctSlotVisited = true;
                    }
                }
            }
        }
        // return the amount of fluid that was inserted
        return totalInserted;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (resource == null || resource.amount <= 0) {
            return null;
        }
        int amountLeft = resource.amount;
        FluidStack totalDrained = null;
        for (IFluidTank handler : tanks) {
            if (!resource.isFluidEqual(handler.getFluid())) {
                continue;
            }
            FluidStack drain = handler.drain(amountLeft, doDrain);
            if (drain != null) {
                if (totalDrained == null) {
                    totalDrained = drain;
                } else {
                    totalDrained.amount += drain.amount;
                }
                amountLeft -= drain.amount;
                if (amountLeft <= 0) {
                    return totalDrained;
                }
            }
        }
        return totalDrained;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (maxDrain <= 0) {
            return null;
        }
        FluidStack totalDrained = null;
        for (IFluidTank handler : tanks) {
            if (totalDrained == null) {
                var drained = handler.drain(maxDrain, doDrain);
                if (drained != null) {
                    totalDrained = drained.copy();
                    maxDrain -= totalDrained.amount;
                }
            } else {
                if (!totalDrained.isFluidEqual(handler.getFluid())) {
                    continue;
                }
                FluidStack drain = handler.drain(maxDrain, doDrain);
                if (drain != null) {
                    totalDrained.amount += drain.amount;
                    maxDrain -= drain.amount;
                }
            }
            if (maxDrain <= 0) {
                return totalDrained;
            }
        }
        return totalDrained;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound fluidInventory = new NBTTagCompound();
        NBTTagList tanks = new NBTTagList();
        for (TankWrapper tank : this.tanks) {
            tanks.appendTag(tank.serializeNBT());
        }
        fluidInventory.setTag("Tanks", tanks);
        return fluidInventory;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        NBTTagList tanks = nbt.getTagList("Tanks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tanks.tagCount(); i++) {
            this.tanks[i].deserializeNBT(tanks.getCompoundTagAt(i));
        }
    }

    @Override
    public @NotNull Iterator<IFluidTank> iterator() {
        return new AbstractIterator<>() {

            final int length = tanks.length;
            int index = 0;

            @Override
            protected IFluidTank computeNext() {
                return index < length ? tanks[index++] : endOfData();
            }
        };
    }

    private TankWrapper wrap(IFluidTank tank) {
        return tank instanceof TankWrapper ? (TankWrapper) tank : new TankWrapper(tank, allowSameFluidFill);
    }

    private IFluidTankProperties createProp(IFluidTank tank) {
        return new IFluidTankProperties() {

            @Override
            public FluidStack getContents() {
                return tank.getFluid() == null ? null : tank.getFluid().copy();
            }

            @Override
            public int getCapacity() {
                return tank.getCapacity();
            }

            @Override
            public boolean canFill() {
                return allowSameFluidFill;
            }

            @Override
            public boolean canDrain() {
                return true;
            }

            @Override
            public boolean canFillFluidType(FluidStack fluidStack) {
                // special logic
                return false;
            }

            @Override
            public boolean canDrainFluidType(FluidStack fluidStack) {
                return true;
            }
        };
    }

    public static class TankWrapper implements IFluidTank, IFilteredFluidContainer, INBTSerializable<NBTTagCompound> {

        final IFluidTank tank;
        private final boolean allowSameFluidFill;

        private TankWrapper(IFluidTank tank, boolean allowSameFluidFill) {
            this.allowSameFluidFill = allowSameFluidFill;
            this.tank = tank;
        }

        @Override
        public FluidStack getFluid() {
            return tank.getFluid();
        }

        @Override
        public int getFluidAmount() {
            return tank.getFluidAmount();
        }

        @Override
        public int getCapacity() {
            return tank.getCapacity();
        }

        @Override
        public FluidTankInfo getInfo() {
            return tank.getInfo();
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return tank.fill(resource, doFill);
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return tank.drain(maxDrain, doDrain);
        }

        @Override
        public @Nullable IFilter<FluidStack> getFilter() {
            return tank instanceof IFilteredFluidContainer filter ? filter.getFilter() : null;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public NBTTagCompound serializeNBT() {
            if (tank instanceof FluidTank fluidTank) {
                return fluidTank.writeToNBT(new NBTTagCompound());
            } else if (tank instanceof INBTSerializable serializable) {
                return (NBTTagCompound) serializable.serializeNBT();
            }
            return new NBTTagCompound();
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            if (tank instanceof FluidTank fluidTank) {
                fluidTank.readFromNBT(nbt);
            } else if (tank instanceof INBTSerializable serializable) {
                serializable.deserializeNBT(nbt);
            }
        }
    }
}
