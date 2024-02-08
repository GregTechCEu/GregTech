package gregtech.common.covers.filter;

import gregtech.api.cover.CoverWithUI;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.sync.FixedFluidSlotSH;
import gregtech.common.covers.filter.readers.SimpleFluidFilterReader;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.FluidSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Row;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class SimpleFluidFilter extends BaseFilter implements IFluidFilter {

    private static final int MAX_FLUID_SLOTS = 9;

    private final SimpleFluidFilterReader filterReader;

    public SimpleFluidFilter(ItemStack stack) {
        this.filterReader = new SimpleFluidFilterReader(stack, MAX_FLUID_SLOTS);
        setFilterReader(this.filterReader);
    }

    @Override
    public void configureFilterTanks(int amount) {
        this.filterReader.setFluidAmounts(amount);
        this.markDirty();
    }

    @Override
    public @NotNull ModularPanel createPopupPanel(GuiSyncManager syncManager) {
        return GTGuis.createPopupPanel("simple_fluid_filter", 98, 81)
                .padding(4)
                .child(CoverWithUI.createTitleRow(getContainerStack()))
                .child(createWidgets(syncManager).top(22));
    }

    @Override
    public @NotNull ModularPanel createPanel(GuiSyncManager syncManager) {
        return GTGuis.createPanel(getContainerStack(), 176, 168);
    }

    @Override
    public @NotNull Widget<?> createWidgets(GuiSyncManager syncManager) {
        return new Row().coverChildrenHeight().widthRel(1f)
                .child(SlotGroupWidget.builder()
                        .matrix("FFF",
                                "FFF",
                                "FFF")
                        .key('F', i -> new FluidSlot()
                                .syncHandler(new FixedFluidSlotSH(filterReader.getFluidTank(i)).phantom(true)))
                        .build().marginRight(4))
                .child(createBlacklistUI());
    }

    @Override
    public MatchResult<FluidStack> match(FluidStack toMatch) {
        int index = -1;
        FluidStack returnable = null;
        for (int i = 0; i < filterReader.getSize(); i++) {
            var fluid = filterReader.getFluidStack(i);
            if (fluid != null && fluid.isFluidEqual(toMatch)) {
                index = i;
                returnable = fluid.copy();
                break;
            }
        }
        return createResult(index != -1, returnable, index);
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        for (int i = 0; i < filterReader.getSize(); i++) {
            var fluid = filterReader.getFluidStack(i);
            if (fluid != null && fluid.isFluidEqual(fluidStack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void initUI(Consumer<gregtech.api.gui.Widget> widgetGroup) {
        for (int i = 0; i < 9; ++i) {
            widgetGroup.accept((new gregtech.api.gui.widgets.PhantomFluidWidget(10 + 18 * (i % 3), 18 * (i / 3), 18, 18,
                    filterReader.getFluidTank(i)))
                            .setBackgroundTexture(gregtech.api.gui.GuiTextures.SLOT));
        }
    }

    @Override
    public boolean showGlobalTransferLimitSlider() {
        return isBlacklistFilter() && getMaxTransferSize() > 0;
    }

    @Override
    public int getTransferLimit(FluidStack fluidStack, int transferSize) {
        int limit = 0;

        for (int i = 0; i < this.filterReader.getSize(); i++) {
            var fluid = this.filterReader.getFluidStack(i);
            if (fluid != null && fluid.isFluidEqual(fluidStack)) {
                limit = fluid.amount;
            }
        }
        return isBlacklistFilter() ? transferSize : limit;
    }
}
