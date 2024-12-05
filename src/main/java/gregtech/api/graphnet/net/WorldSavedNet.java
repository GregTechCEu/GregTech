package gregtech.api.graphnet.net;

import gregtech.api.graphnet.GraphNetBacker;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.graph.INetGraph;
import gregtech.api.graphnet.graph.NetDirectedGraph;
import gregtech.api.graphnet.graph.NetUndirectedGraph;
import gregtech.api.graphnet.logic.WeightFactorLogic;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.WorldSavedData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public abstract class WorldSavedNet extends WorldSavedData implements IGraphNet {

    protected final GraphNetBacker backer;

    public WorldSavedNet(String name, @NotNull Function<IGraphNet, INetGraph> graphBuilder) {
        super(name);
        this.backer = new GraphNetBacker(this, graphBuilder.apply(this));
    }

    public WorldSavedNet(String name, boolean directed) {
        this(name, directed ? NetDirectedGraph.standardBuilder() : NetUndirectedGraph.standardBuilder());
    }

    @Override
    public void addNode(@NotNull NetNode node) {
        this.backer.addNode(node);
    }

    @Override
    public @Nullable NetNode getNode(@NotNull Object equivalencyData) {
        return backer.getNode(equivalencyData);
    }

    @Override
    public void removeNode(@NotNull NetNode node) {
        this.backer.removeNode(node);
    }

    @Override
    public NetEdge addEdge(@NotNull NetNode source, @NotNull NetNode target, boolean bothWays) {
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
        return backer.getEdge(source, target);
    }

    @Override
    public void removeEdge(@NotNull NetNode source, @NotNull NetNode target, boolean bothWays) {
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
}
