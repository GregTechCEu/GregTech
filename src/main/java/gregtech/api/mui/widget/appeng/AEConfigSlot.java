package gregtech.api.mui.widget.appeng;

import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;

import appeng.api.storage.data.IAEStack;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.Nullable;

public class AEConfigSlot<T extends IAEStack<T>> extends Widget<AEConfigSlot<T>> {

    private final IConfigurableSlot<T> config;
    private IConfigurableSlot<T> cached;
    private final boolean isStocking;

    public AEConfigSlot(IConfigurableSlot<T> config, boolean isStocking) {
        super();
        this.config = config;
        this.isStocking = isStocking;
    }

    @Override
    public @Nullable RichTooltip getTooltip() {
        if (config.getConfig() == null) {

        }
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        super.drawForeground(context);
    }
}
