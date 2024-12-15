package gregtech.api.capability.impl;

import gregtech.api.capability.IFilter;
import gregtech.api.capability.IFilteredFluidContainer;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.IMultipleTankHandler2;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class FluidTankList2 implements IMultipleTankHandler2 {

    private final boolean allowSameFluidFill;
    private Entry[] tanks = new Entry[0];
    private IFluidTankProperties[] properties = new IFluidTankProperties[0];

    public FluidTankList2(boolean allowSameFluidFill, IFluidTank... fluidTanks) {
        if (!ArrayUtils.isEmpty(fluidTanks)) {
            tanks = new Entry[fluidTanks.length];
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
                if (!tank.allowSameFluidFill()) {
                    distinctSlotVisited = true;
                }
            }
        }
        // if we still have fluid to insert, loop through empty tanks until we find one that can accept the fluid
        for (var tank : fluidTanks) {
            // if the tank uses distinct fluid fill (allowSameFluidFill disabled) and another distinct tank had
            // received the fluid, skip this tank
            boolean usesDistinctFluidFill = tank.allowSameFluidFill();
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
        for (Entry tank : this.tanks) {
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
    public @NotNull List<Entry> getFluidTanks() {
        return Collections.unmodifiableList(Arrays.asList(this.tanks));
    }

    @Override
    public int getTanks() {
        return tanks.length;
    }

    @Override
    public @NotNull IMultipleTankHandler2.Entry getTankAt(int index) {
        return tanks[index];
    }

    @Override
    public boolean allowSameFluidFill() {
        return allowSameFluidFill;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean lineBreak) {
        StringBuilder stb = new StringBuilder("FluidTankList[").append(this.tanks.length).append(";");
        for (int i = 0; i < this.tanks.length; i++) {
            if (i != 0) stb.append(',');
            stb.append(lineBreak ? "\n  " : " ");

            FluidStack fluid = this.tanks[i].getFluid();
            if (fluid == null || fluid.amount == 0) {
                stb.append("None 0 / ").append(this.tanks[i].getCapacity());
            } else {
                stb.append(fluid.getFluid().getName()).append(' ').append(fluid.amount)
                        .append(" / ").append(this.tanks[i].getCapacity());
            }
        }
        if (lineBreak) stb.append('\n');
        return stb.append(']').toString();
    }

    private TankWrapper wrap(IFluidTank tank) {
        return tank instanceof TankWrapper ? (TankWrapper) tank : new TankWrapper(tank, this);
    }

    protected static IFluidTankProperties createProp(IFluidTank tank) {
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
                return true;
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

    public static class TankWrapper implements Entry {

        private final IFluidTank tank;
        private final IMultipleTankHandler2 parent;
        private final IFluidTankProperties[] props;

        private TankWrapper(IFluidTank tank, IMultipleTankHandler2 parent) {
            this.tank = tank;
            this.parent = parent;
            this.props = new IFluidTankProperties[] {
                    createProp(this)
            };
        }

        @Override
        public @NotNull IMultipleTankHandler2 getParentHandler() {
            return parent;
        }

        @Override
        public @NotNull IFluidTank getDelegate() {
            return tank;
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return this.props;
        }
    }
}
