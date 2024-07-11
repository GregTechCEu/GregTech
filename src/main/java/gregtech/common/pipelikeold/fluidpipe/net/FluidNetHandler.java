package gregtech.common.pipelikeold.fluidpipe.net;

import gregtech.api.cover.Cover;
import gregtech.api.graphnet.pipenetold.IPipeNetHandler;
import gregtech.api.graphnet.pipenetold.PipeNetNode;
import gregtech.api.graphnet.pipenetold.PipeNetPath;
import gregtech.api.graphnet.pipenetold.NodeLossResult;
import gregtech.api.graphnet.edge.NetFlowEdge;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.edge.util.FlowConsumerList;
import gregtech.api.graphnet.predicate.test.FluidTestObject;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.common.covers.CoverFluidRegulator;
import gregtech.common.covers.CoverPump;
import gregtech.common.pipelikeold.fluidpipe.FluidPipeType;
import gregtech.common.pipelikeold.fluidpipe.tile.TileEntityFluidPipe;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.FMLCommonHandler;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FluidNetHandler implements IFluidHandler, IPipeNetHandler {

    protected static final PipeNetNode<FluidPipeType, FluidPipeProperties, NetFlowEdge> FAKE_SOURCE = new PipeNetNode<>(
            new FluidPipeProperties(Integer.MAX_VALUE, Integer.MAX_VALUE, true, true, true, true, Integer.MAX_VALUE));

    private static final IFluidTankProperties[] EMPTY = new IFluidTankProperties[0];

    private final WorldFluidPipeNet net;
    private TileEntityFluidPipe pipe;
    private final EnumFacing facing;

    private final IFluidHandler testHandler = new FluidTank(Integer.MAX_VALUE);

    private SimulatorKey simulatorKey;
    private FluidStack lastFillResource;
    private final Map<PipeNetNode<FluidPipeType, FluidPipeProperties, NetFlowEdge>, NodeLossResult> lossResultCache = new Object2ObjectOpenHashMap<>();

    private final NetFlowEdge inputEdge;

    public FluidNetHandler(WorldFluidPipeNet net, TileEntityFluidPipe pipe, EnumFacing facing) {
        this.net = net;
        this.pipe = pipe;
        this.facing = facing;
        this.inputEdge = new NetFlowEdge(1) {

            @Override
            public PipeNetNode<?, ?, ?> getSource() {
                return FAKE_SOURCE;
            }

            @Override
            public PipeNetNode<?, ?, ?> getTarget() {
                return FluidNetHandler.this.pipe.getNode();
            }
        };
    }

    public void updatePipe(TileEntityFluidPipe pipe) {
        this.pipe = pipe;
    }

    @Override
    public WorldFluidPipeNet getNet() {
        return net;
    }

    @Override
    public EnumFacing getFacing() {
        return facing;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        // TODO instead collect a list of all connected tanks?
        return EMPTY;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (net == null || pipe == null || pipe.isInvalid() || pipe.isFaceBlocked(facing)) return 0;

        if (!resource.isFluidStackIdentical(this.lastFillResource)) {
            // cache needs to persist through the entire 'try with doFill false then try with doFill true' process,
            // but should not persist into a new fill attempt.
            this.lossResultCache.clear();
            this.lastFillResource = resource.copy();
        }

        FluidTestObject testObject = new FluidTestObject(resource);
        long queryTick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
        FluidStack helper = resource.copy();
        if (!doFill) this.simulatorKey = SimulatorKey.getNewSimulatorInstance();
        else this.simulatorKey = null;
        this.getNet().getGraph().prepareForDynamicWeightAlgorithmRun(testObject, simulatorKey);
        mainloop:
        for (Iterator<PipeNetPath<FluidPipeType, FluidPipeProperties, NetFlowEdge>> iter = this.getNet()
                .getPaths(pipe); iter.hasNext();) {
            PipeNetPath<FluidPipeType, FluidPipeProperties, NetFlowEdge> path = iter.next();
            for (Iterator<EnumFacing> it = path.getFacingIterator(); it.hasNext();) {
                EnumFacing facing = it.next();
                PipeNetPath.FacedPipeNetPath<FluidPipeType, FluidPipeProperties, NetFlowEdge> routePath = path
                        .withFacing(facing);
                helper.amount -= this.fill(routePath, testObject, queryTick, helper, doFill);
                if (helper.amount <= 0) break mainloop;
            }
        }
        if (!doFill) this.simulatorKey = null;
        if (doFill && !this.lossResultCache.isEmpty()) {
            this.lossResultCache.forEach((k, v) -> v.getPostAction().accept(k));
            this.lossResultCache.clear();
        }
        return resource.amount - helper.amount;
    }

    public int fill(PipeNetPath.FacedPipeNetPath<FluidPipeType, FluidPipeProperties, NetFlowEdge> routePath,
                    FluidTestObject testObject, long tick, FluidStack resource, boolean doFill) {
        if (routePath.getTargetNode().getNodePos().equals(this.pipe.getPos()) && routePath.facing == this.facing) {
            return 0;
        }
        int allowed = resource.amount;
        Cover pipeCover = routePath.getTargetNode().getHeldMTE().getCoverableImplementation()
                .getCoverAtSide(routePath.facing);
        Cover tileCover = getCoverOnNeighbour(routePath.getTargetNode().getNodePos(),
                routePath.oppositeFacing());

        if (pipeCover != null) {
            FluidStack helper;
            testHandler.fill(resource, true);
            IFluidHandler fluidHandler = pipeCover.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                    testHandler);
            if (fluidHandler == null || (fluidHandler != testHandler &&
                    ((helper = fluidHandler.drain(resource, false)) == null ||
                            (allowed = helper.amount) <= 0))) {
                testHandler.drain(Integer.MAX_VALUE, true);
                return 0;
            }
            testHandler.drain(Integer.MAX_VALUE, true);
        }
        IFluidHandler neighbourHandler = routePath.getTargetTE()
                .getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, routePath.oppositeFacing());
        if (neighbourHandler == null) return 0;

        if (pipeCover instanceof CoverFluidRegulator &&
                ((CoverFluidRegulator) pipeCover).getPumpMode() == CoverPump.PumpMode.EXPORT) {
            return fillOverRegulator(routePath, neighbourHandler, testObject, tick, (CoverFluidRegulator) pipeCover,
                    resource, allowed, doFill);
        } else if (tileCover instanceof CoverFluidRegulator &&
                ((CoverFluidRegulator) tileCover).getPumpMode() == CoverPump.PumpMode.IMPORT) {
                    return fillOverRegulator(routePath, neighbourHandler, testObject, tick,
                            (CoverFluidRegulator) tileCover,
                            resource, allowed, doFill);
                } else {
                    return fill(routePath, neighbourHandler, testObject, tick, resource, allowed, doFill);
                }
    }

    public int fillOverRegulator(PipeNetPath.FacedPipeNetPath<FluidPipeType, FluidPipeProperties, NetFlowEdge> routePath,
                                 IFluidHandler handler, FluidTestObject testObject, long tick,
                                 CoverFluidRegulator regulator, FluidStack resource, int allowed, boolean doFill) {
        var matched = regulator.getFluidFilterContainer().match(resource);
        boolean isStackSpecific = false;
        int rate, count;

        if (matched.isMatched()) {
            int index = matched.getFilterIndex();
            rate = regulator.getFluidFilterContainer().getTransferLimit(index);
            isStackSpecific = true;
        } else {
            rate = regulator.getFluidFilterContainer().getTransferSize();
        }

        switch (regulator.getTransferMode()) {
            case TRANSFER_ANY -> {
                return fill(routePath, handler, testObject, tick, resource, allowed, doFill);
            }
            case KEEP_EXACT -> {
                count = rate - countStack(handler, resource, regulator, isStackSpecific);
                if (count <= 0) return 0;
                count = Math.min(allowed, Math.min(resource.amount, count));
                return fill(routePath, handler, testObject, tick, resource, count, doFill);
            }
            case TRANSFER_EXACT -> {
                // TODO can regulators have the equivalent of robot arm buffering?
                count = Math.min(allowed, Math.min(rate, resource.amount));
                if (count < rate) {
                    return 0;
                }
                if (fill(routePath, handler, testObject, tick, resource, count, false) != count) {
                    return 0;
                }
                return fill(routePath, handler, testObject, tick, resource, count, true);
            }
        }
        return 0;
    }

    private int fill(PipeNetPath.FacedPipeNetPath<FluidPipeType, FluidPipeProperties, NetFlowEdge> routePath,
                     IFluidHandler handler,
                     FluidTestObject testObject, long tick, FluidStack resource, int allowed, boolean doFill) {
        FluidStack helper = new FluidStack(resource, allowed);
        allowed = handler.fill(helper, false);
        if (allowed == 0) return 0;

        // iterate through path
        List<PipeNetNode<FluidPipeType, FluidPipeProperties, NetFlowEdge>> nodeList = routePath.getNodeList();
        List<NetFlowEdge> edgeList = routePath.getEdgeList();
        FlowConsumerList<FluidPipeType, FluidPipeProperties, NetFlowEdge> flowLimitConsumers = new FlowConsumerList<>();

        int inputAmount = resource.amount;
        int outputAmount = resource.amount;

        for (int i = 0; i < nodeList.size(); i++) {
            NetFlowEdge edge = i == 0 ? inputEdge : edgeList.get(i - 1);
            if (!edge.getPredicate().test(testObject)) return 0;
            long flow = Math.min(edge.getFlowLimit(testObject, getNet().getGraph(), tick, simulatorKey), outputAmount);
            double ratio = (double) flow / outputAmount;
            inputAmount *= ratio;
            flowLimitConsumers.modifyRatios(ratio);
            flowLimitConsumers.add(edge, testObject, getNet().getGraph(), flow, tick, simulatorKey);
            // TODO undo loss when backflowing
            NodeLossResult targetResult = getOrGenerateLossResult(nodeList.get(i), resource);
            outputAmount = (int) (flow * targetResult.getLossFunction());
            if (outputAmount == 0) break;
        }
        // outputAmount is currently the maximum flow to the endpoint, and inputAmount is the requisite flow into the
        // net
        allowed = Math.min(allowed, outputAmount);

        helper.amount = allowed;
        double ratio;
        if (outputAmount == 0) {
            ratio = 1;
        } else {
            allowed = handler.fill(helper, doFill);
            ratio = (double) allowed / outputAmount;
        }
        flowLimitConsumers.doConsumption(ratio);

        return (int) (inputAmount * ratio);
    }

    public static int countStack(IFluidHandler handler, FluidStack stack, CoverFluidRegulator regulator,
                                 boolean isStackSpecific) {
        if (regulator == null) return 0;
        int count = 0;
        for (int i = 0; i < handler.getTankProperties().length; i++) {
            FluidStack slot = handler.getTankProperties()[i].getContents();
            if (slot == null) continue;
            if (isStackSpecific ? slot.isFluidEqual(stack) :
                    regulator.getFluidFilterContainer().test(slot)) {
                count += slot.amount;
            }
        }
        return count;
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        // TODO support pulling from net?
        return null;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        // TODO support pulling from net?
        return null;
    }

    protected NodeLossResult getOrGenerateLossResult(PipeNetNode<FluidPipeType, FluidPipeProperties, NetFlowEdge> node,
                                                     FluidStack resource) {
        var cachedResult = this.lossResultCache.get(node);
        if (cachedResult == null) {
            cachedResult = node.getData().determineFluidPassthroughResult(resource, net.getWorld(), node.getNodePos());
            this.lossResultCache.put(node, cachedResult);
        }
        return cachedResult;
    }
}
