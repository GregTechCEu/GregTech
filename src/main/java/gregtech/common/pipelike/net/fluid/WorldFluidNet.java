package gregtech.common.pipelike.net.fluid;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.alg.DynamicWeightsShortestPathsAlgorithm;
import gregtech.api.graphnet.edge.NetFlowEdge;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.pipenet.FlowWorldPipeNetPath;
import gregtech.api.graphnet.pipenet.WorldPipeNet;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;
import gregtech.common.pipelike.net.item.WorldItemNet;

import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class WorldFluidNet extends WorldPipeNet implements FlowWorldPipeNetPath.Provider {

    public static final Capability<?>[] CAPABILITIES = new Capability[] {
            CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY };

    private static final String DATA_ID_BASE = "gregtech.world_fluid_net";

    public static @NotNull WorldFluidNet getWorldNet(World world) {
        final String DATA_ID = getDataID(DATA_ID_BASE, world);
        WorldFluidNet net = (WorldFluidNet) world.loadData(WorldFluidNet.class, DATA_ID);
        if (net == null) {
            net = new WorldFluidNet(DATA_ID);
            world.setData(DATA_ID, net);
            net.setWorld(world);
        }
        return net;
    }

    public WorldFluidNet(String name) {
        super(name, false, DynamicWeightsShortestPathsAlgorithm::new);
    }

    @Override
    public boolean usesDynamicWeights(int algorithmID) {
        return true;
    }

    @Override
    public boolean clashesWith(IGraphNet net) {
        return net instanceof WorldItemNet;
    }

    @Override
    public Capability<?>[] getTargetCapabilities() {
        return CAPABILITIES;
    }

    @Override
    public IPipeCapabilityObject[] getNewCapabilityObjects(WorldPipeNetNode node) {
        return new IPipeCapabilityObject[] { new FluidCapabilityObject(this, node) };
    }

    @Override
    public Iterator<FlowWorldPipeNetPath> getPaths(WorldPipeNetNode node, IPredicateTestObject testObject,
                                                   @Nullable SimulatorKey simulator, long queryTick) {
        return backer.getPaths(node, 0, FlowWorldPipeNetPath.MAPPER, testObject, simulator, queryTick);
    }

    @Override
    public @NotNull NetFlowEdge getNewEdge() {
        return new NetFlowEdge(10);
    }
}
