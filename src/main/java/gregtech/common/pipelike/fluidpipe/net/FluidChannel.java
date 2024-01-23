package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.cover.Cover;
import gregtech.api.pipenet.FlowChannel;
import gregtech.api.pipenet.NetEdge;
import gregtech.api.pipenet.NetGroup;
import gregtech.api.pipenet.NodeG;
import gregtech.api.pipenet.WorldPipeFlowNetG;
import gregtech.api.pipenet.alg.MaximumFlowAlgorithm;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.common.covers.CoverPump;
import gregtech.common.covers.CoverShutter;
import gregtech.common.covers.filter.FluidFilter;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jgrapht.Graph;

import java.util.Map;
import java.util.Set;

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
        // Kill this channel if we have no more active sources
        if (this.activeSources.size() == 0) {
            this.manager.removeChannel(this.fluid.getFluid());
            return;
        }

        activate();

        if (network instanceof WorldPipeFlowNetG.IFlowGraph<?, ?>graph) {
            graph.setTestObject(fluid.getFluid());
            ((WorldPipeFlowNetG.IFlowGraph<FluidPipeProperties, FluidPipeType>) graph)
                    .setQueryingChannel(this);
        } else throw new IllegalStateException("Attempted to do flow calculations on a non-flow graph!");

        if (alg == null) alg = new MaximumFlowAlgorithm<>(network);

        alg.calculateMaximumFlow(this.manager.getSuperSource(), this.manager.getSuperSink());
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
                if (node.getHeldMTE() instanceof TileEntityFluidPipe f) {
                    f.checkAndDestroy(fluid);
                    // TODO implement fluid leakage?
                }
            }
        }

        oldNodes.removeAll(nodes);
        for (NodeG<FluidPipeType, FluidPipeProperties> oldNode : oldNodes) {
            oldNode.removeChannel(this);
        }
        oldNodes = nodes;

        // Everything should be properly balanced at this point due to earlier operations.
        // If something is off, it's too late to fix.
        for (NodeG<FluidPipeType, FluidPipeProperties> source : activeSources) {
            double flow = inMap.getOrDefault(source, 0d);
            if (flow != 0)
                pullFromNode(source, (int) flow, true);
        }
        for (NodeG<FluidPipeType, FluidPipeProperties> sink : this.manager.getActiveSinks()) {
            double flow = outMap.getOrDefault(sink, 0d);
            if (flow != 0)
                pushToNode(sink, (int) flow, true);
        }

        deactivate();
    }

    @Override
    protected double getSourceValue(NodeG<FluidPipeType, FluidPipeProperties> source) {
        return pullFromNode(source, Integer.MAX_VALUE, false);
    }

    private double pullFromNode(NodeG<FluidPipeType, FluidPipeProperties> source, int amount, boolean doDrain) {
        FluidStack stack = null;
        if (source.getHeldMTE() instanceof TileEntityFluidPipe f) {
            IFluidHandler handler = f.getCapability(
                    CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
            if (handler != null) {
                stack = handler.drain(new FluidStack(this.fluid.getFluid(), amount), doDrain);
            }
        }
        return stack != null ? stack.amount : 0;
    }

    @Override
    protected double getSinkValue(NodeG<FluidPipeType, FluidPipeProperties> sink) {
        return pushToNode(sink, Integer.MAX_VALUE, false);
    }

    private double pushToNode(NodeG<FluidPipeType, FluidPipeProperties> sink, int amount, boolean doFill) {
        int flow = 0;
        if (sink.getHeldMTE() instanceof TileEntityFluidPipe f) {
            int fill;
            Byte receiveSides = this.receiveSidesMap.get(sink);
            for (Map.Entry<EnumFacing, TileEntity> connected : sink.getConnecteds().entrySet()) {
                if (receiveSides != null) {
                    int facing = (1 << connected.getKey().getIndex());
                    if ((receiveSides & facing) != 0) {
                        if (doFill) this.receiveSidesMap.compute(sink, (k, v) -> {
                            assert v != null;
                            // bitwise XOR to remove this side from the map
                            return (byte) (v ^ facing);
                        });
                        continue;
                    }
                }
                Cover thisCover = f.getCoverableImplementation().getCoverAtSide(connected.getKey());
                Cover themCover = getCoverOnNeighbour(sink, connected.getKey().getOpposite());
                int transferMax = evaluateCover(themCover, evaluateCover(thisCover, amount));
                IFluidHandler handler = connected.getValue().getCapability(
                        CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, connected.getKey().getOpposite());
                if (handler != null) {
                    fill = handler.fill(new FluidStack(this.fluid.getFluid(), transferMax), doFill);
                    flow += fill;
                    amount -= fill;
                }
            }
        }
        return flow;
    }

    private int evaluateCover(Cover cover, int transferMax) {
        if (cover instanceof CoverPump p) {
            switch (p.getManualImportExportMode()) {
                case DISABLED -> {
                    return 0;
                }
                case FILTERED -> {
                    if (!p.getFluidFilterContainer().testFluidStack(this.fluid)) return 0;
                }
            }
            return Math.min(transferMax, p.getTransferRate());
        }
        if (cover instanceof FluidFilter f) {
            return f.testFluid(this.fluid) ? transferMax : 0;
        }
        if (cover instanceof CoverShutter) return 0;
        return transferMax;
    }

    public static FluidChannel getChannelFromGroup(Fluid key, NetGroup<FluidPipeType, FluidPipeProperties> group) {
        FluidChannel channel = (FluidChannel) group.getChannel(key);
        if (channel == null) {
            channel = new FluidChannel(group.getGraph(), key);
            group.setChannel(key, channel);
        }
        return channel;
    }

    @Override
    protected FlowChannel<FluidPipeType, FluidPipeProperties> getNew() {
        return new FluidChannel(this.network, this.fluid.getFluid());
    }
}
