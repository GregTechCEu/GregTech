package gregtech.common.pipelike.net.fluid;

import gregtech.api.cover.Cover;
import gregtech.api.cover.filter.CoverWithFluidFilter;
import gregtech.api.graphnet.net.IGraphNet;
import gregtech.api.graphnet.net.NetEdge;
import gregtech.api.graphnet.pipenet.WorldPipeNet;
import gregtech.api.graphnet.pipenet.WorldPipeNode;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.physical.tile.NodeManagingPCW;
import gregtech.api.graphnet.pipenet.physical.tile.PipeCapabilityWrapper;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.graphnet.pipenet.predicate.BlockedPredicate;
import gregtech.api.graphnet.pipenet.predicate.FilterPredicate;
import gregtech.common.covers.FluidFilterMode;
import gregtech.common.covers.ManualImportExportMode;
import gregtech.common.pipelike.net.item.WorldItemNet;

import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldFluidNet extends WorldPipeNet {

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
        super(name, true);
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
    public boolean clashesWith(IGraphNet net) {
        return net instanceof WorldItemNet;
    }

    @Override
    public PipeCapabilityWrapper buildCapabilityWrapper(@NotNull PipeTileEntity owner, @NotNull WorldPipeNode node) {
        Object2ObjectOpenHashMap<Capability<?>, IPipeCapabilityObject> map = new Object2ObjectOpenHashMap<>();
        map.put(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, new FluidCapabilityObject(node));
        return new NodeManagingPCW(owner, node, map, 0, 0);
    }

    public static int getBufferTicks() {
        return 10;
    }

    @Override
    public int getNetworkID() {
        return 1;
    }
}
