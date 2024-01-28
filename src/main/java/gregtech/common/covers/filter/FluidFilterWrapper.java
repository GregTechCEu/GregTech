package gregtech.common.covers.filter;

import org.jetbrains.annotations.ApiStatus;

/**
 * @deprecated use {@link FluidFilterContainer}
 */
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "2.10")
public class FluidFilterWrapper {

    FluidFilterContainer container;

    public FluidFilterWrapper(FluidFilterContainer container) {
        this.container = container;
    }

    public void setFluidFilter(FluidFilter fluidFilter) {
        this.container.setFilter(fluidFilter);
    }

    public FluidFilter getFluidFilter() {
        return container.getFilter();
    }
}
