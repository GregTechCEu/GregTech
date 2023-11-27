package gregtech.api.capability;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

/**
 * Interface for fluid containers ({@link net.minecraftforge.fluids.IFluidTank IFluidTank} or
 * {@link net.minecraftforge.fluids.capability.IFluidHandler IFluidHandler}) associated with {@link IFilter}.
 */
public interface IFilteredFluidContainer {

    /**
     * Compare logic for filtered instances.
     */
    Comparator<IFilteredFluidContainer> COMPARATOR = Comparator.nullsLast(
            Comparator.comparing(IFilteredFluidContainer::getFilter, IFilter.FILTER_COMPARATOR));

    /**
     * @return instance of {@link IFilter} associated to this object, or {@code null} if there's no filter
     *         associated.
     */
    @Nullable
    IFilter<FluidStack> getFilter();
}
