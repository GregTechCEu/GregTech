package gregtech.api.mui.widget.appeng;

import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;

import appeng.api.storage.data.IAEStack;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;

public abstract class AEConfigSlot<T extends IAEStack<T>> extends Widget<AEConfigSlot<T>> {

    protected final IConfigurableSlot<T> backingSlot;
    protected final boolean isStocking;

    protected static final int jeiDropSyncID = 1;

    public AEConfigSlot(IConfigurableSlot<T> backingSlot, boolean isStocking) {
        this.backingSlot = backingSlot;
        this.isStocking = isStocking;
    }

    @Override
    public void onInit() {
        tooltip(this::getSlotTooltip);
    }

    protected void getSlotTooltip(@NotNull RichTooltip tooltip) {
        tooltip.addLine(IKey.lang("gregtech.gui.config_slot"));
        if (isStocking) {
            tooltip.addLine(IKey.lang("gregtech.gui.config_slot.set_only"));
        } else {
            tooltip.addLine(IKey.lang("gregtech.gui.config_slot.set"));
            tooltip.addLine(IKey.lang("gregtech.gui.config_slot.scroll"));
        }
        tooltip.addLine(IKey.lang("gregtech.gui.config_slot.remove"));
    }
}
