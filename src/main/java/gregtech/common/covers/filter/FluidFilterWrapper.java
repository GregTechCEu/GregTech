package gregtech.common.covers.filter;

@Deprecated
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
