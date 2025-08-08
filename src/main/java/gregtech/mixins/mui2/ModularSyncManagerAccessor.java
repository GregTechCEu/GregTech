package gregtech.mixins.mui2;

import com.cleanroommc.modularui.value.sync.ModularSyncManager;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

// TODO: MUI2 2.5 RC5 changes getMainPSM() to public
@Mixin(value = ModularSyncManager.class, remap = false)
public interface ModularSyncManagerAccessor {

    @Accessor(value = "panelSyncManagerMap")
    Map<String, PanelSyncManager> getPanelSyncManagers();

    @Accessor(value = "mainPSM")
    PanelSyncManager getMainPanelSyncManager();
}
