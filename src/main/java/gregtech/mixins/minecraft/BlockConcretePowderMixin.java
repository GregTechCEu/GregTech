package gregtech.mixins.minecraft;

import gregtech.common.ConfigHolder;

import net.minecraft.block.BlockConcretePowder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockConcretePowder.class)
public class BlockConcretePowderMixin {

    @Inject(method = "tryTouchWater", at = @At("HEAD"), cancellable = true)
    public void disableConversion(World worldIn, BlockPos pos, IBlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (ConfigHolder.recipes.disableConcreteInWorld) {
            cir.setReturnValue(false);
        }
    }
}
