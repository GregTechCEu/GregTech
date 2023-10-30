package gregtech.mixins.ccl;

import codechicken.lib.internal.ModDescriptionEnhancer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// CCL's supporter stuff is broken, the link no longer works
@Mixin(ModDescriptionEnhancer.class)
public class CCLDescriptionMixin {

    @Inject(method = "init", at = @At("HEAD"), remap = false, cancellable = true)
    private static void stopDescriptionEnhancement(CallbackInfo ci) {
        ci.cancel();
    }
}
