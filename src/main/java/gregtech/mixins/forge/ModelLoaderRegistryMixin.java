package gregtech.mixins.forge;

import gregtech.api.unification.material.info.MaterialIconType;

import net.minecraft.client.resources.IResourceManager;
import net.minecraftforge.client.model.ModelLoaderRegistry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelLoaderRegistry.class)
public class ModelLoaderRegistryMixin {

    @Inject(method = "clearModelCache", at = @At("TAIL"), remap = false)
    private static void clearCache(IResourceManager manager, CallbackInfo ci) {
        MaterialIconType.clearCache();
    }
}
