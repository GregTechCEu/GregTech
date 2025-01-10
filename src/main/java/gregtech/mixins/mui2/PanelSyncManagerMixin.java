package gregtech.mixins.mui2;

import gregtech.api.util.GTLog;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

// todo remove once rc3 is released
@Mixin(value = PanelSyncManager.class, remap = false)
public abstract class PanelSyncManagerMixin {

    @Shadow
    @Final
    private Map<String, SyncHandler> syncHandlers;

    @Shadow
    private String panelName;

    @Inject(method = "receiveWidgetUpdate", at = @At("HEAD"), cancellable = true)
    public void injectCheck(String mapKey, int id, PacketBuffer buf, CallbackInfo ci) {
        if (!this.syncHandlers.containsKey(mapKey)) {
            GTLog.logger.warn("[ModularUI] SyncHandler \"{}\" does not exist for panel \"{}\"! ID was {}.", mapKey,
                    panelName, id);
            ci.cancel();
        }
    }
}
