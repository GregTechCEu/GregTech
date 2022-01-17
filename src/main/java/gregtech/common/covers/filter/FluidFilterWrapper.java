package gregtech.common.covers.filter;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.util.IDirtyNotifiable;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FluidFilterWrapper {

    private final IDirtyNotifiable dirtyNotifiable;
    private boolean isBlacklistFilter = false;
    private FluidFilter currentFluidFilter;
    private Supplier<Boolean> showTipSupplier;

    public FluidFilterWrapper(IDirtyNotifiable dirtyNotifiable) {
        this.dirtyNotifiable = dirtyNotifiable;
    }

    public void initUI(int y, Consumer<Widget> widgetGroup) {
        widgetGroup.accept(new ToggleButtonWidget(144, y, 18, 18, GuiTextures.BUTTON_BLACKLIST,
                this::isBlacklistFilter, this::setBlacklistFilter).setTooltipText("cover.filter.blacklist"));
        widgetGroup.accept(new WidgetGroupFluidFilter(y, this::getFluidFilter, shouldShowTip()));
    }

    public void setFluidFilter(FluidFilter fluidFilter) {
        this.currentFluidFilter = fluidFilter;
        if (currentFluidFilter != null) {
            currentFluidFilter.setDirtyNotifiable(dirtyNotifiable);
        }
    }

    private Supplier<Boolean> shouldShowTip() {
        return showTipSupplier;
    }

    protected void setTipSupplier(Supplier<Boolean> supplier) {
        this.showTipSupplier = supplier;
    }

    public FluidFilter getFluidFilter() {
        return currentFluidFilter;
    }

    public void onFilterInstanceChange() {
        dirtyNotifiable.markAsDirty();
    }

    public void setBlacklistFilter(boolean blacklistFilter) {
        isBlacklistFilter = blacklistFilter;
        dirtyNotifiable.markAsDirty();
    }

    public boolean isBlacklistFilter() {
        return isBlacklistFilter;
    }

    public boolean testFluidStack(FluidStack fluidStack) {
        boolean result = true;
        if (currentFluidFilter != null) {
            result = currentFluidFilter.testFluid(fluidStack);
        }
        if (isBlacklistFilter) {
            result = !result;
        }
        return result;
    }
}
