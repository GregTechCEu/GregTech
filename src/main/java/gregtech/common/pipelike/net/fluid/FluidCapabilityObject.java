package gregtech.common.pipelike.net.fluid;

import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.FluidAttribute;
import gregtech.api.graphnet.edge.AbstractNetFlowEdge;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.edge.SimulatorKey;
import gregtech.api.graphnet.logic.ChannelCountLogic;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.pipenet.NodeExposingCapabilities;
import gregtech.api.graphnet.pipenet.WorldPipeNode;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.physical.tile.NodeManagingPCW;
import gregtech.api.graphnet.pipenet.physical.tile.PipeCapabilityWrapper;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.graphnet.predicate.test.FluidTestObject;
import gregtech.api.graphnet.traverse.FDTraverse;
import gregtech.api.util.GTUtility;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FluidCapabilityObject implements IPipeCapabilityObject, IFluidHandler, IFluidTankProperties {

    private PipeTileEntity tile;
    private NodeManagingPCW capabilityWrapper;

    private final EnumMap<EnumFacing, Wrapper> wrappers = new EnumMap<>(EnumFacing.class);
    private final WorldPipeNode node;
    private final IFluidTankProperties[] properties;

    private boolean transferring = false;

    public FluidCapabilityObject(WorldPipeNode node) {
        this.node = node;
        properties = new IFluidTankProperties[node.getData().getLogicEntryDefaultable(ChannelCountLogic.TYPE)
                .getValue()];
        Arrays.fill(properties, this);
        for (EnumFacing facing : EnumFacing.VALUES) {
            wrappers.put(facing, new Wrapper(facing));
        }
    }

    @Override
    public void init(@NotNull PipeTileEntity tile, @NotNull PipeCapabilityWrapper wrapper) {
        this.tile = tile;
        if (!(wrapper instanceof NodeManagingPCW p))
            throw new IllegalArgumentException("FluidCapabilityObjects must be initialized to NodeManagingPCWs!");
        this.capabilityWrapper = p;
    }

    private boolean inputDisallowed(EnumFacing side) {
        if (side == null) return false;
        else return tile.isBlocked(side);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        // can't expose the sided capability if there is no node to interact with
        if (facing != null && capabilityWrapper.getNodeForFacing(facing) == null) return null;
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(facing == null ? this : wrappers.get(facing));
        }
        return null;
    }

    protected @Nullable NetNode getRelevantNode(EnumFacing facing) {
        return facing == null ? node : capabilityWrapper.getNodeForFacing(facing);
    }

    protected int fill(FluidStack resource, boolean doFill, EnumFacing side) {
        if (this.transferring || inputDisallowed(side)) return 0;
        NetNode node = getRelevantNode(side);
        if (node == null) return 0;
        this.transferring = true;

        int flow = resource.amount;
        SimulatorKey key = doFill ? null : SimulatorKey.getNewSimulatorInstance();
        FluidTestObject testObject = new FluidTestObject(resource);
        AtomicInteger report = new AtomicInteger();
        FDTraverse.flood(node.getNet(),
                (n, f) -> {
                    if (n == node) report.addAndGet(f);
                    else if (doFill) reportFlow(n, f, testObject);
                },
                (e, f) -> reportFlow(e, f, testObject, key, true),
                e -> e instanceof AbstractNetFlowEdge n && e.test(testObject) ?
                        GTUtility.safeCastLongToInt(n.getFlowLimit(testObject, node.getNet(), getQueryTick(), key)) : 0,
                n -> n == node ? flow : getSupply(n, testObject, false),
                n -> n.getData().getLogicEntryDefaultable(FluidContainmentLogic.TYPE).handles(testObject));

        this.transferring = false;
        return report.get();
    }

    protected FluidStack drain(int maxDrain, boolean doDrain, EnumFacing side) {
        // TODO expose connected fluidnet through capability & allow untyped draining
        return null;
    }

    protected FluidStack drain(FluidStack resource, boolean doDrain, EnumFacing side) {
        if (this.transferring) return null;
        NetNode node = getRelevantNode(side);
        if (node == null) return null;
        this.transferring = true;

        int flow = resource.amount;
        SimulatorKey key = doDrain ? null : SimulatorKey.getNewSimulatorInstance();
        FluidTestObject testObject = new FluidTestObject(resource);
        AtomicInteger report = new AtomicInteger();
        FDTraverse.flood(node.getNet(),
                (n, f) -> {
                    if (n == node) report.addAndGet(f);
                    else if (doDrain) reportFlow(n, f, testObject);
                },
                (e, f) -> reportFlow(e, f, testObject, key, false),
                e -> e instanceof AbstractNetFlowEdge n ?
                        GTUtility.safeCastLongToInt(n.getFlowLimit(testObject, node.getNet(), getQueryTick(), key)) : 0,
                n -> n == node ? flow : getSupply(n, testObject, true),
                n -> n.getData().getLogicEntryDefaultable(FluidContainmentLogic.TYPE).handles(testObject));

        this.transferring = false;
        return testObject.recombine(report.get());
    }

    protected void reportFlow(NetEdge edge, int flow, FluidTestObject testObject, SimulatorKey key,
                              boolean sourceBias) {
        if (edge instanceof AbstractNetFlowEdge n)
            n.consumeFlowLimit(testObject, node.getNet(), flow, getQueryTick(), key);
        if (key == null) {
            NetNode node = sourceBias ? edge.getSource() : edge.getTarget();
            if (node == null) return;
            FluidFlowLogic logic = node.getData().getLogicEntryNullable(FluidFlowLogic.TYPE);
            if (logic == null) {
                logic = FluidFlowLogic.TYPE.getNew();
                node.getData().setLogicEntry(logic);
            }
            logic.recordFlow(getQueryTick(), testObject.recombine(flow));
        }
    }

    protected void reportFlow(NetNode node, int flow, FluidTestObject testObject) {
        if (flow == 0) return;
        FluidContainmentLogic logic = node.getData().getLogicEntryDefaultable(FluidContainmentLogic.TYPE);
        if (!logic.handles(testObject)) {
            FluidStack stack = testObject.recombine(flow);
            // failing attributes take priority over state
            for (FluidAttribute attribute : FluidAttribute.inferAttributes(stack)) {
                if (!logic.contains(attribute)) {
                    attribute.handleFailure(tile.getWorld(), tile.getPos(), stack);
                    return;
                }
            }
            FluidState state = FluidState.inferState(stack);
            if (!logic.contains(state)) state.handleFailure(tile.getWorld(), tile.getPos(), stack);
        } else if (node instanceof NodeExposingCapabilities exposer) {
            IFluidHandler handler = exposer.getProvider().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                    exposer.exposedFacing());
            if (handler != null) {
                // positive flow is supply, aka we pulled flow from this node
                if (flow > 0) {
                    handler.drain(testObject.recombine(flow), true);
                } else {
                    handler.fill(testObject.recombine(flow), true);
                }
            }
        }
    }

    protected int getSupply(NetNode node, FluidTestObject testObject, boolean supply) {
        if (node instanceof NodeExposingCapabilities exposer) {
            IFluidHandler handler = exposer.getProvider().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                    exposer.exposedFacing());
            if (handler != null) {
                if (supply) {
                    FluidStack s = handler.drain(testObject.recombine(Integer.MAX_VALUE), false);
                    return s == null ? 0 : s.amount;
                } else {
                    return -handler.fill(testObject.recombine(Integer.MAX_VALUE), false);
                }
            }
        }
        return 0;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return fill(resource, doFill, null);
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return properties;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return drain(maxDrain, doDrain, null);
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return drain(resource, doDrain, null);
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
        return true;
    }

    @Override
    public boolean canFillFluidType(FluidStack fluidStack) {
        return true;
    }

    @Override
    public boolean canDrainFluidType(FluidStack fluidStack) {
        return true;
    }

    protected class Wrapper implements IFluidHandler, IFluidTankProperties {

        private final EnumFacing facing;
        private final IFluidTankProperties[] properties;

        public Wrapper(EnumFacing facing) {
            this.facing = facing;
            properties = new IFluidTankProperties[FluidCapabilityObject.this.properties.length];
            Arrays.fill(properties, this);
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
            return FluidCapabilityObject.this.drain(resource, doDrain, facing);
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return FluidCapabilityObject.this.drain(maxDrain, doDrain, facing);
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
            return true;
        }

        @Override
        public boolean canFillFluidType(FluidStack fluidStack) {
            return true;
        }

        @Override
        public boolean canDrainFluidType(FluidStack fluidStack) {
            return true;
        }
    }
}
