package gregtech.api.graphnet.servernet;

import gregtech.api.graphnet.GraphNetBacker;
import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.alg.AlgorithmBuilder;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.graph.INetGraph;
import gregtech.api.graphnet.graph.NetDirectedGraph;
import gregtech.api.graphnet.graph.NetUndirectedGraph;
import gregtech.api.graphnet.logic.WeightFactorLogic;
import gregtech.api.util.DimensionPos;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.WorldSavedData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Unused demonstration net that would allow for edges bridging dimensions inside the graph representation.
 */
@SuppressWarnings("unused")
public abstract class ServerNet extends WorldSavedData implements IGraphNet {

    protected final GraphNetBacker backer;

    public ServerNet(String name, Function<IGraphNet, INetGraph> graphBuilder,
                     AlgorithmBuilder... algorithmBuilders) {
        super(name);
        this.backer = new GraphNetBacker(this, graphBuilder.apply(this), algorithmBuilders);
    }

    public ServerNet(String name, boolean directed, AlgorithmBuilder... algorithmBuilders) {
        this(name, directed ? NetDirectedGraph.standardBuilder() : NetUndirectedGraph.standardBuilder(),
                algorithmBuilders);
    }

    @Override
    public void addNode(@NotNull NetNode node) {
        nodeClassCheck(node);
        this.backer.addNode(node);
    }

    public @Nullable ServerNetNode getNode(@NotNull DimensionPos equivalencyData) {
        return (ServerNetNode) getNode((Object) equivalencyData);
    }

    @Override
    public @Nullable NetNode getNode(@NotNull Object equivalencyData) {
        return backer.getNode(equivalencyData);
    }

    @Override
    public void removeNode(@NotNull NetNode node) {
        nodeClassCheck(node);
        this.backer.removeNode(node);
    }

    @Override
    public NetEdge addEdge(@NotNull NetNode source, @NotNull NetNode target, boolean bothWays) {
        nodeClassCheck(source);
        nodeClassCheck(target);
        double weight = source.getData().getLogicEntryDefaultable(WeightFactorLogic.TYPE).getValue() +
                target.getData().getLogicEntryDefaultable(WeightFactorLogic.TYPE).getValue();
        NetEdge edge = backer.addEdge(source, target, weight);
        if (bothWays) {
            if (this.getGraph().isDirected()) {
                backer.addEdge(target, source, weight);
            }
            return null;
        } else return edge;
    }

    @Override
    public @Nullable NetEdge getEdge(@NotNull NetNode source, @NotNull NetNode target) {
        nodeClassCheck(source);
        nodeClassCheck(target);
        return backer.getEdge(source, target);
    }

    @Override
    public void removeEdge(@NotNull NetNode source, @NotNull NetNode target, boolean bothWays) {
        nodeClassCheck(source);
        nodeClassCheck(target);
        this.backer.removeEdge(source, target);
        if (bothWays && this.getGraph().isDirected()) {
            this.backer.removeEdge(target, source);
        }
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound nbt) {
        backer.readFromNBT(nbt);
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        return backer.writeToNBT(compound);
    }

    @Override
    public GraphNetBacker getBacker() {
        return backer;
    }

    @Override
    public Class<? extends NetNode> getNodeClass() {
        return ServerNetNode.class;
    }

    @Override
    public @NotNull ServerNetNode getNewNode() {
        return new ServerNetNode(this);
    }
}
