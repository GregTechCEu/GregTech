package gregtech.api.capability;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

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
public interface IMultipleTankHandler2 extends IFluidHandler, Iterable<IMultipleTankHandler2.Entry>, INBTSerializable<NBTTagCompound> {

    /**
     * Comparator for entries that can be used in insertion logic
     */
    Comparator<Entry> ENTRY_COMPARATOR = (o1, o2) -> {
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
    List<Entry> getFluidTanks();

    /**
     * @return Number of tanks in this tank handler
     */
    int getTanks();

    @NotNull
    IMultipleTankHandler2.Entry getTankAt(int index);

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
        List<Entry> fluidTanks = getFluidTanks();
        for (int i = 0; i < fluidTanks.size(); i++) {
            FluidStack tankStack = fluidTanks.get(i).getFluid();
            if (fluidStack == tankStack || tankStack != null && tankStack.isFluidEqual(fluidStack)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    default Iterator<Entry> iterator() {
        return getFluidTanks().iterator();
    }

    /**
     * Entry of multi fluid tanks. Retains reference to original {@link IMultipleTankHandler2} for accessing
     * information such as {@link IMultipleTankHandler2#allowSameFluidFill()}.
     */
    interface Entry extends IFluidTank, IFluidHandler, IFilteredFluidContainer, INBTSerializable<NBTTagCompound> {

        @NotNull
        IMultipleTankHandler2 getParentHandler();

        @NotNull
        IFluidTank getDelegate();

        default boolean allowSameFluidFill() {
            return getParentHandler().allowSameFluidFill();
        }

        @Nullable
        @Override
        default IFilter<FluidStack> getFilter() {
            return getDelegate() instanceof IFilteredFluidContainer filtered ? filtered.getFilter() : null;
        }

        @Nullable
        @Override
        default FluidStack getFluid() {
            return getDelegate().getFluid();
        }

        @Override
        default int getFluidAmount() {
            return getDelegate().getFluidAmount();
        }

        @Override
        default int getCapacity() {
            return getDelegate().getCapacity();
        }

        @Override
        default FluidTankInfo getInfo() {
            return getDelegate().getInfo();
        }

        @Override
        default int fill(FluidStack resource, boolean doFill) {
            return getDelegate().fill(resource, doFill);
        }

        @Nullable
        @Override
        default FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || resource.amount <= 0) {
                return null;
            }
            if (getDelegate() instanceof IFluidHandler fluidHandler) {
                return fluidHandler.drain(resource, doDrain);
            }
            // just imitate the logic
            FluidStack fluid = getFluid();
            return fluid != null && fluid.isFluidEqual(resource) ? drain(resource.amount, doDrain) : null;
        }

        @Nullable
        @Override
        default FluidStack drain(int maxDrain, boolean doDrain) {
            return getDelegate().drain(maxDrain, doDrain);
        }

        @SuppressWarnings("rawtypes")
        @Override
        default NBTTagCompound serializeNBT() {
            if (getDelegate() instanceof FluidTank fluidTank) {
                return fluidTank.writeToNBT(new NBTTagCompound());
            } else if (getDelegate() instanceof INBTSerializable serializable) {
                return (NBTTagCompound) serializable.serializeNBT();
            }
            return new NBTTagCompound();
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        default void deserializeNBT(NBTTagCompound nbt) {
            if (getDelegate() instanceof FluidTank fluidTank) {
                fluidTank.readFromNBT(nbt);
            } else if (getDelegate() instanceof INBTSerializable serializable) {
                serializable.deserializeNBT(nbt);
            }
        }
    }
}
