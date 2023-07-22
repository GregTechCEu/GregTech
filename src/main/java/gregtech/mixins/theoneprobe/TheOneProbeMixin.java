package gregtech.mixins.theoneprobe;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import gregtech.api.block.machines.BlockMachine;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.network.PacketGetInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// TODO, unsure if we actually need this one
@Mixin(PacketGetInfo.class)
@SuppressWarnings({"unused", "deprecation"})
public class TheOneProbeMixin {

    @ModifyExpressionValue(method = "getProbeInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;"))
    private static IBlockState getActualState(IBlockState originalState, EntityPlayer player, ProbeMode mode, World world, BlockPos blockPos, EnumFacing sideHit, Vec3d hitVec, ItemStack pickBlock) {
        IBlockState modifiedState = world.getBlockState(blockPos);
        if (modifiedState.getBlock() instanceof BlockMachine) {
            return modifiedState.getBlock().getActualState(modifiedState, world, blockPos);
        }
        return originalState;
    }
}
