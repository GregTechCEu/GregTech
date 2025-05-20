package gregtech.api.mui.widget.appeng;

import gregtech.api.mui.sync.appeng.AESyncHandler;

import appeng.api.storage.data.IAEStack;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.integration.jei.JeiIngredientProvider;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.value.sync.ModularSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

public abstract class AEConfigSlot<T extends IAEStack<T>> extends Widget<AEConfigSlot<T>>
                                  implements JeiIngredientProvider {

    protected final boolean isStocking;
    protected final BooleanSupplier isAutoPull;

    public AEConfigSlot(boolean isStocking, BooleanSupplier isAutoPull) {
        this.isStocking = isStocking;
        this.isAutoPull = isAutoPull;
    }

    @Override
    public void onInit() {
        tooltipBuilder(this::buildTooltip);
    }

    @Override
    public void initialiseSyncHandler(ModularSyncManager syncManager) {
        super.initialiseSyncHandler(syncManager);
        if (getSyncHandler() instanceof AESyncHandler<?>aeSyncHandler) {
            aeSyncHandler.setOnConfigChanged(this::markTooltipDirty);
        } else {
            throw new IllegalStateException("Sync handler for AEConfigSlot is not a AESyncHandler!");
        }
    }

    protected void buildTooltip(@NotNull RichTooltip tooltip) {
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
