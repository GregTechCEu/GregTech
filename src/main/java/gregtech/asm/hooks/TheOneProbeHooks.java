package gregtech.asm.hooks;

import gregtech.api.block.machines.BlockMachine;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@SuppressWarnings("unused")
public class TheOneProbeHooks {

    @SuppressWarnings("deprecation")
    public static IBlockState getActualState(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockMachine) {
            state = state.getBlock().getActualState(state, world, pos);
        }
        return state;
    }
}
