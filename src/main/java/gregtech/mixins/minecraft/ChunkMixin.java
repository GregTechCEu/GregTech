package gregtech.mixins.minecraft;

import gregtech.api.metatileentity.GTBaseTileEntity;
import gregtech.api.util.GTLog;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Chunk.class)
public abstract class ChunkMixin {

    @Shadow
    public abstract World getWorld();

    @WrapOperation(method = "getTileEntity",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/chunk/Chunk;createNewTileEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;"))
    public TileEntity fixGetter(Chunk instance, BlockPos pos, Operation<TileEntity> original) {
        if (this.getWorld().isRemote && GTBaseTileEntity.hasTE(pos)) {
            // fix creation of mtes
            return GTBaseTileEntity.getTEByPos(pos);
        }
        TileEntity tile = null;
        try {
            tile = original.call(instance, pos);
        } catch (Exception e) {
            GTLog.logger.warn("Failed to load TE at {}", pos);
        }

        return tile;
    }
}
