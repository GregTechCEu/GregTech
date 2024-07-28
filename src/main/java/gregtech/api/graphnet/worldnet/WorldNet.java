package gregtech.api.graphnet.worldnet;

import gregtech.api.graphnet.GraphNetBacker;
import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.alg.INetAlgorithm;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.graph.INetGraph;
import gregtech.api.graphnet.graph.NetDirectedGraph;
import gregtech.api.graphnet.graph.NetUndirectedGraph;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.logic.WeightFactorLogic;
import gregtech.api.graphnet.path.GenericGraphNetPath;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Function;

public abstract class WorldNet extends WorldSavedData implements IGraphNet, GenericGraphNetPath.Provider {

    protected final GraphNetBacker backer;
    private World world;

    @SafeVarargs
    public WorldNet(String name, Function<IGraphNet, INetGraph> graphBuilder,
                    Function<IGraphNet, INetAlgorithm>... algorithmBuilders) {
        super(name);
        this.backer = new GraphNetBacker(this, graphBuilder.apply(this), algorithmBuilders);
    }

    @SafeVarargs
    public WorldNet(String name, boolean directed, Function<IGraphNet, INetAlgorithm>... algorithmBuilders) {
        this(name, directed ? NetDirectedGraph.standardBuilder() : NetUndirectedGraph.standardBuilder(),
                algorithmBuilders);
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    @Override
    public Iterator<GenericGraphNetPath> getPaths(NetNode node, IPredicateTestObject testObject,
                                                  @Nullable SimulatorKey simulator, long queryTick) {
        nodeClassCheck(node);
        return backer.getPaths(node, 0, GenericGraphNetPath.MAPPER, testObject, simulator, queryTick);
    }

    @NotNull
    public WorldNetNode getOrCreateNode(@NotNull BlockPos pos) {
        WorldNetNode node = getNode(pos);
        if (node != null) return node;
        node = getNewNode();
        node.setPos(pos);
        addNode(node);
        return node;
    }

    @Override
    public void addNode(@NotNull NetNode node) {
        nodeClassCheck(node);
        this.backer.addNode(node);
    }

    public @Nullable WorldNetNode getNode(@NotNull BlockPos equivalencyData) {
        return (WorldNetNode) getNode((Object) equivalencyData);
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
        double weight = source.getData().getLogicEntryDefaultable(WeightFactorLogic.INSTANCE).getValue() +
                target.getData().getLogicEntryDefaultable(WeightFactorLogic.INSTANCE).getValue();
        NetEdge edge = backer.addEdge(source, target, weight);
        if (edge != null) initializeEdge(source, target, edge);
        if (bothWays) {
            if (this.getGraph().isDirected()) {
                edge = backer.addEdge(target, source, weight);
                if (edge != null) initializeEdge(target, source, edge);
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

    protected void initializeEdge(@NotNull NetNode source, @NotNull NetNode target, @NotNull NetEdge edge) {
        edge.setData(NetLogicData.union(source.getData(), target.getData()));
    }

    protected int getDimension() {
        return world.provider.getDimension();
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
        return WorldNetNode.class;
    }

    @Override
    public @NotNull WorldNetNode getNewNode() {
        return new WorldNetNode(this);
    }
}
