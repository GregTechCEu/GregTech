package gregtech.mixins.theoneprobe;

import gregtech.api.block.machines.BlockMachine;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.network.PacketGetInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Makes TheOneProbe call {@link IBlockState#getActualState(IBlockAccess, BlockPos)} when
 * looking at GT machines to show the correct harvest tool and level
 */
@Mixin(PacketGetInfo.class)
@SuppressWarnings("deprecation")
public class TheOneProbeMixin {

    @ModifyExpressionValue(method = "getProbeInfo",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;"))
    private static IBlockState getActualState(IBlockState originalState, EntityPlayer player, ProbeMode mode,
                                              World world, BlockPos blockPos, EnumFacing sideHit, Vec3d hitVec,
                                              ItemStack pickBlock) {
        IBlockState modifiedState = world.getBlockState(blockPos);
        if (modifiedState.getBlock() instanceof BlockMachine) {
            return modifiedState.getBlock().getActualState(modifiedState, world, blockPos);
        }
        return originalState;
    }
}
