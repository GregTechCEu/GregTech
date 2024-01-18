package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.pipenet.FlowChannel;
import gregtech.api.pipenet.NetEdge;
import gregtech.api.pipenet.NodeG;

import gregtech.api.pipenet.WorldPipeFlowNetG;
import gregtech.api.pipenet.alg.MaximumFlowAlgorithm;

import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;

import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipeTickable;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jgrapht.Graph;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class FluidChannel extends FlowChannel<FluidPipeType, FluidPipeProperties> {

    private FluidStack fluid;

    private MaximumFlowAlgorithm<FluidPipeType, FluidPipeProperties> alg = null;

    private Set<NodeG<FluidPipeType, FluidPipeProperties>> oldNodes = null;

    public FluidChannel(Graph<NodeG<FluidPipeType, FluidPipeProperties>, NetEdge> network, Fluid fluid) {
        super(network);
        this.fluid = new FluidStack(fluid, 1);
    }

    public void setFluid(Fluid fluid) {
        this.fluid = new FluidStack(fluid, 1);
    }

    @Override
    public void evaluate() {
        activate();

        if (network instanceof WorldPipeFlowNetG.IFlowGraph graph) {
            graph.setTestObject(fluid.getFluid());
        }
        else throw new IllegalStateException("Attempted to do flow calculations on a non-flow graph!");

        if (alg == null) alg = new MaximumFlowAlgorithm<>(network);

        double max = alg.calculateMaximumFlow(superSource, superSink);
        Map<NetEdge, Double> flows = alg.getFlowMap();
        Map<NodeG<?, ?>, Double> inMap = new Object2DoubleOpenHashMap<>();
        Map<NodeG<?, ?>, Double> outMap = new Object2DoubleOpenHashMap<>();
        Set<NodeG<FluidPipeType, FluidPipeProperties>> nodes = new ObjectOpenHashSet<>();

        for (Map.Entry<NetEdge, Double> flow : flows.entrySet()) {
            if (flow.getValue() == 0) continue;
            inMap.merge(flow.getKey().getTarget(), flow.getValue(), Double::sum);
            outMap.merge(flow.getKey().getSource(), flow.getValue(), Double::sum);
            nodes.add((NodeG<FluidPipeType, FluidPipeProperties>) flow.getKey().getSource());
            nodes.add((NodeG<FluidPipeType, FluidPipeProperties>) flow.getKey().getTarget());
        }
        
        for (NodeG<FluidPipeType, FluidPipeProperties> node : nodes) {
            // dataless nodes are only the superSource and superSink
            if (node.getData() == null) continue;
            if (!node.addChannel(this))
                throw new IllegalStateException("Node rejected channel despite approving it earlier!");
            if (!node.getData().test(fluid)) {
                // destroyethify
                if (node.getHeldMTE() instanceof TileEntityFluidPipeTickable f) {
                    f.checkAndDestroy(fluid);
                }
            }
        }

        oldNodes.removeAll(nodes);
        for (NodeG<FluidPipeType, FluidPipeProperties> oldNode : oldNodes) {
            oldNode.removeChannel(this);
        }
        oldNodes = nodes;
        for (Map.Entry<NodeG<FluidPipeType, FluidPipeProperties>, Double> sink : activeSinks.entrySet()) {
            if (sink.getKey().getHeldMTE() instanceof TileEntityFluidPipeTickable f) {
                double flow = outMap.getOrDefault(sink.getKey(), 0d);
                // insert flow to the node's neighbors
            }
        }

        deactivate();
    }

    @Override
    public void adjustSource(NodeG<FluidPipeType, FluidPipeProperties> source, Function<Double, Double> adjuster) {
        this.alg = null;
        super.adjustSource(source, adjuster);
    }

    @Override
    public void adjustSink(NodeG<FluidPipeType, FluidPipeProperties> sink, Function<Double, Double> adjuster) {
        this.alg = null;
        super.adjustSink(sink, adjuster);
    }

}
