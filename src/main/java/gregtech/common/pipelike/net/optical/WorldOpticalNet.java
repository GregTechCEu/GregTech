package gregtech.common.pipelike.net.optical;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.graphnet.alg.DynamicWeightsShortestPathsAlgorithm;
import gregtech.api.graphnet.alg.ShortestPathsAlgorithm;
import gregtech.api.graphnet.alg.SinglePathAlgorithm;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.pipenet.BasicWorldPipeNetPath;
import gregtech.api.graphnet.pipenet.WorldPipeNet;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import gregtech.common.pipelike.net.item.WorldItemNet;

import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import net.minecraftforge.items.CapabilityItemHandler;

import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class WorldOpticalNet extends WorldPipeNet implements BasicWorldPipeNetPath.Provider {

    public static final Capability<?>[] CAPABILITIES = new Capability[] { GregtechTileCapabilities.CAPABILITY_COMPUTATION_PROVIDER, GregtechTileCapabilities.CAPABILITY_DATA_ACCESS };

    private static final String DATA_ID_BASE = "gregtech.world_optical_net";

    public static WorldOpticalNet getWorldNet(World world) {
        final String DATA_ID = getDataID(DATA_ID_BASE, world);
        WorldOpticalNet net = (WorldOpticalNet) world.loadData(WorldOpticalNet.class, DATA_ID);
        if (net == null) {
            net = new WorldOpticalNet(DATA_ID);
            world.setData(DATA_ID, net);
        }
        return net;
    }

    public WorldOpticalNet(String name) {
        super(name, false, SinglePathAlgorithm::new);
    }

    @Override
    public Iterator<BasicWorldPipeNetPath> getPaths(WorldPipeNetNode node, IPredicateTestObject testObject,
                                                    @Nullable SimulatorKey simulator, long queryTick) {
        return backer.getPaths(node, 0, BasicWorldPipeNetPath.MAPPER, testObject, simulator, queryTick);
    }

    @Override
    public Capability<?>[] getTargetCapabilities() {
        return CAPABILITIES;
    }

    @Override
    public IPipeCapabilityObject[] getNewCapabilityObjects(WorldPipeNetNode node) {
        return null; // TODO
    }
}
