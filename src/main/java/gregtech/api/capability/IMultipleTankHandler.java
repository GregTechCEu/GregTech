package gregtech.api.capability;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Base class for multi-tank fluid handlers. Handles insertion logic, along with other standard
 * {@link IFluidHandler} functionalities.
 *
 * @see gregtech.api.capability.impl.FluidTankList FluidTankList
 */
public interface IMultipleTankHandler extends IFluidHandler, Iterable<IMultipleTankHandler.MultiFluidTankEntry> {

    /**
     * Comparator for entries that can be used in insertion logic
     */
    Comparator<MultiFluidTankEntry> ENTRY_COMPARATOR = (o1, o2) -> {
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

    /**
     * @return unmodifiable view of {@code MultiFluidTankEntry}s. Note that it's still possible to access
     *         and modify inner contents of the tanks.
     */
    @NotNull
    List<MultiFluidTankEntry> getFluidTanks();

    /**
     * @return Number of tanks in this tank handler
     */
    int getTanks();

    @NotNull
    MultiFluidTankEntry getTankAt(int index);

    /**
     * @return {@code false} if insertion to this fluid handler enforces input to be
     *         filled in one slot at max. {@code true} if it bypasses the rule.
     */
    boolean allowSameFluidFill();

    /**
     * Tries to search tank with contents equal to {@code fluidStack}. If {@code fluidStack} is
     * {@code null}, an empty tank is searched instead.
     *
     * @param fluidStack Fluid stack to search index
     * @return Index corresponding to tank at {@link #getFluidTanks()} with matching
     */
    default int getIndexOfFluid(@Nullable FluidStack fluidStack) {
        List<MultiFluidTankEntry> fluidTanks = getFluidTanks();
        for (int i = 0; i < fluidTanks.size(); i++) {
            FluidStack tankStack = fluidTanks.get(i).getFluid();
            if (fluidStack == tankStack || tankStack != null && tankStack.isFluidEqual(fluidStack)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    default Iterator<MultiFluidTankEntry> iterator() {
        return getFluidTanks().iterator();
    }

    /**
     * Entry of multi fluid tanks. Retains reference to original {@link IMultipleTankHandler} for accessing
     * information such as {@link IMultipleTankHandler#allowSameFluidFill()}.
     */
    final class MultiFluidTankEntry implements IFluidTank, IFluidHandler, IFilteredFluidContainer {

        private final IMultipleTankHandler tank;
        private final IFluidTank delegate;

        public MultiFluidTankEntry(@NotNull IMultipleTankHandler tank, @NotNull IFluidTank delegate) {
            this.tank = tank;
            this.delegate = delegate;
        }

        @NotNull
        public IMultipleTankHandler getTank() {
            return tank;
        }

        @NotNull
        public IFluidTank getDelegate() {
            return delegate;
        }

        public boolean allowSameFluidFill() {
            return tank.allowSameFluidFill();
        }

        @Nullable
        @Override
        public IFilter<FluidStack> getFilter() {
            return this.delegate instanceof IFilteredFluidContainer filtered ? filtered.getFilter() : null;
        }

        @NotNull
        public IFluidTankProperties[] getTankProperties() {
            return delegate instanceof IFluidHandler fluidHandler ?
                    fluidHandler.getTankProperties() :
                    new IFluidTankProperties[] { new FallbackTankProperty() };
        }

        public NBTTagCompound trySerialize() {
            if (delegate instanceof FluidTank fluidTank) {
                return fluidTank.writeToNBT(new NBTTagCompound());
            } else if (delegate instanceof INBTSerializable serializable) {
                try {
                    return (NBTTagCompound) serializable.serializeNBT();
                } catch (ClassCastException ignored) {}
            }
            return new NBTTagCompound();
        }

        @SuppressWarnings({ "unchecked" })
        public void tryDeserialize(NBTTagCompound tag) {
            if (delegate instanceof FluidTank fluidTank) {
                fluidTank.readFromNBT(tag);
            } else if (delegate instanceof INBTSerializable serializable) {
                try {
                    serializable.deserializeNBT(tag);
                } catch (ClassCastException ignored) {}
            }
        }

        @Nullable
        @Override
        public FluidStack getFluid() {
            return delegate.getFluid();
        }

        @Override
        public int getFluidAmount() {
            return delegate.getFluidAmount();
        }

        @Override
        public int getCapacity() {
            return delegate.getCapacity();
        }

        @Override
        public FluidTankInfo getInfo() {
            return delegate.getInfo();
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
