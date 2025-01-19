package gregtech.mixins.mui2;

import com.cleanroommc.modularui.api.widget.ISynced;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.ModularSyncManager;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.WidgetTree;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicInteger;

// todo remove once rc3 is released
@Mixin(value = PanelSyncHandler.class, remap = false)
public abstract class PanelSyncHandlerMixin {

    @Shadow
    private PanelSyncManager syncManager;

    @Shadow
    public abstract boolean isPanelOpen();

    @Redirect(method = "openPanel(Z)V",
              at = @At(value = "INVOKE",
                       target = "Lcom/cleanroommc/modularui/widget/WidgetTree;collectSyncValues(Lcom/cleanroommc/modularui/value/sync/PanelSyncManager;Lcom/cleanroommc/modularui/screen/ModularPanel;)V"))
    protected void redirectCollectValues(PanelSyncManager syncManager, ModularPanel panel) {
        AtomicInteger id = new AtomicInteger(0);
        String syncKey = ModularSyncManager.AUTO_SYNC_PREFIX + panel.getName();
        WidgetTree.foreachChildBFS(panel, widget -> {
            if (widget instanceof ISynced<?>synced) {
                if (synced.isSynced() && !this.syncManager.hasSyncHandler(synced.getSyncHandler())) {
                    this.syncManager.syncValue(syncKey, id.getAndIncrement(), synced.getSyncHandler());
                }
            }
            return true;
        }, false);
    }

    @Inject(method = "openPanel(Z)V", at = @At("HEAD"), cancellable = true)
    protected void openCheck(boolean syncToServer, CallbackInfo ci) {
        if (isPanelOpen()) ci.cancel();
    }
}
