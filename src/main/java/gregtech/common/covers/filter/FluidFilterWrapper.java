package gregtech.common.covers.filter;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ServerWidgetGroup;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.util.IDirtyNotifiable;

import net.minecraftforge.fluids.FluidStack;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FluidFilterWrapper {

    FluidFilterContainer container;

    public FluidFilterWrapper(FluidFilterContainer container) {
        this.container = container;
    }

    public void setFluidFilter(FluidFilter fluidFilter) {
        this.container.setFluidFilter(fluidFilter);
    }

    public FluidFilter getFluidFilter() {
        return container.getFluidFilter();
    }
}
