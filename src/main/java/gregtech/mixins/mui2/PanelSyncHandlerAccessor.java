package gregtech.mixins.mui2;

import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = PanelSyncHandler.class, remap = false)
public interface PanelSyncHandlerAccessor {

    @Accessor(value = "syncManager")
    PanelSyncManager getPanelSyncManager();
}
