package gregtech.mixins.minecraft;

import net.minecraft.world.WorldEntitySpawner;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static gregtech.common.ConfigHolder.vanillaOptimizeOptions;

@Mixin(WorldEntitySpawner.class)
public abstract class WorldEntitySpawnerMixin {

    @Inject(method = "findChunksForSpawning", at = @At("HEAD"), cancellable = true)
    private void injectFindChunks(CallbackInfoReturnable<Integer> cir) {
        if(vanillaOptimizeOptions.disableSpawnEnable) {
            cir.setReturnValue(0);
            cir.cancel();
        }
    }
}
