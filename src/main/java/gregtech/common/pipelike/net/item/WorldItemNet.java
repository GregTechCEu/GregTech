package gregtech.common.pipelike.net.item;

import gregtech.api.cover.Cover;
import gregtech.api.cover.filter.CoverWithItemFilter;
import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.edge.NetFlowEdge;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.pipenet.WorldPipeNet;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.predicate.BlockedPredicate;
import gregtech.api.graphnet.pipenet.predicate.FilterPredicate;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;
import gregtech.common.covers.ItemFilterMode;
import gregtech.common.covers.ManualImportExportMode;
import gregtech.common.pipelike.net.fluid.WorldFluidNet;

import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class WorldItemNet extends WorldPipeNet implements FlowWorldPipeNetPath.Provider {

    public static final Capability<?>[] CAPABILITIES = new Capability[] {
            CapabilityItemHandler.ITEM_HANDLER_CAPABILITY };

    private static final String DATA_ID_BASE = "gregtech.world_item_net";

    public static @NotNull WorldItemNet getWorldNet(World world) {
        final String DATA_ID = getDataID(DATA_ID_BASE, world);
        WorldItemNet net = (WorldItemNet) world.loadData(WorldItemNet.class, DATA_ID);
        if (net == null) {
            net = new WorldItemNet(DATA_ID);
            world.setData(DATA_ID, net);
        }
        net.setWorld(world);
        return net;
    }

    public WorldItemNet(String name) {
        super(name, true);
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
        if (a instanceof CoverWithItemFilter filter) {
            if (filter.getManualMode() == ManualImportExportMode.DISABLED) {
                edge.getPredicateHandler().clearPredicates();
                edge.getPredicateHandler().setPredicate(BlockedPredicate.TYPE.getNew());
                return;
            } else if (filter.getManualMode() == ManualImportExportMode.FILTERED &&
                    filter.getFilterMode() != ItemFilterMode.FILTER_INSERT) {
                        predicate = FilterPredicate.TYPE.getNew();
                        predicate.setSourceFilter(filter.getItemFilter());
                    }
        }
        if (b instanceof CoverWithItemFilter filter) {
            if (filter.getManualMode() == ManualImportExportMode.DISABLED) {
                edge.getPredicateHandler().clearPredicates();
                edge.getPredicateHandler().setPredicate(BlockedPredicate.TYPE.getNew());
                return;
            } else if (filter.getManualMode() == ManualImportExportMode.FILTERED &&
                    filter.getFilterMode() != ItemFilterMode.FILTER_EXTRACT) {
                        if (predicate == null) predicate = FilterPredicate.TYPE.getNew();
                        predicate.setTargetFilter(filter.getItemFilter());
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
        return net instanceof WorldFluidNet;
    }

    @Override
    public Capability<?>[] getTargetCapabilities() {
        return CAPABILITIES;
    }

    @Override
    public IPipeCapabilityObject[] getNewCapabilityObjects(WorldPipeNetNode node) {
        return new IPipeCapabilityObject[] { new ItemCapabilityObject(this, node) };
    }

    @Override
    public Iterator<FlowWorldPipeNetPath> getPaths(WorldPipeNetNode node, IPredicateTestObject testObject,
                                                   @Nullable SimulatorKey simulator, long queryTick) {
        return backer.getPaths(node, 0, FlowWorldPipeNetPath.MAPPER, testObject, simulator, queryTick);
    }

    @Override
    public @NotNull NetFlowEdge getNewEdge() {
        return new NetFlowEdge(2, 5);
    }

    @Override
    public int getNetworkID() {
        return 2;
    }
}
