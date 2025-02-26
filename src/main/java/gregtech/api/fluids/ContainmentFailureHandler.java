package gregtech.api.fluids;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public interface ContainmentFailureHandler {

    void handleFailure(World world, BlockPos failingBlock, FluidStack failingStack);

    void handleFailure(EntityPlayer failingPlayer, FluidStack failingStack);
}
