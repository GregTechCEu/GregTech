package gregtech.common.pipelike.handlers;

import gregtech.api.graphnet.pipenet.IPipeNetNodeHandler;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.physical.IPipeStructure;
import gregtech.common.pipelike.block.laser.LaserStructure;
import gregtech.common.pipelike.net.laser.WorldLaserNet;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Collections;

public final class LaserNetHandler implements IPipeNetNodeHandler {

    public static final LaserNetHandler INSTANCE = new LaserNetHandler();

    @Override
    public void addToNets(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof LaserStructure) {
            WorldLaserNet.getWorldNet(world).getOrCreateNode(pos);
        }
    }

    @Override
    public Collection<WorldPipeNetNode> getFromNets(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof LaserStructure) {
            WorldPipeNetNode node = WorldLaserNet.getWorldNet(world).getNode(pos);
            if (node != null) return Collections.singletonList(node);
        }
        return Collections.emptyList();
    }

    @Override
    public void removeFromNets(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof LaserStructure) {
            WorldLaserNet net = WorldLaserNet.getWorldNet(world);
            WorldPipeNetNode node = net.getNode(pos);
            if (node != null) net.removeNode(node);
        }
    }
}
