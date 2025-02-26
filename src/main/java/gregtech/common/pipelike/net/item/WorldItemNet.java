package gregtech.common.pipelike.net.item;

import gregtech.api.cover.Cover;
import gregtech.api.cover.filter.CoverWithItemFilter;
import gregtech.api.graphnet.group.GroupData;
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
import gregtech.common.covers.ItemFilterMode;
import gregtech.common.covers.ManualImportExportMode;
import gregtech.common.pipelike.net.fluid.WorldFluidNet;

import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldItemNet extends WorldPipeNet {

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
    public boolean clashesWith(IGraphNet net) {
        return net instanceof WorldFluidNet;
    }

    @Override
    public PipeCapabilityWrapper buildCapabilityWrapper(@NotNull PipeTileEntity owner, @NotNull WorldPipeNode node) {
        Object2ObjectOpenHashMap<Capability<?>, IPipeCapabilityObject> map = new Object2ObjectOpenHashMap<>();
        map.put(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, new ItemCapabilityObject(node));
        return new NodeManagingPCW(owner, node, map, 0, 0);
    }

    public static int getBufferTicks() {
        return 10;
    }

    @Override
    public int getNetworkID() {
        return 2;
    }

    @Override
    public @Nullable GroupData getBlankGroupData() {
        return new ItemNetworkViewGroupData();
    }
}
