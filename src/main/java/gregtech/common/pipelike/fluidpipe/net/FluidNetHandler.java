package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.cover.Cover;
import gregtech.api.pipenet.IPipeNetHandler;
import gregtech.api.pipenet.NetEdge;
import gregtech.api.pipenet.NetPath;
import gregtech.api.pipenet.NodeG;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.common.covers.CoverFluidRegulator;
import gregtech.common.covers.CoverPump;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class FluidNetHandler implements IFluidHandler, IPipeNetHandler {

    private static final IFluidTankProperties[] EMPTY = new IFluidTankProperties[0];

    private final WorldFluidPipeNet net;
    private TileEntityFluidPipe pipe;
    private final EnumFacing facing;

    private final IFluidHandler testHandler = new FluidTank(Integer.MAX_VALUE);

    private NetEdge.ChannelSimulator simulator;

    public FluidNetHandler(WorldFluidPipeNet net, TileEntityFluidPipe pipe, EnumFacing facing) {
        this.net = net;
        this.pipe = pipe;
        this.facing = facing;
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

        FluidTestObject testObject = new FluidTestObject(resource);
        long tick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
        // push flow through net
        List<NetPath<FluidPipeType, FluidPipeProperties>> paths =
                this.getNet().getPaths(pipe, testObject);
        FluidStack helper = resource.copy();
        if (!doFill) this.simulator = NetEdge.getNewSimulatorInstance();
        else this.simulator = null;
        mainloop:
        for (NetPath<FluidPipeType, FluidPipeProperties> path : paths) {
            for (Iterator<EnumFacing> it = path.getFacingIterator(); it.hasNext(); ) {
                EnumFacing facing = it.next();
                NetPath.FacedNetPath<FluidPipeType, FluidPipeProperties> routePath = path.withFacing(facing);
                helper.amount -= this.fill(routePath, testObject, tick, helper, doFill);
                if (helper.amount <= 0) break mainloop;
            }
        }
        if (!doFill) this.simulator = null;
        return resource.amount - helper.amount;
    }

    public int fill(NetPath.FacedNetPath<FluidPipeType, FluidPipeProperties> routePath,
                            FluidTestObject testObject, long tick, FluidStack resource, boolean doFill) {
        if (routePath.getTargetNode().getNodePos().equals(this.pipe.getPos()) && routePath.facing == this.facing) {
            return 0;
        }
        int allowed = resource.amount;
        Cover pipeCover = routePath.getTargetNode().getHeldMTE().getCoverableImplementation()
                .getCoverAtSide(routePath.facing);
        Cover tileCover = getCoverOnNeighbour(routePath.getTargetNode().getNodePos(),
                routePath.facing.getOpposite());

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
                .getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, routePath.facing.getOpposite());
        if (neighbourHandler == null) return 0;

        if (pipeCover instanceof CoverFluidRegulator &&
                ((CoverFluidRegulator) pipeCover).getPumpMode() == CoverPump.PumpMode.EXPORT) {
            return fillOverRegulator(routePath, neighbourHandler, testObject, tick, (CoverFluidRegulator) pipeCover,
                    resource, allowed, doFill);
        } else if (tileCover instanceof CoverFluidRegulator &&
                ((CoverFluidRegulator) tileCover).getPumpMode() == CoverPump.PumpMode.IMPORT) {
            return fillOverRegulator(routePath, neighbourHandler, testObject, tick, (CoverFluidRegulator) tileCover,
                    resource, allowed, doFill);
        } else {
            return fill(routePath, neighbourHandler, testObject, tick, resource, allowed, doFill);
        }
    }

    public int fillOverRegulator(NetPath.FacedNetPath<FluidPipeType, FluidPipeProperties> routePath,
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

    private int fill(NetPath.FacedNetPath<FluidPipeType, FluidPipeProperties> routePath, IFluidHandler handler,
                     FluidTestObject testObject, long tick, FluidStack resource, int allowed, boolean doFill) {
        FluidStack helper = new FluidStack(resource, allowed);
        allowed = handler.fill(helper, false);

        // iterate through path
        List<NodeG<FluidPipeType, FluidPipeProperties>> nodeList = routePath.getNodeList();
        List<NetEdge> edgeList = routePath.getEdgeList();
        List<Consumer<Double>> flowLimitConsumers = new ObjectArrayList<>();
        int inputAmount = resource.amount;
        int outputAmount = resource.amount;
        // always 1 less edge than nodes
        for (int i = 0; i < edgeList.size(); i++) {
            NodeG<FluidPipeType, FluidPipeProperties> source = nodeList.get(i);
            NodeG<FluidPipeType, FluidPipeProperties> target = nodeList.get(i + 1);
            NetEdge edge = edgeList.get(i);
            int flow = Math.min(edge.getFlowLimit(testObject, getNet().getGraph(), tick, simulator), outputAmount);
            double ratio = (double) flow / outputAmount;
            inputAmount *= ratio;
            flowLimitConsumers.add(finalRatio ->
                    edge.consumeFlowLimit(testObject, getNet().getGraph(), (int) (finalRatio * flow), tick, simulator));
            double loss = 1; // TODO fluid loss & pipe damage
            outputAmount = (int) (flow * loss);
        }
        // outputAmount is currently the maximum flow to the endpoint, and inputAmount is the requisite flow into the net
        allowed = Math.min(allowed, outputAmount);

        helper.amount = allowed;
        allowed = handler.fill(helper, doFill);
        double ratio = (double) allowed / outputAmount;
        flowLimitConsumers.forEach(a -> a.accept(ratio));

        return (int) (inputAmount * ratio);

    }

    public static int countStack(IFluidHandler handler, FluidStack stack, CoverFluidRegulator regulator, boolean isStackSpecific) {
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

    protected static class FluidTestObject {

        public final Fluid fluid;
        public final NBTTagCompound tag;

        public FluidTestObject(FluidStack stack) {
            this.fluid = stack.getFluid();
            this.tag = stack.tag;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FluidTestObject that = (FluidTestObject) o;
            return Objects.equals(fluid, that.fluid) && Objects.equals(tag, that.tag);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fluid, tag);
        }
    }
}
