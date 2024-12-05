package gregtech.api.graphnet.pipenet;

import gregtech.api.cover.Cover;
import gregtech.api.cover.CoverableView;
import gregtech.api.graphnet.GraphClassType;
import gregtech.api.graphnet.net.IGraphNet;
import gregtech.api.graphnet.MultiNodeHelper;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.graph.INetGraph;
import gregtech.api.graphnet.net.WorldSavedNet;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.physical.tile.PipeCapabilityWrapper;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.graphnet.pipenet.predicate.BlockedPredicate;
import gregtech.api.graphnet.predicate.EdgePredicate;
import gregtech.api.graphnet.predicate.NetPredicateType;
import gregtech.api.graphnet.traverse.iter.EdgeDirection;
import gregtech.api.util.GTUtility;
import gregtech.api.util.IDirtyNotifiable;
import gregtech.api.util.reference.WeakHashSet;
import gregtech.common.covers.CoverShutter;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;

import net.minecraft.nbt.NBTTagCompound;
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
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class WorldPipeNet extends WorldSavedNet {

    public static final int MULTI_NET_TIMEOUT = 10;

    private static final Int2ObjectOpenHashMap<Set<WorldPipeNet>> dimensionNets = new Int2ObjectOpenHashMap<>();

    private World world;
    private int fallbackDimensionID;

    public WorldPipeNet(String name, Function<IGraphNet, INetGraph> graphBuilder) {
        super(name, graphBuilder);
    }

    public WorldPipeNet(String name, boolean directed) {
        super(name, directed);
    }

    public void setWorld(World world) {
        if (getWorld() == world) return;
        this.world = world;
        dimensionNets.compute(getDimension(), (k, v) -> {
            if (v == null) v = new WeakHashSet<>();
            v.add(this);
            return v;
        });
    }

    public World getWorld() {
        return world;
    }

    protected int getDimension() {
        if (world == null) return fallbackDimensionID;
        else return world.provider.getDimension();
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound nbt) {
        fallbackDimensionID = nbt.getInteger("Dimension");
        super.readFromNBT(nbt);
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        compound.setInteger("Dimension", getDimension());
        return super.writeToNBT(compound);
    }

    /**
     * Called when a PipeTileEntity is marked dirty through {@link IDirtyNotifiable#markAsDirty()}, which is generally
     * when the state of its covers is changed.
     *
     * @param tile the tile marked dirty.
     * @param node the associated node.
     */
    public void updatePredication(@NotNull WorldPipeNode node, @NotNull PipeTileEntity tile) {
        boolean dirty = false;
        for (NetEdge edge : getBacker().getTouchingEdges(node, EdgeDirection.ALL)) {
            NetNode neighbor = edge.getOppositeNode(node);
            if (neighbor == null) continue;
            Cover cNode = null;
            Cover cNeighbor = null;
            if (neighbor instanceof NodeWithFacingToOthers n) {
                EnumFacing facing = n.getFacingToOther(node);
                if (facing != null) {
                    cNode = node.getTileEntity().getCoverHolder().getCoverAtSide(facing.getOpposite());
                    CoverableView view;
                    if (neighbor instanceof NodeWithCovers c && (view = c.getCoverableView()) != null) {
                        cNeighbor = view.getCoverAtSide(facing);
                    }
                }
            }
            dirty |= predicateEdge(edge, node, cNode, neighbor, cNeighbor);
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
    protected boolean predicateEdge(@NotNull NetEdge edge, @NotNull NetNode source,
                                    @Nullable Cover coverSource,
                                    @NotNull NetNode target, @Nullable Cover coverTarget) {
        Map<NetPredicateType<?>, EdgePredicate<?, ?>> prevValue = new Object2ObjectOpenHashMap<>(
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
            edge.getPredicateHandler().setPredicate(BlockedPredicate.TYPE.getNew());
        }
    }

    public Capability<?>[] getTargetCapabilities() {
        return null;
    }

    public IPipeCapabilityObject[] getNewCapabilityObjects(WorldPipeNode node) {
        return null;
    }

    public abstract PipeCapabilityWrapper buildCapabilityWrapper(@NotNull PipeTileEntity owner, @NotNull WorldPipeNode node);

    @Override
    public @NotNull GraphClassType<? extends NetNode> getDefaultNodeType() {
        return WorldPipeNode.TYPE;
    }

    public @Nullable WorldPipeNode getNode(@NotNull BlockPos equivalencyData) {
        return (WorldPipeNode) getNode((Object) equivalencyData);
    }

    protected Stream<@NotNull WorldPipeNet> sameDimensionNetsStream() {
        return dimensionNets.getOrDefault(this.getDimension(), Collections.emptySet()).stream()
                .filter(Objects::nonNull);
    }

    public void synchronizeNode(WorldPipeNode node) {
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

    @Contract(value = " -> new", pure = true)
    public static <T> @NotNull Object2ObjectOpenCustomHashMap<NetNode, T> getSensitiveHashMap() {
        return new Object2ObjectOpenCustomHashMap<>(SensitiveStrategy.INSTANCE);
    }

    protected static class SensitiveStrategy implements Hash.Strategy<NetNode> {

        public static final SensitiveStrategy INSTANCE = new SensitiveStrategy();

        @Override
        public int hashCode(NetNode o) {
            return Objects.hash(o, o.getNet());
        }

        @Override
        public boolean equals(NetNode a, NetNode b) {
            return a.equals(b) && a.getNet().equals(b.getNet());
        }
    }
}
