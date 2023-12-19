package gregtech.mixins.littletiles;

import gregtech.asm.hooks.LittleTilesHooks;

import com.creativemd.littletiles.client.render.cache.LayeredRenderBoxCache;
import com.creativemd.littletiles.client.render.world.TileEntityRenderManager;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// TODO, I am not quite sure if this matches with what the ASM was actually doing
@Mixin(value = TileEntityRenderManager.class, remap = false)
public class LittleTilesRenderMangerMixin {

    @Mutable
    @Shadow
    @Final
    private LayeredRenderBoxCache boxCache;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    public void adjustRenderLayerdBox(TileEntityLittleTiles te, CallbackInfo ci) {
        boxCache = LittleTilesHooks.initLayeredRenderBoxCache();
    }
}
