package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.cover.Cover;
import gregtech.api.pipenet.NetEdge;
import gregtech.api.pipenet.NetGroup;
import gregtech.api.pipenet.NodeG;
import gregtech.api.pipenet.alg.MaximumFlowAlgorithm;
import gregtech.api.pipenet.flow.FlowChannel;
import gregtech.api.pipenet.flow.FlowChannelTicker;
import gregtech.api.pipenet.flow.WorldPipeFlowNetG;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.common.covers.CoverFluidFilter;
import gregtech.common.covers.CoverPump;
import gregtech.common.covers.CoverShutter;
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

    private Set<NodeG<FluidPipeType, FluidPipeProperties>> oldNodes = new ObjectOpenHashSet<>(0);

    public FluidChannel(Graph<NodeG<FluidPipeType, FluidPipeProperties>, NetEdge> network, Fluid fluid) {
        super(network);
        this.fluid = new FluidStack(fluid, 1);
    }

    public void setFluid(Fluid fluid) {
        if (this.manager != null) return;
        this.fluid = new FluidStack(fluid, 1);
    }

    @Override
    protected Object getKey() {
        return fluid.getFluid();
    }

    @Override
    public void clearAlg() {
        this.alg = null;
    }

    @Override
    public void evaluate() {
        // Kill this channel if we have no more active sources
        if (this.activeSources.size() == 0) {
            // should I put the channel in a 'recycling queue' to be reused instead?
            this.manager.removeChannel(this.getKey());
            oldNodes.forEach(oldNode -> oldNode.removeChannel(this));
            return;
        }

        activate();

        if (network instanceof WorldPipeFlowNetG.IFlowGraph<?, ?>graph) {
            graph.setTestObject(fluid);
            ((WorldPipeFlowNetG.IFlowGraph<FluidPipeProperties, FluidPipeType>) graph)
                    .setQueryingChannel(this);
        } else throw new IllegalStateException("Attempted to do flow calculations on a non-flow graph!");

        if (alg == null) alg = new MaximumFlowAlgorithm<>(network);

        Map<NetEdge, Double> flows = alg.getMaximumFlow(manager.getSuperSource(), manager.getSuperSink()).getFlowMap();
        Map<NodeG<?, ?>, Double> inMap = new Object2DoubleOpenHashMap<>();
        Map<NodeG<?, ?>, Double> outMap = new Object2DoubleOpenHashMap<>();
        Map<NodeG<FluidPipeType, FluidPipeProperties>, Double> interMap = new Object2DoubleOpenHashMap<>();

        for (Map.Entry<NetEdge, Double> flow : flows.entrySet()) {
            if (flow.getValue() == 0) continue;
            NetEdge edge = flow.getKey();
            // the interflows receive values from both inflow and outflow, which are equivalent, thus divide by two.
            if (edge.getSource() == this.manager.getSuperSource()) {
                inMap.merge(edge.getTarget(), flow.getValue(), Double::sum);
                interMap.merge((NodeG<FluidPipeType, FluidPipeProperties>) edge.getTarget(),
                        flow.getValue() / 2, Double::sum);
            } else if (edge.getTarget() == this.manager.getSuperSink()) {
                outMap.merge(edge.getSource(), flow.getValue(), Double::sum);
                interMap.merge((NodeG<FluidPipeType, FluidPipeProperties>) edge.getSource(),
                        flow.getValue() / 2, Double::sum);
            } else {
                interMap.merge((NodeG<FluidPipeType, FluidPipeProperties>) edge.getSource(),
                        flow.getValue() / 2, Double::sum);
                interMap.merge((NodeG<FluidPipeType, FluidPipeProperties>) edge.getTarget(),
                        flow.getValue() / 2, Double::sum);
            }
        }

        for (var flow : interMap.entrySet()) {
            NodeG<FluidPipeType, FluidPipeProperties> node = flow.getKey();
            if (!node.addChannel(this))
                throw new IllegalStateException("Node rejected channel despite approving it earlier!");
            if (!node.getData().test(fluid)) {
                // destroyethify
                if (node.getHeldMTE() instanceof TileEntityFluidPipe f) {
                    FluidStack flowStack = new FluidStack(fluid, (int) (double) flow.getValue());
                    f.checkAndDestroy(flowStack);
                    // TODO fix fluid leakage
                }
            }
        }

        oldNodes.removeAll(interMap.keySet());
        oldNodes.forEach(oldNode -> oldNode.removeChannel(this));
        oldNodes = interMap.keySet();

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

    private int pushToNode(NodeG<FluidPipeType, FluidPipeProperties> sink, int amount, boolean doFill) {
        int flow = 0;
        if (sink.getHeldMTE() instanceof TileEntityFluidPipe f) {
            int fill;
            Byte receiveSides = this.receiveSidesMap.get(sink);
            for (Map.Entry<EnumFacing, TileEntity> connected : sink.getConnecteds().entrySet()) {
                if (amount == 0) break;
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
                int transferMax = Math.min(evaluateCover(themCover, evaluateCover(thisCover, amount)),
                        // max flow per side cannot exceed throughput
                        sink.getData().getThroughput() * FlowChannelTicker.FLOWNET_TICKRATE);
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
                    if (!p.getFluidFilterContainer().test(this.fluid)) return 0;
                }
            }
            return Math.min(transferMax, p.getTransferRate());
        }
        if (cover instanceof CoverFluidFilter f) {
            return f.getFilter().testFluid(this.fluid) ? transferMax : 0;
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
}
