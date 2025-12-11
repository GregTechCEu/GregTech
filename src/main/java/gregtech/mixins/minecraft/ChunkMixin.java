package gregtech.mixins.minecraft;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import gregtech.GregTechMod;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.registry.MTERegistry;
import gregtech.common.blocks.MetaBlocks;
import gregtech.core.CoreModule;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Chunk.class, remap = false)
public abstract class ChunkMixin {

    @Inject(method = "createNewTileEntity", at = @At(value = "INVOKE",
                                                     target = "Lnet/minecraft/block/Block;createTileEntity(Lnet/minecraft/world/World;Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/tileentity/TileEntity;"))
    public void setBlock(BlockPos pos, CallbackInfoReturnable<TileEntity> cir) {
        CoreModule.placingPos.set(pos);
        if (CoreModule.gtTileMap.containsKey(pos.toLong())) {
            GregTechAPI.mteManager.getRegistry(GTValues.MODID)
                    .getBlock().testMessage.set(CoreModule.gtTileMap.get(pos.toLong())
                            .getMetaTileEntity().metaTileEntityId.toString());
        }

    }

    @ModifyReturnValue(method = "createNewTileEntity", at = @At("RETURN"))
    public TileEntity modret(TileEntity original) {
        CoreModule.placingPos.remove();
        GregTechAPI.mteManager.getRegistry(GTValues.MODID)
                .getBlock().testMessage.remove();
        return original;
    }
}
