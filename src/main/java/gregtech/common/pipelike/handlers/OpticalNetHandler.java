package gregtech.common.pipelike.handlers;

import gregtech.api.graphnet.pipenet.IPipeNetNodeHandler;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.physical.IPipeStructure;

import gregtech.common.pipelike.block.optical.OpticalStructure;
import gregtech.common.pipelike.net.optical.WorldOpticalNet;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Collections;

public class OpticalNetHandler implements IPipeNetNodeHandler {

    public static final OpticalNetHandler INSTANCE = new OpticalNetHandler();

    @Override
    public void addToNets(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof OpticalStructure) {
            WorldOpticalNet.getWorldNet(world).getOrCreateNode(pos);
        }
    }

    @Override
    public Collection<WorldPipeNetNode> getFromNets(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof OpticalStructure) {
            WorldPipeNetNode node = WorldOpticalNet.getWorldNet(world).getNode(pos);
            if (node != null) return Collections.singletonList(node);
        }
        return Collections.emptyList();
    }

    @Override
    public void removeFromNets(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof OpticalStructure) {
            WorldOpticalNet net = WorldOpticalNet.getWorldNet(world);
            WorldPipeNetNode node = net.getNode(pos);
            if (node != null) net.removeNode(node);
        }
    }
}
