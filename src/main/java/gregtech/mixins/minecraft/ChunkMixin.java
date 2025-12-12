package gregtech.mixins.minecraft;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.client.utils.TrackedDummyWorld;
import gregtech.core.CoreModule;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Chunk.class, remap = false)
public abstract class ChunkMixin {

    @Shadow
    @Final
    private World world;

    @Inject(method = "createNewTileEntity",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/block/Block;createTileEntity(Lnet/minecraft/world/World;Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/tileentity/TileEntity;"))
    public void setData(BlockPos pos, CallbackInfoReturnable<TileEntity> cir) {
        if (this.world instanceof TrackedDummyWorld) return;
        CoreModule.placingPos.set(pos);
        if (CoreModule.gtTileMap.containsKey(pos.toLong())) {
            gregTech$setMessage(CoreModule.gtTileMap.get(pos.toLong())
                    .getMetaTileEntity());
        }
    }

    @Unique
    private static void gregTech$setMessage(MetaTileEntity mte) {
        GregTechAPI.mteManager.getRegistry(GTValues.MODID)
                .getBlock().testMessage.set(() -> mte.createMetaTileEntity(null));
    }

    @ModifyReturnValue(method = "createNewTileEntity", at = @At("RETURN"))
    public TileEntity clearData(TileEntity original) {
        CoreModule.placingPos.remove();
        GregTechAPI.mteManager.getRegistry(GTValues.MODID)
                .getBlock().testMessage.remove();
        return original;
    }
}
