package gregtech.api.mui.widget.appeng;

import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;

import appeng.api.storage.data.IAEStack;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.Nullable;

public class AEConfigSlot<T extends IAEStack<T>> extends Widget<AEConfigSlot<T>> {

    protected final IConfigurableSlot<T> backingSlot;
    private IConfigurableSlot<T> cached;
    protected final boolean isStocking;

    public AEConfigSlot(IConfigurableSlot<T> backingSlot, boolean isStocking) {
        this.backingSlot = backingSlot;
        this.isStocking = isStocking;
    }

    @Override
    public @Nullable RichTooltip getTooltip() {
        if (backingSlot.getConfig() != null) {
            return null;
        }

        RichTooltip tooltip = new RichTooltip(this);

        tooltip.addLine(IKey.lang("gregtech.gui.config_slot"));
        if (isStocking) {
            tooltip.addLine(IKey.lang("gregtech.gui.config_slot.set_only"));
        } else {
            tooltip.addLine(IKey.lang("gregtech.gui.config_slot.set"));
            tooltip.addLine(IKey.lang("gregtech.gui.config_slot.scroll"));
        }
        tooltip.addLine(IKey.lang("gregtech.gui.config_slot.remove"));

        return tooltip;
    }
}
