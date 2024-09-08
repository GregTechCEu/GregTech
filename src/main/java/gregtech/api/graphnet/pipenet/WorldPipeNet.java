package gregtech.api.graphnet.pipenet;

import gregtech.api.cover.Cover;
import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.MultiNodeHelper;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.alg.AlgorithmBuilder;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.graph.INetGraph;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.graphnet.pipenet.predicate.BlockedPredicate;
import gregtech.api.graphnet.predicate.EdgePredicate;
import gregtech.api.graphnet.worldnet.WorldPosNet;
import gregtech.api.util.IDirtyNotifiable;
import gregtech.api.util.reference.WeakHashSet;
import gregtech.common.covers.CoverShutter;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class WorldPipeNet extends WorldPosNet {

    public static final int MULTI_NET_TIMEOUT = 10;

    private static final Int2ObjectOpenHashMap<Set<WorldPipeNet>> dimensionNets = new Int2ObjectOpenHashMap<>();

    public WorldPipeNet(String name, Function<IGraphNet, INetGraph> graphBuilder,
                        AlgorithmBuilder... algorithmBuilders) {
        super(name, graphBuilder, algorithmBuilders);
    }

    public WorldPipeNet(String name, boolean directed, AlgorithmBuilder... algorithmBuilders) {
        super(name, directed, algorithmBuilders);
    }

    @Override
    public void setWorld(World world) {
        if (getWorld() == world) return;
        super.setWorld(world);
        dimensionNets.compute(getDimension(), (k, v) -> {
            if (v == null) v = new WeakHashSet<>();
            v.add(this);
            return v;
        });
    }

    public final void updatePredication(@NotNull WorldPipeNetNode node, @NotNull PipeTileEntity tile) {
        if (supportsPredication()) updatePredicationInternal(node, tile);
    }

    /**
     * Called when a PipeTileEntity is marked dirty through {@link IDirtyNotifiable#markAsDirty()}, which is generally
     * when the state of its covers is changed.
     * 
     * @param tile the tile marked dirty.
     * @param node the associated node.
     */
    protected void updatePredicationInternal(@NotNull WorldPipeNetNode node, @NotNull PipeTileEntity tile) {
        boolean dirty = false;
        for (EnumFacing facing : EnumFacing.VALUES) {
            PipeTileEntity neighbor = tile.getPipeNeighbor(facing, false);
            if (neighbor == null) continue;
            WorldPipeNetNode neighborNode = this.getNode(neighbor.getPos());
            if (neighborNode == null) continue;
            NetEdge edge = getEdge(node, neighborNode);
            if (edge == null) continue;
            dirty |= predicateEdge(edge, node, tile.getCoverHolder().getCoverAtSide(facing), neighborNode,
                    neighbor.getCoverHolder().getCoverAtSide(facing.getOpposite()));
        }
        if (dirty) markDirty();
    }

    /**
     * Preferred method to override if your net has complex custom predication rules. If the net is directed,
     * this method will <b>not</b> be called twice, so special handling for directedness is needed.
     * 
     * @param source      the source of the edge.
     * @param coverSource the cover on the source facing the target.
     * @param target      the target of the edge.
     * @param coverTarget the cover on the target facing the source.
     * @return whether the predication state has changed and this net needs to be marked dirty.
     */
    protected boolean predicateEdge(@NotNull NetEdge edge, @NotNull WorldPipeNetNode source,
                                    @Nullable Cover coverSource,
                                    @NotNull WorldPipeNetNode target, @Nullable Cover coverTarget) {
        Map<String, EdgePredicate<?, ?>> prevValue = new Object2ObjectOpenHashMap<>(
                edge.getPredicateHandler().getPredicateSet());
        edge.getPredicateHandler().clearPredicates();
        coverPredication(edge, coverSource, coverTarget);
        boolean edgeSame = !prevValue.equals(edge.getPredicateHandler().getPredicateSet());
        if (getGraph().isDirected()) {
            edge = getEdge(target, source);
            if (edge == null) return edgeSame;
            if (edgeSame) {
                prevValue.clear();
                prevValue.putAll(edge.getPredicateHandler().getPredicateSet());
            }
            edge.getPredicateHandler().clearPredicates();
            coverPredication(edge, coverSource, coverTarget);
            if (edgeSame) {
                edgeSame = !prevValue.equals(edge.getPredicateHandler().getPredicateSet());
            }
        }
        return edgeSame;
    }

    /**
     * Preferred method to override if your net has custom predication rules that only depend on covers.
     * If the net is directed, this method <b>will</b> be called twice, so no special handling for directedness is
     * needed.
     *
     * @param edge the edge to predicate
     * @param a    the cover on the source of the edge
     * @param b    the cover on the sink of the edge
     */
    protected void coverPredication(@NotNull NetEdge edge, @Nullable Cover a, @Nullable Cover b) {
        if (a instanceof CoverShutter aS && aS.isWorkingEnabled() ||
                b instanceof CoverShutter bS && bS.isWorkingEnabled()) {
            edge.getPredicateHandler().setPredicate(BlockedPredicate.INSTANCE);
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
        return dimensionNets.getOrDefault(this.getDimension(), Collections.emptySet()).stream()
                .filter(Objects::nonNull);
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
                        node.overlapHelper.addNode(n);
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

    /**
     * Get the network ID for this net. Must be unique and deterministic between server and client, but can change
     * between mod versions.
     *
     * @return the net's network id.
     */
    public abstract int getNetworkID();

    @Override
    public final Class<? extends NetNode> getNodeClass() {
        return WorldPipeNetNode.class;
    }

    @Override
    public final @NotNull WorldPipeNetNode getNewNode() {
        return new WorldPipeNetNode(this);
    }

    @Contract(value = " -> new", pure = true)
    public static <T> @NotNull Object2ObjectOpenCustomHashMap<WorldPipeNetNode, T> getSensitiveHashMap() {
        return new Object2ObjectOpenCustomHashMap<>(SensitiveStrategy.INSTANCE);
    }

    protected static class SensitiveStrategy implements Hash.Strategy<WorldPipeNetNode> {

        public static final SensitiveStrategy INSTANCE = new SensitiveStrategy();

        @Override
        public int hashCode(WorldPipeNetNode o) {
            return Objects.hash(o, o.getNet());
        }

        @Override
        public boolean equals(WorldPipeNetNode a, WorldPipeNetNode b) {
            return a.equals(b) && a.getNet().equals(b.getNet());
        }
    }
}