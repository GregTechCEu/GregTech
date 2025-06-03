package gregtech.mixins.mui2;

import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = PanelSyncManager.class, remap = false)
public interface PanelSyncManagerAccessor {

    @Accessor()
    Map<String, SyncHandler> getSyncHandlers();

    @Accessor()
    Map<String, SyncHandler> getSubPanels();
}
