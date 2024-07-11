package gregtech.api.graphnet.pipenet;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.MultiNetNodeHandler;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.alg.INetAlgorithm;
import gregtech.api.graphnet.graph.INetGraph;
import gregtech.api.graphnet.logic.MultiNetCountLogic;
import gregtech.api.graphnet.worldnet.WorldNet;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import net.minecraft.util.math.BlockPos;

import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class WorldPipeNet extends WorldNet {

    public static final int MULTI_NET_TIMEOUT = 10;

    private static final Object2ObjectOpenHashMap<Integer, Set<WeakReference<WorldPipeNet>>> dimensionNets = new Object2ObjectOpenHashMap<>();

    public WorldPipeNet(String name, Function<IGraphNet, INetAlgorithm> algorithmBuilder,
                        Function<IGraphNet, INetGraph> graphBuilder) {
        super(name, algorithmBuilder, graphBuilder);
        dimensionNets.compute(getDimension(), (k, v) -> {
            if (v == null) v = new ObjectOpenHashSet<>();
            v.add(new WeakReference<>(this));
            return v;
        });
    }

    public WorldPipeNet(String name, Function<IGraphNet, INetAlgorithm> algorithmBuilder, boolean directed) {
        super(name, algorithmBuilder, directed);
        dimensionNets.compute(getDimension(), (k, v) -> {
            if (v == null) v = new ObjectOpenHashSet<>();
            v.add(new WeakReference<>(this));
            return v;
        });
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
                    if (n.handler != node.handler) {
                        if (node.handler == null) {
                            // n handler is not null
                            node.handler = n.handler;
                            return;
                        }
                    } else if (n.handler == null) {
                        // both handlers are null
                        node.handler = new MultiNetNodeHandler(node.getData().getLogicEntryDefaultable(
                                MultiNetCountLogic.INSTANCE).getValue(), MULTI_NET_TIMEOUT);
                    }
                    // n handler does not match cast handler
                    n.handler = node.handler;
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
