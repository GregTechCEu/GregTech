package gregtech.common.pipelike.net.fluid;

import gregtech.api.cover.Cover;
import gregtech.api.cover.filter.CoverWithFluidFilter;
import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.alg.DynamicWeightsShortestPathsAlgorithm;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.edge.NetFlowEdge;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.pipenet.WorldPipeNet;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.predicate.BlockedPredicate;
import gregtech.api.graphnet.pipenet.predicate.FilterPredicate;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;
import gregtech.common.covers.FluidFilterMode;
import gregtech.common.covers.ManualImportExportMode;
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
        }
        net.setWorld(world);
        return net;
    }

    public WorldFluidNet(String name) {
        super(name, true, DynamicWeightsShortestPathsAlgorithm::new);
    }

    @Override
    public boolean supportsPredication() {
        return true;
    }

    @Override
    protected void coverPredication(@NotNull NetEdge edge, @Nullable Cover a, @Nullable Cover b) {
        super.coverPredication(edge, a, b);
        if (edge.getPredicateHandler().hasPredicate(BlockedPredicate.TYPE)) return;
        FilterPredicate predicate = null;
        if (a instanceof CoverWithFluidFilter filter) {
            if (filter.getManualMode() == ManualImportExportMode.DISABLED) {
                edge.getPredicateHandler().clearPredicates();
                edge.getPredicateHandler().setPredicate(BlockedPredicate.TYPE.getNew());
                return;
            } else if (filter.getManualMode() == ManualImportExportMode.FILTERED &&
                    filter.getFilterMode() != FluidFilterMode.FILTER_FILL) {
                        predicate = FilterPredicate.TYPE.getNew();
                        predicate.setSourceFilter(filter.getFluidFilter());
                    }
        }
        if (b instanceof CoverWithFluidFilter filter) {
            if (filter.getManualMode() == ManualImportExportMode.DISABLED) {
                edge.getPredicateHandler().clearPredicates();
                edge.getPredicateHandler().setPredicate(BlockedPredicate.TYPE.getNew());
                return;
            } else if (filter.getManualMode() == ManualImportExportMode.FILTERED &&
                    filter.getFilterMode() != FluidFilterMode.FILTER_DRAIN) {
                        if (predicate == null) predicate = FilterPredicate.TYPE.getNew();
                        predicate.setTargetFilter(filter.getFluidFilter());
                    }
        }
        if (predicate != null) edge.getPredicateHandler().setPredicate(predicate);
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

    @Override
    public int getNetworkID() {
        return 1;
    }
}
