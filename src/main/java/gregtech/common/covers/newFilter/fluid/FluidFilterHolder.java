package gregtech.common.covers.newFilter.fluid;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.widget.ChangeableWidget;
import com.cleanroommc.modularui.common.widget.MultiChildWidget;
import com.cleanroommc.modularui.common.widget.SlotWidget;
import com.cleanroommc.modularui.common.widget.TextWidget;
import gregtech.api.util.IDirtyNotifiable;
import gregtech.common.covers.newFilter.FilterHolder;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.function.Consumer;

public class FluidFilterHolder extends FilterHolder<FluidStack, FluidFilter> {

    public FluidFilterHolder(IDirtyNotifiable dirtyNotifiable) {
        super(dirtyNotifiable);
    }

    public FluidFilterHolder(IItemHandlerModifiable filterInventory, int filterSlotIndex, IDirtyNotifiable dirtyNotifiable) {
        super(filterInventory, filterSlotIndex, dirtyNotifiable);
    }

    @Override
    public Class<FluidFilter> getFilterClass() {
        return FluidFilter.class;
    }

    public Widget createFilterUI(UIBuildContext buildContext, Consumer<Widget> controlsAmountHandler) {
        MultiChildWidget widget = new MultiChildWidget();
        ChangeableWidget filterWidget = new ChangeableWidget(() -> getCurrentFilter() == null ? null : getCurrentFilter().createFilterUI(buildContext, controlsAmountHandler).setDebugLabel("Filter"));
        SlotWidget filterSlot = new SlotWidget(filterInventory, filterSlotIndex);
        filterSlot.setFilter(item -> getFilterOf(item) != null);
        filterSlot.setChangeListener(() -> {
            ModularUI.LOGGER.info("On slot changed {}", filterSlot.getMcSlot().getStack());
            checkFilter(filterSlot.getMcSlot().getStack());
            filterWidget.notifyChangeServer();
        });

        return widget.addChild(filterSlot.setPos(144, 0))
                .addChild(filterWidget)
                .addChild(new TextWidget(new Text("cover.filter.label").localise())
                        .setTextAlignment(Alignment.CenterLeft)
                        .setPos(1, 0)
                        .setSize(36, 20))
                .setDebugLabel("FilterHolder");
    }

    @Override
    public Widget createFilterUI(UIBuildContext buildContext) {
        return createFilterUI(buildContext, null);
    }
}
