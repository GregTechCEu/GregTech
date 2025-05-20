package gregtech.api.mui.widget.appeng;

import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;

import appeng.api.storage.data.IAEStack;
import com.cleanroommc.modularui.widget.Widget;

public abstract class AEDisplaySlot<T extends IAEStack<T>> extends Widget<AEDisplaySlot<T>> {

    private final IConfigurableSlot<T> config;
    private IConfigurableSlot<T> cached;

    public AEDisplaySlot(IConfigurableSlot<T> config) {
        super();
        this.config = config;
    }
}
