package gregtech.api.capability.impl;

import gregtech.api.capability.IFilter;
import gregtech.api.capability.MultipleTankHandler;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class FluidTankList extends MultipleTankHandler {

    private final ITankEntry[] fluidTanks;
    private final boolean allowSameFluidFill;
    private Entry[] tanks = new Entry[0];

    public FluidTankList(boolean allowSameFluidFill, IFluidTank... fluidTanks) {
        if (!ArrayUtils.isEmpty(fluidTanks)) {
            tanks = new Entry[fluidTanks.length];
            Arrays.setAll(tanks, value -> wrap(fluidTanks[value]));
        }
        this.allowSameFluidFill = allowSameFluidFill;
    }

    public FluidTankList(boolean allowSameFluidFill, @NotNull List<? extends IFluidTank> fluidTanks) {
        this(allowSameFluidFill, fluidTanks.toArray(new IFluidTank[0]));
    }

    public FluidTankList(boolean allowSameFluidFill, @NotNull MultipleTankHandler parent,
                         IFluidTank... additionalTanks) {
        int tanks = parent.size();
        int additional = 0;

        if (!ArrayUtils.isEmpty(additionalTanks))
            additional = additionalTanks.length;

        this.tanks = new Entry[tanks + additional];

        Arrays.setAll(this.tanks, value -> {
            if (value < tanks) return parent.getTankAt(value);
            else return wrap(additionalTanks[value - tanks]);
        });

        this.allowSameFluidFill = allowSameFluidFill;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0)
            return 0;

        FluidStack copy = resource.copy();

        int totalInserted = 0;

        Entry[] fluidTanks = this.tanks.clone();
        Arrays.sort(fluidTanks, ENTRY_COMPARATOR);

        // search for tanks with same fluid type first
        for (Entry tank : fluidTanks) {
            if (tank.getFluidAmount() == 0 || !resource.isFluidEqual(tank.getFluid()))
                continue;

            // if the fluid to insert matches the tank, insert the fluid
            int inserted = tank.fill(copy, doFill);
            if (inserted <= 0) continue;

            totalInserted += inserted;
            copy.amount -= inserted;
            if (copy.amount <= 0) return totalInserted;
        }

        boolean overflow = false;

        // if we still have fluid to insert, loop through empty tanks until we find one that can accept the fluid
        for (Entry tank : fluidTanks) {
            // if the tank uses distinct fluid fill (allowSameFluidFill disabled) and another distinct tank had
            // received the fluid, skip this tank
            if (overflow && !tank.allowSameFluidFill()) continue;
            if (tank.getFluidAmount() > 0 || !tank.canFillFluidType(resource)) continue;

            int inserted = tank.fill(copy, doFill);
            if (inserted <= 0) continue;

            totalInserted += inserted;
            copy.amount -= inserted;
            if (copy.amount <= 0) return totalInserted;
            else overflow = true;
        }

        // return the amount of fluid that was inserted
        return totalInserted;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (resource == null || resource.amount <= 0)
            return null;

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
        if (maxDrain <= 0) return null;

        FluidStack totalDrained = null;
        for (IFluidTank handler : tanks) {
            if (totalDrained == null) {
                var drained = handler.drain(maxDrain, doDrain);
                if (drained == null) continue;

                totalDrained = drained.copy();
                maxDrain -= totalDrained.amount;

            } else {
                if (!totalDrained.isFluidEqual(handler.getFluid()))
                    continue;

                FluidStack drain = handler.drain(maxDrain, doDrain);
                if (drain == null) continue;

                totalDrained.amount += drain.amount;
                maxDrain -= drain.amount;
            }
            if (maxDrain <= 0) return totalDrained;
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
    public IFluidTankProperties[] getTankProperties() {
        return this.tanks;
    }

    @Override
    public @NotNull List<Entry> getFluidTanks() {
        return Collections.unmodifiableList(Arrays.asList(this.tanks));
    }

    @Override
    public int size() {
        return tanks.length;
    }

    @Override
    public @NotNull Entry getTankAt(int index) {
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

    /**
     * Entry of multi fluid tanks. Retains reference to original {@link IMultipleTankHandler} for accessing
     * information such as {@link IMultipleTankHandler#allowSameFluidFill()}.
     */
    private static final class MultiFluidTankEntry implements ITankEntry {

        private final IMultipleTankHandler tank;
        private final IFluidTank delegate;
        private final IFluidTankProperties[] fallback;

        public MultiFluidTankEntry(@NotNull IMultipleTankHandler tank, @NotNull IFluidTank delegate) {
            this.tank = tank;
            this.delegate = delegate;
            this.fallback = new IFluidTankProperties[] {
                    new FallbackTankProperty()
            };
        }

        @NotNull
        @Override
        public IMultipleTankHandler getParent() {
            return tank;
        }

        @NotNull
        @Override
        public IFluidTank getDelegate() {
            return delegate;
        }

        @NotNull
        public IFluidTankProperties[] getTankProperties() {
            return delegate instanceof IFluidHandler fluidHandler ?
                    fluidHandler.getTankProperties() : fallback;
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return delegate.fill(resource, doFill);
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || resource.amount <= 0) {
                return null;
            }
            if (delegate instanceof IFluidHandler fluidHandler) {
                return fluidHandler.drain(resource, doDrain);
            }
            // just imitate the logic
            FluidStack fluid = delegate.getFluid();
            return fluid != null && fluid.isFluidEqual(resource) ? drain(resource.amount, doDrain) : null;
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return delegate.drain(maxDrain, doDrain);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        public boolean equals(Object obj) {
            return this == obj || delegate.equals(obj);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        private final class FallbackTankProperty implements IFluidTankProperties {

            @Nullable
            @Override
            public FluidStack getContents() {
                return delegate.getFluid();
            }

            @Override
            public int getCapacity() {
                return delegate.getCapacity();
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
                IFilter<FluidStack> filter = getFilter();
                return filter == null || filter.test(fluidStack);
            }

            @Override
            public boolean canDrainFluidType(FluidStack fluidStack) {
                return true;
            }
        }
    }
}
