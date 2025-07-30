package gregtech.mixins.mui2;

import gregtech.client.IsGuiActuallyClosing;

import com.cleanroommc.modularui.value.sync.ModularSyncManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModularSyncManager.class, remap = false)
public class ModularSyncManagerMixin {

    @Inject(method = "onClose", at = @At(value = "HEAD"), cancellable = true)
    private void cancelIfGUIAboutToOpenIsJEI(CallbackInfo ci) {
        if (!IsGuiActuallyClosing.isGuiActuallyClosing) {
            ci.cancel();
        }
    }
}
