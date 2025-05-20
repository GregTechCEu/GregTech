package gregtech.api.mui.widget.appeng;

import gregtech.api.mui.sync.appeng.AESyncHandler;

import appeng.api.storage.data.IAEStack;
import com.cleanroommc.modularui.integration.jei.JeiIngredientProvider;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.value.sync.ModularSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;

public abstract class AEDisplaySlot<T extends IAEStack<T>> extends Widget<AEDisplaySlot<T>>
                                   implements JeiIngredientProvider {

    public AEDisplaySlot() {
        super();
    }

    @Override
    public void onInit() {
        tooltipBuilder(this::buildTooltip);
    }

    @Override
    public void initialiseSyncHandler(ModularSyncManager syncManager) {
        super.initialiseSyncHandler(syncManager);
        ((AESyncHandler<?>) getSyncHandler()).setOnConfigChanged(this::markTooltipDirty);
    }

    protected void buildTooltip(@NotNull RichTooltip tooltip) {}
}
