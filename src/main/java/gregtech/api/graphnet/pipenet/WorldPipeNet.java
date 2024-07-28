package gregtech.api.graphnet.pipenet;

import gregtech.api.cover.Cover;
import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.MultiNodeHelper;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.alg.INetAlgorithm;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.graph.INetGraph;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.graphnet.pipenet.predicate.ShutterPredicate;
import gregtech.api.graphnet.worldnet.WorldNet;
import gregtech.api.util.IDirtyNotifiable;
import gregtech.common.covers.CoverShutter;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class WorldPipeNet extends WorldNet {

    public static final int MULTI_NET_TIMEOUT = 10;

    private static final Object2ObjectOpenHashMap<Integer, Set<WeakReference<WorldPipeNet>>> dimensionNets = new Object2ObjectOpenHashMap<>();

    @SafeVarargs
    public WorldPipeNet(String name, Function<IGraphNet, INetGraph> graphBuilder,
                        Function<IGraphNet, INetAlgorithm>... algorithmBuilders) {
        super(name, graphBuilder, algorithmBuilders);
    }

    @SafeVarargs
    public WorldPipeNet(String name, boolean directed, Function<IGraphNet, INetAlgorithm>... algorithmBuilders) {
        super(name, directed, algorithmBuilders);
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        dimensionNets.compute(getDimension(), (k, v) -> {
            if (v == null) v = new ObjectOpenHashSet<>();
            v.add(new WeakReference<>(this));
            return v;
        });
    }

    /**
     * Called when a PipeTileEntity is marked dirty through {@link IDirtyNotifiable#markAsDirty()}, which is generally
     * when the state of its covers is changed.
     * 
     * @param tile the tile marked dirty.
     * @param node the associated node.
     */
    public void updatePredication(@NotNull WorldPipeNetNode node, @NotNull PipeTileEntity tile) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            PipeTileEntity neighbor = tile.getPipeNeighbor(facing, false);
            if (neighbor == null) continue;
            WorldPipeNetNode neighborNode = this.getNode(neighbor.getPos());
            if (neighborNode == null) continue;
            NetEdge edge = getEdge(node, neighborNode);
            if (edge == null) continue;
            predicateEdge(edge, node, tile.getCoverHolder().getCoverAtSide(facing), neighborNode,
                    neighbor.getCoverHolder().getCoverAtSide(facing.getOpposite()));
        }
    }

    /**
     * Preferred method to override if your net has custom predication rules. If the net is directed,
     * this method will <b>not</b> be called twice, so special handling for directedness is needed.
     * 
     * @param source      the source of the edge.
     * @param coverSource the cover on the source facing the target.
     * @param target      the target of the edge.
     * @param coverTarget the cover on the target facing the source.
     */
    public void predicateEdge(@NotNull NetEdge edge, @NotNull WorldPipeNetNode source, @Nullable Cover coverSource,
                              @NotNull WorldPipeNetNode target, @Nullable Cover coverTarget) {
        edge.getPredicateHandler().clearPredicates();
        shutterify(edge, coverSource, coverTarget);
        if (getGraph().isDirected()) {
            edge = getEdge(target, source);
            if (edge == null) return;
            edge.getPredicateHandler().clearPredicates();
            shutterify(edge, coverSource, coverTarget);
        }
    }

    protected final void shutterify(@NotNull NetEdge edge, @Nullable Cover a, @Nullable Cover b) {
        if (a instanceof CoverShutter s && !s.canPipePassThrough()) {
            edge.getPredicateHandler().setPredicate(ShutterPredicate.INSTANCE);
            return;
        }
        if (b instanceof CoverShutter s && !s.canPipePassThrough()) {
            edge.getPredicateHandler().setPredicate(ShutterPredicate.INSTANCE);
        }
    }

    public abstract Capability<?>[] getTargetCapabilities();

    public abstract IPipeCapabilityObject[] getNewCapabilityObjects(WorldPipeNetNode node);

    @Override
    public @NotNull WorldPipeNetNode getOrCreateNode(@NotNull BlockPos pos) {
        return (WorldPipeNetNode) super.getOrCreateNode(pos);
    }

    @Override
    public @Nullable WorldPipeNetNode getNode(@NotNull BlockPos equivalencyData) {
        return (WorldPipeNetNode) getNode((Object) equivalencyData);
    }

    protected Stream<@NotNull WorldPipeNet> sameDimensionNetsStream() {
        ObjectArrayList<WeakReference<WorldPipeNet>> expired = new ObjectArrayList<>();
        Stream<@NotNull WorldPipeNet> returnable = dimensionNets.get(this.getDimension()).stream()
                .map(ref -> {
                    WorldPipeNet net = ref.get();
                    if (net == null) expired.add(ref);
                    return net;
                }).filter(Objects::nonNull);
        expired.forEach(dimensionNets.get(this.getDimension())::remove);
        return returnable;
    }

    public void synchronizeNode(WorldPipeNetNode node) {
        // basically, if another net has a node in the exact same position, then we know it's the same block.
        // thus, we set up a multi net node handler for the node in order to manage the overlap
        // this is disk-load safe, since this method is called during nbt deserialization.
        sameDimensionNetsStream().map(n -> n.getNode(node.getEquivalencyData())).filter(Objects::nonNull)
                .forEach(n -> {
                    if (n.overlapHelper != node.overlapHelper) {
                        if (node.overlapHelper == null) {
                            // n handler is not null
                            node.overlapHelper = n.overlapHelper;
                            n.overlapHelper.addNode(node);
                            return;
                        }
                    } else if (n.overlapHelper == null) {
                        // both handlers are null
                        node.overlapHelper = new MultiNodeHelper(MULTI_NET_TIMEOUT);
                    }
                    // n handler does not match cast handler
                    n.overlapHelper = node.overlapHelper;
                    n.overlapHelper.addNode(node);
                });
    }

    public static String getDataID(final String baseID, final World world) {
        if (world == null || world.isRemote)
            throw new RuntimeException("WorldPipeNets should only be created on the server!");
        int dimension = world.provider.getDimension();
        return baseID + '.' + dimension;
    }

    @Override
    public final Class<? extends NetNode> getNodeClass() {
        return WorldPipeNetNode.class;
    }

    @Override
    public final @NotNull WorldPipeNetNode getNewNode() {
        return new WorldPipeNetNode(this);
    }
}
