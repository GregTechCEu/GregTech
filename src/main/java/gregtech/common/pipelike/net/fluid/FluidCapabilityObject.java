package gregtech.common.pipelike.net.fluid;

import gregtech.api.graphnet.edge.AbstractNetFlowEdge;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.logic.ChannelCountLogic;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.pipenet.FlowWorldPipeNetPath;
import gregtech.api.graphnet.pipenet.WorldPipeNet;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.graphnet.predicate.test.FluidTestObject;
import gregtech.api.graphnet.traverse.ITraverseData;
import gregtech.api.graphnet.traverse.TraverseDataProvider;
import gregtech.api.graphnet.traverse.TraverseGuide;
import gregtech.api.graphnet.traverse.TraverseHelpers;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.FMLCommonHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.function.LongConsumer;

public class FluidCapabilityObject implements IPipeCapabilityObject, IFluidHandler, IFluidTankProperties,
                                   IFluidTraverseGuideProvider {

    private final WorldPipeNet net;
    private @Nullable PipeTileEntity tile;

    private final EnumMap<EnumFacing, Wrapper> wrappers = new EnumMap<>(EnumFacing.class);
    private final WorldPipeNetNode node;
    private final IFluidTankProperties[] properties;

    private boolean transferring = false;

    public <N extends WorldPipeNet & FlowWorldPipeNetPath.Provider> FluidCapabilityObject(@NotNull N net,
                                                                                          WorldPipeNetNode node) {
        this.net = net;
        this.node = node;
        properties = new IFluidTankProperties[node.getData().getLogicEntryDefaultable(ChannelCountLogic.INSTANCE)
                .getValue()];
        Arrays.fill(properties, this);
        for (EnumFacing facing : EnumFacing.VALUES) {
            AbstractNetFlowEdge edge = (AbstractNetFlowEdge) net.getNewEdge();
            edge.setData(NetLogicData.union(node.getData(), (NetLogicData) null));
            wrappers.put(facing, new Wrapper(facing, edge));
        }
    }

    private FlowWorldPipeNetPath.Provider getProvider() {
        return (FlowWorldPipeNetPath.Provider) net;
    }

    private boolean inputDisallowed(EnumFacing side) {
        if (side == null) return false;
        if (tile == null) return true;
        else return tile.isBlocked(side);
    }

    private Iterator<FlowWorldPipeNetPath> getPaths(@NotNull ITraverseData<?, ?> data) {
        assert tile != null;
        return getProvider().getPaths(net.getNode(tile.getPos()), data.getTestObject(), data.getSimulatorKey(),
                data.getQueryTick());
    }

    @Override
    public void setTile(@Nullable PipeTileEntity tile) {
        this.tile = tile;
    }

    @Override
    public Capability<?>[] getCapabilities() {
        return WorldFluidNet.CAPABILITIES;
    }

    @Override
    public <T> T getCapabilityForSide(Capability<T> capability, @Nullable EnumFacing facing) {
        if (facing == null) return null; // hard override to prevent TOP from displaying a tank.
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            // noinspection ConstantValue
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(facing == null ? this : wrappers.get(facing));
        }
        return null;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return fill(resource, doFill, null);
    }

    public int fill(FluidStack resource, boolean doFill, EnumFacing side) {
        if (this.transferring) return 0;
        this.transferring = true;

        var guide = getGuide(FluidTraverseData::new, new FluidTestObject(resource), resource.amount, !doFill, side);
        if (guide == null) return 0;
        int accepted = (int) TraverseHelpers.traverseFlood(guide.getData(), guide.getPaths(), guide.getFlow());
        guide.reportConsumedFlow(accepted);

        this.transferring = false;
        return accepted;
    }

    @Override
    public @Nullable <
            D extends ITraverseData<WorldPipeNetNode, FlowWorldPipeNetPath>> TraverseGuide<WorldPipeNetNode, FlowWorldPipeNetPath, D> getGuide(
                                                                                                                                               TraverseDataProvider<D, FluidTestObject> provider,
                                                                                                                                               FluidTestObject testObject,
                                                                                                                                               long flow,
                                                                                                                                               boolean simulate) {
        return getGuide(provider, testObject, flow, simulate, null);
    }

    public @Nullable <
            D extends ITraverseData<WorldPipeNetNode, FlowWorldPipeNetPath>> TraverseGuide<WorldPipeNetNode, FlowWorldPipeNetPath, D> getGuide(
                                                                                                                                               TraverseDataProvider<D, FluidTestObject> provider,
                                                                                                                                               FluidTestObject testObject,
                                                                                                                                               long flow,
                                                                                                                                               boolean simulate,
                                                                                                                                               EnumFacing side) {
        if (tile == null || inputDisallowed(side)) return null;
        SimulatorKey simulator = simulate ? SimulatorKey.getNewSimulatorInstance() : null;
        long tick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
        D data = provider.of(net, testObject, simulator, tick, tile.getPos(), side);

        LongConsumer flowReport = null;
        Wrapper wrapper = this.wrappers.get(side);
        if (wrapper != null) {
            AbstractNetFlowEdge internalBuffer = wrapper.getBuffer();
            if (internalBuffer != null) {
                long limit = internalBuffer.getFlowLimit(testObject, net, tick, simulator);
                if (limit <= 0) {
                    this.transferring = false;
                    return null;
                }
                flow = Math.min(limit, flow);
                flowReport = l -> data.consumeFlowLimit(internalBuffer, node, l);
            }
        }
        return new TraverseGuide<>(data, () -> getPaths(data), flow, flowReport);
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return properties;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return null;
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return null;
    }

    @Override
    public FluidStack getContents() {
        return null;
    }

    @Override
    public int getCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canFill() {
        return true;
    }

    @Override
    public boolean canDrain() {
        return false;
    }

    @Override
    public boolean canFillFluidType(FluidStack fluidStack) {
        return true;
    }

    @Override
    public boolean canDrainFluidType(FluidStack fluidStack) {
        return false;
    }

    protected class Wrapper implements IFluidHandler, IFluidTankProperties, IFluidTraverseGuideProvider {

        private final EnumFacing facing;
        private final AbstractNetFlowEdge buffer;
        private final IFluidTankProperties[] properties;

        public Wrapper(EnumFacing facing, AbstractNetFlowEdge buffer) {
            this.facing = facing;
            this.buffer = buffer;
            properties = new IFluidTankProperties[FluidCapabilityObject.this.properties.length];
            Arrays.fill(properties, this);
        }

        @Override
        public @Nullable <
                D extends ITraverseData<WorldPipeNetNode, FlowWorldPipeNetPath>> TraverseGuide<WorldPipeNetNode, FlowWorldPipeNetPath, D> getGuide(
                                                                                                                                                   TraverseDataProvider<D, FluidTestObject> provider,
                                                                                                                                                   FluidTestObject testObject,
                                                                                                                                                   long flow,
                                                                                                                                                   boolean simulate) {
            return FluidCapabilityObject.this.getGuide(provider, testObject, flow, simulate, facing);
        }

        public AbstractNetFlowEdge getBuffer() {
            return buffer;
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return properties;
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return FluidCapabilityObject.this.fill(resource, doFill, facing);
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return null;
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return null;
        }

        @Override
        public FluidStack getContents() {
            return null;
        }

        @Override
        public int getCapacity() {
            return (int) Math.min(Integer.MAX_VALUE, buffer.getThroughput());
        }

        @Override
        public boolean canFill() {
            return true;
        }

        @Override
        public boolean canDrain() {
            return false;
        }

        @Override
        public boolean canFillFluidType(FluidStack fluidStack) {
            return true;
        }

        @Override
        public boolean canDrainFluidType(FluidStack fluidStack) {
            return false;
        }
    }
}
