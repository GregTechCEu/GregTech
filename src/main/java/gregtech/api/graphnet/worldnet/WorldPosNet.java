package gregtech.api.graphnet.worldnet;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.alg.AlgorithmBuilder;
import gregtech.api.graphnet.graph.INetGraph;

import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public abstract class WorldPosNet extends WorldNet {

    public WorldPosNet(String name, @NotNull Function<IGraphNet, INetGraph> graphBuilder,
                       AlgorithmBuilder... algorithmBuilders) {
        super(name, graphBuilder, algorithmBuilders);
    }

    public WorldPosNet(String name, boolean directed, AlgorithmBuilder... algorithmBuilders) {
        super(name, directed, algorithmBuilders);
    }

    @NotNull
    public WorldPosNetNode getOrCreateNode(@NotNull BlockPos pos) {
        WorldPosNetNode node = getNode(pos);
        if (node != null) return node;
        node = getNewNode();
        node.setPos(pos);
        addNode(node);
        return node;
    }

    public @Nullable WorldPosNetNode getNode(@NotNull BlockPos equivalencyData) {
        return (WorldPosNetNode) getNode((Object) equivalencyData);
    }

    @Override
    public Class<? extends NetNode> getNodeClass() {
        return WorldPosNetNode.class;
    }

    @Override
    public @NotNull WorldPosNetNode getNewNode() {
        return new WorldPosNetNode(this);
    }
}
