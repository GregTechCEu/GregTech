package gregtech.api.graphnet.pipenet;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.pipenet.physical.IPipeStructure;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Map;

public interface IPipeNetNodeHandler {
    void addToNets(World world, BlockPos pos, IPipeStructure structure);

    Collection<WorldPipeNetNode> getFromNets(World world, BlockPos pos, IPipeStructure structure);

    void removeFromNets(World world, BlockPos pos, IPipeStructure structure);
}
