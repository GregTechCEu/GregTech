package gregtech.api.graphnet.pipenet;

import gregtech.api.graphnet.pipenet.block.IPipeStructure;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IPipeNetNodeHandler {
    void addToNets(World world, BlockPos pos, IPipeStructure structure);

    void removeFromNets(World world, BlockPos pos, IPipeStructure structure);
}
